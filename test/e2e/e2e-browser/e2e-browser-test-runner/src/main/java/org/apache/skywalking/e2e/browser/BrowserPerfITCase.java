/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.e2e.browser;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.network.common.Commands;
import org.apache.skywalking.apm.network.common.KeyIntValuePair;
import org.apache.skywalking.apm.network.common.ServiceType;
import org.apache.skywalking.apm.network.language.agent.BrowserErrorLog;
import org.apache.skywalking.apm.network.language.agent.BrowserPerfData;
import org.apache.skywalking.apm.network.language.agent.BrowserPerfServiceGrpc;
import org.apache.skywalking.apm.network.language.agent.ErrorCategory;
import org.apache.skywalking.apm.network.register.v2.RegisterGrpc;
import org.apache.skywalking.apm.network.register.v2.Service;
import org.apache.skywalking.apm.network.register.v2.ServiceInstance;
import org.apache.skywalking.apm.network.register.v2.ServiceInstanceRegisterMapping;
import org.apache.skywalking.apm.network.register.v2.ServiceInstances;
import org.apache.skywalking.apm.network.register.v2.ServiceRegisterMapping;
import org.apache.skywalking.apm.network.register.v2.Services;
import org.apache.skywalking.e2e.service.ServicesMatcher;
import org.apache.skywalking.e2e.service.ServicesQuery;
import org.apache.skywalking.e2e.service.endpoint.Endpoint;
import org.apache.skywalking.e2e.service.endpoint.EndpointQuery;
import org.apache.skywalking.e2e.service.endpoint.Endpoints;
import org.apache.skywalking.e2e.service.endpoint.EndpointsMatcher;
import org.apache.skywalking.e2e.service.instance.Instance;
import org.apache.skywalking.e2e.service.instance.Instances;
import org.apache.skywalking.e2e.service.instance.InstancesMatcher;
import org.apache.skywalking.e2e.service.instance.InstancesQuery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static java.util.Objects.nonNull;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_BROWSER_METRICS;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_BROWSER_PAGE_METRICS;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_BROWSER_PAGE_MULTIPLE_LINEAR_METRICS;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_BROWSER_SINGLE_VERSION_METRICS;
import static org.apache.skywalking.e2e.metrics.MetricsMatcher.verifyMetrics;
import static org.apache.skywalking.e2e.metrics.MetricsMatcher.verifyPercentileMetrics;

/**
 * @author zhangwei
 */
@Slf4j
public abstract class BrowserPerfITCase {

    private static final int MAX_INBOUND_MESSAGE_SIZE = 1024 * 1024 * 50;

    private static final String BROWSER_NAME = "e2e";

    private static final String BROWSER_SINGLE_VERSION_NAME = "v1.0.0";

    private final int retryInterval = 1000;

    private BrowserQueryClient queryClient;

    private RegisterGrpc.RegisterBlockingStub registerStub;

    private BrowserPerfServiceGrpc.BrowserPerfServiceStub browserPerfServiceStub;

    private int browserId;

    private int browserSingleVersionId;

    @Before
    public void setUp() {
        final String swWebappHost = System.getProperty("sw.webapp.host", "127.0.0.1");
        final String swWebappPort = System.getProperty("sw.webapp.port", "12800");
        final String oapPort = System.getProperty("oap.port", "11800");
        final String oapHost = System.getProperty("oap.host", "127.0.0.1");
        queryClient = new BrowserQueryClient(swWebappHost, swWebappPort);

        final ManagedChannelBuilder builder =
            NettyChannelBuilder.forAddress(oapHost, Integer.parseInt(oapPort))
                .nameResolverFactory(new DnsNameResolverProvider())
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                .usePlaintext();

        final ManagedChannel channel = builder.build();
        registerStub = RegisterGrpc.newBlockingStub(channel);
        browserPerfServiceStub = BrowserPerfServiceGrpc.newStub(channel);
    }

    @Test(timeout = 1200000)
    @DirtiesContext
    public void verify() throws Exception {
        final LocalDateTime minutesAgo = LocalDateTime.now(ZoneOffset.UTC);
        generateTraffic();

        doRetryableVerification(() -> {
            try {
                verifyBrowser(minutesAgo);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    private void verifyBrowser(LocalDateTime minutesAgo) throws Exception {
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1);

        final List<org.apache.skywalking.e2e.service.Service> services = queryClient.browserServices(new ServicesQuery().start(minutesAgo).end(now));
        log.info("services: {}", services);
        InputStream expectedInputStream =
            new ClassPathResource("expected-data/org.apache.skywalking.e2e.browser.BrowserPerfITCase.services.yml").getInputStream();

        final ServicesMatcher servicesMatcher = new Yaml().loadAs(expectedInputStream, ServicesMatcher.class);
        servicesMatcher.verify(services);

        for (org.apache.skywalking.e2e.service.Service service : services) {
            log.info("verifying service version: {}", service);
            // service metrics
            verifyBrowserMetrics(minutesAgo, now, service.getKey());

            // service version
            verifyBrowserSingleVersion(minutesAgo, now, service);

            // service page path
            verifyBrowserPagePath(service, minutesAgo, now);
        }
    }

    private Instances verifyBrowserSingleVersion(LocalDateTime minutesAgo, LocalDateTime now, org.apache.skywalking.e2e.service.Service service) throws Exception {
        Instances instances = queryClient.instances(
            new InstancesQuery()
                .serviceId(service.getKey())
                .start(minutesAgo)
                .end(now)
        );
        log.info("instances: {}", instances);
        InputStream expectedInputStream =
            new ClassPathResource("expected-data/org.apache.skywalking.e2e.browser.BrowserPerfITCase.version.yml").getInputStream();
        final InstancesMatcher instancesMatcher = new Yaml().loadAs(expectedInputStream, InstancesMatcher.class);
        instancesMatcher.verify(instances);
        // service version metrics
        for (Instance instance : instances.getInstances()) {
            verifyBrowserSingleVersionMetrics(instance, minutesAgo, now);
        }
        return instances;
    }


    private Endpoints verifyBrowserPagePath(org.apache.skywalking.e2e.service.Service service,
                                            LocalDateTime minutesAgo, LocalDateTime now) throws Exception {
        Endpoints endpoints = queryClient.endpoints(new EndpointQuery().serviceId(String.valueOf(service.getKey())));
        log.info("endpoints: {}", endpoints);
        InputStream expectedInputStream =
            new ClassPathResource("expected-data/org.apache.skywalking.e2e.browser.BrowserPerfITCase.pagePath.yml").getInputStream();
        final EndpointsMatcher endpointsMatcher = new Yaml().loadAs(expectedInputStream, EndpointsMatcher.class);
        endpointsMatcher.verify(endpoints);
        // service page metrics
        for (Endpoint endpoint : endpoints.getEndpoints()) {
            verifyBrowserPagePathMetrics(minutesAgo, now, service, endpoint);
        }
        return endpoints;
    }

    private void verifyBrowserMetrics(LocalDateTime minutesAgo, LocalDateTime now, String serviceId) throws Exception {
        for (String metricName : ALL_BROWSER_METRICS) {
            verifyMetrics(queryClient, metricName, serviceId, minutesAgo, now, retryInterval, this::generateTraffic);
        }
    }

    private void verifyBrowserSingleVersionMetrics(Instance instance, LocalDateTime minutesAgo, LocalDateTime now) throws Exception {
        for (String metricName : ALL_BROWSER_SINGLE_VERSION_METRICS) {
            verifyMetrics(queryClient, metricName, instance.getKey(), minutesAgo, now, retryInterval, this::generateTraffic);
        }
    }

    private void verifyBrowserPagePathMetrics(LocalDateTime minutesAgo, LocalDateTime now, org.apache.skywalking.e2e.service.Service service, Endpoint endpoint) throws Exception {
        for (String metricName : ALL_BROWSER_PAGE_METRICS) {
            verifyMetrics(queryClient, metricName, endpoint.getKey(), minutesAgo, now, retryInterval, this::generateTraffic);
        }

        for (String metricName : ALL_BROWSER_PAGE_MULTIPLE_LINEAR_METRICS) {
            verifyPercentileMetrics(queryClient, metricName, endpoint.getKey(), minutesAgo, now, retryInterval, this::generateTraffic);
        }
    }

    private void doRetryableVerification(Runnable runnable) throws Exception {
        while (true) {
            try {
                runnable.run();
                break;
            } catch (Throwable ignored) {
                generateTraffic();
                Thread.sleep(retryInterval);
            }
        }
    }

    private void generateTraffic() {
        try {
            int retryIndex = 0;
            while (browserId == 0 || browserSingleVersionId == 0) {
                try {
                    if (browserId == 0) {
                        browserId = registerBrowser();
                    }
                    if (browserId > 0) {
                        browserSingleVersionId = registerBrowserSingleVersion(browserId);
                        break;
                    }
                } catch (Throwable e) {
                    if (++retryIndex % 3 == 0) {
                        log.error(e.getMessage(), e);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            BrowserPerfData.Builder perfBuilder = BrowserPerfData.newBuilder()
                .setServiceId(browserId)
                .setServiceVersionId(browserSingleVersionId)
                .setPagePath("/e2e-browser")
                .setRedirectTime(10)
                .setDnsTime(10)
                .setReqTime(10)
                .setDomAnalysisTime(10)
                .setDomReadyTime(10)
                .setBlankTime(10);
            sendBrowserPerfData(perfBuilder.build());

            for (ErrorCategory category : ErrorCategory.values()) {
                if (category == ErrorCategory.UNRECOGNIZED) {
                    continue;
                }
                BrowserErrorLog.Builder errorLogBuilder = BrowserErrorLog.newBuilder()
                    .setUniqueId(UUID.randomUUID().toString())
                    .setServiceId(browserId)
                    .setServiceVersionId(browserSingleVersionId)
                    .setPagePath("/e2e-browser")
                    .setCategory(category)
                    .setMessage("test")
                    .setLine(1)
                    .setCol(1)
                    .setStack("e2e")
                    .setErrorUrl("/e2e/browser");
                sendBrowserErrorLog(errorLogBuilder.build());
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void sendBrowserErrorLog(BrowserErrorLog browserErrorLog) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<BrowserErrorLog> collectStream = browserPerfServiceStub.collectErrorLogs(new StreamObserver<Commands>() {
            @Override
            public void onNext(Commands commands) {

            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });
        collectStream.onNext(browserErrorLog);
        collectStream.onCompleted();
        latch.await();
    }

    private void sendBrowserPerfData(BrowserPerfData browserPerfData) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        browserPerfServiceStub.collectPerfData(browserPerfData, new StreamObserver<Commands>() {
            @Override
            public void onNext(Commands commands) {

            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });
        latch.await();
    }

    private int registerBrowser() {
        ServiceRegisterMapping serviceRegisterMapping = registerStub.doServiceRegister(
            Services.newBuilder().addServices(Service.newBuilder().setServiceName(BROWSER_NAME).setType(ServiceType.browser).build()).build());
        if (nonNull(serviceRegisterMapping)) {
            for (KeyIntValuePair registered : serviceRegisterMapping.getServicesList()) {
                if (BROWSER_NAME.equals(registered.getKey())) {
                    return registered.getValue();
                }
            }
        }
        return 0;
    }

    private int registerBrowserSingleVersion(int serviceId) {
        ServiceInstanceRegisterMapping instanceRegisterMapping = registerStub.doServiceInstanceRegister(ServiceInstances.newBuilder()
            .addInstances(ServiceInstance.newBuilder().setServiceId(serviceId).setInstanceUUID(BROWSER_SINGLE_VERSION_NAME).build()).build());
        if (nonNull(instanceRegisterMapping)) {
            for (KeyIntValuePair registered : instanceRegisterMapping.getServiceInstancesList()) {
                if (BROWSER_SINGLE_VERSION_NAME.equals(registered.getKey())) {
                    return registered.getValue();
                }
            }
        }
        return 0;
    }
}

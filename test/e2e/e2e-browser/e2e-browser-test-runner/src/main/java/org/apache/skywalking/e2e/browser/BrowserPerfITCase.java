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
import org.apache.skywalking.apm.network.language.agent.PerfDetail;
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
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static java.util.Objects.nonNull;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_SERVICE_METRICS;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_SERVICE_MULTIPLE_LINEAR_METRICS;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_SERVICE_PAGE_METRICS;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_SERVICE_PAGE_MULTIPLE_LINEAR_METRICS;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_SERVICE_VERSION_METRICS;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_SERVICE_VERSION_MULTIPLE_LINEAR_METRICS;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_SERVICE_VERSION_PAGE_METRICS;
import static org.apache.skywalking.e2e.browser.metrics.BrowserMetricsQuery.ALL_SERVICE_VERSION_PAGE_MULTIPLE_LINEAR_METRICS;
import static org.apache.skywalking.e2e.metrics.MetricsMatcher.verifyMetrics;
import static org.apache.skywalking.e2e.metrics.MetricsMatcher.verifyPercentileMetrics;

/**
 * @author zhangwei
 */
@Slf4j
public abstract class BrowserPerfITCase {

    private static final int MAX_INBOUND_MESSAGE_SIZE = 1024 * 1024 * 50;

    private static final String SERVICE_NAME = "e2e";

    private static final String SERVICE_VERSION_NAME = "v1.0.0";

    private final int retryInterval = 1000;

    private BrowserQueryClient queryClient;

    private RegisterGrpc.RegisterBlockingStub registerStub;

    private BrowserPerfServiceGrpc.BrowserPerfServiceStub browserPerfServiceStub;

    private int serviceId;

    private int serviceVersionId;

    @Before
    public void setUp() {
        final String swWebappHost = System.getProperty("sw.webapp.host", "127.0.0.1");
        final String swWebappPort = System.getProperty("sw.webapp.host", "12800");
        final String oapPort = System.getProperty("oap.port", "11800");
        queryClient = new BrowserQueryClient(swWebappHost, swWebappPort);

        final ManagedChannelBuilder builder =
                NettyChannelBuilder.forAddress("127.0.0.1", Integer.parseInt(oapPort))
                        .nameResolverFactory(new DnsNameResolverProvider())
                        .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                        .usePlaintext();

        final ManagedChannel channel = builder.build();
        registerStub = RegisterGrpc.newBlockingStub(channel);
        browserPerfServiceStub = BrowserPerfServiceGrpc.newStub(channel);
    }

    @Test(timeout = 120000)
    public void verify() throws Exception {
        final LocalDateTime minutesAgo = LocalDateTime.now();
        generateTraffic();

        doRetryableVerification(() -> {
            try {
                verifyServices(minutesAgo);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    private void verifyServices(LocalDateTime minutesAgo) throws Exception {
        final LocalDateTime now = LocalDateTime.now().plusMinutes(1);

        final List<org.apache.skywalking.e2e.service.Service> services = queryClient.browserServices(new ServicesQuery().start(minutesAgo).end(now));
        log.info("services: {}", services);
        InputStream expectedInputStream =
                new ClassPathResource("expected-data/org.apache.skywalking.e2e.browser.BrowserPerfITCase.services.yml").getInputStream();

        final ServicesMatcher servicesMatcher = new Yaml().loadAs(expectedInputStream, ServicesMatcher.class);
        servicesMatcher.verify(services);

        for (org.apache.skywalking.e2e.service.Service service : services) {
            log.info("verifying service version: {}", service);
            // service metrics
            verifyServiceMetrics(minutesAgo, now, service.getKey());

            // service version
            Instances instances = verifyServiceVersions(minutesAgo, now, service);

            // service page path
            verifyServicePagePaths(service, instances, minutesAgo, now);
        }
    }

    private Instances verifyServiceVersions(LocalDateTime minutesAgo, LocalDateTime now, org.apache.skywalking.e2e.service.Service service) throws Exception {
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
            verifyServiceVersionMetrics(instance, minutesAgo, now);
        }
        return instances;
    }


    private Endpoints verifyServicePagePaths(org.apache.skywalking.e2e.service.Service service, Instances instances,
                                             LocalDateTime minutesAgo, LocalDateTime now) throws Exception {
        Endpoints endpoints = queryClient.endpoints(new EndpointQuery().serviceId(String.valueOf(service.getKey())));
        log.info("endpoints: {}", endpoints);
        InputStream expectedInputStream =
                new ClassPathResource("expected-data/org.apache.skywalking.e2e.browser.BrowserPerfITCase.pagePath.yml").getInputStream();
        final EndpointsMatcher endpointsMatcher = new Yaml().loadAs(expectedInputStream, EndpointsMatcher.class);
        endpointsMatcher.verify(endpoints);
        // service page metrics
        for (Endpoint endpoint : endpoints.getEndpoints()) {
            verifyServicePagePathMetrics(minutesAgo, now, service, endpoint);

            // service version page metrics
            for (Instance instance : instances.getInstances()) {
                verifyServiceVersionPagePathMetrics(minutesAgo, now, instance, endpoint);
            }
        }
        return endpoints;
    }

    private void verifyServiceMetrics(LocalDateTime minutesAgo, LocalDateTime now, String serviceId) throws Exception {
        for (String metricName : ALL_SERVICE_METRICS) {
            verifyMetrics(queryClient, metricName, serviceId, minutesAgo, now, retryInterval, this::generateTraffic);
        }

        for (String metricName : ALL_SERVICE_MULTIPLE_LINEAR_METRICS) {
            verifyPercentileMetrics(queryClient, metricName, serviceId, minutesAgo, now, retryInterval, this::generateTraffic);
        }
    }

    private void verifyServiceVersionMetrics(Instance instance, LocalDateTime minutesAgo, LocalDateTime now) throws Exception {
        for (String metricName : ALL_SERVICE_VERSION_METRICS) {
            verifyMetrics(queryClient, metricName, instance.getKey(), minutesAgo, now, retryInterval, this::generateTraffic);
        }

        for (String metricName : ALL_SERVICE_VERSION_MULTIPLE_LINEAR_METRICS) {
            verifyPercentileMetrics(queryClient, metricName, instance.getKey(), minutesAgo, now, retryInterval, this::generateTraffic);
        }
    }

    private void verifyServicePagePathMetrics(LocalDateTime minutesAgo, LocalDateTime now, org.apache.skywalking.e2e.service.Service service, Endpoint endpoint) throws Exception {
        String id = String.join("_", service.getKey(), endpoint.getKey());
        for (String metricName : ALL_SERVICE_PAGE_METRICS) {
            verifyMetrics(queryClient, metricName, id, minutesAgo, now, retryInterval, this::generateTraffic);
        }

        for (String metricName : ALL_SERVICE_PAGE_MULTIPLE_LINEAR_METRICS) {
            verifyPercentileMetrics(queryClient, metricName, id, minutesAgo, now, retryInterval, this::generateTraffic);
        }
    }

    private void verifyServiceVersionPagePathMetrics(LocalDateTime minutesAgo, LocalDateTime now, Instance instance, Endpoint endpoint) throws Exception {
        String id = String.join("_", instance.getKey(), endpoint.getKey());
        for (String metricName : ALL_SERVICE_VERSION_PAGE_METRICS) {
            verifyMetrics(queryClient, metricName, id, minutesAgo, now, retryInterval, this::generateTraffic);
        }

        for (String metricName : ALL_SERVICE_VERSION_PAGE_MULTIPLE_LINEAR_METRICS) {
            verifyPercentileMetrics(queryClient, metricName, id, minutesAgo, now, retryInterval, this::generateTraffic);
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
            while (serviceId == 0 || serviceVersionId == 0) {
                try {
                    if (serviceId == 0) {
                        serviceId = registerService();
                    }
                    if (serviceId > 0) {
                        serviceVersionId = registerServiceVersion(serviceId);
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
            BrowserPerfData.Builder builder = BrowserPerfData.newBuilder()
                    .setUniqueId(UUID.randomUUID().toString())
                    .setServiceId(serviceId)
                    .setServiceVersionId(serviceVersionId)
                    .setPagePath("/e2e-browser")
                    .addLogs(BrowserErrorLog.newBuilder()
                            .setCatalog("/e2e-browser")
                            .setMessage("test")
                            .setLine(1)
                            .setCol(1)
                            .setStack("e2e")
                            .setErrorUrl("/e2e/browser")
                            .build())
                    .setPerfDetail(PerfDetail.newBuilder()
                            .setRedirectTime(10)
                            .setDnsTime(10)
                            .setReqTime(10)
                            .setDomAnalysisTime(10)
                            .setDomReadyTime(10)
                            .setBlankTime(10)
                            .build());
            sendBrowserPerfData(builder.build());
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void sendBrowserPerfData(BrowserPerfData browserPerfData) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<BrowserPerfData> collect = browserPerfServiceStub.collect(new StreamObserver<Commands>() {
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
        collect.onNext(browserPerfData);
        collect.onCompleted();
        latch.await();
    }

    private int registerService() {
        ServiceRegisterMapping serviceRegisterMapping = registerStub.doServiceRegister(
                Services.newBuilder().addServices(Service.newBuilder().setServiceName(SERVICE_NAME).setType(ServiceType.browser).build()).build());
        if (nonNull(serviceRegisterMapping)) {
            for (KeyIntValuePair registered : serviceRegisterMapping.getServicesList()) {
                if (SERVICE_NAME.equals(registered.getKey())) {
                    return registered.getValue();
                }
            }
        }
        return 0;
    }

    private int registerServiceVersion(int serviceId) {
        ServiceInstanceRegisterMapping instanceRegisterMapping = registerStub.doServiceInstanceRegister(ServiceInstances.newBuilder()
                .addInstances(ServiceInstance.newBuilder().setServiceId(serviceId).setInstanceUUID(SERVICE_VERSION_NAME).build()).build());
        if (nonNull(instanceRegisterMapping)) {
            for (KeyIntValuePair registered : instanceRegisterMapping.getServiceInstancesList()) {
                if (SERVICE_VERSION_NAME.equals(registered.getKey())) {
                    return registered.getValue();
                }
            }
        }
        return 0;
    }
}

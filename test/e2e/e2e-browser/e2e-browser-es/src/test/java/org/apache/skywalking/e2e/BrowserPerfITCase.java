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
 */

package org.apache.skywalking.e2e;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.network.common.Commands;
import org.apache.skywalking.apm.network.common.KeyIntValuePair;
import org.apache.skywalking.apm.network.common.ServiceType;
import org.apache.skywalking.apm.network.language.agent.BrowserPerfData;
import org.apache.skywalking.apm.network.language.agent.BrowserPerfServiceGrpc;
import org.apache.skywalking.apm.network.register.v2.*;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static java.util.Objects.nonNull;

/**
 * @author zhangwei
 */
@Slf4j
public class BrowserPerfITCase {

    private static final int MAX_INBOUND_MESSAGE_SIZE = 1024 * 1024 * 50;

    private static final String SERVICE_NAME = "e2e";

    private static final String SERVICE_VERSION_NAME = "v1.0.0";

    private SimpleQueryClient queryClient;

    private RegisterGrpc.RegisterBlockingStub registerStub;

    private BrowserPerfServiceGrpc.BrowserPerfServiceStub browserPerfServiceStub;

    private int serviceId;

    private int serviceVersionId;

    @Before
    public void setUp() {
        final String swWebappHost = System.getProperty("sw.webapp.host", "127.0.0.1");
        final String swWebappPort = System.getProperty("sw.webapp.host", "12800");
        final String oapPort = System.getProperty("oap.port", "11800");
        queryClient = new SimpleQueryClient(swWebappHost, swWebappPort);

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
    public void verify() throws InterruptedException {
        register();

        BrowserPerfData data = BrowserPerfData.newBuilder()
                .setServiceId(serviceId)
                .setServiceVersionId(serviceVersionId)
                .setPagePath("/e2e")
                .build();

        sendBrowserPerfData(data);
    }

    private void register() {
        do {
            try {
                if (serviceId == 0) {
                    serviceId = registerService();
                }
                if (serviceId > 0) {
                    serviceVersionId = registerServiceVersion(serviceId);
                }
            } catch (Exception e) {
                // ignore
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        } while (serviceId == 0 || serviceVersionId == 0);
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


    private void sendBrowserPerfData(final BrowserPerfData data) throws InterruptedException {
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
        collect.onNext(data);
        collect.onCompleted();

        latch.await();
    }
}

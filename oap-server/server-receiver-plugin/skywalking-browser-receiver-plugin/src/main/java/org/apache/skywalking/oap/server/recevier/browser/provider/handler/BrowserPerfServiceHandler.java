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

package org.apache.skywalking.oap.server.recevier.browser.provider.handler;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.network.common.Commands;
import org.apache.skywalking.apm.network.language.agent.BrowserPerfData;
import org.apache.skywalking.apm.network.language.agent.BrowserPerfServiceGrpc;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.server.grpc.GRPCHandler;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.BrowserPerfDataParse;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.HistogramMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;

/**
 * @author zhangwei
 */
@Slf4j
public class BrowserPerfServiceHandler extends BrowserPerfServiceGrpc.BrowserPerfServiceImplBase implements GRPCHandler {

    private final BrowserPerfDataParse.Producer browserPerfProducer;
    private HistogramMetrics histogram;

    public BrowserPerfServiceHandler(BrowserPerfDataParse.Producer browserPerfProducer, ModuleManager moduleManager) {
        this.browserPerfProducer = browserPerfProducer;
        MetricsCreator metricsCreator = moduleManager.find(TelemetryModule.NAME).provider().getService(MetricsCreator.class);
        histogram = metricsCreator.createHistogramMetric("browser_perf_grpc_in_latency", "The process browser perf data",
                MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
    }

    @Override
    public StreamObserver<BrowserPerfData> collect(StreamObserver<Commands> responseObserver) {
        return new StreamObserver<BrowserPerfData>() {
            @Override
            public void onNext(BrowserPerfData browserPerfData) {
                if (log.isDebugEnabled()) {
                    log.debug("receive browser perf data");
                }

                HistogramMetrics.Timer timer = histogram.createTimer();
                try {
                    browserPerfProducer.send(browserPerfData);
                } finally {
                    timer.finish();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error(throwable.getMessage(), throwable);
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Commands.newBuilder().build());
                responseObserver.onCompleted();
            }
        };
    }
}

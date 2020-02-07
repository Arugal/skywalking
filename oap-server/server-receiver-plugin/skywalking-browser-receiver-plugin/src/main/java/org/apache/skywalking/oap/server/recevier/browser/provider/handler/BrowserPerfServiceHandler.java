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
import org.apache.skywalking.apm.network.language.agent.BrowserErrorLog;
import org.apache.skywalking.apm.network.language.agent.BrowserPerfData;
import org.apache.skywalking.apm.network.language.agent.BrowserPerfServiceGrpc;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.server.grpc.GRPCHandler;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.BrowserErrorLogParser;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.BrowserPerfDataParser;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.HistogramMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;

/**
 * @author zhangwei
 */
@Slf4j
public class BrowserPerfServiceHandler extends BrowserPerfServiceGrpc.BrowserPerfServiceImplBase implements GRPCHandler {

    private final BrowserPerfDataParser.Producer browserPerfProducer;
    private final BrowserErrorLogParser.Producer browserErrorLogProducer;

    private HistogramMetrics perfHistogram;
    private HistogramMetrics errorLogHistogram;

    public BrowserPerfServiceHandler(BrowserPerfDataParser.Producer browserPerfProducer, BrowserErrorLogParser.Producer browserErrorLogProducer, ModuleManager moduleManager) {
        this.browserPerfProducer = browserPerfProducer;
        this.browserErrorLogProducer = browserErrorLogProducer;

        MetricsCreator metricsCreator = moduleManager.find(TelemetryModule.NAME).provider().getService(MetricsCreator.class);
        perfHistogram = metricsCreator.createHistogramMetric("browser_perf_grpc_in_latency", "The process browser perf data",
            MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);

        errorLogHistogram = metricsCreator.createHistogramMetric("browser_error_log_grpc_in_latency", "The process browser error log",
            MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
    }

    @Override
    public void collectPerfData(BrowserPerfData request, StreamObserver<Commands> responseObserver) {
        if (log.isDebugEnabled()) {
            log.debug("receive browser perf data");
        }
        try {
            HistogramMetrics.Timer timer = perfHistogram.createTimer();
            try {
                browserPerfProducer.send(request);
            } finally {
                timer.finish();
            }
            responseObserver.onNext(Commands.newBuilder().build());
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<BrowserErrorLog> collectErrorLogs(StreamObserver<Commands> responseObserver) {
        return new StreamObserver<BrowserErrorLog>() {
            @Override
            public void onNext(BrowserErrorLog browserErrorLog) {
                if (log.isDebugEnabled()) {
                    log.debug("receive browser error log");
                }

                HistogramMetrics.Timer timer = errorLogHistogram.createTimer();
                try {
                    browserErrorLogProducer.send(browserErrorLog);
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

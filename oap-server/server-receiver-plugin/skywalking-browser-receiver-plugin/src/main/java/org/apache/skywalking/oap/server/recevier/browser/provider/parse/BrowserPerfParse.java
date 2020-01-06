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

package org.apache.skywalking.oap.server.recevier.browser.provider.parse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.network.language.agent.BrowserPerfData;
import org.apache.skywalking.oap.server.library.buffer.BufferData;
import org.apache.skywalking.oap.server.library.buffer.DataStreamReader;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.standardization.BrowserPerfStandardizationWorker;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.CounterMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;

/**
 * @author zhangwei
 */
@Slf4j
public class BrowserPerfParse {

    private ModuleManager moduleManager;
    @Setter
    private BrowserPerfStandardizationWorker standardizationWorker;
    private volatile static CounterMetrics BROWSER_PERF_BUFFER_FILE_RETRY;
    private volatile static CounterMetrics BROWSER_PERF_BUFFER_FILE_OUT;
    private volatile static CounterMetrics BROWSER_PERF_PARSE_ERROR;

    public BrowserPerfParse(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        if (BROWSER_PERF_BUFFER_FILE_RETRY == null) {
            MetricsCreator metricsCreator = moduleManager.find(TelemetryModule.NAME).provider().getService(MetricsCreator.class);
            BROWSER_PERF_BUFFER_FILE_RETRY = metricsCreator.createCounter("browser_perf_buffer_file_retry", "The number of retry browser perf from the buffer file, but haven't registered successfully.",
                    MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
            BROWSER_PERF_BUFFER_FILE_OUT = metricsCreator.createCounter("browser_perf_buffer_file_out", "The number of browser perf out of the buffer file",
                    MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
            BROWSER_PERF_PARSE_ERROR = metricsCreator.createCounter("browser_perf_parse_error", "The number of browser perf parse data",
                    MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
        }
    }

    public boolean parse(BufferData<BrowserPerfData> bufferData, BrowserPerfSource source) {
        // create browser perf listeners

        try {
            BrowserPerfData browserPerfData = bufferData.getMessageType();
            // id exchanger

            // register endpoint

            // source builder
            return true;
        } catch (Throwable e) {
            BROWSER_PERF_PARSE_ERROR.inc();
            log.error(e.getMessage(), e);
            return true;
        }
    }

    public static class Producer implements DataStreamReader.CallBack<BrowserPerfData> {

        @Setter
        private BrowserPerfStandardizationWorker standardizationWorker;
        private final ModuleManager moduleManager;

        public Producer(ModuleManager moduleManager) {
            this.moduleManager = moduleManager;
        }

        public void send(BrowserPerfData perf) {
            BrowserPerfParse perfParse = new BrowserPerfParse(moduleManager);
            perfParse.setStandardizationWorker(standardizationWorker);
            perfParse.parse(new BufferData<>(perf), BrowserPerfSource.Agent);
        }

        @Override
        public boolean call(BufferData<BrowserPerfData> bufferData) {
            BrowserPerfParse perfParse = new BrowserPerfParse(moduleManager);
            boolean parseResult = perfParse.parse(bufferData, BrowserPerfSource.Buffer);
            if (parseResult) {
                BROWSER_PERF_BUFFER_FILE_OUT.inc();
            }
            return false;
        }
    }
}

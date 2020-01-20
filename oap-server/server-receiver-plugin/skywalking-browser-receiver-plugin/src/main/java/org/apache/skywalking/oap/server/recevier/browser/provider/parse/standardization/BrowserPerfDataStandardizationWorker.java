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

package org.apache.skywalking.oap.server.recevier.browser.provider.parse.standardization;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.commons.datacarrier.DataCarrier;
import org.apache.skywalking.apm.commons.datacarrier.consumer.IConsumer;
import org.apache.skywalking.apm.network.language.agent.BrowserPerfData;
import org.apache.skywalking.oap.server.core.worker.AbstractWorker;
import org.apache.skywalking.oap.server.library.buffer.BufferStream;
import org.apache.skywalking.oap.server.library.buffer.DataStreamReader;
import org.apache.skywalking.oap.server.library.module.ModuleDefineHolder;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.CounterMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;

import java.io.IOException;
import java.util.List;

/**
 * @author zhangwei
 */
@Slf4j
public class BrowserPerfDataStandardizationWorker extends AbstractWorker<BrowserPerfDataStandardization> {

    private final DataCarrier<BrowserPerfDataStandardization> dataCarrier;
    private CounterMetrics browserPerfDataBufferFileIn;

    public BrowserPerfDataStandardizationWorker(ModuleDefineHolder moduleDefineHolder,
                                                DataStreamReader.CallBack<BrowserPerfData> perfDataParse, String path, int offsetFileMaxSize,
                                                int dataFileMaxSize, boolean cleanWhenRestart) throws IOException {
        super(moduleDefineHolder);
        BufferStream.Builder<BrowserPerfData> builder = new BufferStream.Builder<>(path);
        builder.cleanWhenRestart(cleanWhenRestart)
            .dataFileMaxSize(dataFileMaxSize)
            .offsetFileMaxSize(offsetFileMaxSize)
            .parser(BrowserPerfData.parser())
            .callBack(perfDataParse);

        BufferStream<BrowserPerfData> stream = builder.build();
        stream.initialize();

        dataCarrier = new DataCarrier<>("BrowserPerfDataStandardizationWorker", 1, 1024);
        dataCarrier.consume(new Consumer(stream), 1, 200);

        MetricsCreator metricsCreator = moduleDefineHolder.find(TelemetryModule.NAME).provider().getService(MetricsCreator.class);
        browserPerfDataBufferFileIn = metricsCreator.createCounter("browser_perf_data_buffer_file_in", "The number of browser pert data into the buffer file",
            MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
    }

    @Override
    public void in(BrowserPerfDataStandardization standardization) {
        dataCarrier.produce(standardization);
    }

    private class Consumer implements IConsumer<BrowserPerfDataStandardization> {

        private final BufferStream<BrowserPerfData> stream;

        public Consumer(BufferStream<BrowserPerfData> stream) {
            this.stream = stream;
        }

        @Override
        public void init() {

        }

        @Override
        public void consume(List<BrowserPerfDataStandardization> data) {
            data.forEach(aData -> {
                browserPerfDataBufferFileIn.inc();
                stream.write(aData.getBrowserPerfData());
            });
        }

        @Override
        public void onError(List<BrowserPerfDataStandardization> data, Throwable t) {
            log.error(t.getMessage(), t);
        }

        @Override
        public void onExit() {

        }
    }
}

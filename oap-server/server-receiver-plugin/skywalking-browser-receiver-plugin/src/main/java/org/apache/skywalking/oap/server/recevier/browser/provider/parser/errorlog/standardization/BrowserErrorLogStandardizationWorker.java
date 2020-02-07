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

package org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.standardization;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.commons.datacarrier.DataCarrier;
import org.apache.skywalking.apm.commons.datacarrier.consumer.IConsumer;
import org.apache.skywalking.apm.network.language.agent.BrowserErrorLog;
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
public class BrowserErrorLogStandardizationWorker extends AbstractWorker<BrowserErrorLogStandardization> {

    private final DataCarrier<BrowserErrorLogStandardization> dataCarrier;
    private CounterMetrics browserErrorLogBufferFileIn;

    public BrowserErrorLogStandardizationWorker(ModuleDefineHolder moduleDefineHolder,
                                                DataStreamReader.CallBack<BrowserErrorLog> callBack, String path, int offsetFileMaxSize,
                                                int dataFileMaxSize, boolean cleanWhenRestart) throws IOException {
        super(moduleDefineHolder);
        BufferStream.Builder<BrowserErrorLog> builder = new BufferStream.Builder<>(path);
        builder.cleanWhenRestart(cleanWhenRestart)
            .dataFileMaxSize(dataFileMaxSize)
            .offsetFileMaxSize(offsetFileMaxSize)
            .parser(BrowserErrorLog.parser())
            .callBack(callBack);

        BufferStream<BrowserErrorLog> stream = builder.build();
        stream.initialize();

        dataCarrier = new DataCarrier<>("BrowserErrorLogStandardizationWorker", 1, 1024);
        dataCarrier.consume(new Consumer(stream), 1, 200);

        MetricsCreator metricsCreator = moduleDefineHolder.find(TelemetryModule.NAME).provider().getService(MetricsCreator.class);
        browserErrorLogBufferFileIn = metricsCreator.createCounter("browser_error_log_buffer_file_in", "The number of browser error log into the buffer file",
            MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
    }

    @Override
    public void in(BrowserErrorLogStandardization browserErrorLog) {
        dataCarrier.produce(browserErrorLog);
    }

    private class Consumer implements IConsumer<BrowserErrorLogStandardization> {

        private final BufferStream<BrowserErrorLog> stream;

        public Consumer(BufferStream<BrowserErrorLog> stream) {
            this.stream = stream;
        }

        @Override
        public void init() {

        }

        @Override
        public void consume(List<BrowserErrorLogStandardization> data) {
            data.forEach(aData -> {
                browserErrorLogBufferFileIn.inc();
                stream.write(aData.getBrowserErrorLog());
            });
        }

        @Override
        public void onError(List<BrowserErrorLogStandardization> data, Throwable t) {
            log.error(t.getMessage(), t);
        }

        @Override
        public void onExit() {

        }
    }
}

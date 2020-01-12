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
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.cache.ServiceInstanceInventoryCache;
import org.apache.skywalking.oap.server.library.buffer.BufferData;
import org.apache.skywalking.oap.server.library.buffer.DataStreamReader;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.BrowserServiceModuleConfig;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserErrorLogDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfDataCoreInfo;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfDataDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.BrowserPerfDataListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.standardization.BrowserPerfDataStandardization;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.standardization.BrowserPerfDataStandardizationWorker;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.standardization.PagePathIdExchanger;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.CounterMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zhangwei
 */
@Slf4j
public class BrowserPerfDataParse {

    private final ModuleManager moduleManager;
    private final BrowserPerfDataParseListenerManager listenerManager;
    private final BrowserServiceModuleConfig config;
    private final List<BrowserPerfDataListener> browserPerfDataListeners;
    private final ServiceInstanceInventoryCache serviceInstanceInventoryCache;
    private final BrowserPerfDataCoreInfo browserPerfDataCoreInfo;
    @Setter private BrowserPerfDataStandardizationWorker standardizationWorker;
    private volatile static CounterMetrics BROWSER_PERF_BUFFER_FILE_RETRY;
    private volatile static CounterMetrics BROWSER_PERF_BUFFER_FILE_OUT;
    private volatile static CounterMetrics BROWSER_PERF_PARSE_ERROR;

    public BrowserPerfDataParse(ModuleManager moduleManager, BrowserPerfDataParseListenerManager listenerManager, BrowserServiceModuleConfig config) {
        this.moduleManager = moduleManager;
        this.listenerManager = listenerManager;
        this.config = config;
        this.browserPerfDataListeners = new LinkedList<>();
        this.browserPerfDataCoreInfo = new BrowserPerfDataCoreInfo();

        if (BROWSER_PERF_BUFFER_FILE_RETRY == null) {
            MetricsCreator metricsCreator = moduleManager.find(TelemetryModule.NAME).provider().getService(MetricsCreator.class);
            BROWSER_PERF_BUFFER_FILE_RETRY = metricsCreator.createCounter("browser_perf_buffer_file_retry", "The number of retry browser perf from the buffer file, but haven't registered successfully.",
                    MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
            BROWSER_PERF_BUFFER_FILE_OUT = metricsCreator.createCounter("browser_perf_buffer_file_out", "The number of browser perf out of the buffer file",
                    MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
            BROWSER_PERF_PARSE_ERROR = metricsCreator.createCounter("browser_perf_parse_error", "The number of browser perf parse data",
                    MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
        }

        this.serviceInstanceInventoryCache = moduleManager.find(CoreModule.NAME).provider().getService(ServiceInstanceInventoryCache.class);
    }

    public boolean parse(BufferData<BrowserPerfData> bufferData, BrowserPerfDataSource source) {
        createBrowserPerfListeners();

        try {
            BrowserPerfData browserPerfData = bufferData.getMessageType();

            final int serviceVersionId = browserPerfData.getServiceVersionId();
            if (serviceInstanceInventoryCache.get(serviceVersionId) == null) {
                log.warn("Cannot recognize service version id [{}] from cache, browser perf data will be ignored", serviceVersionId);
                return true; // to mark it "completed" thus won't be retried
            }

            BrowserPerfDataDecorator decorator = new BrowserPerfDataDecorator(browserPerfData);
            if (!preBuild(decorator)) {
                if (log.isDebugEnabled()) {
                    log.debug("This browser perf data id exchange not success, write to buffer file, serviceId:{}, pagePath:{}",
                            decorator.getServiceId(), decorator.getPagePath());
                }

                if (source.equals(BrowserPerfDataSource.Browser)) {
                    writeToBufferFile(decorator);
                } else {
                    BROWSER_PERF_BUFFER_FILE_RETRY.inc();
                }
                return false;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("This browser perf data id exchange success, serviceId: {}, pagePath:{}",
                            decorator.getServiceId(), decorator.getPagePath());
                }

                notifyListenerToBuild();
                return true;
            }
        } catch (Throwable e) {
            BROWSER_PERF_PARSE_ERROR.inc();
            log.error(e.getMessage(), e);
            return true;
        }
    }

    private boolean preBuild(BrowserPerfDataDecorator decorator) {
        if (decorator.getTime() == Const.NONE) {
            // Use the server side current time, if the client side not set.
            long nowMinute = System.currentTimeMillis();
            decorator.setTime(nowMinute);
            if (decorator.isError()) {
                for (int i = 0; i < decorator.getBrowserErrorLogCount(); i++) {
                    BrowserErrorLogDecorator errorLogDecorator = decorator.getBrowserErrorLog(i);
                    if (errorLogDecorator.getTime() == Const.NONE) {
                        errorLogDecorator.setTime(nowMinute);
                    }
                }
            }
        }

        boolean exchanged = true;
        if (!PagePathIdExchanger.getInstance(moduleManager).exchange(decorator, browserPerfDataCoreInfo.getServiceId())) {
            exchanged = false;
        }

        if (exchanged) {
            browserPerfDataCoreInfo.setUniqueId(decorator.getUniqueId());
            browserPerfDataCoreInfo.setServiceId(decorator.getServiceId());
            browserPerfDataCoreInfo.setServiceVersionId(decorator.getServiceVersionId());
            browserPerfDataCoreInfo.setPagePathId(decorator.getPagePathId());
            browserPerfDataCoreInfo.setPagePath(decorator.getPagePath());
            browserPerfDataCoreInfo.setError(decorator.isError());
            browserPerfDataCoreInfo.setDataBinary(decorator.toByteArray());
            long minuteTimeBucket = TimeBucket.getMinuteTimeBucket(decorator.getTime());
            browserPerfDataCoreInfo.setTime(decorator.getTime());
            browserPerfDataCoreInfo.setMinuteTimeBucket(minuteTimeBucket);
            notifyParseListener(decorator);
        }
        return exchanged;
    }

    private void writeToBufferFile(BrowserPerfDataDecorator decorator) {
        if (log.isDebugEnabled()) {
            log.debug("push to segment buffer write worker, serviceId: {}, serviceVersionId: {}", decorator.getServiceId(), decorator.getServiceVersionId());
        }

        BrowserPerfDataStandardization standardization = new BrowserPerfDataStandardization();
        /**
         * {@link BrowserPerfData#getTime()} It may be set by the backend.
         */
        standardization.setBrowserPerfData(decorator.build());
        standardizationWorker.in(standardization);
    }

    private void notifyListenerToBuild() {
        browserPerfDataListeners.forEach(BrowserPerfDataListener::build);
    }

    private void notifyParseListener(BrowserPerfDataDecorator decorator) {
        browserPerfDataListeners.forEach(listener -> listener.parse(decorator, browserPerfDataCoreInfo));
    }

    private void createBrowserPerfListeners() {
        listenerManager.getBrowserPerfListenerFactories().forEach(listenerFactory -> browserPerfDataListeners.add(listenerFactory.create(moduleManager, config)));
    }

    public static class Producer implements DataStreamReader.CallBack<BrowserPerfData> {

        @Setter
        private BrowserPerfDataStandardizationWorker standardizationWorker;
        private final ModuleManager moduleManager;
        private final BrowserPerfDataParseListenerManager listenerManager;
        private final BrowserServiceModuleConfig config;

        public Producer(ModuleManager moduleManager, BrowserPerfDataParseListenerManager listenerManager, BrowserServiceModuleConfig config) {
            this.moduleManager = moduleManager;
            this.listenerManager = listenerManager;
            this.config = config;
        }

        public void send(BrowserPerfData perf) {
            BrowserPerfDataParse perfParse = new BrowserPerfDataParse(moduleManager, listenerManager, config);
            perfParse.setStandardizationWorker(standardizationWorker);
            perfParse.parse(new BufferData<>(perf), BrowserPerfDataSource.Browser);
        }

        @Override
        public boolean call(BufferData<BrowserPerfData> bufferData) {
            BrowserPerfDataParse perfParse = new BrowserPerfDataParse(moduleManager, listenerManager, config);
            boolean parseResult = perfParse.parse(bufferData, BrowserPerfDataSource.Buffer);
            if (parseResult) {
                BROWSER_PERF_BUFFER_FILE_OUT.inc();
            }
            return parseResult;
        }
    }
}

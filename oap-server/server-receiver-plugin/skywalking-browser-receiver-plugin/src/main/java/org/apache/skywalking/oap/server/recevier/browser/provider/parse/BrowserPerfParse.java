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
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfCoreInfo;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfDataDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.BrowserPerfListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.standardization.BrowserPerfStandardization;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.standardization.BrowserPerfStandardizationWorker;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.standardization.EndpointIdExchanger;
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
public class BrowserPerfParse {

    private final ModuleManager moduleManager;
    private final BrowserPerfParseListenerManager listenerManager;
    private final BrowserServiceModuleConfig config;
    private final List<BrowserPerfListener> browserPerfListeners;
    private final ServiceInstanceInventoryCache serviceInstanceInventoryCache;
    private final BrowserPerfCoreInfo browserPerfCoreInfo;
    @Setter private BrowserPerfStandardizationWorker standardizationWorker;
    private volatile static CounterMetrics BROWSER_PERF_BUFFER_FILE_RETRY;
    private volatile static CounterMetrics BROWSER_PERF_BUFFER_FILE_OUT;
    private volatile static CounterMetrics BROWSER_PERF_PARSE_ERROR;

    public BrowserPerfParse(ModuleManager moduleManager, BrowserPerfParseListenerManager listenerManager, BrowserServiceModuleConfig config) {
        this.moduleManager = moduleManager;
        this.listenerManager = listenerManager;
        this.config = config;
        this.browserPerfListeners = new LinkedList<>();
        this.browserPerfCoreInfo = new BrowserPerfCoreInfo();

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

    public boolean parse(BufferData<BrowserPerfData> bufferData, BrowserPerfSource source) {
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

                if (source.equals(BrowserPerfSource.Browser)) {
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
        if (!EndpointIdExchanger.getInstance(moduleManager).exchange(decorator, browserPerfCoreInfo.getServiceId())) {
            exchanged = false;
        }

        if (exchanged) {
            browserPerfCoreInfo.setServiceId(decorator.getServiceId());
            browserPerfCoreInfo.setServiceVersionId(decorator.getServiceVersionId());
            browserPerfCoreInfo.setPagePathId(decorator.getPagePathId());
            browserPerfCoreInfo.setPagePath(decorator.getPagePath());
            browserPerfCoreInfo.setError(decorator.isError());
            long minuteTimeBucket = TimeBucket.getMinuteTimeBucket(decorator.getTime());
            browserPerfCoreInfo.setMinuteTimeBucket(minuteTimeBucket);

            notifyParseListener(decorator);
        }
        return exchanged;
    }

    private void writeToBufferFile(BrowserPerfDataDecorator decorator) {
        if (log.isDebugEnabled()) {
            log.debug("push to segment buffer write worker, serviceId: {}, serviceVersionId: {}", decorator.getServiceId(), decorator.getServiceVersionId());
        }

        BrowserPerfStandardization standardization = new BrowserPerfStandardization();
        /**
         * {@link BrowserPerfData#getTime()} It may be set by the backend.
         */
        standardization.setBrowserPerfData(decorator.build());
        standardizationWorker.in(standardization);
    }

    private void notifyListenerToBuild() {
        browserPerfListeners.forEach(BrowserPerfListener::build);
    }

    private void notifyParseListener(BrowserPerfDataDecorator decorator) {
        browserPerfListeners.forEach(listener -> listener.parse(decorator, browserPerfCoreInfo));
    }

    private void createBrowserPerfListeners() {
        listenerManager.getBrowserPerfListenerFactories().forEach(listenerFactory -> browserPerfListeners.add(listenerFactory.create(moduleManager, config)));
    }

    public static class Producer implements DataStreamReader.CallBack<BrowserPerfData> {

        @Setter
        private BrowserPerfStandardizationWorker standardizationWorker;
        private final ModuleManager moduleManager;
        private final BrowserPerfParseListenerManager listenerManager;
        private final BrowserServiceModuleConfig config;

        public Producer(ModuleManager moduleManager, BrowserPerfParseListenerManager listenerManager, BrowserServiceModuleConfig config) {
            this.moduleManager = moduleManager;
            this.listenerManager = listenerManager;
            this.config = config;
        }

        public void send(BrowserPerfData perf) {
            BrowserPerfParse perfParse = new BrowserPerfParse(moduleManager, listenerManager, config);
            perfParse.setStandardizationWorker(standardizationWorker);
            perfParse.parse(new BufferData<>(perf), BrowserPerfSource.Browser);
        }

        @Override
        public boolean call(BufferData<BrowserPerfData> bufferData) {
            BrowserPerfParse perfParse = new BrowserPerfParse(moduleManager, listenerManager, config);
            boolean parseResult = perfParse.parse(bufferData, BrowserPerfSource.Buffer);
            if (parseResult) {
                BROWSER_PERF_BUFFER_FILE_OUT.inc();
            }
            return parseResult;
        }
    }
}

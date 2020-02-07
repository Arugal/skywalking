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

package org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.network.language.agent.BrowserErrorLog;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.cache.ServiceInstanceInventoryCache;
import org.apache.skywalking.oap.server.library.buffer.BufferData;
import org.apache.skywalking.oap.server.library.buffer.DataStreamReader;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.BrowserServiceModuleConfig;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.BrowserDataSource;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.decorator.BrowserErrorLogCoreInfo;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.decorator.BrowserErrorLogDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.listener.BrowserErrorLogListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.standardization.BrowserErrorLogStandardization;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.standardization.BrowserErrorLogStandardizationWorker;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.standardization.PagePathIdExchanger;
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
public class BrowserErrorLogParser {

    private final ModuleManager moduleManager;
    private final BrowserErrorLogParserListenerManager listenerManager;
    private final BrowserServiceModuleConfig config;
    private final List<BrowserErrorLogListener> browserErrorLogListeners;
    private final ServiceInstanceInventoryCache serviceInstanceInventoryCache;
    private final BrowserErrorLogCoreInfo browserErrorLogCoreInfo;
    @Setter
    private BrowserErrorLogStandardizationWorker standardizationWorker;
    private volatile static CounterMetrics BROWSER_ERROR_LOG_BUFFER_FILE_RETRY;
    private volatile static CounterMetrics BROWSER_ERROR_LOG_BUFFER_FILE_OUT;
    private volatile static CounterMetrics BROWSER_ERROR_LOG_PARSE_ERROR;

    public BrowserErrorLogParser(ModuleManager moduleManager, BrowserErrorLogParserListenerManager listenerManager, BrowserServiceModuleConfig config) {
        this.moduleManager = moduleManager;
        this.listenerManager = listenerManager;
        this.config = config;
        this.browserErrorLogListeners = new LinkedList<>();
        this.browserErrorLogCoreInfo = new BrowserErrorLogCoreInfo();

        if (BROWSER_ERROR_LOG_BUFFER_FILE_RETRY == null) {
            MetricsCreator metricsCreator = moduleManager.find(TelemetryModule.NAME).provider().getService(MetricsCreator.class);
            BROWSER_ERROR_LOG_BUFFER_FILE_RETRY = metricsCreator.createCounter("browser_error_log_buffer_file_retry", "The number of retry browser error log from the buffer file, but haven't registered successfully.",
                MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
            BROWSER_ERROR_LOG_BUFFER_FILE_OUT = metricsCreator.createCounter("browser_error_log_buffer_file_out", "The number of browser error log out of the buffer file",
                MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
            BROWSER_ERROR_LOG_PARSE_ERROR = metricsCreator.createCounter("browser_error_log_parse_error", "The number of browser error log parse data",
                MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE);
        }
        this.serviceInstanceInventoryCache = moduleManager.find(CoreModule.NAME).provider().getService(ServiceInstanceInventoryCache.class);
    }

    public boolean parse(BufferData<BrowserErrorLog> bufferData, BrowserDataSource source) {
        createBrowserErrorLogListeners();
        try {
            BrowserErrorLog browserErrorLog = bufferData.getMessageType();

            final int serviceVersionId = browserErrorLog.getServiceVersionId();
            if (serviceInstanceInventoryCache.get(serviceVersionId) == null) {
                log.warn("Cannot recognize service version id [{}] from cache, browser error log will be ignored", serviceVersionId);
                return true;
            }

            BrowserErrorLogDecorator decorator = new BrowserErrorLogDecorator(browserErrorLog);
            if (!preBuild(decorator)) {
                if (log.isDebugEnabled()) {
                    log.debug("This browser error log id exchange not success, write to buffer file, serviceId:{}, pagePath:{}",
                        decorator.getServiceId(), decorator.getPagePath());
                }

                if (source.equals(BrowserDataSource.Browser)) {
                    writeToBufferFile(decorator);
                } else {
                    BROWSER_ERROR_LOG_BUFFER_FILE_RETRY.inc();
                }
                return false;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("This browser error log id exchange success, serviceId: {}, pagePath:{}",
                        decorator.getServiceId(), decorator.getPagePath());
                }

                notifyListenerToBuild();
                return true;
            }
        } catch (Throwable e) {
            BROWSER_ERROR_LOG_PARSE_ERROR.inc();
            log.error(e.getMessage(), e);
            return true;
        }
    }

    private boolean preBuild(BrowserErrorLogDecorator decorator) {
        if (decorator.getTime() == Const.NONE) {
            long nowMillis = System.currentTimeMillis();
            decorator.setTime(nowMillis);
        }

        boolean exchanged = true;
        if (!PagePathIdExchanger.getInstance(moduleManager).exchange(decorator, decorator.getServiceId())) {
            exchanged = false;
        }

        if (exchanged) {
            browserErrorLogCoreInfo.setUniqueId(decorator.getUniqueId());
            browserErrorLogCoreInfo.setServiceId(decorator.getServiceId());
            browserErrorLogCoreInfo.setServiceVersionId(decorator.getServiceVersionId());
            browserErrorLogCoreInfo.setPagePathId(decorator.getPagePathId());
            long minuteTimeBucket = TimeBucket.getMinuteTimeBucket(decorator.getTime());
            browserErrorLogCoreInfo.setTime(decorator.getTime());
            browserErrorLogCoreInfo.setCategory(decorator.getCategory());
            browserErrorLogCoreInfo.setMinuteTimeBucket(minuteTimeBucket);
            notifyParseListener(decorator);
        }
        return exchanged;
    }

    private void writeToBufferFile(BrowserErrorLogDecorator decorator) {
        if (log.isDebugEnabled()) {
            log.debug("push error log to browser buffer write worker, serviceId: {}, serviceVersionId: {}", decorator.getServiceId(), decorator.getServiceVersionId());
        }
        BrowserErrorLogStandardization standardization = new BrowserErrorLogStandardization();
        standardization.setBrowserErrorLog(decorator.build());
        standardizationWorker.in(standardization);
    }

    private void notifyListenerToBuild() {
        browserErrorLogListeners.forEach(BrowserErrorLogListener::build);
    }

    private void notifyParseListener(BrowserErrorLogDecorator decorator) {
        browserErrorLogListeners.forEach(listener -> listener.parse(decorator, browserErrorLogCoreInfo));
    }

    private void createBrowserErrorLogListeners() {
        listenerManager.getFactories().forEach(listenerFactory -> browserErrorLogListeners.add(listenerFactory.create(moduleManager, config)));
    }

    public static class Producer implements DataStreamReader.CallBack<BrowserErrorLog> {

        @Setter
        private BrowserErrorLogStandardizationWorker standardizationWorker;
        private final ModuleManager moduleManager;
        private final BrowserErrorLogParserListenerManager listenerManager;
        private final BrowserServiceModuleConfig config;

        public Producer(ModuleManager moduleManager, BrowserErrorLogParserListenerManager listenerManager, BrowserServiceModuleConfig config) {
            this.moduleManager = moduleManager;
            this.listenerManager = listenerManager;
            this.config = config;
        }

        public void send(BrowserErrorLog errorLog) {
            BrowserErrorLogParser parser = new BrowserErrorLogParser(moduleManager, listenerManager, config);
            parser.setStandardizationWorker(standardizationWorker);
            parser.parse(new BufferData<>(errorLog), BrowserDataSource.Browser);
        }

        @Override
        public boolean call(BufferData<BrowserErrorLog> bufferData) {
            BrowserErrorLogParser parser = new BrowserErrorLogParser(moduleManager, listenerManager, config);
            boolean parseResult = parser.parse(bufferData, BrowserDataSource.Buffer);
            if (parseResult) {
                BROWSER_ERROR_LOG_BUFFER_FILE_OUT.inc();
            }
            return parseResult;
        }
    }
}

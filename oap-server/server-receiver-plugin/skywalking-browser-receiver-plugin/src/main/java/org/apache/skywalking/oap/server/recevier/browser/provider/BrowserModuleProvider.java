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

package org.apache.skywalking.oap.server.recevier.browser.provider;

import org.apache.skywalking.oap.server.configuration.api.ConfigurationModule;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.oal.rt.OALEngine;
import org.apache.skywalking.oap.server.core.oal.rt.OALEngineService;
import org.apache.skywalking.oap.server.core.server.GRPCHandlerRegister;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.module.ServiceNotProvidedException;
import org.apache.skywalking.oap.server.receiver.sharing.server.SharingServerModule;
import org.apache.skywalking.oap.server.recevier.browser.module.BrowserModule;
import org.apache.skywalking.oap.server.recevier.browser.provider.handler.BrowserPerfServiceHandler;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.BrowserErrorLogParser;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.BrowserErrorLogParserListenerManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.listener.errorlog.MultiScopesBrowserAppErrorLogListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.listener.record.BrowserErrorLogRecordListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.standardization.BrowserErrorLogStandardizationWorker;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.BrowserPerfDataParseListenerManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.BrowserPerfDataParser;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.listener.inventory.BrowserHeartbeatListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.listener.performance.MultiScopesBrowserAppPerfListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.standardization.BrowserPerfDataStandardizationWorker;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;

import java.io.IOException;

/**
 * @author zhangwei
 */
public class BrowserModuleProvider extends ModuleProvider {

    private final BrowserServiceModuleConfig moduleConfig = new BrowserServiceModuleConfig();
    private BrowserPerfDataParser.Producer browserPerfDataProducer;
    private BrowserErrorLogParser.Producer browserErrorLogProducer;

    @Override
    public String name() {
        return "default";
    }

    @Override
    public Class<? extends ModuleDefine> module() {
        return BrowserModule.class;
    }

    @Override
    public ModuleConfig createConfigBeanIfAbsent() {
        return moduleConfig;
    }

    @Override
    public void prepare() throws ServiceNotProvidedException {
        browserPerfDataProducer = new BrowserPerfDataParser.Producer(getManager(), performanceListenerManager(), moduleConfig);
        browserErrorLogProducer = new BrowserErrorLogParser.Producer(getManager(), errorLogListenerManager(), moduleConfig);
    }

    private BrowserPerfDataParseListenerManager performanceListenerManager() {
        BrowserPerfDataParseListenerManager listenerManager = new BrowserPerfDataParseListenerManager();
        listenerManager.add(new MultiScopesBrowserAppPerfListener.Factory());
        listenerManager.add(new BrowserHeartbeatListener.Factory());
        return listenerManager;
    }

    private BrowserErrorLogParserListenerManager errorLogListenerManager() {
        BrowserErrorLogParserListenerManager listenerManager = new BrowserErrorLogParserListenerManager();
        listenerManager.add(new BrowserErrorLogRecordListener.Factory(moduleConfig.getSampleRate()));
        listenerManager.add(new MultiScopesBrowserAppErrorLogListener.Factory());
        return listenerManager;
    }


    @Override
    public void start() throws ServiceNotProvidedException, ModuleStartException {
        getManager().find(CoreModule.NAME).provider().getService(OALEngineService.class).activate(OALEngine.Group.BROWSER);

        GRPCHandlerRegister grpcHandlerRegister = getManager().find(SharingServerModule.NAME).provider().getService(GRPCHandlerRegister.class);
        try {
            grpcHandlerRegister.addHandler(new BrowserPerfServiceHandler(browserPerfDataProducer, browserErrorLogProducer, getManager()));

            // performance
            BrowserPerfDataStandardizationWorker perfDataStandardizationWorker = new BrowserPerfDataStandardizationWorker(getManager(), browserPerfDataProducer,
                moduleConfig.getPerfBufferPath(), moduleConfig.getPerfBufferOffsetMaxFileSize(),
                moduleConfig.getPerfBufferDataMaxFileSize(), moduleConfig.isPerfBufferFileCleanWhenRestart());
            browserPerfDataProducer.setStandardizationWorker(perfDataStandardizationWorker);

            // error log
            BrowserErrorLogStandardizationWorker errorLogStandardizationWorker = new BrowserErrorLogStandardizationWorker(getManager(), browserErrorLogProducer,
                moduleConfig.getErrorLogBufferPath(), moduleConfig.getErrorLogBufferOffsetMaxFileSize(),
                moduleConfig.getErrorLogBufferDataMaxFileSize(), moduleConfig.isErrorLogBufferFileCleanWhenRestart());
            browserErrorLogProducer.setStandardizationWorker(errorLogStandardizationWorker);
        } catch (IOException e) {
            throw new ModuleStartException(e.getMessage(), e);
        }
    }

    @Override
    public void notifyAfterCompleted() throws ServiceNotProvidedException, ModuleStartException {
    }

    @Override
    public String[] requiredModules() {
        return new String[] {TelemetryModule.NAME, CoreModule.NAME, SharingServerModule.NAME, ConfigurationModule.NAME};
    }
}

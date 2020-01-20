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
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.BrowserPerfDataParse;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.BrowserPerfDataParseListenerManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.detail.MultiScopesPerfDetailDataListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.errorlog.BrowserErrorLogListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.inventory.BrowserInventoryListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.standardization.BrowserPerfDataStandardizationWorker;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;

import java.io.IOException;

/**
 * @author zhangwei
 */
public class BrowserModuleProvider extends ModuleProvider {

    private final BrowserServiceModuleConfig moduleConfig = new BrowserServiceModuleConfig();
    private BrowserPerfDataParse.Producer browserPerfProducer;

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
        browserPerfProducer = new BrowserPerfDataParse.Producer(getManager(), listenerManager(), moduleConfig);
    }

    public BrowserPerfDataParseListenerManager listenerManager() {
        BrowserPerfDataParseListenerManager listenerManager = new BrowserPerfDataParseListenerManager();
        listenerManager.add(new MultiScopesPerfDetailDataListener.Factory());
        listenerManager.add(new BrowserErrorLogListener.Factory(moduleConfig.getSampleRate()));
        listenerManager.add(new BrowserInventoryListener.Factory());
        return listenerManager;
    }

    @Override
    public void start() throws ServiceNotProvidedException, ModuleStartException {
        getManager().find(CoreModule.NAME).provider().getService(OALEngineService.class).activate(OALEngine.Group.BROWSER);

        GRPCHandlerRegister grpcHandlerRegister = getManager().find(SharingServerModule.NAME).provider().getService(GRPCHandlerRegister.class);
        try {

            grpcHandlerRegister.addHandler(new BrowserPerfServiceHandler(browserPerfProducer, getManager()));
            BrowserPerfDataStandardizationWorker standardizationWorker = new BrowserPerfDataStandardizationWorker(getManager(), browserPerfProducer,
                moduleConfig.getBufferPath(), moduleConfig.getBufferOffsetMaxFileSize(), moduleConfig.getBufferDataMaxFileSize(), moduleConfig.isBufferFileCleanWhenRestart());
            browserPerfProducer.setStandardizationWorker(standardizationWorker);
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

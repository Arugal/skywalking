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

package org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.inventory;

import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.register.service.IServiceInstanceInventoryRegister;
import org.apache.skywalking.oap.server.core.register.service.IServiceInventoryRegister;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.BrowserServiceModuleConfig;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfDataCoreInfo;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfDataDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.BrowserPerfDataListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.BrowserPerfDataListenerFactory;

/**
 * @author zhangwei
 */
public class BrowserInventoryListener implements BrowserPerfDataListener {

    private final IServiceInventoryRegister serviceInventoryRegister;
    private final IServiceInstanceInventoryRegister serviceInstanceInventoryRegister;
    private BrowserPerfDataCoreInfo coreInfo;

    public BrowserInventoryListener(ModuleManager moduleManager) {
        this.serviceInventoryRegister = moduleManager.find(CoreModule.NAME).provider().getService(IServiceInventoryRegister.class);
        this.serviceInstanceInventoryRegister = moduleManager.find(CoreModule.NAME).provider().getService(IServiceInstanceInventoryRegister.class);
    }

    @Override
    public void build() {
        // TODO Sample
        serviceInventoryRegister.heartbeat(coreInfo.getServiceId(), coreInfo.getTime());
        serviceInstanceInventoryRegister.heartbeat(coreInfo.getServiceVersionId(), coreInfo.getTime());
    }

    @Override
    public void parse(BrowserPerfDataDecorator decorator, BrowserPerfDataCoreInfo coreInfo) {
        this.coreInfo = coreInfo;
    }

    public static class Factory implements BrowserPerfDataListenerFactory {

        @Override
        public BrowserPerfDataListener create(ModuleManager moduleManager, BrowserServiceModuleConfig moduleConfig) {
            return new BrowserInventoryListener(moduleManager);
        }
    }
}

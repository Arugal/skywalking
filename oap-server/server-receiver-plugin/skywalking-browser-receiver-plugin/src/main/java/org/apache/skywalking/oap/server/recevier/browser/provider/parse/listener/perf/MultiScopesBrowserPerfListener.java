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

package org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.perf;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.cache.ServiceInstanceInventoryCache;
import org.apache.skywalking.oap.server.core.cache.ServiceInventoryCache;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.BrowserServiceModuleConfig;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfCoreInfo;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfDataDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.PerfDetailDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.BrowserPerfListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.BrowserPerfListenerFactory;

/**
 * @author zhangwei
 */
@Slf4j
public class MultiScopesBrowserPerfListener implements BrowserPerfListener {

    private final SourceReceiver sourceReceiver;
    private final ServiceInventoryCache serviceInventoryCache;
    private final ServiceInstanceInventoryCache instanceInventoryCache;
    private final BrowserServiceModuleConfig config;
    private final SourceBuilder sourceBuilder;


    public MultiScopesBrowserPerfListener(ModuleManager moduleManager, BrowserServiceModuleConfig config) {
        this.sourceReceiver = moduleManager.find(CoreModule.NAME).provider().getService(SourceReceiver.class);
        this.serviceInventoryCache = moduleManager.find(CoreModule.NAME).provider().getService(ServiceInventoryCache.class);
        this.instanceInventoryCache = moduleManager.find(CoreModule.NAME).provider().getService(ServiceInstanceInventoryCache.class);
        this.sourceBuilder = new SourceBuilder();
        this.config = config;
    }

    @Override
    public void build() {
        sourceReceiver.receive(sourceBuilder.toServicePagePath());
    }

    @Override
    public void parse(BrowserPerfDataDecorator decorator, BrowserPerfCoreInfo coreInfo) {
        sourceBuilder.setServiceId(decorator.getServiceId());
        sourceBuilder.setServiceName(serviceInventoryCache.get(decorator.getServiceId()).getName());
        sourceBuilder.setServiceVersionName(instanceInventoryCache.get(decorator.getServiceVersionId()).getName());
        sourceBuilder.setServiceVersionId(decorator.getServiceVersionId());
        sourceBuilder.setTime(decorator.getTime());
        sourceBuilder.setPagePathId(decorator.getPagePathId());
        sourceBuilder.setPagePath(decorator.getPagePath());
        sourceBuilder.setError(decorator.isError());

        PerfDetailDecorator perfDetailDecorator = decorator.getPerfDetailDecorator();
        sourceBuilder.setRedirectTime(perfDetailDecorator.getRedirectTime());
        sourceBuilder.setDnsTime(perfDetailDecorator.getDnsTime());
        sourceBuilder.setDomAnalysisTime(perfDetailDecorator.getDomAnalysisTime());
        sourceBuilder.setDomReadyTime(perfDetailDecorator.getDomReadyTime());
        sourceBuilder.setBlankTime(perfDetailDecorator.getBlankTime());
    }

    public static class Factory implements BrowserPerfListenerFactory {

        @Override
        public BrowserPerfListener create(ModuleManager moduleManager, BrowserServiceModuleConfig moduleConfig) {
            return new MultiScopesBrowserPerfListener(moduleManager, moduleConfig);
        }
    }
}

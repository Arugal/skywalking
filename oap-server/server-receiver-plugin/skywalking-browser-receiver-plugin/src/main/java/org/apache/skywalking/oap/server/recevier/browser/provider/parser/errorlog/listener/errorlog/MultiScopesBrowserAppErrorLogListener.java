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

package org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.listener.errorlog;

import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.cache.ServiceInstanceInventoryCache;
import org.apache.skywalking.oap.server.core.cache.ServiceInventoryCache;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.BrowserServiceModuleConfig;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.decorator.BrowserErrorLogCoreInfo;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.decorator.BrowserErrorLogDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.listener.BrowserErrorLogListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.listener.BrowserErrorLogListenerFactory;

/**
 * @author zhangwei
 */
public class MultiScopesBrowserAppErrorLogListener implements BrowserErrorLogListener {

    private final SourceReceiver sourceReceiver;
    private final ServiceInventoryCache serviceInventoryCache;
    private final ServiceInstanceInventoryCache serviceInstanceInventoryCache;
    private final SourceBuilder sourceBuilder;

    private MultiScopesBrowserAppErrorLogListener(ModuleManager moduleManager) {
        this.sourceReceiver = moduleManager.find(CoreModule.NAME).provider().getService(SourceReceiver.class);
        this.serviceInventoryCache = moduleManager.find(CoreModule.NAME).provider().getService(ServiceInventoryCache.class);
        this.serviceInstanceInventoryCache = moduleManager.find(CoreModule.NAME).provider().getService(ServiceInstanceInventoryCache.class);
        this.sourceBuilder = new SourceBuilder();
    }

    @Override
    public void build() {
        sourceReceiver.receive(sourceBuilder.toBrowserAppErrorLog());
        sourceReceiver.receive(sourceBuilder.toBrowserAppPageErrorLog());
    }

    @Override
    public void parse(BrowserErrorLogDecorator decorator, BrowserErrorLogCoreInfo coreInfo) {
        sourceBuilder.setServiceId(coreInfo.getServiceId());
        sourceBuilder.setServiceVersionId(coreInfo.getServiceVersionId());
        sourceBuilder.setPagePathId(coreInfo.getPagePathId());
        sourceBuilder.setTime(coreInfo.getTime());
        sourceBuilder.setMinuteTimeBucket(coreInfo.getMinuteTimeBucket());
        sourceBuilder.setPagePath(decorator.getPagePath());
        sourceBuilder.setCategory(coreInfo.getCategory());
    }

    public static class Factory implements BrowserErrorLogListenerFactory {

        @Override
        public BrowserErrorLogListener create(ModuleManager moduleManager, BrowserServiceModuleConfig moduleConfig) {
            return new MultiScopesBrowserAppErrorLogListener(moduleManager);
        }
    }
}

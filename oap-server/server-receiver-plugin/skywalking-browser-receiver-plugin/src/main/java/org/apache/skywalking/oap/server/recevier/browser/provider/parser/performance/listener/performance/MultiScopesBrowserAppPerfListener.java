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

package org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.listener.performance;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.BrowserServiceModuleConfig;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.decorator.BrowserPerfDataCoreInfo;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.decorator.BrowserPerfDataDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.listener.BrowserPerfDataListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.listener.BrowserPerfDataListenerFactory;

/**
 * @author zhangwei
 */
@Slf4j
public class MultiScopesBrowserAppPerfListener implements BrowserPerfDataListener {

    private final SourceReceiver sourceReceiver;
    private final SourceBuilder sourceBuilder;


    private MultiScopesBrowserAppPerfListener(ModuleManager moduleManager) {
        this.sourceReceiver = moduleManager.find(CoreModule.NAME).provider().getService(SourceReceiver.class);
        this.sourceBuilder = new SourceBuilder();
    }

    @Override
    public void build() {
        sourceReceiver.receive(sourceBuilder.toBrowserPerfDetail());
        sourceReceiver.receive(sourceBuilder.toBrowserPagePathPerfDetail());
        sourceReceiver.receive(sourceBuilder.toBrowserSingleVersionPerfDetail());
    }

    @Override
    public void parse(BrowserPerfDataDecorator decorator, BrowserPerfDataCoreInfo coreInfo) {
        sourceBuilder.setServiceId(coreInfo.getServiceId());
        sourceBuilder.setServiceVersionId(coreInfo.getServiceVersionId());
        sourceBuilder.setTime(coreInfo.getTime());
        sourceBuilder.setMinuteTimeBucket(coreInfo.getMinuteTimeBucket());
        sourceBuilder.setPagePathId(coreInfo.getPagePathId());
        sourceBuilder.setPagePath(decorator.getPagePath());

        sourceBuilder.setRedirectTime(decorator.getRedirectTime());
        sourceBuilder.setDnsTime(decorator.getDnsTime());
        sourceBuilder.setReqTime(decorator.getReqTime());
        sourceBuilder.setDomAnalysisTime(decorator.getDomAnalysisTime());
        sourceBuilder.setDomReadyTime(decorator.getDomReadyTime());
        sourceBuilder.setBlankTime(decorator.getBlankTime());
    }

    public static class Factory implements BrowserPerfDataListenerFactory {

        @Override
        public BrowserPerfDataListener create(ModuleManager moduleManager, BrowserServiceModuleConfig moduleConfig) {
            return new MultiScopesBrowserAppPerfListener(moduleManager);
        }
    }
}

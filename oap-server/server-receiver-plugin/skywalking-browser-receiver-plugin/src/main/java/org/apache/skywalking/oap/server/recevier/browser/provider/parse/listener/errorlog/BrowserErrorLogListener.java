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

package org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.errorlog;

import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.browser.source.BrowserPerfData;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.util.BooleanUtils;
import org.apache.skywalking.oap.server.recevier.browser.provider.BrowserServiceModuleConfig;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfDataCoreInfo;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfDataDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.BrowserPerfDataListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.BrowserPerfDataListenerFactory;

/**
 * @author zhangwei
 */
public class BrowserErrorLogListener implements BrowserPerfDataListener {

    private final SourceReceiver sourceReceiver;
    private final BrowserPerfData browserPerfData;
    private BrowserErrorLogSampler sampler;
    private SAMPLE_STATUS sampleStatus = SAMPLE_STATUS.UNKNOWN;

    private BrowserErrorLogListener(ModuleManager moduleManager, BrowserErrorLogSampler sampler) {
        sourceReceiver = moduleManager.find(CoreModule.NAME).provider().getService(SourceReceiver.class);
        browserPerfData = new BrowserPerfData();
        this.sampler = sampler;
    }

    @Override
    public void build() {
        filter(() -> sourceReceiver.receive(browserPerfData));
    }

    @Override
    public void parse(BrowserPerfDataDecorator decorator, BrowserPerfDataCoreInfo coreInfo) {
        filter(coreInfo.getUniqueId(), coreInfo.isError(), () -> {
            browserPerfData.setUniqueId(coreInfo.getUniqueId());
            browserPerfData.setServiceId(coreInfo.getServiceId());
            browserPerfData.setServiceInstanceId(coreInfo.getServiceVersionId());
            browserPerfData.setPagePathId(coreInfo.getPagePathId());
            browserPerfData.setPagePath(coreInfo.getPagePath());
            browserPerfData.setTime(decorator.getTime());
            browserPerfData.setIsError(BooleanUtils.booleanToValue(coreInfo.isError()));
            browserPerfData.setTimeBucket(coreInfo.getMinuteTimeBucket());
            browserPerfData.setDataBinary(coreInfo.getDataBinary());
        });
    }

    private void filter(Runnable runnable) {
        filter(null, false, runnable);
    }

    private void filter(String uniqueId, boolean isError, Runnable runnable) {
        if (sampleStatus.equals(SAMPLE_STATUS.IGNORE)) {
            return;
        }
        if (!isError) {
            sampleStatus = SAMPLE_STATUS.IGNORE;
            return;
        }
        if (sampleStatus.equals(SAMPLE_STATUS.UNKNOWN)) {
            if (sampler.shouldSample(uniqueId.hashCode())) {
                sampleStatus = SAMPLE_STATUS.SAMPLED;
            } else {
                sampleStatus = SAMPLE_STATUS.IGNORE;
            }
        }
        if (sampleStatus.equals(SAMPLE_STATUS.SAMPLED)) {
            runnable.run();
        }
    }

    private enum SAMPLE_STATUS {
        UNKNOWN, SAMPLED, IGNORE
    }

    public static class Factory implements BrowserPerfDataListenerFactory {

        private final BrowserErrorLogSampler sampler;

        public Factory(int segmentSamplingRate) {
            this.sampler = new BrowserErrorLogSampler(segmentSamplingRate);
        }

        @Override
        public BrowserPerfDataListener create(ModuleManager moduleManager, BrowserServiceModuleConfig moduleConfig) {
            return new BrowserErrorLogListener(moduleManager, sampler);
        }
    }
}

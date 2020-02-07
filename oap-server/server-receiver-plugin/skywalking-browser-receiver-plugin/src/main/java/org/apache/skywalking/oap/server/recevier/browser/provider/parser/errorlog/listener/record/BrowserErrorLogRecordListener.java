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

package org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.listener.record;

import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.browser.source.BrowserErrorLog;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.BrowserServiceModuleConfig;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.decorator.BrowserErrorLogCoreInfo;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.decorator.BrowserErrorLogDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.listener.BrowserErrorLogListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.listener.BrowserErrorLogListenerFactory;

/**
 * Process error record.
 *
 * @author zhangwei
 */
public class BrowserErrorLogRecordListener implements BrowserErrorLogListener {

    private final SourceReceiver sourceReceiver;
    private final BrowserErrorLog browserErrorLog;
    private BrowserErrorLogRecordSampler sampler;
    private SAMPLE_STATUS sampleStatus = SAMPLE_STATUS.UNKNOWN;

    private BrowserErrorLogRecordListener(ModuleManager moduleManager, BrowserErrorLogRecordSampler sampler) {
        sourceReceiver = moduleManager.find(CoreModule.NAME).provider().getService(SourceReceiver.class);
        browserErrorLog = new BrowserErrorLog();
        this.sampler = sampler;
    }

    @Override
    public void build() {
        filter(() -> sourceReceiver.receive(browserErrorLog));
    }

    @Override
    public void parse(BrowserErrorLogDecorator decorator, BrowserErrorLogCoreInfo coreInfo) {
        filter(coreInfo.getUniqueId(), () -> {
            browserErrorLog.setUniqueId(coreInfo.getUniqueId());
            browserErrorLog.setServiceId(coreInfo.getServiceId());
            browserErrorLog.setServiceVersionId(coreInfo.getServiceVersionId());
            browserErrorLog.setPagePathId(coreInfo.getPagePathId());
            browserErrorLog.setPagePath(decorator.getPagePath());
            browserErrorLog.setTime(decorator.getTime());
            browserErrorLog.setTimeBucket(coreInfo.getMinuteTimeBucket());
            browserErrorLog.setCategory(coreInfo.getCategory());
            browserErrorLog.setDataBinary(decorator.toByteArray());
        });
    }

    private void filter(Runnable runnable) {
        filter(null, runnable);
    }

    private void filter(String uniqueId, Runnable runnable) {
        if (sampleStatus.equals(SAMPLE_STATUS.IGNORE)) {
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

    public static class Factory implements BrowserErrorLogListenerFactory {

        private final BrowserErrorLogRecordSampler sampler;

        public Factory(int segmentSamplingRate) {
            this.sampler = new BrowserErrorLogRecordSampler(segmentSamplingRate);
        }

        @Override
        public BrowserErrorLogListener create(ModuleManager moduleManager, BrowserServiceModuleConfig moduleConfig) {
            return new BrowserErrorLogRecordListener(moduleManager, sampler);
        }
    }
}

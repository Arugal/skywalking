package org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.record;

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
public class PerfDataListener implements BrowserPerfDataListener {

    private final SourceReceiver sourceReceiver;
    private final BrowserPerfData browserPerfData;
    private PerfDataSampler sampler;
    private SAMPLE_STATUS sampleStatus = SAMPLE_STATUS.UNKNOWN;

    private PerfDataListener(ModuleManager moduleManager, PerfDataSampler sampler) {
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
            if (sampler.shouldSample(uniqueId)) {
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

        private final PerfDataSampler sampler;

        public Factory(int segmentSamplingRate) {
            this.sampler = new PerfDataSampler(segmentSamplingRate);
        }

        @Override
        public BrowserPerfDataListener create(ModuleManager moduleManager, BrowserServiceModuleConfig moduleConfig) {
            return new PerfDataListener(moduleManager, sampler);
        }
    }
}

package org.apache.skywalking.oap.server.core.browser.manual;

import org.apache.skywalking.oap.server.core.analysis.SourceDispatcher;
import org.apache.skywalking.oap.server.core.analysis.worker.RecordStreamProcessor;
import org.apache.skywalking.oap.server.core.browser.source.BrowserPerfData;

/**
 * @author zhangwei
 */
public class BrowserPerfDataDispatcher implements SourceDispatcher<BrowserPerfData> {

    @Override
    public void dispatch(BrowserPerfData source) {
        BrowserPerfDataRecord record = new BrowserPerfDataRecord();

        record.setUniqueId(source.getUniqueId());
        record.setServiceId(source.getServiceId());
        record.setServiceInstanceId(source.getServiceInstanceId());
        record.setPagePathId(source.getPagePathId());
        record.setPagePath(source.getPagePath());
        record.setTime(source.getTime());
        record.setTimeBucket(source.getTimeBucket());
        record.setIsError(source.getIsError());
        record.setDataBinary(source.getDataBinary());

        RecordStreamProcessor.getInstance().in(record);
    }
}

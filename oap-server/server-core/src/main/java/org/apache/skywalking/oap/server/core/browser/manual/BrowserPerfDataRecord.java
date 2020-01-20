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

package org.apache.skywalking.oap.server.core.browser.manual;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.apm.util.StringUtil;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.analysis.Stream;
import org.apache.skywalking.oap.server.core.analysis.record.Record;
import org.apache.skywalking.oap.server.core.analysis.worker.RecordStreamProcessor;
import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.annotation.Column;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangwei
 */
@Stream(name = BrowserPerfDataRecord.INDEX_NAME, scopeId = DefaultScopeDefine.BROWSER_PERF_DATA, builder = BrowserPerfDataRecord.Builder.class, processor = RecordStreamProcessor.class)
public class BrowserPerfDataRecord extends Record {

    public static final String INDEX_NAME = "browser_perf_data";
    public static final String UNIQUE_ID = "unique_id";
    public static final String SERVICE_ID = "service_id";
    public static final String SERVICE_INSTANCE_ID = "service_instance_id";
    public static final String PAGE_PATH_ID = "page_path_id";
    public static final String PAGE_PATH = "page_path";
    public static final String TIME = "time";
    public static final String IS_ERROR = "is_error";
    public static final String DATA_BINARY = "data_binary";

    @Setter @Getter @Column(columnName = UNIQUE_ID) private String uniqueId;
    @Getter @Setter @Column(columnName = SERVICE_ID) private int serviceId;
    @Getter @Setter @Column(columnName = SERVICE_INSTANCE_ID) private int serviceInstanceId;
    @Getter @Setter @Column(columnName = PAGE_PATH_ID) private int pagePathId;
    @Getter @Setter @Column(columnName = PAGE_PATH, matchQuery = true) private String pagePath;
    @Getter @Setter @Column(columnName = TIME) private long time;
    @Getter @Setter @Column(columnName = IS_ERROR) private int isError;
    @Getter @Setter @Column(columnName = DATA_BINARY) private byte[] dataBinary;

    @Override
    public String id() {
        return uniqueId;
    }

    public static class Builder implements StorageBuilder<BrowserPerfDataRecord> {

        @Override
        public BrowserPerfDataRecord map2Data(Map<String, Object> dbMap) {
            BrowserPerfDataRecord record = new BrowserPerfDataRecord();
            record.setUniqueId((String) dbMap.get(UNIQUE_ID));
            record.setServiceId(((Number) dbMap.get(SERVICE_ID)).intValue());
            record.setServiceInstanceId(((Number) dbMap.get(SERVICE_INSTANCE_ID)).intValue());
            record.setPagePathId(((Number) dbMap.get(PAGE_PATH_ID)).intValue());
            record.setPagePath((String) dbMap.get(PAGE_PATH));
            record.setTime(((Number) dbMap.get(TIME)).longValue());
            record.setIsError(((Number) dbMap.get(IS_ERROR)).intValue());
            record.setTimeBucket(((Number) dbMap.get(TIME_BUCKET)).intValue());
            String dataBinary = (String) dbMap.get(DATA_BINARY);
            if (StringUtil.isEmpty(dataBinary)) {
                record.setDataBinary(new byte[]{});
            } else {
                record.setDataBinary(Base64.getDecoder().decode(dataBinary));
            }
            return record;
        }

        @Override
        public Map<String, Object> data2Map(BrowserPerfDataRecord storageData) {
            Map<String, Object> map = new HashMap<>();
            map.put(UNIQUE_ID, storageData.getUniqueId());
            map.put(SERVICE_ID, storageData.getServiceId());
            map.put(SERVICE_INSTANCE_ID, storageData.getServiceInstanceId());
            map.put(PAGE_PATH_ID, storageData.getPagePathId());
            map.put(PAGE_PATH, storageData.getPagePath());
            map.put(TIME, storageData.getTime());
            map.put(IS_ERROR, storageData.getIsError());
            map.put(TIME_BUCKET, storageData.getTimeBucket());
            if (CollectionUtils.isEmpty(storageData.getDataBinary())) {
                map.put(DATA_BINARY, Const.EMPTY_STRING);
            } else {
                map.put(DATA_BINARY, new String(Base64.getEncoder().encode(storageData.getDataBinary())));
            }
            return map;
        }
    }
}

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

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.browser.source.BrowserAppErrorLog;
import org.apache.skywalking.oap.server.core.browser.source.BrowserAppPageErrorLog;
import org.apache.skywalking.oap.server.core.browser.source.BrowserErrorCategory;

/**
 * @author zhangwei
 */
@Setter
@Getter
class SourceBuilder {

    private int serviceId;
    private int serviceVersionId;
    private int pagePathId;
    private String pagePath;
    private long time;
    private long minuteTimeBucket;
    private BrowserErrorCategory category;

    BrowserAppErrorLog toBrowserAppErrorLog() {
        BrowserAppErrorLog errorLog = new BrowserAppErrorLog();
        errorLog.setId(serviceId);
        errorLog.setCategory(category);
        errorLog.setTimeBucket(minuteTimeBucket);
        return errorLog;
    }

    BrowserAppPageErrorLog toBrowserAppPageErrorLog() {
        BrowserAppPageErrorLog errorLog = new BrowserAppPageErrorLog();
        errorLog.setId(pagePathId);
        errorLog.setServiceId(serviceId);
        errorLog.setTimeBucket(minuteTimeBucket);
        errorLog.setCategory(category);
        return errorLog;
    }
}

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

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.browser.source.BrowserAppPagePerf;
import org.apache.skywalking.oap.server.core.browser.source.BrowserAppPerf;
import org.apache.skywalking.oap.server.core.browser.source.BrowserAppSingleVersionPerf;

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

    // perf detail
    private int redirectTime;
    private int dnsTime;
    private int reqTime;
    private int domAnalysisTime;
    private int domReadyTime;
    private int blankTime;

    BrowserAppPerf toBrowserPerfDetail() {
        BrowserAppPerf browserAppPerf = new BrowserAppPerf();
        browserAppPerf.setId(serviceId);
        browserAppPerf.setRedirectTime(redirectTime);
        browserAppPerf.setDnsTime(dnsTime);
        browserAppPerf.setReqTime(reqTime);
        browserAppPerf.setDomAnalysisTime(domAnalysisTime);
        browserAppPerf.setDomReadyTime(domReadyTime);
        browserAppPerf.setBlankTime(blankTime);
        browserAppPerf.setTimeBucket(minuteTimeBucket);
        return browserAppPerf;
    }

    BrowserAppPagePerf toBrowserPagePathPerfDetail() {
        BrowserAppPagePerf browserAppPagePerf = new BrowserAppPagePerf();
        browserAppPagePerf.setId(pagePathId);
        browserAppPagePerf.setServiceId(serviceId);
        browserAppPagePerf.setRedirectTime(redirectTime);
        browserAppPagePerf.setDnsTime(dnsTime);
        browserAppPagePerf.setReqTime(reqTime);
        browserAppPagePerf.setDomAnalysisTime(domAnalysisTime);
        browserAppPagePerf.setDomReadyTime(domReadyTime);
        browserAppPagePerf.setBlankTime(blankTime);
        browserAppPagePerf.setTimeBucket(minuteTimeBucket);
        return browserAppPagePerf;
    }

    BrowserAppSingleVersionPerf toBrowserSingleVersionPerfDetail() {
        BrowserAppSingleVersionPerf browserAppSingleVersionPerf = new BrowserAppSingleVersionPerf();
        browserAppSingleVersionPerf.setId(serviceVersionId);
        browserAppSingleVersionPerf.setRedirectTime(redirectTime);
        browserAppSingleVersionPerf.setDnsTime(dnsTime);
        browserAppSingleVersionPerf.setReqTime(reqTime);
        browserAppSingleVersionPerf.setDomAnalysisTime(domAnalysisTime);
        browserAppSingleVersionPerf.setDomReadyTime(domReadyTime);
        browserAppSingleVersionPerf.setBlankTime(blankTime);
        browserAppSingleVersionPerf.setTimeBucket(minuteTimeBucket);
        return browserAppSingleVersionPerf;
    }
}

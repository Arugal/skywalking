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

package org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.detail;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.browser.source.BrowserPagePathPerfDetail;
import org.apache.skywalking.oap.server.core.browser.source.BrowserPerfDetail;
import org.apache.skywalking.oap.server.core.browser.source.BrowserSingleVersionPagePathPerfDetail;
import org.apache.skywalking.oap.server.core.browser.source.BrowserSingleVersionPerfDetail;

/**
 * @author zhangwei
 */
@Setter
@Getter
class SourceBuilder {

    private int serviceId;
    private String serviceName;
    private int serviceVersionId;
    private String serviceVersionName;
    private long time;
    private long minuteTimeBucket;
    private int pagePathId;
    private String pagePath;

    private boolean isError;
    private boolean status;

    // perfDetail
    private int redirectTime;
    private int dnsTime;
    private int reqTime;
    private int domAnalysisTime;
    private int domReadyTime;
    private int blankTime;

    public void setError(boolean error) {
        isError = error;
        status = !error;
    }

    BrowserPerfDetail toBrowserPerfDetail() {
        BrowserPerfDetail browserPerfDetail = new BrowserPerfDetail();
        browserPerfDetail.setId(serviceId);
        browserPerfDetail.setName(serviceVersionName);
        browserPerfDetail.setStatus(status);
        browserPerfDetail.setRedirectTime(redirectTime);
        browserPerfDetail.setDnsTime(dnsTime);
        browserPerfDetail.setReqTime(reqTime);
        browserPerfDetail.setDomAnalysisTime(domAnalysisTime);
        browserPerfDetail.setDomReadyTime(domReadyTime);
        browserPerfDetail.setBlankTime(blankTime);
        browserPerfDetail.setTimeBucket(minuteTimeBucket);
        return browserPerfDetail;
    }

    BrowserPagePathPerfDetail toBrowserPagePathPerfDetail() {
        BrowserPagePathPerfDetail browserPagePathPerfDetail = new BrowserPagePathPerfDetail();
        browserPagePathPerfDetail.setId(pagePathId);
        browserPagePathPerfDetail.setName(pagePath);
        browserPagePathPerfDetail.setServiceId(serviceId);
        browserPagePathPerfDetail.setServiceName(serviceName);
        browserPagePathPerfDetail.setStatus(status);
        browserPagePathPerfDetail.setRedirectTime(redirectTime);
        browserPagePathPerfDetail.setDnsTime(dnsTime);
        browserPagePathPerfDetail.setReqTime(reqTime);
        browserPagePathPerfDetail.setDomAnalysisTime(domAnalysisTime);
        browserPagePathPerfDetail.setDomReadyTime(domReadyTime);
        browserPagePathPerfDetail.setBlankTime(blankTime);
        browserPagePathPerfDetail.setTimeBucket(minuteTimeBucket);
        return browserPagePathPerfDetail;
    }

    BrowserSingleVersionPerfDetail toBrowserSingleVersionPerfDetail() {
        BrowserSingleVersionPerfDetail browserSingleVersionPerfDetail = new BrowserSingleVersionPerfDetail();
        browserSingleVersionPerfDetail.setId(serviceVersionId);
        browserSingleVersionPerfDetail.setName(serviceVersionName);
        browserSingleVersionPerfDetail.setStatus(status);
        browserSingleVersionPerfDetail.setRedirectTime(redirectTime);
        browserSingleVersionPerfDetail.setDnsTime(dnsTime);
        browserSingleVersionPerfDetail.setReqTime(reqTime);
        browserSingleVersionPerfDetail.setDomAnalysisTime(domAnalysisTime);
        browserSingleVersionPerfDetail.setDomReadyTime(domReadyTime);
        browserSingleVersionPerfDetail.setBlankTime(blankTime);
        browserSingleVersionPerfDetail.setTimeBucket(minuteTimeBucket);
        return browserSingleVersionPerfDetail;
    }

    BrowserSingleVersionPagePathPerfDetail toBrowserSingleVersionPagePathPerfDetail() {
        BrowserSingleVersionPagePathPerfDetail browserSingleVersionPagePathPerfDetail = new BrowserSingleVersionPagePathPerfDetail();
        browserSingleVersionPagePathPerfDetail.setId(pagePathId);
        browserSingleVersionPagePathPerfDetail.setName(pagePath);
        browserSingleVersionPagePathPerfDetail.setServiceVersionId(serviceVersionId);
        browserSingleVersionPagePathPerfDetail.setServiceVersionName(serviceVersionName);
        browserSingleVersionPagePathPerfDetail.setStatus(status);
        browserSingleVersionPagePathPerfDetail.setRedirectTime(redirectTime);
        browserSingleVersionPagePathPerfDetail.setDnsTime(dnsTime);
        browserSingleVersionPagePathPerfDetail.setReqTime(reqTime);
        browserSingleVersionPagePathPerfDetail.setDomAnalysisTime(domAnalysisTime);
        browserSingleVersionPagePathPerfDetail.setDomReadyTime(domReadyTime);
        browserSingleVersionPagePathPerfDetail.setBlankTime(blankTime);
        browserSingleVersionPagePathPerfDetail.setTimeBucket(minuteTimeBucket);
        return browserSingleVersionPagePathPerfDetail;
    }

}

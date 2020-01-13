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
import org.apache.skywalking.oap.server.core.browser.source.*;

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

    // perfDetail
    private int redirectTime;
    private int dnsTime;
    private int reqTime;
    private int domAnalysisTime;
    private int domReadyTime;
    private int blankTime;

    ServicePerfDetail toServicePerfDetail() {
        ServicePerfDetail servicePerfDetail = new ServicePerfDetail();
        servicePerfDetail.setId(serviceId);
        servicePerfDetail.setName(serviceVersionName);
        servicePerfDetail.setStatus(!isError);
        servicePerfDetail.setRedirectTime(redirectTime);
        servicePerfDetail.setDnsTime(dnsTime);
        servicePerfDetail.setReqTime(reqTime);
        servicePerfDetail.setDomAnalysisTime(domAnalysisTime);
        servicePerfDetail.setDomReadyTime(domReadyTime);
        servicePerfDetail.setBlankTime(blankTime);
        servicePerfDetail.setTimeBucket(minuteTimeBucket);
        return servicePerfDetail;
    }

    ServicePagePathPerfDetail toServicePagePathPerfDetail() {
        ServicePagePathPerfDetail servicePagePathPerfDetail = new ServicePagePathPerfDetail();
        servicePagePathPerfDetail.setId(pagePathId);
        servicePagePathPerfDetail.setName(pagePath);
        servicePagePathPerfDetail.setServiceId(serviceId);
        servicePagePathPerfDetail.setServiceName(serviceName);
        servicePagePathPerfDetail.setStatus(!isError);
        servicePagePathPerfDetail.setRedirectTime(redirectTime);
        servicePagePathPerfDetail.setDnsTime(dnsTime);
        servicePagePathPerfDetail.setReqTime(reqTime);
        servicePagePathPerfDetail.setDomAnalysisTime(domAnalysisTime);
        servicePagePathPerfDetail.setDomReadyTime(domReadyTime);
        servicePagePathPerfDetail.setBlankTime(blankTime);
        servicePagePathPerfDetail.setTimeBucket(minuteTimeBucket);
        return servicePagePathPerfDetail;
    }

    ServiceVersionPerfDetail toServiceVersionPerfDetail() {
        ServiceVersionPerfDetail serviceVersionPerfDetail = new ServiceVersionPerfDetail();
        serviceVersionPerfDetail.setId(serviceVersionId);
        serviceVersionPerfDetail.setName(serviceVersionName);
        serviceVersionPerfDetail.setServiceId(serviceId);
        serviceVersionPerfDetail.setServiceName(serviceVersionName);
        serviceVersionPerfDetail.setStatus(!isError);
        serviceVersionPerfDetail.setRedirectTime(redirectTime);
        serviceVersionPerfDetail.setDnsTime(dnsTime);
        serviceVersionPerfDetail.setReqTime(reqTime);
        serviceVersionPerfDetail.setDomAnalysisTime(domAnalysisTime);
        serviceVersionPerfDetail.setDomReadyTime(domReadyTime);
        serviceVersionPerfDetail.setBlankTime(blankTime);
        serviceVersionPerfDetail.setTimeBucket(minuteTimeBucket);
        return serviceVersionPerfDetail;
    }

    ServiceVersionPagePathPerfDetail toServiceVersionPagePathPerfDetail() {
        ServiceVersionPagePathPerfDetail serviceVersionPagePathPerfDetail = new ServiceVersionPagePathPerfDetail();
        serviceVersionPagePathPerfDetail.setId(pagePathId);
        serviceVersionPagePathPerfDetail.setName(pagePath);
        serviceVersionPagePathPerfDetail.setServiceId(serviceId);
        serviceVersionPagePathPerfDetail.setServiceName(serviceName);
        serviceVersionPagePathPerfDetail.setServiceVersionId(serviceVersionId);
        serviceVersionPagePathPerfDetail.setServiceVersionName(serviceVersionName);
        serviceVersionPagePathPerfDetail.setStatus(!isError);
        serviceVersionPagePathPerfDetail.setRedirectTime(redirectTime);
        serviceVersionPagePathPerfDetail.setDnsTime(dnsTime);
        serviceVersionPagePathPerfDetail.setReqTime(reqTime);
        serviceVersionPagePathPerfDetail.setDomAnalysisTime(domAnalysisTime);
        serviceVersionPagePathPerfDetail.setDomReadyTime(domReadyTime);
        serviceVersionPagePathPerfDetail.setBlankTime(blankTime);
        serviceVersionPagePathPerfDetail.setTimeBucket(minuteTimeBucket);
        return serviceVersionPagePathPerfDetail;
    }

}

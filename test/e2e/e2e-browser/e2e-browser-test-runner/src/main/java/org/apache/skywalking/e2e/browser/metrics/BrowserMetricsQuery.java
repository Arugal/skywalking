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

package org.apache.skywalking.e2e.browser.metrics;

import org.apache.skywalking.e2e.metrics.MetricsQuery;

/**
 * @author zhangwei
 */
public class BrowserMetricsQuery extends MetricsQuery {

    public static final String SERVICE_REDIRECT_AVG = "service_redirect_avg";
    public static final String SERVICE_DNS_AVG = "service_dns_avg";
    public static final String SERVICE_REQ_AVG = "service_req_avg";
    public static final String SERVICE_DOM_ANALYSIS_AVG = "service_dom_analysis_avg";
    public static final String SERVICE_DOM_READY_AVG = "service_dom_ready_avg";
    public static final String SERVICE_BLANK_AVG = "service_blank_avg";

    public static final String[] ALL_SERVICE_METRICS = {
            SERVICE_REDIRECT_AVG,
            SERVICE_DNS_AVG,
            SERVICE_REQ_AVG,
            SERVICE_DOM_ANALYSIS_AVG,
            SERVICE_DOM_READY_AVG,
            SERVICE_BLANK_AVG
    };

    public static final String SERVICE_REDIRECT_PERCENTILE = "service_redirect_percentile";
    public static final String SERVICE_DNS_PERCENTILE = "service_dns_percentile";
    public static final String SERVICE_REQ_PERCENTILE = "service_req_percentile";
    public static final String SERVICE_DOM_ANALYSIS_PERCENTILE = "service_dom_analysis_percentile";
    public static final String SERVICE_DOM_READY_PERCENTILE = "service_dom_ready_percentile";
    public static final String SERVICE_BLANK_PERCENTILE = "service_blank_percentile";

    public static final String[] ALL_SERVICE_MULTIPLE_LINEAR_METRICS = {
            SERVICE_REDIRECT_PERCENTILE,
            SERVICE_DNS_PERCENTILE,
            SERVICE_REQ_PERCENTILE,
            SERVICE_DOM_ANALYSIS_PERCENTILE,
            SERVICE_DOM_READY_PERCENTILE,
            SERVICE_BLANK_PERCENTILE
    };

    public static final String SERVICE_PAGE_PV = "service_page_pv";
    public static final String SERVICE_PAGE_ERROR_RATE = "service_page_error_rate";
    public static final String SERVICE_PAGE_REDIRECT_AVG = "service_page_redirect_avg";
    public static final String SERVICE_PAGE_DNS_AVG = "service_page_dns_avg";
    public static final String SERVICE_PAGE_REQ_AVG = "service_page_req_avg";
    public static final String SERVICE_PAGE_DOM_ANALYSIS_AVG = "service_page_dom_analysis_avg";
    public static final String SERVICE_PAGE_DOM_READY_AVG = "service_page_dom_ready_avg";
    public static final String SERVICE_PAGE_BLANK_AVG = "service_page_blank_avg";

    public static final String[] ALL_SERVICE_PAGE_METRICS = {
            SERVICE_PAGE_PV,
//            SERVICE_PAGE_ERROR_RATE,
            SERVICE_PAGE_REDIRECT_AVG,
            SERVICE_PAGE_DNS_AVG,
            SERVICE_PAGE_REQ_AVG,
            SERVICE_PAGE_DOM_ANALYSIS_AVG,
            SERVICE_PAGE_DOM_READY_AVG,
            SERVICE_PAGE_BLANK_AVG
    };

    public static final String SERVICE_PAGE_REDIRECT_PERCENTILE = "service_page_redirect_percentile";
    public static final String SERVICE_PAGE_DNS_PERCENTILE = "service_page_dns_percentile";
    public static final String SERVICE_PAGE_REQ_PERCENTILE = "service_page_req_percentile";
    public static final String SERVICE_PAGE_DOM_ANALYSIS_PERCENTILE = "service_page_dom_analysis_percentile";
    public static final String SERVICE_PAGE_DOM_READY_PERCENTILE = "service_page_dom_ready_percentile";
    public static final String SERVICE_PAGE_BLANK_PERCENTILE = "service_page_blank_percentile";

    public static final String[] ALL_SERVICE_PAGE_MULTIPLE_LINEAR_METRICS = {
            SERVICE_PAGE_REDIRECT_PERCENTILE,
            SERVICE_PAGE_DNS_PERCENTILE,
            SERVICE_PAGE_REQ_PERCENTILE,
            SERVICE_PAGE_DOM_ANALYSIS_PERCENTILE,
            SERVICE_PAGE_DOM_READY_PERCENTILE,
            SERVICE_PAGE_BLANK_PERCENTILE
    };

    public static final String SERVICE_VERSION_REDIRECT_AVG = "service_version_redirect_avg";
    public static final String SERVICE_VERSION_DNS_AVG = "service_version_dns_avg";
    public static final String SERVICE_VERSION_REQ_AVG = "service_version_req_avg";
    public static final String SERVICE_VERSION_DOM_ANALYSIS_AVG = "service_version_dom_analysis_avg";
    public static final String SERVICE_VERSION_DOM_READY_AVG = "service_version_dom_ready_avg";
    public static final String SERVICE_VERSION_BLANK_AVG = "service_version_blank_avg";

    public static final String[] ALL_SERVICE_VERSION_METRICS = {
            SERVICE_VERSION_REDIRECT_AVG,
            SERVICE_VERSION_DNS_AVG,
            SERVICE_VERSION_REQ_AVG,
            SERVICE_VERSION_DOM_ANALYSIS_AVG,
            SERVICE_VERSION_DOM_READY_AVG,
            SERVICE_VERSION_BLANK_AVG
    };

    public static final String SERVICE_VERSION_REDIRECT_PERCENTILE = "service_version_redirect_percentile";
    public static final String SERVICE_VERSION_DNS_PERCENTILE = "service_version_dns_percentile";
    public static final String SERVICE_VERSION_REQ_PERCENTILE = "service_version_req_percentile";
    public static final String SERVICE_VERSION_DOM_ANALYSIS_PERCENTILE = "service_version_dom_analysis_percentile";
    public static final String SERVICE_VERSION_DOM_READY_PERCENTILE = "service_version_dom_ready_percentile";
    public static final String SERVICE_VERSION_BLANK_PERCENTILE = "service_version_blank_percentile";

    public static final String[] ALL_SERVICE_VERSION_MULTIPLE_LINEAR_METRICS = {
            SERVICE_VERSION_REDIRECT_PERCENTILE,
            SERVICE_VERSION_DNS_PERCENTILE,
            SERVICE_VERSION_REQ_PERCENTILE,
            SERVICE_VERSION_DOM_ANALYSIS_PERCENTILE,
            SERVICE_VERSION_DOM_READY_PERCENTILE,
            SERVICE_VERSION_BLANK_PERCENTILE
    };


    public static final String SERVICE_VERSION_PAGE_PV = "service_version_page_pv";
    public static final String SERVICE_VERSION_PAGE_ERROR_RATE = "service_version_page_error_rate";
    public static final String SERVICE_VERSION_PAGE_REDIRECT_AVG = "service_version_page_redirect_avg";
    public static final String SERVICE_VERSION_PAGE_DNS_AVG = "service_version_page_dns_avg";
    public static final String SERVICE_VERSION_PAGE_REQ_AVG = "service_version_page_req_avg";
    public static final String SERVICE_VERSION_PAGE_DOM_ANALYSIS_AVG = "service_version_page_dom_analysis_avg";
    public static final String SERVICE_VERSION_PAGE_DOM_READY_AVG = "service_version_page_dom_ready_avg";
    public static final String SERVICE_VERSION_PAGE_BLANK_AVG = "service_version_page_blank_avg";

    public static final String[] ALL_SERVICE_VERSION_PAGE_METRICS = {
            SERVICE_VERSION_PAGE_PV,
//            SERVICE_VERSION_PAGE_ERROR_RATE,
            SERVICE_VERSION_PAGE_REDIRECT_AVG,
            SERVICE_VERSION_PAGE_DNS_AVG,
            SERVICE_VERSION_PAGE_REQ_AVG,
            SERVICE_VERSION_PAGE_DOM_ANALYSIS_AVG,
            SERVICE_VERSION_PAGE_DOM_READY_AVG,
            SERVICE_VERSION_PAGE_BLANK_AVG
    };

    public static final String SERVICE_VERSION_PAGE_REDIRECT_PERCENTILE = "service_version_page_redirect_percentile";
    public static final String SERVICE_VERSION_PAGE_DNS_PERCENTILE = "service_version_page_dns_percentile";
    public static final String SERVICE_VERSION_PAGE_REQ_PERCENTILE = "service_version_page_req_percentile";
    public static final String SERVICE_VERSION_PAGE_DOM_ANALYSIS_PERCENTILE = "service_version_page_dom_analysis_percentile";
    public static final String SERVICE_VERSION_PAGE_DOM_READY_PERCENTILE = "service_version_page_dom_ready_percentile";
    public static final String SERVICE_VERSION_PAGE_BLANK_PERCENTILE = "service_version_page_blank_percentile";

    public static final String[] ALL_SERVICE_VERSION_PAGE_MULTIPLE_LINEAR_METRICS = {
            SERVICE_VERSION_PAGE_REDIRECT_PERCENTILE,
            SERVICE_VERSION_PAGE_DNS_PERCENTILE,
            SERVICE_VERSION_PAGE_REQ_PERCENTILE,
            SERVICE_VERSION_PAGE_DOM_ANALYSIS_PERCENTILE,
            SERVICE_VERSION_PAGE_DOM_READY_PERCENTILE,
            SERVICE_VERSION_PAGE_BLANK_PERCENTILE
    };
}

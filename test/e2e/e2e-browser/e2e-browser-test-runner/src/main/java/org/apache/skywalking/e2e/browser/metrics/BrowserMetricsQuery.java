package org.apache.skywalking.e2e.browser.metrics;

import org.apache.skywalking.e2e.metrics.MetricsQuery;

/**
 * @author zhangwei
 */
public class BrowserMetricsQuery extends MetricsQuery {

    public static final String SERVICE_PAGE_PV = "service_page_pv";
    public static final String SERVICE_PAGE_ERROR_RATE = "service_page_error_rate";

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
}

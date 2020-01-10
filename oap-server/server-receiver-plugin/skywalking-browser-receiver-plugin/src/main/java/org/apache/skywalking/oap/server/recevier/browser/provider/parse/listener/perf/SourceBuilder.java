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

package org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.perf;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.browser.source.ServicePagePath;

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

    ServicePagePath toServicePagePath() {
        ServicePagePath servicePagePath = new ServicePagePath();
        servicePagePath.setId(pagePathId);
        servicePagePath.setName(pagePath);
        servicePagePath.setServiceId(serviceId);
        servicePagePath.setServiceName(serviceName);
        return servicePagePath;
    }
}

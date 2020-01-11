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

package org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator;

import org.apache.skywalking.apm.network.language.agent.PerfDetail;

/**
 * @author zhangwei
 */
public class PerfDetailDecorator implements StandardBuilder<PerfDetail> {

    private final PerfDetail perfDetail;
    private final StandardBuilder standardBuilder;

    public PerfDetailDecorator(PerfDetail perfDetail, StandardBuilder standardBuilder) {
        this.perfDetail = perfDetail;
        this.standardBuilder = standardBuilder;
    }

    public int getRedirectTime() {
        return perfDetail.getRedirectTime();
    }

    public int getDnsTime() {
        return perfDetail.getDnsTime();
    }

    public int getReqTime() {
        return perfDetail.getReqTime();
    }

    public int getDomAnalysisTime() {
        return perfDetail.getDomAnalysisTime();
    }

    public int getDomReadyTime() {
        return perfDetail.getDomReadyTime();
    }

    public int getBlankTime() {
        return perfDetail.getBlankTime();
    }

    @Override
    public void toBuilder() {

    }

    @Override
    public PerfDetail build() {
        return perfDetail;
    }
}

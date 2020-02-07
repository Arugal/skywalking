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

package org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.decorator;

import org.apache.skywalking.apm.network.language.agent.BrowserPerfData;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.decorator.StandardBuilder;

/**
 * @author zhangwei
 */
public class BrowserPerfDataDecorator implements StandardBuilder<BrowserPerfData> {

    private boolean isOrigin = true;
    private final BrowserPerfData browserPerfData;
    private BrowserPerfData.Builder builder;

    public BrowserPerfDataDecorator(BrowserPerfData browserPerfData) {
        this.browserPerfData = browserPerfData;
    }

    public int getServiceId() {
        if (isOrigin) {
            return browserPerfData.getServiceId();
        } else {
            return builder.getServiceId();
        }
    }

    public int getServiceVersionId() {
        if (isOrigin) {
            return browserPerfData.getServiceVersionId();
        } else {
            return builder.getServiceVersionId();
        }
    }

    public long getTime() {
        if (isOrigin) {
            return browserPerfData.getTime();
        } else {
            return builder.getTime();
        }
    }

    public String getPagePath() {
        if (isOrigin) {
            return browserPerfData.getPagePath();
        } else {
            return builder.getPagePath();
        }
    }

    public int getPagePathId() {
        if (isOrigin) {
            return browserPerfData.getPagePathId();
        } else {
            return builder.getPagePathId();
        }
    }

    public int getRedirectTime() {
        if (isOrigin) {
            return browserPerfData.getRedirectTime();
        } else {
            return builder.getRedirectTime();
        }
    }

    public int getDnsTime() {
        if (isOrigin) {
            return browserPerfData.getDnsTime();
        } else {
            return builder.getDnsTime();
        }
    }

    public int getReqTime() {
        if (isOrigin) {
            return browserPerfData.getReqTime();
        } else {
            return builder.getReqTime();
        }
    }

    public int getDomAnalysisTime() {
        if (isOrigin) {
            return browserPerfData.getDomAnalysisTime();
        } else {
            return builder.getDomAnalysisTime();
        }
    }

    public int getDomReadyTime() {
        if (isOrigin) {
            return browserPerfData.getDomReadyTime();
        } else {
            return builder.getDomReadyTime();
        }
    }

    public int getBlankTime() {
        if (isOrigin) {
            return browserPerfData.getBlankTime();
        } else {
            return builder.getBlankTime();
        }
    }

    public void setPagePathId(int pagePathId) {
        if (isOrigin) {
            toBuilder();
        }
        builder.setPagePathId(pagePathId);
    }

    public void setTime(long time) {
        if (isOrigin) {
            toBuilder();
        }
        builder.setTime(time);
    }

    @Override
    public void toBuilder() {
        if (isOrigin) {
            this.isOrigin = false;
            this.builder = browserPerfData.toBuilder();
        }
    }

    @Override
    public BrowserPerfData build() {
        if (isOrigin) {
            return browserPerfData;
        } else {
            return builder.build();
        }
    }
}

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

import org.apache.skywalking.apm.network.language.agent.BrowserPerfData;

/**
 * @author zhangwei
 */
public class BrowserPerfDataDecorator implements StandardBuilder {

    private boolean isOrigin = true;
    private final BrowserPerfData browserPerfData;
    private final BrowserErrorLogDecorator[] browserErrorLogDecorators;
    private final PerfDetailDecorator perfDetailDecorator;
    private BrowserPerfData.Builder browserPerfBuilder;


    public BrowserPerfDataDecorator(BrowserPerfData browserPerfData) {
        this.browserPerfData = browserPerfData;
        this.browserErrorLogDecorators = new BrowserErrorLogDecorator[browserPerfData.getLogsCount()];
        this.perfDetailDecorator = new PerfDetailDecorator(browserPerfData.getPerfDetail());
    }

    public int getServiceId() {
        return browserPerfData.getServiceId();
    }

    public int getServiceVersionId() {
        return browserPerfData.getServiceVersionId();
    }

    public long getTime() {
        return browserPerfData.getTime();
    }

    public String getPagePath() {
        return browserPerfData.getPagePath();
    }

    public int getPagePathId() {
        return browserPerfData.getPagePathId();
    }

    public PerfDetailDecorator getPerfDetailDecorator() {
        return perfDetailDecorator;
    }

    public boolean isError() {
        return browserErrorLogDecorators.length > 0;
    }

    public int getBrowserErrorLogCount() {
        return browserPerfData.getLogsCount();
    }

    public BrowserErrorLogDecorator getBrowserErrorLog(int index) {
        if (browserErrorLogDecorators[index] == null) {
            if (isOrigin) {
                browserErrorLogDecorators[index] = new BrowserErrorLogDecorator(browserPerfData.getLogs(index));
            } else {
                browserErrorLogDecorators[index] = new BrowserErrorLogDecorator(browserPerfBuilder.getLogs(index));
            }
        }
        return browserErrorLogDecorators[index];
    }

    public void setPagePathId(int pagePathId) {
        if (isOrigin) {
            toBuilder();
        }
        browserPerfBuilder.setPagePathId(pagePathId);
    }

    public void setTime(long time) {
        if (isOrigin) {
            toBuilder();
        }
        browserPerfBuilder.setTime(time);
    }

    @Override
    public void toBuilder() {
        if (isOrigin) {
            this.isOrigin = false;
            this.browserPerfBuilder = browserPerfData.toBuilder();
        }
    }
}

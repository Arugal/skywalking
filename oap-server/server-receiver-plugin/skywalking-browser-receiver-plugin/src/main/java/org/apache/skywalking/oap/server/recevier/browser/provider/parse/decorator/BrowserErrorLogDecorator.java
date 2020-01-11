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

import org.apache.skywalking.apm.network.language.agent.BrowserErrorLog;

/**
 * @author zhangwei
 */
public class BrowserErrorLogDecorator implements StandardBuilder<BrowserErrorLog> {

    private boolean isOrigin = true;
    private final StandardBuilder standardBuilder;
    private final BrowserErrorLog browserErrorLog;
    private BrowserErrorLog.Builder errorLogBuilder;


    public BrowserErrorLogDecorator(BrowserErrorLog browserErrorLog, StandardBuilder standardBuilder) {
        this.standardBuilder = standardBuilder;
        this.browserErrorLog = browserErrorLog;
    }

    public String getCatalog() {
        if (isOrigin) {
            return browserErrorLog.getCatalog();
        } else {
            return errorLogBuilder.getCatalog();
        }
    }

    public String getGrade() {
        if (isOrigin) {
            return browserErrorLog.getGrade();
        } else {
            return errorLogBuilder.getGrade();
        }
    }

    public String getMessage() {
        if (isOrigin) {
            return browserErrorLog.getMessage();
        } else {
            return errorLogBuilder.getMessage();
        }
    }

    public int getLine() {
        if (isOrigin) {
            return browserErrorLog.getLine();
        } else {
            return errorLogBuilder.getLine();
        }
    }

    public int getCol() {
        if (isOrigin) {
            return browserErrorLog.getCol();
        } else {
            return errorLogBuilder.getCol();
        }
    }

    public String getStack() {
        if (isOrigin) {
            return browserErrorLog.getStack();
        } else {
            return errorLogBuilder.getStack();
        }
    }

    public String getErrorUrl() {
        if (isOrigin) {
            return browserErrorLog.getErrorUrl();
        } else {
            return errorLogBuilder.getErrorUrl();
        }
    }

    public long getTime() {
        if (isOrigin) {
            return browserErrorLog.getTime();
        } else {
            return errorLogBuilder.getTime();
        }
    }

    public void setTime(long time) {
        if (isOrigin) {
            toBuilder();
        }
        errorLogBuilder.setTime(time);
    }

    @Override
    public void toBuilder() {
        if (isOrigin) {
            this.isOrigin = false;
            this.errorLogBuilder = browserErrorLog.toBuilder();
            standardBuilder.toBuilder();
        }
    }

    @Override
    public BrowserErrorLog build() {
        if (isOrigin) {
            return browserErrorLog;
        } else {
            return errorLogBuilder.build();
        }
    }
}

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

package org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.decorator;

import org.apache.skywalking.apm.network.language.agent.BrowserErrorLog;
import org.apache.skywalking.apm.network.language.agent.ErrorCategory;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.decorator.StandardBuilder;

/**
 * @author zhangwei
 */
public class BrowserErrorLogDecorator implements StandardBuilder<BrowserErrorLog> {

    private boolean isOrigin = true;
    private final BrowserErrorLog browserErrorLog;
    private BrowserErrorLog.Builder builder;


    public BrowserErrorLogDecorator(BrowserErrorLog browserErrorLog) {
        this.browserErrorLog = browserErrorLog;
    }

    public String getUniqueId() {
        if (isOrigin) {
            return browserErrorLog.getUniqueId();
        } else {
            return builder.getUniqueId();
        }
    }

    public int getServiceId() {
        if (isOrigin) {
            return browserErrorLog.getServiceId();
        } else {
            return builder.getServiceId();
        }
    }

    public int getServiceVersionId() {
        if (isOrigin) {
            return browserErrorLog.getServiceVersionId();
        } else {
            return builder.getServiceVersionId();
        }
    }

    public int getPagePathId() {
        if (isOrigin) {
            return browserErrorLog.getPagePathId();
        } else {
            return builder.getPagePathId();
        }
    }

    public void setPagePathId(int pagePathId) {
        if (isOrigin) {
            toBuilder();
        }
        builder.setPagePathId(pagePathId);
    }

    public String getPagePath() {
        if (isOrigin) {
            return browserErrorLog.getPagePath();
        } else {
            return builder.getPagePath();
        }
    }

    public ErrorCategory getCategory() {
        if (isOrigin) {
            return browserErrorLog.getCategory();
        } else {
            return builder.getCategory();
        }
    }

    public String getGrade() {
        if (isOrigin) {
            return browserErrorLog.getGrade();
        } else {
            return builder.getGrade();
        }
    }

    public String getMessage() {
        if (isOrigin) {
            return browserErrorLog.getMessage();
        } else {
            return builder.getMessage();
        }
    }

    public int getLine() {
        if (isOrigin) {
            return browserErrorLog.getLine();
        } else {
            return builder.getLine();
        }
    }

    public int getCol() {
        if (isOrigin) {
            return browserErrorLog.getCol();
        } else {
            return builder.getCol();
        }
    }

    public String getStack() {
        if (isOrigin) {
            return browserErrorLog.getStack();
        } else {
            return builder.getStack();
        }
    }

    public String getErrorUrl() {
        if (isOrigin) {
            return browserErrorLog.getErrorUrl();
        } else {
            return builder.getErrorUrl();
        }
    }

    public long getTime() {
        if (isOrigin) {
            return browserErrorLog.getTime();
        } else {
            return builder.getTime();
        }
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
            this.builder = browserErrorLog.toBuilder();
        }
    }

    @Override
    public BrowserErrorLog build() {
        if (isOrigin) {
            return browserErrorLog;
        } else {
            return builder.build();
        }
    }

    public byte[] toByteArray() {
        if (isOrigin) {
            return browserErrorLog.toByteArray();
        } else {
            return builder.build().toByteArray();
        }
    }
}

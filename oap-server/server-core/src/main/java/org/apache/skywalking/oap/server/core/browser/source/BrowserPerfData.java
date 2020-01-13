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

package org.apache.skywalking.oap.server.core.browser.source;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.source.ScopeDeclaration;
import org.apache.skywalking.oap.server.core.source.Source;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.BROWSER_PERF_DATA;

/**
 * @author zhangwei
 */
@ScopeDeclaration(id = BROWSER_PERF_DATA, name = "BrowserPerfData")
public class BrowserPerfData extends Source {

    @Override
    public int scope() {
        return BROWSER_PERF_DATA;
    }

    @Override
    public String getEntityId() {
        return uniqueId;
    }

    @Getter @Setter private String uniqueId;
    @Getter @Setter private int serviceId;
    @Getter @Setter private int serviceInstanceId;
    @Getter @Setter private int pagePathId;
    @Getter @Setter private String pagePath;
    @Getter @Setter private long time;
    @Getter @Setter private int isError;
    @Getter @Setter private byte[] dataBinary;
}

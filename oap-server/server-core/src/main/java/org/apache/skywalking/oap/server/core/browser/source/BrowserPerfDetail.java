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
import org.apache.skywalking.oap.server.core.source.ScopeDefaultColumn;
import org.apache.skywalking.oap.server.core.source.Source;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.SERVICE_CATALOG_NAME;
import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.BROWSER_PERF_DETAIL;

/**
 * @author zhangwei
 */
@ScopeDeclaration(id = BROWSER_PERF_DETAIL, name = "BrowserPerfDetail", catalog = SERVICE_CATALOG_NAME)
@ScopeDefaultColumn.VirtualColumnDefinition(fieldName = "entityId", columnName = "entity_id", isID = true, type = String.class)
public class BrowserPerfDetail extends Source {

    @Override
    public int scope() {
        return BROWSER_PERF_DETAIL;
    }

    @Override
    public String getEntityId() {
        return String.valueOf(id);
    }

    @Getter @Setter private int id;
    @Getter @Setter private String name;
    @Getter @Setter private boolean status;
    @Getter @Setter private int redirectTime;
    @Getter @Setter private int dnsTime;
    @Getter @Setter private int reqTime;
    @Getter @Setter private int domAnalysisTime;
    @Getter @Setter private int domReadyTime;
    @Getter @Setter private int blankTime;
}

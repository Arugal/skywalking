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
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.source.ScopeDeclaration;
import org.apache.skywalking.oap.server.core.source.ScopeDefaultColumn;
import org.apache.skywalking.oap.server.core.source.Source;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.*;

/**
 * @author zhangwei
 */
@ScopeDeclaration(id = SERVICE_VERSION_PERF_DETAIL, name = "ServiceVersionPerfDetail", catalog = SERVICE_INSTANCE_CATALOG_NAME)
@ScopeDefaultColumn.VirtualColumnDefinition(fieldName = "entityId", columnName = "entity_id", isID = true, type = String.class)
public class ServiceVersionPerfDetail extends Source {

    @Override
    public int scope() {
        return SERVICE_VERSION_PERF_DETAIL;
    }

    @Override
    public String getEntityId() {
        return serviceId + Const.ID_SPLIT + id;
    }

    @Getter @Setter private int id;
    @Getter @Setter private String name;
    @Getter @Setter @ScopeDefaultColumn.DefinedByField(columnName = "service_id") private int serviceId;
    @Getter @Setter private String serviceName;
    @Getter @Setter private boolean status;
    @Getter @Setter private int redirectTime;
    @Getter @Setter private int dnsTime;
    @Getter @Setter private int reqTime;
    @Getter @Setter private int domAnalysisTime;
    @Getter @Setter private int domReadyTime;
    @Getter @Setter private int blankTime;
}

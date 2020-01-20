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

package org.apache.skywalking.oap.server.core.alarm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;

/**
 * @author zhangwei
 */
@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class ServiceVersionPageMetaInAlarm extends MetaInAlarm {

    private String metricsName;
    private int id;
    private String name;


    @Override
    public String getScope() {
        return DefaultScopeDefine.BROWSER_SINGLE_VERSION_PAGE_PATH_CATALOG_NAME;
    }

    @Override
    public int getScopeId() {
        return DefaultScopeDefine.BROWSER_SINGLE_VERSION_PAGE_PATH_PERF_DETAIL;
    }

    @Override
    public int getId0() {
        return id;
    }

    @Override
    public int getId1() {
        return 0;
    }
}

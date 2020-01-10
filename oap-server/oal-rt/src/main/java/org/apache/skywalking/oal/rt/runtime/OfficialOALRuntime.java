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

package org.apache.skywalking.oal.rt.runtime;

/**
 * @author zhangwei
 */
public class OfficialOALRuntime extends OALRuntime {

    private static final String SOURCE_PACKAGE = "org.apache.skywalking.oap.server.core.source.";
    private static final String DYNAMIC_METRICS_CLASS_PACKAGE = "org.apache.skywalking.oal.rt.official.metrics.";
    private static final String DYNAMIC_METRICS_BUILDER_CLASS_PACKAGE = "org.apache.skywalking.oal.rt.official.metrics.builder.";
    private static final String DYNAMIC_DISPATCHER_CLASS_PACKAGE = "org.apache.skywalking.oal.rt.official.dispatcher.";

    private static final String OAL_CONFIG_FILE = "official_analysis.oal";

    public OfficialOALRuntime() {
        super(SOURCE_PACKAGE, DYNAMIC_METRICS_CLASS_PACKAGE, DYNAMIC_METRICS_BUILDER_CLASS_PACKAGE, DYNAMIC_DISPATCHER_CLASS_PACKAGE, OAL_CONFIG_FILE);
    }
}

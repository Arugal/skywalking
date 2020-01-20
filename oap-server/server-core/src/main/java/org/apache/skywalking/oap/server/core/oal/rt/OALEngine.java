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

package org.apache.skywalking.oap.server.core.oal.rt;

import lombok.Getter;
import org.apache.skywalking.oap.server.core.analysis.DispatcherDetectorListener;
import org.apache.skywalking.oap.server.core.analysis.StreamAnnotationListener;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;

/**
 * OALEngine defines the main entrance of the oal script engine runtime.
 *
 * @author wusheng
 */
public interface OALEngine {
    void setStreamListener(StreamAnnotationListener listener) throws ModuleStartException;

    void setDispatcherListener(DispatcherDetectorListener listener) throws ModuleStartException;

    void start(ClassLoader currentClassLoader) throws ModuleStartException, OALCompileException;

    void notifyAllListeners() throws ModuleStartException;

    @Getter
    enum Group {

        OFFICIAL("official_analysis.oal", "org.apache.skywalking.oap.server.core.source.",
            "org.apache.skywalking.oal.rt.official.metrics.", "org.apache.skywalking.oal.rt.official.metrics.builder",
            "org.apache.skywalking.oal.rt.official.dispatcher."),
        BROWSER("browser_analysis.oal", "org.apache.skywalking.oap.server.core.browser.source.",
            "org.apache.skywalking.oal.rt.browser.metrics.", "org.apache.skywalking.oal.rt.browser.metrics.builder",
            "org.apache.skywalking.oal.rt.browser.dispatcher.");

        private String oalConfigFile;
        private String sourcePackage;
        private String dynamicMetricsPackage;
        private String dynamicMetricsBuilderPackage;
        private String dynamicDispatcherPackage;

        Group(String oalConfigFile, String sourcePackage, String dynamicMetricsPackage, String dynamicMetricsBuilderPackage, String dynamicDispatcherPackage) {
            this.oalConfigFile = oalConfigFile;
            this.sourcePackage = sourcePackage;
            this.dynamicMetricsPackage = dynamicMetricsPackage;
            this.dynamicMetricsBuilderPackage = dynamicMetricsBuilderPackage;
            this.dynamicDispatcherPackage = dynamicDispatcherPackage;
        }
    }
}

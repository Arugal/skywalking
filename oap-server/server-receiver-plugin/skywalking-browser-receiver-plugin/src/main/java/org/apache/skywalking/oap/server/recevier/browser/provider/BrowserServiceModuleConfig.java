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

package org.apache.skywalking.oap.server.recevier.browser.provider;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;

/**
 * @author zhangwei
 */
public class BrowserServiceModuleConfig extends ModuleConfig {
    @Setter @Getter private String perfBufferPath;
    @Setter @Getter private int perfBufferOffsetMaxFileSize;
    @Setter @Getter private int perfBufferDataMaxFileSize;
    @Setter @Getter private boolean perfBufferFileCleanWhenRestart;
    @Setter @Getter private String errorLogBufferPath;
    @Setter @Getter private int errorLogBufferOffsetMaxFileSize;
    @Setter @Getter private int errorLogBufferDataMaxFileSize;
    @Setter @Getter private boolean errorLogBufferFileCleanWhenRestart;
    /**
     * The sample rate precision is 1/10000. 10000 means 100% sample in default.
     */
    @Setter @Getter private int sampleRate = 10000;
}

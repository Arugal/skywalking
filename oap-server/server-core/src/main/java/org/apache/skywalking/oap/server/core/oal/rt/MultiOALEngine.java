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

import org.apache.skywalking.oap.server.core.analysis.DispatcherDetectorListener;
import org.apache.skywalking.oap.server.core.analysis.StreamAnnotationListener;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;

import java.util.List;

/**
 * @author zhangwei
 */
public class MultiOALEngine implements OALEngine {

    private final List<OALEngine> oalEngines;

    public MultiOALEngine(List<OALEngine> oalEngines) {
        this.oalEngines = oalEngines;
    }

    @Override
    public void setStreamListener(StreamAnnotationListener listener) throws ModuleStartException {
        for (OALEngine oalEngine : oalEngines) {
            oalEngine.setStreamListener(listener);
        }
    }

    @Override
    public void setDispatcherListener(DispatcherDetectorListener listener) throws ModuleStartException {
        for (OALEngine oalEngine : oalEngines) {
            oalEngine.setDispatcherListener(listener);
        }
    }

    @Override
    public void start(ClassLoader currentClassLoader) throws ModuleStartException, OALCompileException {
        for (OALEngine oalEngine : oalEngines) {
            oalEngine.start(currentClassLoader);
        }
    }

    @Override
    public void notifyAllListeners() throws ModuleStartException {
        for (OALEngine oalEngine : oalEngines) {
            oalEngine.notifyAllListeners();
        }
    }
}

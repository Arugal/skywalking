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

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.analysis.StreamAnnotationListener;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.module.Service;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zhangwei
 */
@Slf4j
public class OALEngineService implements Service {

    private final Map<OALEngine.Group, OALEngine> engineMap = new HashMap<>();
    private final ModuleManager moduleManager;

    public OALEngineService(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void activate(OALEngine.Group group) throws ModuleStartException {
        activate(group, OALEngineService.class.getClassLoader());
    }

    public void activate(OALEngine.Group group, ClassLoader classLoader) throws ModuleStartException {
        // It's only activated once
        if (!engineMap.containsKey(group)) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("activate {} OALEngine", group.name());
                }
                OALEngine oalEngine = loadOALEngine(group);
                StreamAnnotationListener streamAnnotationListener = new StreamAnnotationListener(moduleManager);
                oalEngine.setStreamListener(streamAnnotationListener);
                oalEngine.setDispatcherListener(moduleManager.find(CoreModule.NAME).provider().getService(SourceReceiver.class).getDispatcherDetectorListener());

                oalEngine.start(classLoader);
                oalEngine.notifyAllListeners();

                engineMap.put(group, oalEngine);
            } catch (Exception e) {
                throw new ModuleStartException(e.getMessage(), e);
            }
        }
    }

    /**
     * Load the OAL Engine runtime, because runtime module depends on core, so we have to use class::forname to locate it.
     * @param group
     * @return
     * @throws ReflectiveOperationException
     */
    private OALEngine loadOALEngine(OALEngine.Group group) throws ReflectiveOperationException {
        Class<?> engineRTClass = Class.forName("org.apache.skywalking.oal.rt.OALRuntime");
        Constructor<?> engineRTConstructor = engineRTClass.getConstructor(OALEngine.Group.class);
        return (OALEngine) engineRTConstructor.newInstance(group);
    }
}

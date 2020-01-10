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

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Load the OAL Engine runtime, because runtime module depends on core, so we have to use {@link ServiceLoader} to locate it.
 *
 * @author wusheng
 * @author zhangwei
 */
@Slf4j
public class OALEngineLoader {
    private static volatile OALEngine ENGINE = null;
    private static ReentrantLock INIT_LOCK = new ReentrantLock();

    public static OALEngine get() {
        if (ENGINE == null) {
            INIT_LOCK.lock();
            try {
                if (ENGINE == null) {
                    init();
                }
            } finally {
                INIT_LOCK.unlock();
            }
        }
        return ENGINE;
    }

    private static void init() {
        ServiceLoader<OALEngine> serviceLoader = ServiceLoader.load(OALEngine.class);
        List<OALEngine> oalEngines = new LinkedList<>();
        serviceLoader.forEach(oalEngines::add);
        if (log.isWarnEnabled() && oalEngines.isEmpty()) {
            log.warn("There is no OAL Engine in the running environment!");
        }
        ENGINE = new MultiOALEngine(oalEngines);
    }
}

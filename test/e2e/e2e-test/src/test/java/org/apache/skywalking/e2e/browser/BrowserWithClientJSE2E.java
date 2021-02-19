/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.skywalking.e2e.browser;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.e2e.annotation.ContainerHostAndPort;
import org.apache.skywalking.e2e.annotation.DockerCompose;
import org.apache.skywalking.e2e.base.SkyWalkingE2E;
import org.apache.skywalking.e2e.base.SkyWalkingTestAdapter;
import org.apache.skywalking.e2e.common.HostAndPort;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.DockerComposeContainer;

@Slf4j
@SkyWalkingE2E
public class BrowserWithClientJSE2E extends SkyWalkingTestAdapter {

    @DockerCompose({
        "docker/browser/docker-compose.h2.client-js.yml"
    })
    private DockerComposeContainer<?> justForSideEffects;

    @SuppressWarnings("unused")
    @ContainerHostAndPort(name = "oap", port = 12800)
    private HostAndPort swWebappHostPort;

    @BeforeAll
    public void setUp() {
        queryClient(swWebappHostPort);
    }

    // browser data

    // error log

    // trace
}

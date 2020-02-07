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

package org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.standardization;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.register.service.IEndpointInventoryRegister;
import org.apache.skywalking.oap.server.core.source.DetectPoint;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.performance.decorator.BrowserPerfDataDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parser.standardization.IdExchanger;

/**
 * @author zhangwei
 */
@Slf4j
public class PagePathIdExchanger implements IdExchanger<BrowserPerfDataDecorator> {

    private static PagePathIdExchanger EXCHANGER;
    private final IEndpointInventoryRegister inventoryService;

    public static PagePathIdExchanger getInstance(ModuleManager moduleManager) {
        if (EXCHANGER == null) {
            EXCHANGER = new PagePathIdExchanger(moduleManager);
        }
        return EXCHANGER;
    }

    private PagePathIdExchanger(ModuleManager moduleManager) {
        this.inventoryService = moduleManager.find(CoreModule.NAME).provider().getService(IEndpointInventoryRegister.class);
    }

    @Override
    public boolean exchange(BrowserPerfDataDecorator standardBuilder, int serviceId) {
        // Page path in browser is the endpoint concept in the backend
        boolean exchanged = true;
        final String pagePath = standardBuilder.getPagePath();
        final int pagePathId = inventoryService.getOrCreate(serviceId, pagePath, DetectPoint.SERVER);
        if (pagePathId == Const.NONE) {
            if (log.isDebugEnabled()) {
                log.debug("pagePath name: {} from service id: {} exchange failed", pagePath, serviceId);
            }
            exchanged = false;
        } else {
            standardBuilder.setPagePathId(pagePathId);
        }
        return exchanged;
    }
}

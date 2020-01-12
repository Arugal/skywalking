package org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.inventory;

import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.register.service.IServiceInstanceInventoryRegister;
import org.apache.skywalking.oap.server.core.register.service.IServiceInventoryRegister;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.recevier.browser.provider.BrowserServiceModuleConfig;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfDataCoreInfo;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.decorator.BrowserPerfDataDecorator;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.BrowserPerfDataListener;
import org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.BrowserPerfDataListenerFactory;

/**
 * @author zhangwei
 */
public class BrowserInventoryListener implements BrowserPerfDataListener {

    private final IServiceInventoryRegister serviceInventoryRegister;
    private final IServiceInstanceInventoryRegister serviceInstanceInventoryRegister;
    private BrowserPerfDataCoreInfo coreInfo;

    public BrowserInventoryListener(ModuleManager moduleManager) {
        this.serviceInventoryRegister = moduleManager.find(CoreModule.NAME).provider().getService(IServiceInventoryRegister.class);
        this.serviceInstanceInventoryRegister = moduleManager.find(CoreModule.NAME).provider().getService(IServiceInstanceInventoryRegister.class);
    }

    @Override
    public void build() {
        // TODO Sample
        serviceInventoryRegister.heartbeat(coreInfo.getServiceId(), coreInfo.getTime());
        serviceInstanceInventoryRegister.heartbeat(coreInfo.getServiceVersionId(), coreInfo.getTime());
    }

    @Override
    public void parse(BrowserPerfDataDecorator decorator, BrowserPerfDataCoreInfo coreInfo) {
        this.coreInfo = coreInfo;
    }

    public static class Factory implements BrowserPerfDataListenerFactory {

        @Override
        public BrowserPerfDataListener create(ModuleManager moduleManager, BrowserServiceModuleConfig moduleConfig) {
            return new BrowserInventoryListener(moduleManager);
        }
    }
}

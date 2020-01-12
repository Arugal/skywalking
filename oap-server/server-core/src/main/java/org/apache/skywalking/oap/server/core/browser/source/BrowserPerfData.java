package org.apache.skywalking.oap.server.core.browser.source;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.source.ScopeDeclaration;
import org.apache.skywalking.oap.server.core.source.Source;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.BROWSER_PERF_DATA;

/**
 * @author zhangwei
 */
@ScopeDeclaration(id = BROWSER_PERF_DATA, name = "BrowserPerfData")
public class BrowserPerfData extends Source {

    @Override
    public int scope() {
        return BROWSER_PERF_DATA;
    }

    @Override
    public String getEntityId() {
        return uniqueId;
    }

    @Getter @Setter private String uniqueId;
    @Getter @Setter private int serviceId;
    @Getter @Setter private int serviceInstanceId;
    @Getter @Setter private int pagePathId;
    @Getter @Setter private String pagePath;
    @Getter @Setter private long time;
    @Getter @Setter private int isError;
    @Getter @Setter private byte[] dataBinary;
}

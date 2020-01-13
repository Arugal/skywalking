package org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.errorlog;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhangwei
 */
public class BrowserErrorLogSamplerTest {

    @Test
    public void sample() {
        BrowserErrorLogSampler sampler = new BrowserErrorLogSampler(100);
        Assert.assertTrue(sampler.shouldSample(0));
        Assert.assertTrue(sampler.shouldSample(50));
        Assert.assertTrue(sampler.shouldSample(99));
        Assert.assertFalse(sampler.shouldSample(100));
        Assert.assertFalse(sampler.shouldSample(101));
        Assert.assertTrue(sampler.shouldSample(10000));
        Assert.assertTrue(sampler.shouldSample(10001));
        Assert.assertFalse(sampler.shouldSample(1019903));
    }
}

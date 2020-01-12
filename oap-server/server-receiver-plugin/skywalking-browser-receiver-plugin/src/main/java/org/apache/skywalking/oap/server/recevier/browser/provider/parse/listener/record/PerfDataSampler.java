package org.apache.skywalking.oap.server.recevier.browser.provider.parse.listener.record;

/**
 * @author zhangwei
 */
public class PerfDataSampler {
    private int sampleRate = 10000;

    public PerfDataSampler(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public boolean shouldSample(String uniqueId) {
        int sampleValue = uniqueId.hashCode() % 10000;
        if (sampleValue < sampleRate) {
            return true;
        }
        return false;
    }
}

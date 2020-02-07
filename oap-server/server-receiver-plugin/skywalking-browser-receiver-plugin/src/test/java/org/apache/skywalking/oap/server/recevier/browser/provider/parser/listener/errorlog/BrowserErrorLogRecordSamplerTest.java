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

package org.apache.skywalking.oap.server.recevier.browser.provider.parser.listener.errorlog;

import org.apache.skywalking.oap.server.recevier.browser.provider.parser.errorlog.listener.record.BrowserErrorLogRecordSampler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhangwei
 */
public class BrowserErrorLogRecordSamplerTest {

    @Test
    public void sample() {
        BrowserErrorLogRecordSampler sampler = new BrowserErrorLogRecordSampler(100);
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

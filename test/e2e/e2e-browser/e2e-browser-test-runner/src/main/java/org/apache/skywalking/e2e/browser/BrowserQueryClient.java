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

package org.apache.skywalking.e2e.browser;

import com.google.common.io.Resources;
import org.apache.skywalking.e2e.GQLResponse;
import org.apache.skywalking.e2e.SimpleQueryClient;
import org.apache.skywalking.e2e.service.Service;
import org.apache.skywalking.e2e.service.ServicesData;
import org.apache.skywalking.e2e.service.ServicesQuery;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zhangwei
 */
public class BrowserQueryClient extends SimpleQueryClient {

    public BrowserQueryClient(String host, String port) {
        super(host, port);
    }


    public List<Service> browserServices(final ServicesQuery query) throws Exception {
        final URL queryFileUrl = Resources.getResource("browserServices.gql");
        final String queryString = Resources.readLines(queryFileUrl, Charset.forName("UTF8"))
                .stream()
                .filter(it -> !it.startsWith("#"))
                .collect(Collectors.joining())
                .replace("{start}", query.start())
                .replace("{end}", query.end())
                .replace("{step}", query.step());
        final ResponseEntity<GQLResponse<ServicesData>> responseEntity = restTemplate.exchange(
                new RequestEntity<>(queryString, HttpMethod.POST, URI.create(endpointUrl)),
                new ParameterizedTypeReference<GQLResponse<ServicesData>>() {
                }
        );

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response status != 200, actual: " + responseEntity.getStatusCode());
        }

        return Objects.requireNonNull(responseEntity.getBody()).getData().getServices();
    }
}

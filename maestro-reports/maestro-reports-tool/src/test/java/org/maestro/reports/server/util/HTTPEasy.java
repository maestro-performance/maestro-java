/*
 * Copyright 2020 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.maestro.reports.server.util;

import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.core.MediaType;


public class HTTPEasy {
    public static WebClient url(final String requestUrl) {
        WebClient webClient = WebClient.create(requestUrl);

        webClient
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);

        return webClient;
    }
}

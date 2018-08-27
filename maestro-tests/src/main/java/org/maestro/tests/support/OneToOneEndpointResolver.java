/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
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

package org.maestro.tests.support;

import org.maestro.client.exchange.support.PeerEndpoint;
import org.maestro.common.Role;
import org.maestro.common.URLUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * A resolver that provides exclusive test endpoints based on the input role. That means that
 * every peer endpoint will be assigned a test endpoint. (i.e: queue.1, queue.2, etc).
 */
public class OneToOneEndpointResolver implements TestEndpointResolver {
    private class ExclusiveTestEndPoint extends DefaultTestEndpoint {
        private int lastCount = 0;

        public ExclusiveTestEndPoint(TestEndpoint testEndpoint) {
            super(testEndpoint.getURL());
        }

        public void increment() {
            lastCount++;
        }

        @Override
        public String getURL() {
            return URLUtils.rewritePath(super.getURL(), String.valueOf(lastCount));
        }
    }

    private final Map<Role, ExclusiveTestEndPoint> endpoints = new HashMap<>();

    @Override
    public void register(Role role, TestEndpoint testEndpoint) {
        endpoints.put(role, new ExclusiveTestEndPoint(testEndpoint));
    }

    @Override
    public TestEndpoint resolve(PeerEndpoint peerEndpoint) {
        ExclusiveTestEndPoint ret = endpoints.get(peerEndpoint.getRole());

        if (ret != null) {
            ret.increment();
            return ret;
        }

        return null;
    }
}

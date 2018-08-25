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

package org.maestro.tests;

import org.maestro.common.Role;

import java.util.Map;

public interface MultiPointProfile {

    final class TestEndpoint {
        private final String brokerURL;

        public TestEndpoint(final String brokerURL) {
            this.brokerURL = brokerURL;
        }

        public String getSendReceiveURL() {
            return brokerURL;
        }
    }

    void addEndPoint(Role role, TestEndpoint testEndpoint);

    Map<Role, TestEndpoint> getTestEndpoints();
}

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

package org.maestro.reports.controllers.common;

import org.maestro.common.test.TestProperties;

public class ExtendedTestProperties extends TestProperties {
    private String role;

    public ExtendedTestProperties(final TestProperties tp) {
        super(tp.getBrokerUri(), tp.getDurationType(), tp.getDuration(), tp.getFcl(), tp.getApiName(),
                tp.getApiVersion(), tp.getProtocol(), tp.getParallelCount(), tp.getMessageSize(),
                tp.isVariableSize(), tp.getRate(), tp.getLimitDestinations());
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

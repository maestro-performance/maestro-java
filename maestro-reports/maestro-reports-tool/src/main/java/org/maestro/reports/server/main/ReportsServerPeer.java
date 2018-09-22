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
 *
 */

package org.maestro.reports.server.main;

import org.maestro.client.exchange.support.WorkerPeer;
import org.maestro.common.Role;

/**
 * Agent-specific peer class
 */
public class ReportsServerPeer extends WorkerPeer {
    private static final String NAME = "reports-server";

    public ReportsServerPeer(final String host) {
        super(NAME, host, Role.REPORTS_SERVER);
    }
}

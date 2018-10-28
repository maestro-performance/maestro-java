/*
 * Copyright 2018 Otavio Rodolfo Piske
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

package org.maestro.reports.common.organizer;


import org.maestro.client.exchange.support.PeerInfo;

import java.io.File;

/**
 * A default organizer for the report layout that organizes the report directory in the format
 * ${basedir}/id/${test id}/number/${test number}/${unique peer id}/${peer host} (ie.: /tmp/sender/failed/0/)
 */
public class DefaultOrganizer implements Organizer<String> {
    protected final String baseDir;

    public DefaultOrganizer(final String baseDir) {
        this.baseDir = baseDir;
    }

    protected String combine(final String uniquePeerPath) {
        return baseDir + File.separator + uniquePeerPath;
    }

    @Override
    public String organize(final String peerInfo) {
        return combine(peerInfo);
    }

    public static String generateUniquePeerPath(final String id, final PeerInfo peerInfo) {
        return id + File.separator + peerInfo.peerHost();
    }
}

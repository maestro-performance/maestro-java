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

package org.maestro.reports.organizer;

import org.maestro.client.exchange.support.PeerInfo;

import java.io.File;

/**
 * A default organizer for the report layout that organizes the report directory in the format
 * ${basedir}/${host type}/${result type dir}/${test number}/ (ie.: /tmp/sender/failed/0/)
 */
public class DefaultOrganizer implements Organizer {
    protected final String baseDir;
    protected final TestTracker tracker = new TestTracker();
    protected String resultType;

    public DefaultOrganizer(final String baseDir) {
        this.baseDir = baseDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    @Override
    public TestTracker getTracker() {
        return tracker;
    }

    protected String combine(final PeerInfo peerInfo) {
        return baseDir + File.separator + peerInfo.getRole() + File.separator + resultType + File.separator +
                tracker.currentTestString() + File.separator + peerInfo.peerHost();
    }

    @Override
    public String organize(final PeerInfo peerInfo) {
        return combine(peerInfo);
    }
}

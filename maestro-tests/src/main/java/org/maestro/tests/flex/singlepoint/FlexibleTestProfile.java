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

package org.maestro.tests.flex.singlepoint;

import org.maestro.client.Maestro;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.common.Role;
import org.maestro.common.agent.Source;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.tests.AbstractTestProfile;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.support.TestEndpointResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.maestro.client.Maestro.set;

/**
 * A simple but flexible single point test profile for usage w/ 3rd party tools
 */
public class FlexibleTestProfile extends AbstractTestProfile {
    private static final Logger logger = LoggerFactory.getLogger(FlexibleTestProfile.class);
    private String sourceURL;
    private String branch;
    private String brokerURL;
    private TestDuration testDuration;

    @SuppressWarnings("unused")
    void setSourceURL(final String sourceURL) {
        this.sourceURL = sourceURL;
    }

    public void setSendReceiveURL(final String url) {
        this.brokerURL = url;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setDuration(TestDuration testDuration) {
        this.testDuration = testDuration;
    }

    @Override
    public long getEstimatedCompletionTime() {
        return testDuration.getNumericDuration();
    }

    @Override
    public void setTestEndpointResolver(TestEndpointResolver endPointResolver) {
        // NO-OP for flexible tests
    }

    @Override
    public void apply(final Maestro maestro, final DistributionStrategy distributionStrategy) throws MaestroException {
        logger.debug("Setting the broker");
        set(maestro::setBroker, MaestroTopics.peerTopic(Role.AGENT), this.brokerURL);

        logger.debug("Sending the source request");
        set(maestro::sourceRequest, MaestroTopics.peerTopic(Role.AGENT), new Source(sourceURL, branch), 30);
    }
}

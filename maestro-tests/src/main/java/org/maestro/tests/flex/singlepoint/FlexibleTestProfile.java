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
import org.maestro.common.agent.Source;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.tests.AbstractTestProfile;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.support.TestEndpointResolver;

import static org.maestro.client.Maestro.set;

/**
 * A simple but flexible single point test profile for usage w/ 3rd party tools
 */
public class FlexibleTestProfile extends AbstractTestProfile {
    private String sourceURL;
    private String brokerURL;

    @SuppressWarnings("unused")
    void setSourceURL(final String sourceURL) {
        this.sourceURL = sourceURL;
    }

    public void setSendReceiveURL(final String url) {
        this.brokerURL = url;
    }

    // NO-OP for this
    @Override
    public long getEstimatedCompletionTime() {
        return 0;
    }


    @Override
    public void setTestEndpointResolver(TestEndpointResolver endPointResolver) {
        // NO-OP for flexible tests
    }

    @Override
    public void apply(final Maestro maestro, final DistributionStrategy distributionStrategy) throws MaestroException {
        set(maestro::setBroker, MaestroTopics.WORKERS_TOPIC, this.brokerURL);

        set(maestro::sourceRequest, MaestroTopics.WORKERS_TOPIC, new Source(sourceURL, null));
    }
}

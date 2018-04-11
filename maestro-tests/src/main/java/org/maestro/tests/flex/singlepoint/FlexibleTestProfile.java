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
import org.maestro.common.exceptions.MaestroException;
import org.maestro.tests.AbstractTestProfile;

/**
 * A simple but flexible single point test profile for usage w/ 3rd party tools
 */
public class FlexibleTestProfile extends AbstractTestProfile {
    private String sourceURL;
    private String brokerURL;

    void setSourceURL(final String sourceURL) {
        this.sourceURL = sourceURL;
    }

    void setBrokerURL(final String brokerURL) {
        this.brokerURL = brokerURL;
    }

    @Override
    public void apply(final Maestro maestro) throws MaestroException {
        maestro.setBroker(this.brokerURL);
        maestro.sourceRequest(sourceURL, null);
    }
}

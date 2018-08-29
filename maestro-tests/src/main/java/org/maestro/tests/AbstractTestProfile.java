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

import org.maestro.client.Maestro;
import org.maestro.client.exchange.support.PeerEndpoint;
import org.maestro.common.Role;
import org.maestro.common.agent.Source;
import org.maestro.common.agent.UserCommandData;
import org.maestro.common.client.exceptions.NotEnoughRepliesException;
import org.maestro.common.duration.DurationCount;
import org.maestro.common.duration.TestDuration;
import org.maestro.tests.support.TestEndpoint;
import org.maestro.tests.support.TestEndpointResolver;
import org.maestro.tests.utils.CompletionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.maestro.client.Maestro.set;

public abstract class AbstractTestProfile implements TestProfile {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestProfile.class);

    private TestEndpointResolver endpointResolver;
    private int testExecutionNumber;
    private String managementInterface;
    private String inspectorName;
    private String extPointSource;
    private String extPointBranch;
    private String extPointCommand;

    public int getTestExecutionNumber() {
        return testExecutionNumber;
    }

    public void incrementTestExecutionNumber() {
        testExecutionNumber++;
    }

    public String getManagementInterface() {
        return managementInterface;
    }

    public void setManagementInterface(final String managementInterface) {
        this.managementInterface = managementInterface;
    }

    public String getInspectorName() {
        return inspectorName;
    }

    public void setInspectorName(final String inspectorName) {
        this.inspectorName = inspectorName;
    }

    public String getExtPointSource() {
        return extPointSource;
    }

    public void setExtPointSource(String extPointSource) {
        this.extPointSource = extPointSource;
    }

    public String getExtPointBranch() {
        return extPointBranch;
    }

    public void setExtPointBranch(String extPointBranch) {
        this.extPointBranch = extPointBranch;
    }

    public String getExtPointCommand() {
        return extPointCommand;
    }

    public void setExtPointCommand(String extPointCommand) {
        this.extPointCommand = extPointCommand;
    }

    protected static long getEstimatedCompletionTime(final TestDuration duration, long rate) {
        if (duration instanceof DurationCount) {
            return CompletionTime.estimate(duration, rate);
        }
        else {
            return duration.getNumericDuration();
        }
    }

    @Override
    public void setTestEndpointResolver(TestEndpointResolver endpointResolver) {
        this.endpointResolver = endpointResolver;
    }

    protected void applyAgent(Maestro maestro, PeerEndpoint endpoint, String destination) {
        if (endpoint.getRole() == Role.AGENT) {
            if (getExtPointSource() != null) {
                if (getExtPointBranch() != null) {
                    logger.info("Setting the extension point source to {} using the {} branch", getExtPointSource(),
                            getExtPointBranch());
                    set(maestro::sourceRequest, destination, new Source(getExtPointSource(), getExtPointBranch()));
                }
            }

            if (getExtPointCommand() != null) {
                logger.info("Setting command to Agent execution to {}", getExtPointCommand());
                set(maestro::userCommand, destination, new UserCommandData(0L, getExtPointCommand()));
            }
        }
    }

    protected void applyInspector(Maestro maestro, PeerEndpoint endpoint, String destination) {
        if (endpoint.getRole() == Role.INSPECTOR) {
            if (getManagementInterface() != null) {
                if (getInspectorName() != null) {
                    logger.info("Setting the management interface to {} using inspector {}", getManagementInterface(),
                            getInspectorName());
                    try {
                        set(maestro::setManagementInterface, destination, getManagementInterface());
                    }
                    catch (NotEnoughRepliesException ne) {
                        logger.warn("Apparently no inspector nodes are enabled on this cluster. Ignoring ...");
                    }
                }
            }
        }
    }

    protected void setSendReceiveURL(Maestro maestro, PeerEndpoint endpoint) {
        final TestEndpoint testEndpoint = endpointResolver.resolve(endpoint);

        if (testEndpoint == null) {
            if (endpoint.getRole().isWorker()) {
                logger.info("There is not test end point set for peers w/ role {}", endpoint.getRole());
            }
            else {
                logger.info("There is no test end point to {}", endpoint.getRole());
            }
        }
        else {
            logger.info("Setting {} end point to {}", endpoint.getRole(), testEndpoint.getURL());

            set(maestro::setBroker, endpoint.getDestination(), testEndpoint.getURL());
        }
    }


    @Override
    public String toString() {
        return "AbstractTestProfile{" +
                "testExecutionNumber=" + testExecutionNumber +
                '}';
    }
}

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

package org.maestro.tests.flex;

import org.maestro.client.Maestro;
import org.maestro.reports.ReportsDownloader;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.AbstractTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple test executor that can be extended for use with 3rd party testing tools
 */
public abstract class FlexibleTestExecutor extends AbstractTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(FlexibleTestExecutor.class);
    private Maestro maestro;

    private FlexibleTestProcessor testProcessor;
    private ReportsDownloader reportsDownloader;
    private AbstractTestProfile testProfile;


    /**
     * Constructor
     * @param maestro
     * @param reportsDownloader
     * @param testProfile
     */
    public FlexibleTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                final AbstractTestProfile testProfile)
    {
        super(maestro, reportsDownloader);

        this.maestro = maestro;
        this.reportsDownloader = reportsDownloader;
        this.testProfile = testProfile;

        testProcessor = new FlexibleTestProcessor(testProfile, reportsDownloader);
    }


    /**
     * These two methods are NO-OP in this case because there are no multiple iterations,
     * therefore cool down period is not required/used
     */
    public long getCoolDownPeriod() {
        return 0;
    }

    public void setCoolDownPeriod(long period) {
        // NO-OP
    }

    abstract public void startServices();

    /**
     * Test execution logic
     * @return
     */
    public boolean run() {
        try {
            int repeat = 2;

            // Clean up the topic
            logger.debug("Cleaning up the topic");
            maestro.collect();

            logger.info("Collecting the number of peers");
            int numPeers = getNumPeers();

            logger.info("Resolving data servers");
            resolveDataServers();
            processReplies(testProcessor, repeat, numPeers);

            getReportsDownloader().getOrganizer().getTracker().setCurrentTest(testProfile.getTestExecutionNumber());

            logger.info("Applying the test profile");
            testProfile.apply(maestro);

            testProcessor.resetNotifications();

            logger.info("Starting the services");
            startServices();

            logger.info("Processing the replies");
            processReplies(testProcessor, repeat, numPeers);

            logger.info("Waiting a while for the Quiver test is running");
            Thread.sleep(80000);

            logger.info( "Processing the notifications");
            processNotifications(testProcessor, repeat * 2, numPeers);
        } catch (InterruptedException e) {
            logger.info("Test execution interrupted");
        } finally {
            maestro.stopAgent();
        }

        return testProcessor.isSuccessful();
    }
}

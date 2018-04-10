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

/**
 * Provides an interface to organize the files in a report directory
 */
public interface Organizer {

    /**
     * Gets the result type string (ie.: success, failed, etc)
     * @return the result type string
     */
    String getResultType();

    /**
     * The result type string
     * @param resultType
     */
    void setResultType(String resultType);


    /**
     * Organize the report directory
     * @param address The original address of the report being downloaded
     * @param hostType the host type that provided the report
     * @return The organized directory layout to use
     */
    String organize(final String address, final String hostType);


    /**
     * Gets the test tracker used to keep track of the current test
     * @return the active test tracker
     */
    TestTracker getTracker();
}

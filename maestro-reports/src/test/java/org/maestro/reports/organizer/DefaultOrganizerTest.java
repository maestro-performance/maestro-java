

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

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class DefaultOrganizerTest {

    @Test
    public void testOrganizer() {

        DefaultOrganizer defaultOrganizer = new DefaultOrganizer("/sample");

        assertTrue("Default tracker must not be null",
                defaultOrganizer.getTracker() != null);

        defaultOrganizer.setResultType("failed");
        String sampleReceiver = defaultOrganizer.organize("http://localhost", "receiver");
        assertEquals("Unexpected directory layout", "/sample/receiver/failed/0/localhost",
                sampleReceiver);

        defaultOrganizer.setResultType("success");
        String sampleSender = defaultOrganizer.organize("http://localhost", "sender");
        assertEquals("Unexpected directory layout", "/sample/sender/success/0/localhost",
                sampleSender);
    }
}

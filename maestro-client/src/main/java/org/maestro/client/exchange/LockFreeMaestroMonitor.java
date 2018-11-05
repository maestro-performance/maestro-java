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

package org.maestro.client.exchange;

import org.maestro.common.client.notes.MaestroNote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

/**
 * This class serves as a monitoring lock for asynchronous receipt of messages (usually,
 * notifications). With this, the calling threads can be put to sleep until new messages
 * matching the predicate have arrived. The responsibility of notifying them relies on
 * the MaestroCollector which receives the messages.
 */
public class LockFreeMaestroMonitor extends MaestroMonitor {
    private static final Logger logger = LoggerFactory.getLogger(LockFreeMaestroMonitor.class);
    private final List<MaestroNote> dest;

    /**
     * Constructs a Monitor using the given predicate (ie.: note instanceof TestSuccessfulNotification)
     * @param object the monitoring predicate
     */
    public LockFreeMaestroMonitor(final Predicate<? super MaestroNote> object, final List<MaestroNote> dest) {
        super(object);

        this.dest = dest;
    }

    /**
     * Tests if a subject matches the predicate
     * @param subject the subject to test (ie.: a new note arriving on the collector)
     * @return true if it matches or false otherwise
     */
    public boolean shouldAwake(final MaestroNote subject) {
        boolean ret = getObject().test(subject);

        if (ret) {
            logger.info("Message {} matches the predicate", subject);
            andThen(subject);
        }

        return ret;
    }


    public void andThen(final MaestroNote subject) {
        dest.add(subject);
    }

}

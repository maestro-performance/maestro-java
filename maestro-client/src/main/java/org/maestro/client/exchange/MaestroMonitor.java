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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * This class serves as a monitoring lock for asynchronous receipt of messages (usually,
 * notifications). With this, the calling threads can be put to sleep until new messages
 * matching the predicate have arrived. The responsibility of notifying them relies on
 * the MaestroCollector which receives the messages.
 */
public class MaestroMonitor {
    private static final Logger logger = LoggerFactory.getLogger(MaestroMonitor.class);
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private final Predicate object;

    /**
     * Constructs a Monitor using the given predicate (ie.: note instanceof TestSuccessfulNotification)
     * @param object the monitoring predicate
     */
    public MaestroMonitor(Predicate object) {
        this.object = object;
    }

    /**
     * Lock the execution
     * @throws InterruptedException if interrupted
     */
    public void doLock() throws InterruptedException {
        lock.lock();
        try {
            logger.trace("Not enough messages satisfying the condition are available. Waiting ...");
            condition.await();
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Lock the execution
     * @throws InterruptedException if interrupted
     */
    public void doLock(long timeout) throws InterruptedException {
        lock.lock();
        try {
            logger.trace("Not enough messages satisfying the condition are available. Waiting ...");
            condition.await(timeout, TimeUnit.MILLISECONDS);
        }
        finally {
            lock.unlock();
        }
    }


    /**
     * Unlock the execution
     */
    public void doUnlock() {
        lock.lock();
        try {
            logger.debug("Some messages arrived ... unlocking for check");
            condition.signal();
        }
        finally {
            lock.unlock();
        }
    }


    /**
     * Tests if a subject matches the predicate
     * @param subject the subject to test (ie.: a new note arriving on the collector)
     * @return true if it matches or false otherwise
     */
    public boolean shouldAwake(Object subject) {
        return object.test(subject);
    }


}

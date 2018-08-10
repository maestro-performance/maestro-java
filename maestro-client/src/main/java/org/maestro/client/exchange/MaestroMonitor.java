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

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class MaestroMonitor {
    private static final Logger logger = LoggerFactory.getLogger(MaestroMonitor.class);
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private Predicate object;

    public MaestroMonitor(Predicate object) {
        this.object = object;
    }

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

    public boolean shouldAwake(Object subject) {
        return object.test(subject);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaestroMonitor monitor = (MaestroMonitor) o;
        return Objects.equals(object, monitor.object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(object);
    }
}

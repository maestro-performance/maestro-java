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

package org.maestro.worker.common.container.initializers;

import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.common.worker.WorkerOptions;

import java.util.concurrent.CountDownLatch;

public class TestWorkerInitializer implements WorkerInitializer {
    private final Class<?> clazz;
    private final WorkerOptions workerOptions;

    public TestWorkerInitializer(final Class<?> clazz, final WorkerOptions workerOptions) {
        this.clazz = clazz;
        this.workerOptions = workerOptions;
    }

    public WorkerOptions getWorkerOptions() {
        return workerOptions;
    }


    @Override
    public MaestroWorker initialize(int number, final CountDownLatch startSignal, final CountDownLatch endSignal) throws IllegalAccessException, InstantiationException {
        final Object object = clazz.newInstance();

        if (object instanceof MaestroWorker) {
            MaestroWorker worker = (MaestroWorker) object;

            worker.setupBarriers(startSignal, endSignal);
            worker.setWorkerOptions(workerOptions);
            worker.setWorkerNumber(number);

            return worker;
        }
        else {
            throw new MaestroException("Invalid class type %s", (object == null ? "null" : object.getClass()));
        }
    }
}

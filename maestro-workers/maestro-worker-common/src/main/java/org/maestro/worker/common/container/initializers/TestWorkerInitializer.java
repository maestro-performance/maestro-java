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

import org.maestro.common.evaluators.Evaluator;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.common.worker.WorkerOptions;

public class TestWorkerInitializer implements WorkerInitializer {
    private Class<MaestroWorker> clazz;
    private Evaluator<?> evaluator;
    private WorkerOptions workerOptions;

    public TestWorkerInitializer(Class<MaestroWorker> clazz, Evaluator<?> evaluator, WorkerOptions workerOptions) {
        this.clazz = clazz;
        this.evaluator = evaluator;
        this.workerOptions = workerOptions;
    }

    public Class<MaestroWorker> getClazz() {
        return clazz;
    }

    public Evaluator<?> getEvaluator() {
        return evaluator;
    }

    public WorkerOptions getWorkerOptions() {
        return workerOptions;
    }


    @Override
    public MaestroWorker initialize(int number) throws IllegalAccessException, InstantiationException {
        final MaestroWorker worker = clazz.newInstance();

        worker.setWorkerOptions(workerOptions);
        worker.setWorkerNumber(number);

        return worker;
    }
}

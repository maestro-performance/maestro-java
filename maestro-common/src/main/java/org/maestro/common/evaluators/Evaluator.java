/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.common.evaluators;

/**
 * Provides an way to evaluate conditions during the test execution
 * @param <T> An evaluator type
 */
public interface Evaluator<T> {

    /**
     * Evaluates whether the condition is met or not
     * @return true if the condition is met or false otherwise
     */
    boolean eval();

    /**
     * Record the condition
     * @param data data containing the condition to be evaluated
     */
    void record(final T data);
}

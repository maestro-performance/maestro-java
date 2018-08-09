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

import org.maestro.client.notes.InternalError;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractTestProfile implements TestProfile {
    private int testExecutionNumber;
    private String managementInterface;
    private String inspectorName;

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

    protected <T> void apply(Function<T, CompletableFuture<List<? extends MaestroNote>>> function, T value) {
        final int timeout = 2;

        List<? extends MaestroNote> replies = null;
        try {
            replies = function.apply(value).get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            throw new MaestroException(e);
        }

        if (replies.size() == 0) {
            throw new MaestroException("Not enough replies when trying to apply a setting to the test cluster");
        }

        for (MaestroNote reply : replies) {
            if (reply instanceof InternalError) {
                InternalError ie = (InternalError) reply;
                throw new MaestroException("Error applying a setting to the test cluster: %s", ie.getMessage());
            }
        }
    }

    protected <T, U> void apply(BiFunction<T, U, CompletableFuture<List<? extends MaestroNote>>> function, T value1, U value2) {
        final int timeout = 1;

        List<? extends MaestroNote> replies = null;
        try {
            replies = function.apply(value1, value2).get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            throw new MaestroException(e);
        }

        if (replies.size() == 0) {
            throw new MaestroException("Not enough replies when trying to apply a setting to the test cluster");
        }

        for (MaestroNote reply : replies) {
            if (reply instanceof InternalError) {
                InternalError ie = (InternalError) reply;
                throw new MaestroException("Error applying a setting to the test cluster: %s", ie.getMessage());
            }
        }

    }

    @Override
    public String toString() {
        return "AbstractTestProfile{" +
                "testExecutionNumber=" + testExecutionNumber +
                '}';
    }
}

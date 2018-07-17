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

package org.maestro.common.inspector.types;

/**
 * A type container for the JVM memory information (of all types)
 */
public class JVMMemoryInfo implements InspectorType, JVMMemoryInfoType {
    private final String memoryAreaName;
    private final long initial;
    private final long committed;
    private final long max;
    private final long used;

    public JVMMemoryInfo(final String memoryAreaName, long initial, long committed, long max, long used) {
        this.memoryAreaName = memoryAreaName;
        this.initial = initial;
        this.committed = committed;
        this.max = max;
        this.used = used;
    }

    /**
     * {@inheritDoc}
     */
    public String getMemoryAreaName() {
        return memoryAreaName;
    }

    public long getInitial() {
        return initial;
    }

    public long getCommitted() {
        return committed;
    }

    public long getMax() {
        return max;
    }

    public long getUsed() {
        return used;
    }

    @Override
    public String toString() {
        return "JVMMemoryInfo{" +
                "memoryAreaName='" + memoryAreaName + '\'' +
                ", initial=" + initial +
                ", committed=" + committed +
                ", max=" + max +
                ", used=" + used +
                '}';
    }
}

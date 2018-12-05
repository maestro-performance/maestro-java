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

package org.maestro.plotter.inspector.serializer;

import org.maestro.plotter.common.statistics.Statistics;

import java.util.Date;
import java.util.List;

public class MemoryData {
    private String name;
    private List<Date> periods;
    private List<Long> used;
    private List<Long> committed;
    private Statistics usedStatistics;
    private Statistics committedStatistics;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Date> getPeriods() {
        return periods;
    }

    public void setPeriods(List<Date> periods) {
        this.periods = periods;
    }

    public List<Long> getUsed() {
        return used;
    }

    public void setUsed(List<Long> used) {
        this.used = used;
    }

    public List<Long> getCommitted() {
        return committed;
    }

    public void setCommitted(List<Long> committed) {
        this.committed = committed;
    }

    public Statistics getUsedStatistics() {
        return usedStatistics;
    }

    public void setUsedStatistics(Statistics usedStatistics) {
        this.usedStatistics = usedStatistics;
    }

    public Statistics getCommittedStatistics() {
        return committedStatistics;
    }

    public void setCommittedStatistics(Statistics committedStatistics) {
        this.committedStatistics = committedStatistics;
    }
}

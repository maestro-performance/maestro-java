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

package net.orpiske.mpt.common.worker;


import java.io.Serializable;
import java.time.Instant;

public final class WorkerSnapshot implements Serializable {
    private long count;
    private Instant startTime;
    private Instant now;
    private Instant eta;
    private PerfStats stats;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getNow() {
        return now;
    }

    public void setNow(Instant now) {
        this.now = now;
    }

    public Instant getEta() {
        return eta;
    }

    public void setEta(Instant eta) {
        this.eta = eta;
    }

    public PerfStats getStats() {
        return stats;
    }

    public void setStats(PerfStats stats) {
        this.stats = stats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkerSnapshot snapshot = (WorkerSnapshot) o;

        if (count != snapshot.count) return false;
        if (startTime != null ? !startTime.equals(snapshot.startTime) : snapshot.startTime != null) return false;
        if (now != null ? !now.equals(snapshot.now) : snapshot.now != null) return false;
        if (eta != null ? !eta.equals(snapshot.eta) : snapshot.eta != null) return false;
        return stats != null ? stats.equals(snapshot.stats) : snapshot.stats == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (count ^ (count >>> 32));
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (now != null ? now.hashCode() : 0);
        result = 31 * result + (eta != null ? eta.hashCode() : 0);
        result = 31 * result + (stats != null ? stats.hashCode() : 0);
        return result;
    }
}

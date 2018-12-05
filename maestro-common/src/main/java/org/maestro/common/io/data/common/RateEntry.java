/*
 * Copyright 2018 Otavio Rodolfo Piske
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

package org.maestro.common.io.data.common;

public class RateEntry {
    public static final int BYTES = Integer.BYTES + Long.BYTES + Long.BYTES;

    private final int metadata;
    private final long count;
    private final long timestamp;

    public RateEntry(int metadata, long count, long timestamp) {
        this.metadata = metadata;
        this.count = count;
        this.timestamp = timestamp;
    }

    public int getMetadata() {
        return metadata;
    }

    public long getCount() {
        return count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "RateEntry{" +
                "metadata=" + metadata +
                ", count=" + count +
                ", timestamp=" + timestamp +
                '}';
    }
}

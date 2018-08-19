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

package org.maestro.common.client.notes;

public class DrainOptions {
    private final String duration;
    private final String url;
    private final int parallelCount;

    /**
     * Constructor
     * @param duration duration of the drain
     * @param url URL to drain
     * @param parallelCount parallel count
     */
    public DrainOptions(final String duration, final String url, int parallelCount) {
        this.duration = duration;
        this.url = url;
        this.parallelCount = parallelCount;
    }

    public String getDuration() {
        return duration;
    }

    public String getUrl() {
        return url;
    }

    public int getParallelCount() {
        return parallelCount;
    }
}

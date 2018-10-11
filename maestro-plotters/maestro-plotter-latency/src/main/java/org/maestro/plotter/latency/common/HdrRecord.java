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

package org.maestro.plotter.latency.common;

public class HdrRecord implements Comparable<HdrRecord> {
    private final double percentile;
    private final double value;

    public HdrRecord(double percentile, double value) {
        this.percentile = percentile;
        this.value = value;
    }

    public double getPercentile() {
        return percentile;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int compareTo(HdrRecord hdrRecord) {

        if (this.percentile < hdrRecord.percentile) {
            return -1;
        }
        else {
            if (this.percentile > hdrRecord.percentile) {
                return 1;
            }
        }

        return 0;
    }
}

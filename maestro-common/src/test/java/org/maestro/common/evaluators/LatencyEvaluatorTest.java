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

package org.maestro.common.evaluators;

import org.HdrHistogram.Histogram;
import org.junit.Test;

import static org.junit.Assert.*;

public class LatencyEvaluatorTest {

    public void testEvalHard(LatencyEvaluator latencyEvaluator) {
        Histogram histogram = new Histogram(1);

        for (int i = 0; i <= 99; i++) {
            histogram.recordValue(i);
            latencyEvaluator.record(histogram);
            assertTrue(latencyEvaluator.eval());
        }

        histogram.recordValue(100);
        latencyEvaluator.record(histogram);
        assertFalse(latencyEvaluator.eval());

        assertEquals(100.0d, latencyEvaluator.getMaxValue(), 0.1);
    }

    @Test
    public void testEvalHard() {
        HardLatencyEvaluator latencyEvaluator = new HardLatencyEvaluator(100);
        testEvalHard(latencyEvaluator);
    }

    @Test
    public void testEvalSoft() {
        SoftLatencyEvaluator latencyEvaluator = new SoftLatencyEvaluator(100, 100);
        testEvalHard(latencyEvaluator);
    }

    @Test
    public void testEvalSoft90th() {
        SoftLatencyEvaluator latencyEvaluator = new SoftLatencyEvaluator(90, 90);

        Histogram histogram = new Histogram(1);

        for (int i = 0; i <= 100; i++) {
            histogram.recordValue(i);
            latencyEvaluator.record(histogram);

            /*
             It passes the threshold for the 90th percentile with 90 as the max value when the counter
             reaches 97. Subsequent calls to the evaluator should always be false after that
             */
            if (i <= 96) {
                assertTrue("Failed at record " + i, latencyEvaluator.eval());
            }
            else {
                assertFalse("Failed at record " + i, latencyEvaluator.eval());
            }
        }

        assertEquals(90.0, latencyEvaluator.getMaxValue(), 0.1);
    }
}
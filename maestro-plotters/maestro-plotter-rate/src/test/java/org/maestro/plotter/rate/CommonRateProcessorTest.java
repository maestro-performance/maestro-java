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

package org.maestro.plotter.rate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class CommonRateProcessorTest {

    public void testRecordCount(final int count, final RateData rateData) {
        assertEquals("Incorrect loaded size for the rate periods", count, rateData.getPeriods().size());
        assertEquals("Incorrect loaded size for the rate values", count, rateData.getPeriods().size());
    }

    protected RateData getData(final String resource) throws Exception {
        final String fileName = this.getClass().getResource(resource).getPath();

        RateDataReader queueReader = new RateDataReader();
        return queueReader.read(fileName);
    }

}

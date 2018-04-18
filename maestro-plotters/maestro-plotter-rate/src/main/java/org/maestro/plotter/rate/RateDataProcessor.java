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

import org.maestro.plotter.common.RecordProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class RateDataProcessor implements RecordProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RateDataProcessor.class);

    private Map<String, RateRecord> cache = new HashMap<>();
    private long errorCount = 0;

    @Override
    public void process(String... records) throws Exception {
        doProcess(records[1]);
    }

    private void doProcess(final String ata) {
        final int indexLen = 19;

        try {
            String period = ata.substring(0, indexLen);
            RateRecord rateRecord = cache.get(period);

            if (rateRecord == null) {
                final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Instant ataDate = formatter.parse(ata).toInstant();

                rateRecord = new RateRecord(ataDate, 1);
                cache.put(period, rateRecord);
            } else {
                int i = rateRecord.getCount();

                i++;
                assert i>=0;
                rateRecord.setCount(i);
            }
        } catch (ParseException e) {
            logger.warn("Error parsing record with values ata {}: {}", ata, e.getMessage());
            errorCount++;
        }
        catch (Exception e) {
            logger.warn("Error parsing record with values ata {}: {}", ata, e.getMessage());
            errorCount++;
        }
    }

    public RateData getRateData() {
        Set<RateRecord> ret = new TreeSet<>(cache.values());
        RateData rateData = new RateData(ret);

        rateData.setErrorCount(errorCount);
        return rateData;
    }
}

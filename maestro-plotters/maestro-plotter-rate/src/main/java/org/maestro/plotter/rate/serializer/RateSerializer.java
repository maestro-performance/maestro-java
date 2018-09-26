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

package org.maestro.plotter.rate.serializer;

import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.plotter.common.serializer.SingleData;
import org.maestro.plotter.rate.RateData;
import org.maestro.plotter.rate.RateDataReader;

import java.io.File;
import java.io.IOException;

public class RateSerializer implements MaestroSerializer<SingleData<Long>> {
    private static final String dataName = "rate";

    private RateDataReader rateDataReader = new RateDataReader();

    @Override
    public String name() {
        return dataName;
    }

    @Override
    public SingleData<Long> serialize(final File file) throws IOException {
        RateData rateData = rateDataReader.read(file);

        SingleData<Long> rate = new SingleData<>();

        rate.setPeriods(rateData.getPeriods());
        rate.setValues(rateData.getRateValues());
        rate.setStatistics(rateData.rateStatistics());

        return rate;
    }


}

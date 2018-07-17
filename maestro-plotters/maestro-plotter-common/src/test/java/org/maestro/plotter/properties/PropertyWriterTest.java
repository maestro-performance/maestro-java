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

package org.maestro.plotter.properties;

import org.junit.Test;
import org.maestro.plotter.common.InstantRecord;
import org.maestro.plotter.common.ReportData;
import org.maestro.plotter.common.properties.PropertyWriter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class PropertyWriterTest {

    private final File dummy = new File(this.getClass().getResource("/").getFile());

    @Test
    public void testEmptyBean() throws IOException {
        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(new Object(), dummy);

        // no-throw == success
    }

    @Test
    public void testLocalEmptyBean() throws IOException  {
        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(new ReportData() {
            @Override
            public List<Date> getPeriods() {
                return null;
            }

            @Override
            public Set<? extends InstantRecord> getRecordSet() {
                return null;
            }
        }, dummy);
    }
}

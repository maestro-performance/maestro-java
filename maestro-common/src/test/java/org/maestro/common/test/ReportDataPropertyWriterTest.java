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

package org.maestro.common.test;

import org.junit.Test;
import org.maestro.common.test.properties.PropertyWriter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ReportDataPropertyWriterTest {
    private static class ReportData {
        public List<Date> getPeriods() {
            return null;
        }

        public Set<Object> getRecordSet() {
            return null;
        }
    }

    private final File dummy = new File(this.getClass().getResource("/").getFile());

    @Test
    public void testEmptyBean() throws IOException {
        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(new Object(), dummy);

    }

    @Test
    public void testLocalEmptyBean() throws IOException  {
        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(new ReportData(), dummy);
    }

    @Test
    public void testNull() throws IOException  {
        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(null, dummy);
    }
}
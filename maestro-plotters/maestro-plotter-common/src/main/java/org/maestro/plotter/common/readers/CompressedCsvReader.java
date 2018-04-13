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

package org.maestro.plotter.common.readers;

import org.apache.commons.io.IOUtils;
import org.maestro.plotter.common.ReportData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public abstract class CompressedCsvReader<T extends ReportData> extends CsvReader<T>  {
    private static final Logger logger = LoggerFactory.getLogger(CompressedCsvReader.class);

    @Override
    public T read(final File filename) throws IOException {
        InputStream fileStream = null;
        InputStream gzipStream = null;

        logger.debug("Reading file {}", filename);

        try {
            fileStream = new FileInputStream(filename);
            gzipStream = new GZIPInputStream(fileStream);

            return readStream(gzipStream);
        }
        finally {
            IOUtils.closeQuietly(gzipStream);
            IOUtils.closeQuietly(fileStream);
        }
    }

}

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
import org.maestro.plotter.common.ReportReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public abstract class CsvReader<T> implements ReportReader<T> {
    private static final Logger logger = LoggerFactory.getLogger(CsvReader.class);

    protected abstract T readReader(final Reader reader) throws IOException;

    protected T readStream(final InputStream stream) throws IOException {
        Reader in = null;

        try {
            in = new InputStreamReader(stream);

            return readReader(in);
        }
        finally {
            IOUtils.closeQuietly(in);
        }
    }

    public T read(final File filename) throws IOException {
        InputStream fileStream = null;
        Reader in = null;

        logger.debug("Reading file {}", filename);

        try {
            fileStream = new FileInputStream(filename);

            return readStream(fileStream);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(fileStream);
        }
    }

}

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

import java.io.File;
import java.io.IOException;

/**
 * The common interface for handling properties saved by peers and
 * read by front-ends
 */
public interface MaestroTestProperties {

    /**
     * Load a properties file (ie.: test.properties)
     * @param testProperties A file object pointing to the file to be loaded
     * @throws IOException If the file cannot be read
     */
    void load(File testProperties) throws IOException;

    /**
     * Write to a properties file
     * @param testProperties A file object pointing to the file to be written
     * @throws IOException If the file cannot be written
     */
    void write(File testProperties) throws IOException;

    void setMessageSize(long messageSize);

    void setMessageSize(final String messageSize);

    long getMessageSize();

    void setParallelCount(int parallelCount);

    void setParallelCount(String parallelCount);

    int getParallelCount();

    void setVariableSize(boolean variableSize);

    boolean isVariableSize();

    void setRate(int rate);

    void setRate(String rate);

    int getRate();
}

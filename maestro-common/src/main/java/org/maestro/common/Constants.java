/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.maestro.common;

import java.io.File;

/**
 * Application constants
 */
public final class Constants {

    public static final String WORKER_LOGS_CONTEXT = "/logs/worker";

    public static final String TEST_LOGS_CONTEXT = "/logs/tests";

    public static final String VERSION = "1.5.0-SNAPSHOT";

    public static final int VERSION_NUMERIC;

    public static final String BIN_NAME = "maestro-java";

    public static final String HOME_PROPERTY = "org.maestro.home";

    public static final String HOME_DIR;

    public static final String MAESTRO_CONFIG_DIR;

    public static final String MAESTRO_LOG_DIR;

    static {
        HOME_DIR = System.getProperty(HOME_PROPERTY);

        MAESTRO_CONFIG_DIR = System.getProperty(HOME_PROPERTY) + File.separator + "config";

        MAESTRO_LOG_DIR = System.getProperty(HOME_PROPERTY) + File.separator + "logs";

        VERSION_NUMERIC = Integer.parseInt(VERSION.replace(".", "").replaceAll("[a-zA-Z-]",""));
    }

    /**
     * File extension for HDR histogram files
     */
    public static final String FILE_EXTENSION_HDR_HISTOGRAM = "hdr";

    /**
     * File extension for Maestro compressed rate files
     */
    public static final String FILE_EXTENSION_MAESTRO = "dat";

    /**
     * File extension for Maestro inspector uncompressed files
     */
    public static final String FILE_EXTENSION_INSPECTOR_REPORT = "csv";

    /**
     * File hint for inspector files
     */
    public static final String FILE_HINT_INSPECTOR = "inspector";

    /**
     * Restricted constructor
     */
    private Constants() {
    }


}

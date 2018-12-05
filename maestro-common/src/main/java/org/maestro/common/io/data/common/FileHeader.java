/*
 * Copyright 2018 Otavio Rodolfo Piske
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

package org.maestro.common.io.data.common;

import org.apache.commons.lang3.StringUtils;
import org.maestro.common.Constants;
import org.maestro.common.Role;
import org.maestro.common.io.data.common.exceptions.InvalidHeaderValueException;

/**
 * Maestro data file header
 */
public class FileHeader {
    public static final int FORMAT_NAME_SIZE = 8;

    private final String formatName;
    private final int fileVersion;
    private final int maestroVersion;
    private final Role role;

    public static final String MAESTRO_FORMAT_NAME = "maestro";
    public static final int CURRENT_FILE_VERSION = 1;

    public static final FileHeader WRITER_DEFAULT_SENDER;
    public static final FileHeader WRITER_DEFAULT_RECEIVER;
    public static final FileHeader WRITER_DEFAULT_INSPECTOR;
    public static final FileHeader WRITER_DEFAULT_AGENT;
    public static final FileHeader WRITER_DEFAULT_EXPORTER;
    public static final int BYTES;

    static {
        WRITER_DEFAULT_SENDER = new FileHeader(MAESTRO_FORMAT_NAME, CURRENT_FILE_VERSION,
                Constants.VERSION_NUMERIC, Role.SENDER);

        WRITER_DEFAULT_RECEIVER = new FileHeader(MAESTRO_FORMAT_NAME, CURRENT_FILE_VERSION,
                Constants.VERSION_NUMERIC, Role.RECEIVER);

        WRITER_DEFAULT_INSPECTOR = new FileHeader(MAESTRO_FORMAT_NAME, CURRENT_FILE_VERSION,
                Constants.VERSION_NUMERIC, Role.INSPECTOR);

        WRITER_DEFAULT_AGENT = new FileHeader(MAESTRO_FORMAT_NAME, CURRENT_FILE_VERSION,
                Constants.VERSION_NUMERIC, Role.AGENT);

        WRITER_DEFAULT_EXPORTER = new FileHeader(MAESTRO_FORMAT_NAME, CURRENT_FILE_VERSION,
                Constants.VERSION_NUMERIC, Role.EXPORTER);

        // The underlying format for the role is an integer
        BYTES = FORMAT_NAME_SIZE + Integer.BYTES + Integer.BYTES + Integer.BYTES;
    }

    public FileHeader(final String formatName, int fileVersion, int maestroVersion, Role role) {
        if (formatName == null || formatName.isEmpty() || formatName.length() > FORMAT_NAME_SIZE) {
            throw new InvalidHeaderValueException("The format name '" +
                    (formatName == null ? "null" : formatName) + "' is not valid");
        }

        if (formatName.length() < FORMAT_NAME_SIZE) {
            this.formatName = StringUtils.leftPad(formatName, FORMAT_NAME_SIZE);
        }
        else {
            this.formatName = formatName;
        }

        this.fileVersion = fileVersion;
        this.maestroVersion = maestroVersion;
        this.role = role;
    }

    public String getFormatName() {
        return formatName;
    }

    public int getFileVersion() {
        return fileVersion;
    }

    public int getMaestroVersion() {
        return maestroVersion;
    }

    public Role getRole() {
        return role;
    }
}

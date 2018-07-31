package org.maestro.common.io.data.common;

import org.apache.commons.lang3.StringUtils;
import org.maestro.common.Constants;
import org.maestro.common.HostTypes;
import org.maestro.common.io.data.common.exceptions.InvalidHeaderValueException;

/**
 * Maestro data file header
 */
public class FileHeader {
    public enum Role {
        OTHER(0),
        SENDER(1),
        RECEIVER(2),
        INSPECTOR(3),
        AGENT(3),
        EXPORTER(4);

        private int code;

        Role(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Role from(int code) {
            switch (code) {
                case 0: {
                    return OTHER;
                }
                case 1: {
                    return SENDER;
                }
                case 2: {
                    return RECEIVER;
                }
                case 3: {
                    return INSPECTOR;
                }
                case 4: {
                    return AGENT;
                }
                default: {
                    return OTHER;
                }
            }
        }

        public static Role hostTypeByName(final String name) {
            if (name == null) {
                return OTHER;
            }

            switch (name) {
                case HostTypes.SENDER_HOST_TYPE: {
                    return SENDER;
                }
                case HostTypes.RECEIVER_HOST_TYPE: {
                    return RECEIVER;
                }
                case HostTypes.INSPECTOR_HOST_TYPE: {
                    return INSPECTOR;
                }
                case HostTypes.AGENT_HOST_TYPE: {
                    return AGENT;
                }
                case HostTypes.EXPORTER_HOST_TYPE: {
                    return EXPORTER;
                }
                default: {
                    return OTHER;
                }
            }
        }
    }


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

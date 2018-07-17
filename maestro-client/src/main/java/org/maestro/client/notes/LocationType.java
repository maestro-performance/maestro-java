package org.maestro.client.notes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The location type for the report as stored in the worker
 */
public enum LocationType {
    /**
     * Any location
     */
    ANY(0),
    /**
     * Last successful test reports
     */
    LAST_SUCCESS(1),

    /**
     * Last failed test reports
     */
    LAST_FAILED(2),

    /**
     * Last test report
     */
    LAST(3);

    final int code;

    /**
     * Constructor
     * @param code location code integer as represent by this enum (ie: 0 to 3)
     */
    LocationType(int code) {
        this.code = code;
    }

    /**
     * Gets a location type by code
     * @param code location code integer as represent by this enum (ie: 0 to 3)
     * @return A location type object
     */
    static LocationType byCode(int code) {
        switch (code) {
            case 0: return ANY;
            case 1: return LAST_SUCCESS;
            case 2: return LAST_FAILED;
            case 3: return LAST;
            default: {
                Logger logger = LoggerFactory.getLogger(LocationType.class);

                logger.error("The value {} is not a recognizable location type", code);
                return null;
            }
        }
    }
}

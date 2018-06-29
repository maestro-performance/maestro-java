package org.maestro.client.notes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LocationType {
    ANY(0),
    LAST_SUCCESS(1),
    LAST_FAILED(2),
    LAST(3);

    int code;

    LocationType(int code) {
        this.code = code;
    }

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

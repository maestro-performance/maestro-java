package org.maestro.common.client.notes;

/**
 * Options available for the GetRequest and GetResponse commands
 */
public enum GetOption {
    /** Gets the data server address */
    MAESTRO_NOTE_OPT_GET_DS(0);

    private long value;

    GetOption(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    static public GetOption from(long value) {
        switch ((int) value) {
            case 0: return MAESTRO_NOTE_OPT_GET_DS;
        }

        return null;
    }
}

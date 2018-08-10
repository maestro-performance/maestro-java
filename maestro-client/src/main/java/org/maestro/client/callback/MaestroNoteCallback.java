package org.maestro.client.callback;

import org.maestro.common.client.notes.MaestroNote;

/**
 * A callback executed when a note is received
 */
public interface MaestroNoteCallback {

    /**
     * Executes the call back
     * @param note the related note
     * @return true if the message should continue to be processed or false otherwise
     */
    boolean call(MaestroNote note);
}

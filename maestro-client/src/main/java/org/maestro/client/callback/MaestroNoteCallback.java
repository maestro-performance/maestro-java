package org.maestro.client.callback;

import org.maestro.common.client.notes.MaestroNote;

/**
 * A callback executed when a note is received
 */
public interface MaestroNoteCallback {

    /**
     * Executes the call back
     * @param note the related note
     */
    void call(MaestroNote note);
}

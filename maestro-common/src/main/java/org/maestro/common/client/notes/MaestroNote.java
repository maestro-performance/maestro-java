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

package org.maestro.common.client.notes;

import java.io.IOException;

/**
 * A basic interface for Maestro Notes
 */
public interface MaestroNote {

    /**
     * Gets the note type
     * @return the note type
     */
    MaestroNoteType getNoteType();

    /**
     * Gets the Maestro command for the note
     * @return the maestro command as an instance of MaestroCommand
     */
    MaestroCommand getMaestroCommand();

    /**
     * Serialize the note
     * @return The serialized note
     * @throws IOException if unable to serialize the note
     */
    byte[] serialize() throws IOException;

    /**
     * Converts the note to string for debugging purposes
     * @return the note in String format
     */
    String toString();

    MessageCorrelation correlate();

    void correlate(final MessageCorrelation correlation);

    void correlate(final MaestroNote note);

    boolean correlatesTo(final MessageCorrelation correlation);

    boolean correlatesTo(final MaestroNote note);

    /**
     * Moves to the next subsequent note
     */
    default void next() {}

    /**
     * Whether the note contains content that cannot be sent
     * in a single exchange. The data navigation is done internally
     * whenever the pack method is called.
     * This should repeat until it returns false
     * @return true if there's a subsequent note to be processed or false otherwise
     */
    default boolean hasNext() {
        return false;
    }
}

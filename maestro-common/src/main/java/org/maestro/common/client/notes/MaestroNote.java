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
     * @return the maestro command as an instace of MaestroCommand
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
     * @return
     */
    String toString();
}

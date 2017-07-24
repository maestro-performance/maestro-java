/*
 *  Copyright ${YEAR} ${USER}
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

package net.orpiske.mpt.maestro;

public class MaestroNote {
    private MaestroNoteType noteType;
    private MaestroCommand maestroCommand;

    public MaestroNote() {

    }

    public MaestroNoteType getNoteType() {
        return noteType;
    }

    public void setNoteType(MaestroNoteType noteType) {
        this.noteType = noteType;
    }

    public MaestroCommand getMaestroCommand() {
        return maestroCommand;
    }

    public void setMaestroCommand(MaestroCommand maestroCommand) {
        this.maestroCommand = maestroCommand;
    }




}

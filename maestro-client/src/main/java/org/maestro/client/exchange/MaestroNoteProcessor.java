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

package org.maestro.client.exchange;

import org.maestro.client.notes.*;
import org.maestro.common.client.notes.MaestroNote;

import java.util.List;

public class MaestroNoteProcessor {
    protected void processGetResponse(final GetResponse note) {

    }

    protected void processPingResponse(final PingResponse note) {

    }

    protected void processNotifySuccess(final TestSuccessfulNotification note) {

    }

    protected void processNotifyFail(final TestFailedNotification note) {

    }

    protected void processAgentGeneralResponse(final UserCommand1Response note) {

    }

    protected void processResponse(final MaestroNote note) {
        switch (note.getMaestroCommand()) {
            case MAESTRO_NOTE_PING: {
                processPingResponse((PingResponse) note);
                break;
            }
            case MAESTRO_NOTE_GET: {
                processGetResponse((GetResponse) note);
                break;
            }
            case MAESTRO_NOTE_USER_COMMAND_1: {
                processAgentGeneralResponse((UserCommand1Response) note);
                break;
            }
        }
    }

    protected void processRequest(final MaestroNote note) {
        // NO-OP
    }

    protected void processNotification(final MaestroNote note) {
        switch (note.getMaestroCommand()) {
            case MAESTRO_NOTE_NOTIFY_FAIL: {
                processNotifyFail((TestFailedNotification) note);
                break;
            }
            case MAESTRO_NOTE_NOTIFY_SUCCESS: {
                processNotifySuccess((TestSuccessfulNotification) note);
                break;
            }
        }
    }

    public void process(final List<MaestroNote> notes) {
        for (MaestroNote note : notes) {
            switch (note.getNoteType()) {
                case MAESTRO_TYPE_RESPONSE: {
                    processResponse(note);

                    break;
                }
                case MAESTRO_TYPE_REQUEST: {
                    processRequest(note);

                    break;
                }
                case MAESTRO_TYPE_NOTIFICATION: {
                    processNotification(note);

                    break;
                }
            }
        }
    }
}

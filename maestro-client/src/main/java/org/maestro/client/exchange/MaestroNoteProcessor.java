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
    protected boolean processGetResponse(final GetResponse note) {
        return true;
    }

    protected boolean processPingResponse(final PingResponse note) {
        return true;
    }

    protected boolean processNotifySuccess(final TestSuccessfulNotification note) {
        return true;
    }

    protected boolean processNotifyFail(final TestFailedNotification note) {
        return true;
    }

    protected boolean processAgentGeneralResponse(final UserCommand1Response note) {
        return true;
    }

    protected boolean processResponse(final MaestroNote note) {
        boolean ret = false;
        switch (note.getMaestroCommand()) {
            case MAESTRO_NOTE_PING: {
                ret = processPingResponse((PingResponse) note);
                break;
            }
            case MAESTRO_NOTE_GET: {
                ret = processGetResponse((GetResponse) note);
                break;
            }
            case MAESTRO_NOTE_USER_COMMAND_1: {
                ret = processAgentGeneralResponse((UserCommand1Response) note);
                break;
            }
        }

        return ret;
    }

    protected boolean processRequest(final MaestroNote note) {
        return true;
    }

    protected boolean processNotification(final MaestroNote note) {
        boolean ret = false;
        switch (note.getMaestroCommand()) {
            case MAESTRO_NOTE_NOTIFY_FAIL: {
                ret = processNotifyFail((TestFailedNotification) note);
                break;
            }
            case MAESTRO_NOTE_NOTIFY_SUCCESS: {
                ret = processNotifySuccess((TestSuccessfulNotification) note);
                break;
            }
        }

        return ret;
    }

    public MaestroProcessedInfo process(final List<MaestroNote> notes) {
        MaestroProcessedInfo ret = new MaestroProcessedInfo(notes.size());

        for (MaestroNote note : notes) {
            switch (note.getNoteType()) {
                case MAESTRO_TYPE_RESPONSE: {
                    if (processResponse(note)) {
                        ret.incrementResponseCount();
                    }

                    break;
                }
                case MAESTRO_TYPE_REQUEST: {
                    if (processRequest(note)) {
                        ret.incrementRequestCount();
                    }

                    break;
                }
                case MAESTRO_TYPE_NOTIFICATION: {
                    if (processNotification(note)) {
                        ret.incrementNotificationCount();
                    }

                    break;
                }
            }
        }

        return ret;
    }
}

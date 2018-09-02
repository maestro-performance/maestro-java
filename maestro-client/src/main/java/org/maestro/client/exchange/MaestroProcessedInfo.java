/*
 * Copyright 2018 Otavio Rodolfo Piske
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.client.exchange;

public class MaestroProcessedInfo {
    private final int noteCount;
    private int responseCount = 0;
    private int notificationCount = 0;
    private int requestCount = 0;

    public MaestroProcessedInfo(int noteCount) {
        this.noteCount = noteCount;
    }

    public int getNoteCount() {
        return noteCount;
    }
    public int getResponseCount() {
        return responseCount;
    }

    public void incrementResponseCount() {
        responseCount++;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void incrementNotificationCount() {
        notificationCount++;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void incrementRequestCount() {
        requestCount++;
    }
}

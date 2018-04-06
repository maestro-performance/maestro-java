/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
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

package org.maestro.common.client;

/**
 * Provides an interface that can be used by peers, workers, etc to publish
 * safe maestro responses and notifications
 */
@SuppressWarnings("unused")
public interface MaestroReceiver {

    /**
     * Publishes a ping response that takes into account a giver number of
     * elapsed seconds/microseconds
     * @param sec Epoch seconds
     * @param uSec Microseconds within the second
     */
    void pingResponse(long sec, long uSec);

    /**
     * Publishes a OK reply in the maestro broker
     */
    void replyOk();

    /**
     * Publishes an internal error reply in the Maestro broker
     */
    void replyInternalError();

    /**
     * Publishes a test success notification message in the broker
     * @param message payload message
     */
    void notifySuccess(final String message);

    /**
     * Publishes a test failure notification message in the broker
     * @param message payload message
     */
    void notifyFailure(final String message);

    /**
     * Publishes an abnormal disconnect notification message in the broker
     */
    void abnormalDisconnect();
}

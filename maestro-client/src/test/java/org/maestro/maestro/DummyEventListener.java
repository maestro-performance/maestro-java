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

package org.maestro.maestro;

import org.maestro.client.notes.*;

class DummyEventListener implements MaestroEventListener, MaestroReceiverEventListener, MaestroInspectorEventListener, MaestroAgentEventListener, MaestroSenderEventListener {
    public boolean handled = false;

    public boolean isHandled() {
        return handled;
    }

    @Override
    public void handle(PingRequest note) {
        handled = true;
    }

    @Override
    public void handle(StatsRequest note) {
        handled = true;
    }

    @Override
    public void handle(Halt note) {
        handled = true;
    }

    @Override
    public void handle(SetRequest note) {
        handled = true;
    }

    @Override
    public void handle(TestFailedNotification note) {
        handled = true;
    }

    @Override
    public void handle(TestSuccessfulNotification note) {
        handled = true;
    }

    @Override
    public void handle(AbnormalDisconnect note) {
        handled = true;
    }

    @Override
    public void handle(GetRequest note) {
        handled = true;
    }

    @Override
    public void handle(LogRequest note) {
        handled = true;
    }

    @Override
    public void handle(DrainCompleteNotification note) {
        handled = true;
    }

    @Override
    public void handle(StartWorker note) {
        handled = true;
    }

    @Override
    public void handle(StopWorker note) {
        handled = true;
    }

    @Override
    public void handle(RoleAssign note) {
        handled = true;
    }

    @Override
    public void handle(RoleUnassign note) {
        handled = true;
    }

    @Override
    public void handle(StartTestRequest note) {
        handled = true;
    }

    @Override
    public void handle(StopTestRequest note) {
        handled = true;
    }

    @Override
    public void handle(TestStartedNotification note) {
        handled = true;
    }

    @Override
    public void handle(DrainRequest note) {
        handled = true;
    }

    @Override
    public void handle(StartAgent note) {
        handled = true;
    }

    @Override
    public void handle(StopAgent note) {
        handled = true;
    }

    @Override
    public void handle(UserCommand1Request note) {
        handled = true;
    }

    @Override
    public void handle(AgentSourceRequest note) {
        handled = true;
    }

    @Override
    public void handle(StartInspector note) {
        handled = true;
    }

    @Override
    public void handle(StopInspector note) {
        handled = true;
    }
}

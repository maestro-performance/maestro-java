/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.maestro.client.notes;

/**
 * Visitor that handles {@link MaestroEvent} instances.
 */
@SuppressWarnings("unused")
public interface MaestroEventListener {

    void handle(PingRequest note);

    void handle(StatsRequest note);

    void handle(Halt note);

    void handle(SetRequest note);

    void handle(TestFailedNotification note);

    void handle(TestSuccessfulNotification note);

    void handle(AbnormalDisconnect note);

    void handle(GetRequest note);

    void handle(LogRequest note);

    void handle(DrainCompleteNotification note);

    void handle(GroupJoinRequest note);

    void handle(GroupLeaveRequest note);

    void handle(StartWorker note);

    void handle(StopWorker note);

    void handle(RoleAssign note);

    void handle(RoleUnassign note);

    void handle(StartTestRequest note);

    void handle(StartTestNotification note);

    void handle(StopTestRequest note);
}

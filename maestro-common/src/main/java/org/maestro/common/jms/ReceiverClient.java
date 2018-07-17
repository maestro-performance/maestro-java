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

package org.maestro.common.jms;

public interface ReceiverClient extends Client {
    static long noMessagePayload() {
        return Long.MIN_VALUE;
    }

    /**
     * Returns the epoch micros of the current received message or {@link #noMessagePayload()} if isn't received any message.
     * @param acknowledge the number of messages to receive before sending the ACK (when applicable)
     * @return the epoch micros of the current received message or {@link #noMessagePayload()} if isn't received any message.
     * @throws Exception client specific exceptions
     */
    long receiveMessages(int acknowledge) throws Exception;
}

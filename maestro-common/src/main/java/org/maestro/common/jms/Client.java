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

/**
 * A generic interface for implementing any kind of Maestro client
 */
public interface Client {

    /**
     * Start the client
     * @throws Exception implementation specific exception
     */
    void start() throws Exception;

    /**
     * Stops the client
     */
    void stop();


    /**
     * Set the target URL
     * @param url the target URL
     */
    void setUrl(String url);


    /**
     * Set the client number
     * @param number the client number
     */
    void setNumber(int number);
}

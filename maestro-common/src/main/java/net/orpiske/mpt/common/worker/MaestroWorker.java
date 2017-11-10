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

package net.orpiske.mpt.common.worker;

public interface MaestroWorker {


    void setBroker(final String url);
    void setDuration(final String duration);
    void setLogLevel(final String logLevel);
    void setParallelCount(final String parallelCount);
    void setMessageSize(final String messageSize);
    void setThrottle(final String value);
    void setRate(final String rate);


    void start();
    void stop();
    void halt();
    Stats stats();

}

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

package org.maestro.worker.common;

import org.maestro.client.notes.LogRequest;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.worker.common.ds.MaestroDataServer;

/**
 * A worker manager that is void of workers. It is used for running a standalone data server.
 */
public class VoidWorkerManager extends MaestroWorkerManager {

    public VoidWorkerManager(final String maestroURL, final String host,
                             final MaestroDataServer dataServer) throws MaestroException
    {
        super(maestroURL, host, dataServer);
    }

    @Override
    public void handle(final LogRequest note) {
        // NO-OP
    }
}

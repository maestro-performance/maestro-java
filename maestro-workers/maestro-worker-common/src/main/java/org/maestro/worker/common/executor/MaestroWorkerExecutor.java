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

package org.maestro.worker.common.executor;

import java.io.File;

import org.maestro.client.exchange.ConsumerEndpoint;
import org.maestro.client.exchange.peer.AbstractMaestroPeer;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.MaestroEvent;
import org.maestro.client.notes.MaestroEventListener;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.exchange.AbstractMaestroExecutor;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.worker.common.ConcurrentWorkerManager;

public class MaestroWorkerExecutor extends AbstractMaestroExecutor {

    @Deprecated
    public MaestroWorkerExecutor(final AbstractMaestroPeer<MaestroEvent<MaestroEventListener>> maestroPeer) {
        super(maestroPeer);
    }

    public MaestroWorkerExecutor(MaestroClient maestroClient, ConsumerEndpoint consumerEndpoint, final PeerInfo peerInfo, final File logDir) throws MaestroException {
        super(new ConcurrentWorkerManager(maestroClient, consumerEndpoint, peerInfo, logDir));
    }
}

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

package org.maestro.worker.tests.support.runner;

import net.orpiske.jms.test.runner.JmsTestRunner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.maestro.client.Maestro;
import org.maestro.worker.tests.support.annotations.MaestroPeer;
import org.maestro.worker.tests.support.annotations.ReceivingPeer;
import org.maestro.worker.tests.support.annotations.SendingPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * A JUnit runner that can inject peers into the test classes
 */
public class WorkerTestRunner extends JmsTestRunner {
    private static final Logger logger = LoggerFactory.getLogger(WorkerTestRunner.class);

    private final List<MiniPeer> peers = new LinkedList<>();
    private final List<Maestro> maestroClientPeers = new LinkedList<>();

    public WorkerTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    private void resetMiniPeers() {
        for (MiniPeer miniPeer : peers) {
            miniPeer.stop();
        }

        peers.clear();
    }

    @Override
    protected Object createTest() throws Exception {
        Object o = super.createTest();

        resetMiniPeers();

        // TODO: improve this
        for (Field f : o.getClass().getDeclaredFields()) {
            SendingPeer sendingPeer = f.getAnnotation(SendingPeer.class);

            if (sendingPeer != null) {
                injectSendingPeer(o, f, sendingPeer);
            } else {
                ReceivingPeer receivingPeer = f.getAnnotation(ReceivingPeer.class);

                if (receivingPeer != null) {
                    injectReceivingPeer(o, f, receivingPeer);
                }
                else {
                    MaestroPeer maestroPeer = f.getAnnotation(MaestroPeer.class);

                    if (maestroPeer != null) {
                        injectMaestroPeer(o, f, maestroPeer);
                    }
                }
            }
        }

        return o;
    }


    // TODO: safer type checking for the inject* methods
    private void injectSendingPeer(Object o, Field f, SendingPeer peer) throws Exception {
        logger.info("Injecting a sending peer into the test object");

        MiniPeer miniPeer = new MiniPeer(peer.worker(), peer.maestroUrl(), peer.role(), peer.host());

        peers.add(miniPeer);

        f.setAccessible(true);
        f.set(o, miniPeer);
        f.setAccessible(false);
    }

    private void injectReceivingPeer(Object o, Field f, ReceivingPeer peer) throws Exception {
        logger.info("Injecting a receiving peer into the test object");

        MiniPeer miniPeer = new MiniPeer(peer.worker(), peer.maestroUrl(), peer.role(), peer.host());

        peers.add(miniPeer);

        f.setAccessible(true);
        f.set(o, miniPeer);
        f.setAccessible(false);
    }

    private void injectMaestroPeer(Object o, Field f, MaestroPeer peer) throws Exception {
        logger.info("Injecting a receiving peer into the test object");

        Maestro maestro = new Maestro(peer.maestroUrl());

        maestroClientPeers.add(maestro);

        f.setAccessible(true);
        f.set(o, maestro);
        f.setAccessible(false);
    }


    @Override
    public void run(RunNotifier notifier) {
        logger.info("Starting the JMS provider");

        super.run(notifier);

        for (MiniPeer miniPeer : peers) {
            miniPeer.stop();
        }
    }


}

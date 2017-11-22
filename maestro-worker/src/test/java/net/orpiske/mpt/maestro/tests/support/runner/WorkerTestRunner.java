package net.orpiske.mpt.maestro.tests.support.runner;

import net.orpiske.jms.test.runner.JmsTestRunner;
import net.orpiske.mpt.maestro.tests.support.annotations.ReceivingPeer;
import net.orpiske.mpt.maestro.tests.support.annotations.SendingPeer;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
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

    private List<MiniPeer> peers = new LinkedList<MiniPeer>();



    public WorkerTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Object createTest() throws Exception {
        Object o = super.createTest();

        for (Field f : o.getClass().getDeclaredFields()) {
            SendingPeer sendingPeer = f.getAnnotation(SendingPeer.class);

            if (sendingPeer != null) {
                injectSendingPeer(o, f, sendingPeer);
            } else {
                ReceivingPeer receivingPeer = f.getAnnotation(ReceivingPeer.class);

                if (receivingPeer != null) {
                    injectReceivingPeer(o, f, receivingPeer);
                }
            }
        }


        return o;
    }


    private void injectSendingPeer(Object o, Field f, SendingPeer peer) throws IllegalAccessException, ClassNotFoundException {
        logger.info("Injecting a sending peer into the test object");

        MiniPeer miniPeer = new MiniPeer(peer.worker(), peer.maestroUrl(), peer.role(), peer.host());

        peers.add(miniPeer);

        f.setAccessible(true);
        f.set(o, miniPeer);
        f.setAccessible(false);
    }

    private void injectReceivingPeer(Object o, Field f, ReceivingPeer peer) throws IllegalAccessException, ClassNotFoundException {
        logger.info("Injecting a receiving peer into the test object");

        MiniPeer miniPeer = new MiniPeer(peer.worker(), peer.maestroUrl(), peer.role(), peer.host());

        peers.add(miniPeer);

        f.setAccessible(true);
        f.set(o, miniPeer);
        f.setAccessible(false);
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);

        for (MiniPeer miniPeer : peers) {
            miniPeer.stop();
        }
    }


}

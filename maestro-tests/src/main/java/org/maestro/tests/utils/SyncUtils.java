package org.maestro.tests.utils;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.content.MessageSize;

/**
 * Generic utilities to help synchronize the peers
 */
public class SyncUtils {

    private SyncUtils() {}

    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    /**
     * Checks whether the flush wait timer should be reset
     * @param parallelCount the test parallel count
     * @param messageSize the message size specification (can be variable message size spec)
     * @return true if it requires a reset or false otherwise
     */
    public static boolean flushWaitResetCheck(int parallelCount, String messageSize) {
        return flushWaitResetCheck(parallelCount, MessageSize.toSizeFromSpec(messageSize));
    }


    /**
     * Checks whether the flush wait timer should be reset
     * @param parallelCount the test parallel count
     * @param messageSize the message size in bytes
     * @return true if it requires a reset or false otherwise
     */
    public static boolean flushWaitResetCheck(int parallelCount, int messageSize) {
        /*
         * At which point it requires a reset of the flush wait timer regardless of the message size
         */
        final int hardCeilingPc = config.getInt("flush.wait.hard.ceiling.parallel.count", 100);
        if (parallelCount > hardCeilingPc) {
            return true;
        }

        /*
         * At which point it requires a reset of the flush wait timer regardless of the parallel count
         */
        final int hardCeilingMessageSize = config.getInt("flush.wait.hard.ceiling.message.size", 10240);
        if (messageSize > hardCeilingMessageSize) {
            return true;
        }

        /*
         * Requires a reset of the flush wait time depending on how large the messages are
         */
        final int flushWaitSoftCeilingPc = config.getInt("flush.wait.soft.ceiling.parallel.count", 30);
        if (parallelCount > flushWaitSoftCeilingPc) {
            final int softCeilingMessageSize = config.getInt("flush.wait.soft.ceiling.message.size", 1024);
            if (messageSize > softCeilingMessageSize) {
                return true;
            }
        }

        return false;
    }
}

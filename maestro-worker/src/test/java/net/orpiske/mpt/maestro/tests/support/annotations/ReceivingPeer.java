package net.orpiske.mpt.maestro.tests.support.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This test annotation can be used to distinguish a receiving peer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ReceivingPeer {

    /**
     * The maestro URL
     * @return
     */
    String maestroUrl() default "mqtt://localhost:1883";

    /**
     * Peer role
     * @return
     */
    String role() default "receiver";

    /**
     * Test hostname
     * @return
     */
    String host() default "localhost";


    /**
     * The worker class
     * @return
     */
    String worker() default "net.orpiske.mpt.maestro.worker.jms.JMSReceiverWorker";
}

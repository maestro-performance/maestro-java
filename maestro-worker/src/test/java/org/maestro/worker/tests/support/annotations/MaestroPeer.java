package org.maestro.worker.tests.support.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This test annotation can be used to describe a Maestro client peer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface MaestroPeer {
    /**
     * The maestro URL
     * @return the maestro URL
     */
    String maestroUrl() default "mqtt://localhost:1883";
}

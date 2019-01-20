package org.maestro.common;

import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PropertyUtilsTest {


    /**
     * Asserts that it can load well formed properties files
     */
    @Test
    public void testLoadProperties() {
        final Map<String, Object> context = new HashMap<>();
        final String path = this.getClass().getResource("test-with-query-params.properties").getPath();
        final File propertiesFile = new File(path);

        PropertyUtils.loadProperties(propertiesFile, context);

        assertEquals("10", context.get("fcl"));
        assertEquals("testApi", context.get("apiName"));
        assertEquals("amqp://some/queue?param1=value1&param2=value2&param3=value3", context.get("brokerUri"));
        assertEquals("0.9", context.get("apiVersion"));
        assertEquals("30", context.get("limitDestinations"));
        assertEquals("100", context.get("duration"));
        assertEquals("256", context.get("messageSize"));
        assertEquals("3", context.get("parallelCount"));
        assertEquals("true", context.get("variableSize"));

        assertEquals("value1", context.get("param1"));
        assertEquals("value2", context.get("param2"));
        assertEquals("value3", context.get("param3"));
    }


    /**
     * Asserts that is does not fail if the file does not exist
     */
    @Test
    public void testNotFailOnNonExistentFile() {
        final Map<String, Object> context = new HashMap<>();
        final File propertiesFile = new File("does not exist");

        PropertyUtils.loadProperties(propertiesFile, context);
    }


    /**
     * Asserts that it sets the encrypted to true when loading AMPQS URLs
     */
    @Test
    public void testLoadPropertiesSetEncrypted() {
        final Map<String, Object> context = new HashMap<>();
        final String path = this.getClass().getResource("test-with-query-params-encrypted-url.properties").getPath();
        final File propertiesFile = new File(path);

        PropertyUtils.loadProperties(propertiesFile, context);

        assertEquals("10", context.get("fcl"));
        assertEquals("testApi", context.get("apiName"));
        assertEquals("amqps://some/queue?param1=value1&param2=value2&param3=value3", context.get("brokerUri"));
        assertEquals("0.9", context.get("apiVersion"));
        assertEquals("30", context.get("limitDestinations"));
        assertEquals("100", context.get("duration"));
        assertEquals("256", context.get("messageSize"));
        assertEquals("3", context.get("parallelCount"));
        assertEquals("true", context.get("variableSize"));

        assertEquals("value1", context.get("param1"));
        assertEquals("value2", context.get("param2"));
        assertEquals("value3", context.get("param3"));


        // PropertyUtils must set encrypted to true if the URL is amqps
        assertEquals("true", context.get("encrypted"));
    }


    /**
     * Asserts that it can load the test parameters even if the test URL is invalid. Bad behavior, but prevents
     * some errors when loading data from older Maestro versions
     */
    @Test
    public void testLoadPropertiesInvalidUrl() {
        final Map<String, Object> context = new HashMap<>();
        final String path = this.getClass().getResource("test-with-invalid-url.properties").getPath();
        final File propertiesFile = new File(path);

        PropertyUtils.loadProperties(propertiesFile, context);

        assertEquals("10", context.get("fcl"));
        assertEquals("testApi", context.get("apiName"));
        assertEquals("0.9", context.get("apiVersion"));
        assertEquals("30", context.get("limitDestinations"));
        assertEquals("100", context.get("duration"));
        assertEquals("256", context.get("messageSize"));
        assertEquals("3", context.get("parallelCount"));
        assertEquals("true", context.get("variableSize"));
    }
}
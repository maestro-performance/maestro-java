/**
 Copyright 2014 Otavio Rodolfo Piske

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package net.orpiske.mpt.common;

import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;

/**
 * Utility class to configure the logger
 */
public class LogConfigurator {

    /**
     * Restricted constructor
     */
    private LogConfigurator() {}


    private static void configureCommon(Properties properties) {
        properties.setProperty("log4j.appender.stdout",
                "org.apache.log4j.ConsoleAppender");

        properties.setProperty("log4j.appender.stdout.Target",
                "System.out");
        properties.setProperty("log4j.appender.stdout.layout",
                "org.apache.log4j.PatternLayout");
        properties.setProperty(
                "log4j.appender.stdout.layout.ConversionPattern",
                "%C.%M:%L [%p] %m%n");
    }

    private static void configureTrace(Properties properties) {
        properties.setProperty("log4j.rootLogger", "TRACE, stdout");
    }

    private static void configureDebug(Properties properties) {
        properties.setProperty("log4j.rootLogger", "DEBUG, stdout");
    }

    private static void configureVerbose(Properties properties) {
        properties.setProperty("log4j.rootLogger", "INFO, stdout");
    }

    private static void configureSilent(Properties properties) {
        properties.setProperty("log4j.rootLogger", "WARN, stdout");
    }


    /**
     * Configure the output to be at trace level
     */
    public static void trace() {
        Properties properties = new Properties();

        configureCommon(properties);
        configureTrace(properties);

        PropertyConfigurator.configure(properties);
    }


    /**
     * Configure the output to be at debug level
     */
    public static void debug() {
        Properties properties = new Properties();

        configureCommon(properties);
        configureDebug(properties);

        PropertyConfigurator.configure(properties);
    }


    /**
     * Configure the output to be at verbose (info) level
     */
    public static void verbose() {
        Properties properties = new Properties();

        configureCommon(properties);
        configureVerbose(properties);

        PropertyConfigurator.configure(properties);
    }


    /**
     * Configure the output to be as silent as possible
     */
    public static void silent() {
        Properties properties = new Properties();

        configureCommon(properties);
        configureSilent(properties);

        PropertyConfigurator.configure(properties);


    }
}

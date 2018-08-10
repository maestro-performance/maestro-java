/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.maestro.worker.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

final class JMSResourceUtil {
    private static final Logger logger = LoggerFactory.getLogger(JMSResourceUtil.class);

    private JMSResourceUtil() {

    }

    @SuppressWarnings("UnusedReturnValue")
    public static Throwable capturingClose(MessageProducer closeable) {
        logger.debug("Closing the producer ");

        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable t) {
                logger.warn("Error closing the producer: {}", t.getMessage(), t);
                return t;
            }
        }
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Throwable capturingClose(Session closeable) {
        logger.debug("Closing the session ");

        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable t) {
                logger.warn("Error closing the session: {}", t.getMessage(), t);
                return t;
            }
        }
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Throwable capturingClose(MessageConsumer closeable) {
        logger.debug("Closing the consumer");

        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable t) {
                logger.warn("Error closing the consumer: {}", t.getMessage(), t);
                return t;
            }
        }
        return null;
    }

    public static Throwable capturingClose(Connection closeable) {
        logger.debug("Closing the connection");

        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable t) {
                logger.warn("Error closing the connection: {}", t.getMessage(), t);
                return t;
            }
        }
        return null;
    }
}

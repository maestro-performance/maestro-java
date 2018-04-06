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

package org.maestro.inspector.base;

import org.maestro.common.inspector.MaestroInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InspectorContainer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(InspectorContainer.class);
    private final MaestroInspector inspector;

    public InspectorContainer(final MaestroInspector inspector) {
        this.inspector = inspector;
    }


    public void run() {
        try {
            inspector.start();
        }
        catch (InterruptedException e) {
            try {
                inspector.stop();
            } catch (Exception e1) {
                logger.error("Error stopping the inspector: {}", e.getMessage(), e);
            }
        }
        catch (Exception e) {
            logger.error("Error running the inspector: {}", e.getMessage(), e);
        }
    }
}

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

package org.maestro.tests.utils;

import org.maestro.client.notes.TestFailedNotification;
import org.maestro.common.ErrorUtils;

/**
 * Utility code for handling ignored errors
 */
public class IgnoredErrorUtils {

    /**
     * Wrapper for checking whether an error is ignored
     * @param testFailedNotification
     * @return
     */
    public static boolean isIgnored(final TestFailedNotification testFailedNotification) {
        return ErrorUtils.isIgnored(testFailedNotification.getPeerInfo().prettyName(), testFailedNotification.getMessage());
    }
}

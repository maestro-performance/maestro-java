/*
 * Copyright 2018 Otavio Rodolfo Piske
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
package org.maestro.common;

import java.io.Closeable;
import java.io.IOException;

public class LangUtils {

    private LangUtils() {}

    /**
     * Closes a closeable resource ignoring exceptions
     * @param object the closeable object to close
     * @param <T> A subtype of closeable
     */
    public static <T extends Closeable> void closeQuietly(T object) {
        if (object != null) {
            try {
                object.close();
            } catch (IOException e) {

            }
        }
    }
}

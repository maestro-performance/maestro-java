/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.common.content;

import java.util.Random;

/**
 * Random content filler
 */
public class ContentFiller {
    private static final String dict = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Fills a buffer with random data. The buffer must be empty, otherwise data will be
     * appended
     * @param buffer the buffer to fill
     * @param capacity the buffer capacity
     */
    public static void randomFill(StringBuffer buffer, int capacity) {
        Random r =  new Random();

        int limit = dict.length() - 1;
        for (int i = 0; i < capacity; i++) {
            int pos = r.nextInt(limit);

            buffer.append(dict.charAt(pos));
        }
    }
}

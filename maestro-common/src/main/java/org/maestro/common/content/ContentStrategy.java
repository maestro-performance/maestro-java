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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The message content strategy for sending data
 */
public interface ContentStrategy {

    /**
     * The endianness of any {@link #prepareContent()}'s {@link ByteBuffer}.
     */
    ByteOrder CONTENT_ENDIANNESS = ByteOrder.LITTLE_ENDIAN;

    /**
     * Gets the message content to send.
     * <p>
     * The returned {@link ByteBuffer} has {@link ByteBuffer#hasArray()} {@code true} and
     * {@link ByteBuffer#order()} equals to {@link #CONTENT_ENDIANNESS}.<br>
     * The {@link ByteBuffer#position()} and {@link ByteBuffer#limit()} delimit the content available to be sent.<br>
     * Its content can be changed but cannot be used less then the provided {@link ByteBuffer#remaining()} size.
     *
     * @return the message content to send
     */
    ByteBuffer prepareContent();
}

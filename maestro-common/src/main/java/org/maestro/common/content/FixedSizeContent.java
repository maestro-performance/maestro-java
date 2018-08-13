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

/**
 * A fixed-sized bytes message content
 */
final class FixedSizeContent implements ContentStrategy {
    private ByteBuffer buffer = null;

    FixedSizeContent(int size) {
        setSize(size);
    }

    FixedSizeContent(final String sizeSpec) {
        setSize(sizeSpec);
    }

    private void setSize(int size) {
        this.buffer = ByteBuffer.allocate(size).order(CONTENT_ENDIANNESS);
        for (int i = 0; i < size; i++) {
            this.buffer.put(i, (byte) i);
        }

        buffer.clear();
        buffer.limit(size);
    }


    private void setSize(final String sizeSpec) {
        setSize(MessageSize.toSizeFromSpec(sizeSpec));
    }

    /*
     * @see ContentStrategy#prepareContent()
     */
    @Override
    public ByteBuffer prepareContent() {
        return buffer;
    }
}

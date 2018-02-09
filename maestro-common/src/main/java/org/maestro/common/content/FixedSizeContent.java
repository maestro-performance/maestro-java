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
    private int size = 0;
    private ByteBuffer buffer = null;

    /*
     * @see ContentStrategy#setSize(int)
     */
    @Override
    public void setSize(int size) {
        this.size = size;
        this.buffer = ByteBuffer.allocate(size).order(CONTENT_ENDIANNESS);
        for (int i = 0; i < size; i++) {
            this.buffer.put(i, (byte) i);
        }
    }

    /*
     * @see ContentStrategy#setSize(String)
     */
    @Override
    public void setSize(String sizeSpec) {
        setSize(MessageSize.toSizeFromSpec(sizeSpec));
    }

    /*
     * @see ContentStrategy#prepareContent()
     */
    @Override
    public ByteBuffer prepareContent() {
        if (buffer == null) {
            assert size == 0;
            return buffer;
        }
        buffer.clear();
        buffer.limit(this.size);
        return buffer;
    }
}

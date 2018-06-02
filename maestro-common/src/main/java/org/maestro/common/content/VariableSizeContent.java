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
import java.util.concurrent.ThreadLocalRandom;

/**
 * A variable-size bytes message content
 */
final class VariableSizeContent implements ContentStrategy {
    private int lowerLimitInclusive = 0;
    private int upperLimitExclusive = 1;
    private ByteBuffer buffer = null;

    VariableSizeContent(int size) {
        setSize(size);
    }

    VariableSizeContent(final String sizeSpec) {
        setSize(sizeSpec);
    }

    public int minSize() {
        return this.lowerLimitInclusive;
    }

    public int maxSize() {
        return this.upperLimitExclusive - 1;
    }

    private void setSize(int size) {
        final int lowerBoundInclusive;
        final int upperBoundExclusive;

        if (size >= 100) {
            final int bound = ((size / 100) * 5);
            lowerBoundInclusive = size - bound + 1;
            upperBoundExclusive = size + bound + 1;
        } else {
            lowerBoundInclusive = size;
            upperBoundExclusive = size + 2;
        }
        //TODO document the minimum expected size of the content
        if (lowerBoundInclusive < Long.BYTES) {
            throw new IllegalStateException("The size is too small: please configure an bigger one");
        }
        this.lowerLimitInclusive = lowerBoundInclusive;
        this.upperLimitExclusive = upperBoundExclusive;
        final int requiredCapacity = upperBoundExclusive - 1;
        this.buffer = ByteBuffer.allocate(requiredCapacity).order(CONTENT_ENDIANNESS);
        for (int i = 0; i < requiredCapacity; i++) {
            this.buffer.put(i, (byte) i);
        }
    }


    private void setSize(final String sizeSpec) {
        setSize(MessageSize.toSizeFromSpec(sizeSpec));
    }

    /*
     * @see ContentStrategy#prepareContent()
     */
    @Override
    public ByteBuffer prepareContent() {
        final int currentLimit = ThreadLocalRandom.current().nextInt(this.lowerLimitInclusive, this.upperLimitExclusive);
        buffer.clear();
        buffer.limit(currentLimit);
        return buffer;
    }
}

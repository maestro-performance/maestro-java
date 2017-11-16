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

package net.orpiske.mpt.common.content;

import java.util.Random;

/**
 * A variable-size String message content
 */
public class VariableSizeContent implements ContentStrategy {
    private Random random = new Random();
    private StringBuffer buffer;
    private int limit = 0;


    /*
     * @see ContentStrategy#setSize(int)
     */
    @Override
    public void setSize(int size) {
        int lowerBound;
        int upperBound;

        if (size >= 100) {
            int bound = ((size / 100) * 5);

            lowerBound = size - bound;
            upperBound = size + bound;

            limit = upperBound - lowerBound;
        } else {
            lowerBound = size - 1;
            upperBound = size + 1;
            limit = upperBound - lowerBound;
        }

        buffer = new StringBuffer(upperBound);

        ContentFiller.randomFill(buffer, upperBound);
    }


    /*
     * @see ContentStrategy#getContent()
     */
    @Override
    public String getContent() {
        return buffer.substring(random.nextInt(limit));
    }
}

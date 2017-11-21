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

/**
 * A fixed-sized string message content
 */
public class FixedSizeContent implements ContentStrategy {
    private static String content;
    private StringBuffer buffer;

    /*
     * @see ContentStrategy#setSize(int)
     */
    @Override
    public void setSize(int size) {
        buffer = new StringBuffer();

        buffer = new StringBuffer(size);

        ContentFiller.randomFill(buffer, size);

        content = buffer.toString();
    }

    /*
     * @see ContentStrategy#setSize(String)
     */
    @Override
    public void setSize(String sizeSpec) {
        setSize(Integer.parseInt(sizeSpec));
    }

    /*
     * @see ContentStrategy#getContent()
     */
    @Override
    public String getContent() {
        return content;
    }
}

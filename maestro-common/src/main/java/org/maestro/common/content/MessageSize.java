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

/**
 * Builds a message/content size specification String. The size specification is a specially-formatted string that
 * determines the content size and other aspects of it (ie.: like whether it's variable or not). The format of the
 * string is either "~num" (for variable size message content) or "num" (for fixed size message content), such as "~256"
 * or "256"
 */
public class MessageSize {
    private MessageSize() {}

    /**
     * Formats a spec string for variable message content for the given size
     * @param value the message size
     * @return A size-spec string for the given size
     */
    public static String variable(long value) {
        return "~" + Long.toString(value);
    }

    /**
     * Formats a spec string for fixed message content for the given size
     * @param value the message size
     * @return A fixed-size-spec string for the given size
     */
    public static String fixed(long value) {
        return Long.toString(value);
    }

    public static boolean isVariable(final String sizeSpec) {
        return sizeSpec.startsWith("~");

    }

    /**
     * Given a content/message size specification string, return it's base size
     * @param sizeSpec A message size specification string
     * @return The content size
     */
    public static int toSizeFromSpec(final String sizeSpec) {
        if (isVariable(sizeSpec)) {
            return Integer.parseInt(sizeSpec.replace("~", ""));
        }

        return Integer.parseInt(sizeSpec);
    }
}

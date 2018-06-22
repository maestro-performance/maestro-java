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

package org.maestro.common.content;

/**
 * Creates the appropriate {@link ContentStrategy} based on a size specification
 */
public class ContentStrategyFactory {

    private ContentStrategyFactory() {}

    /**
     * Parse a content size specification string and creates the respective ContentStrategy.
     * @param sizeSpec The size specification string
     * @return A ContentStrategy instance for the size spec string
     */
    public static ContentStrategy parse(final String sizeSpec) {
        ContentStrategy ret;

        if (sizeSpec.startsWith("~")) {
            ret = new VariableSizeContent(sizeSpec);
        }
        else {
            ret = new FixedSizeContent(sizeSpec);
        }

        return ret;
    }
}

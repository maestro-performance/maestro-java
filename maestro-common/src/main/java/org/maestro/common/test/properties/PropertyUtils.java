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
 *
 */

package org.maestro.common.test.properties;

import org.maestro.common.StringUtils;

public class PropertyUtils {

    public static String getPropertyName(final String propertyName, final String methodPropertyName,
                                         final boolean join) {

        String newPropertyName;

        if (join) {
            newPropertyName = propertyName + StringUtils.capitalize(methodPropertyName);
        }
        else {
            newPropertyName = methodPropertyName;
        }

        return newPropertyName;
    }

    public static boolean canHandle(Object object) {
        if (Number.class.isAssignableFrom(object.getClass())) {
            return true;
        }

        if (Boolean.class.isAssignableFrom(object.getClass())) {
            return true;
        }

        return object instanceof String;
    }
}

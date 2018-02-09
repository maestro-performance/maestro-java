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

package org.maestro.common;

public class NodeUtils {

    private NodeUtils() {}


    /**
     * Given a peer name of format type@host, returns the type
     * @param name name of the peer
     * @return type of the peer
     */
    public static String getTypeFromName(final String name) {
        return name.split("@")[0];

    }


    /**
     * Given a peer full name of format type@host, returns the name
     * @param name name of the peer
     * @return peer name
     */
    public static String getHostFromName(final String name) {
        return name.split("@")[1];
    }
}

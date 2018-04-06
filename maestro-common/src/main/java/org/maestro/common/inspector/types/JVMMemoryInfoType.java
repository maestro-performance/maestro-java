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

package org.maestro.common.inspector.types;

/**
 * JVM memory information
 */
public interface JVMMemoryInfoType {

    /**
     * The name of the JVM memory area (ie.: heap, tenured, old gen, etc)
     * @return the name of the JVM memory area
     */
    String getMemoryAreaName();

    /**
     * The initial size for the memory area
     * @return the initial number of bytes
     */
    long getInitial();

    /**
     * The committed size for the memory area
     * @return the committed number of bytes
     */
    long getCommitted();

    /**
     * The maximum size for the memory area
     * @return the maximum number of bytes
     */
    long getMax();


    /**
     * Currently used memory for the memory area
     * @return the number of bytes used
     */
    long getUsed();
}

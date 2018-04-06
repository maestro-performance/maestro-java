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

package org.maestro.agent.base;

import java.io.File;

final class ExtensionPoint {
    private final File path;
    private final boolean transientFlag;

    ExtensionPoint(File path, boolean transientFlag) {
        this.path = path;
        this.transientFlag = transientFlag;
    }


    public File getPath() {
        return path;
    }

    public boolean isTransient() {
        return transientFlag;
    }

    @Override
    public String toString() {
        return "ExtensionPoint{" +
                "path=" + path +
                ", transientFlag=" + transientFlag +
                '}';
    }
}

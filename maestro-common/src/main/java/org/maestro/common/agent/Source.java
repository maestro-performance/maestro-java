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

package org.maestro.common.agent;

/**
 * Source URL abstraction for the Agent
 */
public final class Source {
    private final String sourceUrl;
    private final String branch;

    /**
     * Constructor
     * @param sourceUrl the source url (ie.: git://host/path/to/extension-endpoint.git)
     * @param branch branch to use for the source URL
     */
    public Source(String sourceUrl, String branch) {
        this.sourceUrl = sourceUrl;
        this.branch = branch;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getBranch() {
        return branch;
    }
}

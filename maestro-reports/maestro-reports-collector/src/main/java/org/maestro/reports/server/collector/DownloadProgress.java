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

package org.maestro.reports.server.collector;

import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.server.collector.exceptions.DownloadCountOverflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadProgress {
    private static final Logger logger = LoggerFactory.getLogger(DownloadProgress.class);

    private int downloaded = 0;
    private final int total;

    public DownloadProgress(int total) {
        if (total <= 0) {
            logger.error("Invalid file count value: {}", total);
            throw new MaestroException("The file count should be greater than zero");
        }

        this.total = total;
    }

    public int getDownloaded() {
        return downloaded;
    }

    public int remaining() {
        return total - downloaded;
    }

    public void increment() {
        if (downloaded < total) {
            downloaded++;
        }
        else {
            throw new DownloadCountOverflowException("Download count overflow: already downloaded %d of %d ",
                    downloaded, total);
        }
    }

    public boolean isDone() {
        return downloaded == total;
    }

    public boolean inProgress() {
        return !isDone();
    }
}

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

package org.maestro.reports.downloaders;

import org.maestro.reports.ReportResolver;
import org.maestro.reports.organizer.Organizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PooledDownloaderDecorator implements ReportsDownloader {

    private static final Logger logger = LoggerFactory.getLogger(PooledDownloaderDecorator.class);

    private final ReportsDownloader reportsDownloader;
    private final ExecutorService executorService;
    private final List<CompletableFuture<Void>> futures = new LinkedList<>();

    public PooledDownloaderDecorator(ReportsDownloader reportsDownloader) {
        this.reportsDownloader = reportsDownloader;
        AtomicInteger count = new AtomicInteger();

        this.executorService = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors() * 2, 4), r -> {
            Thread thread = new Thread(r, String.format("pooled-downloader-%d", count.incrementAndGet()));
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public Organizer getOrganizer() {
        return reportsDownloader.getOrganizer();
    }

    @Override
    public void addReportResolver(String hostType, ReportResolver reportResolver) {
        reportsDownloader.addReportResolver(hostType, reportResolver);
    }

    @Override
    public void downloadLastSuccessful(String type, String host) {
        futures.add(CompletableFuture.runAsync(() -> reportsDownloader.downloadLastSuccessful(type, host), executorService));
    }

    @Override
    public void downloadLastFailed(String type, String host) {
        futures.add(CompletableFuture.runAsync(() -> reportsDownloader.downloadLastFailed(type, host), executorService));
    }

    @Override
    public void downloadAny(String type, String host, String testNumber) {
        futures.add(CompletableFuture.runAsync(() -> reportsDownloader.downloadAny(type, host, testNumber), executorService));
    }

    @Override
    public void waitForComplete() {
        executorService.shutdown();
        try {
            CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
            all.get();
        } catch (CompletionException | ExecutionException e) {
            logger.error("One or more downloads failed", e);
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            } else {
                throw new RuntimeException(cause);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

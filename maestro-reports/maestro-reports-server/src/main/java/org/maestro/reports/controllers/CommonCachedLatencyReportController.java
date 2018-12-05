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

package org.maestro.reports.controllers;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.plotter.latency.serializer.LatencyDistribution;
import org.maestro.plotter.latency.serializer.SmoothLatencySerializer;
import org.maestro.reports.dto.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

abstract class CommonCachedLatencyReportController<T> extends AbstractReportFileController {
    private static final Logger logger = LoggerFactory.getLogger(CommonCachedLatencyReportController.class);
    private static final String REPORT_FILE_NAME = "receiverd-latency.hdr";

    private static CacheManager cacheManager;
    private static Cache<File, LatencyDistribution> latencyDistributionCache;

    protected CommonCachedLatencyReportController() {
        if (cacheManager == null) {
            synchronized (this) {
                if (cacheManager == null) {
                    CacheConfigurationBuilder<File, LatencyDistribution> config = CacheConfigurationBuilder
                            .newCacheConfigurationBuilder(File.class, LatencyDistribution.class, ResourcePoolsBuilder.heap(30))
                            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(30)));

                    cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                            .withCache("latDistribution",config)
                            .build();

                    cacheManager.init();

                    latencyDistributionCache = cacheManager.getCache("latDistribution", File.class,
                            LatencyDistribution.class);
                }
            }
        }
    }

    final protected void processReports(final Report report, final T latencyDistribution) {
        File file = getReportFile(report, REPORT_FILE_NAME);

        try {
            logger.info("Processing report data for {}", file);

            LatencyDistribution data = latencyDistributionCache.get(file);
            if (data == null) {
                MaestroSerializer<?> serializer = new SmoothLatencySerializer();
                data = (LatencyDistribution) serializer.serialize(file);

                latencyDistributionCache.put(file, data);
            }
            else {
                logger.debug("File {} is cached, no processing required", file);
            }

            setResponseData(latencyDistribution, data);
        } catch (IOException e) {
            logger.error("Unable to process data: {}", e.getMessage(), e);
        }
    }

    abstract void setResponseData(T responseData, LatencyDistribution data);
}

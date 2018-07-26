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

package org.maestro.reports;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.maestro.common.LogConfigurator;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;
import static org.maestro.reports.ReportAggregator.AGGREGATED_REPORT_DIRNAME;

public class ReportAggregatorTest {

    @Rule
    public final TestName name = new TestName();
    private Path tempDirectory;

    @Before
    public void setUp() throws Exception {
        LogConfigurator.silent();
        tempDirectory = Files.createTempDirectory(String.format("%s_%d", name.getMethodName(), System.currentTimeMillis()));
    }

    @After
    public void tearDown() throws Exception {
        LogConfigurator.silent();

        if (tempDirectory != null) {
            Files.walk(tempDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test(timeout = 65000)
    public void testAggregateSenderReports() throws Exception {
        Path root = Paths.get(this.getClass().getResource("/data-ok/sender/success").toURI());
        copyTree(root, tempDirectory);

        ReportAggregator reportAggregator = new ReportAggregator(tempDirectory.toString());
        reportAggregator.aggregate();

        verifyAllReportRoots(tempDirectory, this::testSenderReportDir);
    }

    @Test(timeout = 65000)
    public void testAggregateReceiverReports() throws Exception {
        Path root = Paths.get(this.getClass().getResource("/data-ok/receiver/success").toURI());
        copyTree(root, tempDirectory);

        ReportAggregator reportAggregator = new ReportAggregator(tempDirectory.toString());
        reportAggregator.aggregate();

        verifyAllReportRoots(tempDirectory, this::testReceiverReportDir);
    }

    private void verifyAllReportRoots(Path root, Consumer<Path> test) throws IOException {

        final AtomicInteger reportRootSeen = new AtomicInteger();

        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

                if (dir.equals(root)) {
                    return FileVisitResult.CONTINUE;
                }

                Path aggregateReportNameDir = dir.resolve(AGGREGATED_REPORT_DIRNAME);
                assertTrue(String.format("Aggregate directory %s must exist", aggregateReportNameDir),
                        Files.isDirectory(aggregateReportNameDir));

                test.accept(aggregateReportNameDir);

                reportRootSeen.incrementAndGet();
                return FileVisitResult.SKIP_SUBTREE;
            }
        });

        assertTrue("Expected at least one report root", reportRootSeen.get() > 0);
    }

    private void testSenderReportDir(Path aggregateReportNameDir) {
        Path aggregateReportName = aggregateReportNameDir.resolve("sender.dat");

        assertTrue(String.format("Aggregate report %s must exist", aggregateReportName),
                Files.isRegularFile(aggregateReportName));
    }

    private void testReceiverReportDir(Path aggregateReportNameDir) {
        Path aggregateReportName = aggregateReportNameDir.resolve("receiver.dat");
        Path aggregateLatencyReportName = aggregateReportNameDir.resolve("receiverd-latency.hdr");

        assertTrue(String.format("Aggregate report %s must exist", aggregateReportName),
                Files.isRegularFile(aggregateReportName));
        assertTrue(String.format("Aggregate report %s must exist", aggregateLatencyReportName),
                Files.isRegularFile(aggregateLatencyReportName));
    }

    private void copyTree(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }

    private void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

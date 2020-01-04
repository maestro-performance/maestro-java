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

package org.maestro.worker;

import org.maestro.client.Maestro;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.notes.DrainCompleteNotification;
import org.maestro.client.notes.MaestroNotification;
import org.maestro.client.notes.TestFailedNotification;
import org.maestro.client.notes.TestSuccessfulNotification;
import org.maestro.common.LogConfigurator;
import org.maestro.common.Role;
import org.maestro.common.client.notes.*;
import org.maestro.worker.tests.support.common.EndToEndTest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractProtocolTest extends EndToEndTest {

    protected abstract int numWorkers();
    protected abstract int numReceivers();
    protected abstract int numSenders();
    protected abstract int numPeers();

    protected void setupMaestroConnectionProperties() {
        System.out.println("This test will take some time to run");
        LogConfigurator.verbose();
        System.setProperty("maestro.mqtt.no.reuse", "true");
    }

    protected void stopWorkers(final Maestro maestro) {
        System.out.println("Shutting down workers");
        maestro.halt(MaestroTopics.WORKERS_TOPIC);

        waitForWorkerShutdown();
    }

    protected void setupFixedCountTest(final Maestro maestro, final String uri) throws Exception {
        List<? extends MaestroNote> set1 = maestro.setParallelCount(MaestroTopics.WORKERS_TOPIC, 1).get();
        assertEquals("Set parallel count replies don't match: " + set1.size(), numWorkers(), set1.size());

        List<? extends MaestroNote> set2 = maestro.setDuration(MaestroTopics.WORKERS_TOPIC, "5").get();
        assertEquals("Set duration replies don't match: " + set2.size(), numWorkers(), set2.size());

        List<? extends MaestroNote> set3 = maestro.setMessageSize(MaestroTopics.WORKERS_TOPIC, 100).get();
        assertEquals("Set message size replies don't match: " + set3.size(), numWorkers(), set3.size());

        List<? extends MaestroNote> set4 = maestro.setFCL(MaestroTopics.WORKERS_TOPIC, 1000).get();
        assertEquals("Set FCL replies don't match: " + set4.size(), numWorkers(), set4.size());

        List<? extends MaestroNote> set5 = maestro.setRate(MaestroTopics.WORKERS_TOPIC, 100).get();
        assertEquals("Set rate replies don't match: " + set5.size(), numWorkers(), set5.size());

        List<? extends MaestroNote> set6 = maestro.setBroker(MaestroTopics.WORKERS_TOPIC, uri).get();
        assertEquals("Set broker replies don't match: " + set6.size(), numWorkers(), set6.size());
    }

    protected void testFixedCountTest(final Maestro maestro, final String uri) throws Exception {
        TestExecutionInfo testExecutionInfo = runTest(maestro, uri);

        List<? extends MaestroNote> replies = validateTestRun(maestro, testExecutionInfo);

        validateTestResults(replies);

        validateDrain(maestro);
    }

    protected void testFixedCountTestNoValidate(final Maestro maestro, final String uri) throws Exception {
        List<? extends MaestroNote> replies = maestro
                .waitForNotifications(numWorkers())
                .get(90, TimeUnit.SECONDS);

        validateTestResults(replies);

        validateDrain(maestro);
    }

    private List<? extends MaestroNote> validateTestRun(Maestro maestro, TestExecutionInfo testExecutionInfo) throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
        List<? extends MaestroNote> testStarted = maestro.startTest(MaestroTopics.PEER_TOPIC, testExecutionInfo).get(2, TimeUnit.SECONDS);
        assertEquals("Test started replies don't match: " + testStarted.size(), numPeers(), testStarted.size());

        System.out.println("Sending the worker start commands");
        // 12 = 6 commands * 2 peers (sending and receiving peers)

        List<? extends MaestroNote> receiverStarted = maestro
                .startWorker(MaestroTopics.peerTopic(Role.RECEIVER), new WorkerStartOptions("JmsReceiver")).get();
        assertEquals("Receiver start replies don't match: " + receiverStarted.size(), numReceivers(),
                receiverStarted.size());

        List<? extends MaestroNote> senderStarted = maestro
                .startWorker(MaestroTopics.peerTopic(Role.SENDER), new WorkerStartOptions("JmsSender")).get();
        assertEquals("Sender start replies don't match: " + senderStarted.size(), numSenders(),
                senderStarted.size());

        System.out.println("Waiting for notifications ...");
        // Get the test result notification
        List<? extends MaestroNote> replies = maestro
                .waitForNotifications(numWorkers())
                .get(90, TimeUnit.SECONDS);
        return replies;
    }

    private TestExecutionInfo runTest(Maestro maestro, String uri) throws Exception {
        System.out.println("Running a short-lived test");

        setupFixedCountTest(maestro, uri);

        System.out.println("Sending the test start command");
        TestExecutionInfo testExecutionInfo = TestExecutionInfoBuilder.newBuilder()
                .withDescription("some description")
                .withComment("test comments")
                .withSutId(SutDetails.UNSPECIFIED)
                .withSutName("unit test sut")
                .withSutVersion("1.0.1")
                .withSutJvmVersion("1.7.0")
                .withSutOtherInfo("")
                .withSutTags("maestro,devel,test")
                .withTestName("integration test")
                .withTestTags("integration,java,junit")
                .withLabName("local")
                .withScriptName("junit")
                .build();
        return testExecutionInfo;
    }

    protected void validateDrain(Maestro maestro) throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
        System.out.println("Waiting for drain ...");
        List<? extends MaestroNote> drainNotification = maestro
                .waitForDrain(numReceivers())
                .get(180, TimeUnit.SECONDS);
        assertEquals("Replies don't match: " + drainNotification.size(), numReceivers(), drainNotification.size());
        assertEquals(numReceivers(), drainNotification.stream().filter(n -> n instanceof DrainCompleteNotification).count());
    }

    public static void validateTestNotification(TestSuccessfulNotification note) {
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_NOTIFY_SUCCESS);

        validateTestMetadata(note.getTest());
    }

    public static void validateTestNotification(TestFailedNotification note) {
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_NOTIFY_FAIL);

        validateTestMetadata(note.getTest());
    }

    public static void validateTestMetadata(Test test) {
        assertEquals("junit", test.getScriptName());
        assertEquals("integration test", test.getTestName());
        assertNotNull("test details are null", test.getTestDetails());
        assertEquals("some description", test.getTestDetails().getTestDescription());
        assertEquals("test comments", test.getTestDetails().getTestComments());
        assertEquals(SutDetails.UNSPECIFIED, test.getTestNumber());
    }

    protected void validateTestResults(List<? extends MaestroNote> replies) {
        assertEquals("Replies don't match: " + replies.size(), 2, replies.size());

        assertEquals(2, replies.stream().filter(n -> n instanceof MaestroNotification).count());

        replies.stream().filter(n -> n instanceof TestSuccessfulNotification).forEach(
                n -> validateTestNotification((TestSuccessfulNotification) n));

        replies.stream().filter(n -> n instanceof TestFailedNotification).forEach(
                n -> validateTestNotification((TestFailedNotification) n));
    }
}

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

package org.maestro.maestro;

import org.junit.Test;
import org.maestro.client.exchange.MaestroDeserializer;
import org.maestro.client.exchange.support.DefaultGroupInfo;
import org.maestro.client.exchange.support.GroupInfo;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.exchange.support.WorkerPeer;
import org.maestro.client.notes.*;
import org.maestro.client.notes.InternalError;
import org.maestro.common.Role;
import org.maestro.common.client.notes.*;

import java.util.function.Consumer;

import static org.junit.Assert.*;

public class MaestroProtocolTest {
    private final PeerInfo peerInfo = new WorkerPeer(Role.OTHER, "unittest", "localhost",
            new DefaultGroupInfo("test", "all"));

    private byte[] doSerialize(MaestroNote note) throws Exception {
        return note.serialize();
    }

    private org.maestro.common.client.notes.Test mockTest() {
        return new org.maestro.common.client.notes.Test(1, 1, "unit", "none", new TestDetails());
    }

    private <T extends MaestroNote> MaestroNote serializeTest(T note, MaestroNoteType noteType, MaestroCommand command) throws Exception {
        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(note));

        assertEquals("Parsed class do not match", note.getClass(), parsed.getClass());
        assertSame("Unexpected note type", noteType, parsed.getNoteType());
        assertSame("Unexpected command", command, parsed.getMaestroCommand());
        assertTrue("Messages do not correlated", note.correlatesTo(note));
        assertNotNull("toString must not be null", note.toString());

        String value = note.toString();
        String simpleName = note.getClass().getSimpleName();

        assertTrue("toString must contain the class name", value.contains(simpleName));
        assertFalse("toString must not be auto-generated the class name", value.contains("@"));

        return parsed;
    }


    private <T extends MaestroNote> void serializeTest(T note, MaestroNoteType noteType, MaestroCommand command,
                                                       Consumer<T> consumer) throws Exception {
        MaestroNote parsed = serializeTest(note, noteType, command);

        consumer.accept((T) parsed);
    }

    private void verifyPingRequestPayload(PingRequest note) {
        assertTrue( note.getSec() != 0);
        assertTrue(note.getUsec() != 0);
    }


    @Test
    public void serializePingRequest() throws Exception {
        serializeTest(new PingRequest(), MaestroNoteType.MAESTRO_TYPE_REQUEST, MaestroCommand.MAESTRO_NOTE_PING,
                this::verifyPingRequestPayload);
    }


    @Test
    public void serializeOkResponse() throws Exception {
        OkResponse okResponse = new OkResponse();

        okResponse.setId("testid");
        okResponse.setPeerInfo(peerInfo);

        serializeTest(okResponse, MaestroNoteType.MAESTRO_TYPE_RESPONSE, MaestroCommand.MAESTRO_NOTE_OK);
    }


    private void verifyMockTest(org.maestro.common.client.notes.Test test) {
        assertEquals("Unexpected test number", 1, test.getTestNumber());
        assertEquals("Unexpected test iteration", 1, test.getTestIteration());
        assertEquals("Unexpected test name", "unit", test.getTestName());
        assertEquals("Unexpected test script name", "none", test.getScriptName());
    }


    private void verifyTestSuccessfulNotificationPayload(TestSuccessfulNotification note) {
        assertNotNull("Unexpected peer Info", note.getPeerInfo());
        assertEquals("Unexpected notification message","Test completed successfully", note.getMessage());
        assertEquals("Unexpected peer ID", "asfas45", note.getId());
        verifyMockTest(note.getTest());
    }


    @Test
    public void serializeTestSuccessfulNotification() throws Exception {
        TestSuccessfulNotification tsn = new TestSuccessfulNotification();

        setupNotification(tsn);

        org.maestro.common.client.notes.Test test = mockTest();
        tsn.setTest(test);
        tsn.setMessage("Test completed successfully");

        serializeTest(tsn, MaestroNoteType.MAESTRO_TYPE_NOTIFICATION, MaestroCommand.MAESTRO_NOTE_NOTIFY_SUCCESS,
                this::verifyTestSuccessfulNotificationPayload);
    }

    private void verifyTestFailedNotificationPayload(TestFailedNotification note) {
        assertNotNull("Unexpected peer Info", note.getPeerInfo());
        assertEquals("Unexpected notification message", "Test failed", note.getMessage());
        assertEquals("Unexpected peer ID", "asfas45", note.getId());

        verifyMockTest(note.getTest());
    }

    private void setupNotification(MaestroNotification tsn) {
        tsn.setId("asfas45");
        tsn.setPeerInfo(peerInfo);
    }

    @Test
    public void serializeTestFailedNotification() throws Exception {
        TestFailedNotification tsn = new TestFailedNotification();

        setupNotification(tsn);

        org.maestro.common.client.notes.Test test = mockTest();
        tsn.setTest(test);
        tsn.setMessage("Test failed");

        serializeTest(tsn, MaestroNoteType.MAESTRO_TYPE_NOTIFICATION, MaestroCommand.MAESTRO_NOTE_NOTIFY_FAIL,
                this::verifyTestFailedNotificationPayload);
    }


    private void verifyGetRequestPayload(GetRequest note) {
        assertEquals("URLs do not match", GetOption.MAESTRO_NOTE_OPT_GET_DS, note.getOption());
    }

    @Test
    public void serializeGetRequest() throws Exception {
        GetRequest getRequest = new GetRequest();
        getRequest.setGetOption(GetOption.MAESTRO_NOTE_OPT_GET_DS);

        serializeTest(getRequest, MaestroNoteType.MAESTRO_TYPE_REQUEST, MaestroCommand.MAESTRO_NOTE_GET,
                this::verifyGetRequestPayload);
    }

    private void verifyGetResponsePayload(GetResponse note) {
        final String url = "http://0.0.0.0:8101/";

        assertNotNull(note.getOption());
        assertEquals("URLs do not match", url, note.getValue());
    }

    @Test
    public void serializeGetResponse() throws Exception {
        final String url = "http://0.0.0.0:8101/";
        GetResponse getResponse = new GetResponse();

        getResponse.setId("testid");
        getResponse.setPeerInfo(peerInfo);

        getResponse.setOption(GetOption.MAESTRO_NOTE_OPT_GET_DS);
        getResponse.setValue(url);

        serializeTest(getResponse, MaestroNoteType.MAESTRO_TYPE_RESPONSE, MaestroCommand.MAESTRO_NOTE_GET,
                this::verifyGetResponsePayload);
    }

    @Test
    public void serializeStatsRequest() throws Exception {
        serializeTest(new StatsRequest(), MaestroNoteType.MAESTRO_TYPE_REQUEST, MaestroCommand.MAESTRO_NOTE_STATS);
    }

    private void verifyStatsResponsePayload(StatsResponse note) {
        assertEquals("Unexpected peer name", "unittest", note.getPeerInfo().peerName());
        assertEquals("Unexpected latency value", 1.123, note.getLatency(), 0.0);
        assertEquals("Unexpected timestamp value", "1521027548", note.getTimestamp());
        assertEquals("Unexpected rate value", 1122, note.getRate(), 0.0);
        assertEquals("Unexpected child count", 0, note.getChildCount());
        assertEquals("Unexpected role info", "", note.getRoleInfo());
        assertEquals("Unexpected stats type", 0, note.getStatsType());
        assertEquals("Unexpected count value", 0, note.getCount());
    }

    @Test
    public void serializeStatsResponse() throws Exception {
        StatsResponse statsResponse = new StatsResponse();

        statsResponse.setChildCount(0);

        statsResponse.setId("testid");
        statsResponse.setPeerInfo(peerInfo);

        statsResponse.setLatency(1.123);
        statsResponse.setRate(1122);
        statsResponse.setRoleInfo("");
        statsResponse.setTimestamp("1521027548");

        serializeTest(statsResponse, MaestroNoteType.MAESTRO_TYPE_RESPONSE, MaestroCommand.MAESTRO_NOTE_STATS,
                this::verifyStatsResponsePayload);
    }

    private void verifyDrainCompleteNotificationPayload(DrainCompleteNotification note) {
        assertEquals("Test failed", note.getMessage());
        assertEquals("asfas45", note.getId());
    }

    @Test
    public void serializeDrainCompleteNotification() throws Exception {
        DrainCompleteNotification note = new DrainCompleteNotification();

        note.setId("asfas45");
        note.setPeerInfo(peerInfo);
        note.setMessage("Test failed");

        serializeTest(note, MaestroNoteType.MAESTRO_TYPE_NOTIFICATION, MaestroCommand.MAESTRO_NOTE_NOTIFY_DRAIN_COMPLETE,
                this::verifyDrainCompleteNotificationPayload);
    }

    private void verifyInternalErrorPayload(InternalError note) {
        assertEquals("Unexpected error message","Something bad happened", note.getMessage());
        assertEquals("Unexpected peer id","asfas45", note.getId());
        assertEquals("Unexpected error code", ErrorCode.TRY_AGAIN, note.getErrorCode());
    }

    @Test
    public void serializeInternalError() throws Exception {
        InternalError note = new InternalError(ErrorCode.TRY_AGAIN, "Something bad happened");

        note.setId("asfas45");
        note.setPeerInfo(peerInfo);

        serializeTest(note, MaestroNoteType.MAESTRO_TYPE_RESPONSE, MaestroCommand.MAESTRO_NOTE_INTERNAL_ERROR,
                this::verifyInternalErrorPayload);

        InternalError noteWithDefaults = new InternalError("Something bad happened");

        noteWithDefaults.setId("asfas45");
        noteWithDefaults.setPeerInfo(peerInfo);

        serializeTest(noteWithDefaults, MaestroNoteType.MAESTRO_TYPE_RESPONSE, MaestroCommand.MAESTRO_NOTE_INTERNAL_ERROR);
    }

    private void verifyStartWorkerPayload(StartWorker note) {
        assertEquals("FakeWorker", note.getOptions().getWorkerName());
    }

    @Test
    public void serializeStartWorkerRequest() throws Exception {
        StartWorker note = new StartWorker(new WorkerStartOptions("FakeWorker"));
        serializeTest(note, MaestroNoteType.MAESTRO_TYPE_REQUEST, MaestroCommand.MAESTRO_NOTE_START_WORKER,
                this::verifyStartWorkerPayload);
    }

    private void verifyStartInspectorPayload(StartInspector note) {
        assertEquals("Inspector name don't match", "testInspector", note.getPayload());
    }

    @Test
    public void serializeStartInspectorRequest() throws Exception {
        StartInspector note = new StartInspector("testInspector");
        serializeTest(note, MaestroNoteType.MAESTRO_TYPE_REQUEST, MaestroCommand.MAESTRO_NOTE_START_INSPECTOR,
                this::verifyStartInspectorPayload);
    }

    @Test
    public void serializeStopWorkerRequest() throws Exception {
        serializeTest(new StopWorker(), MaestroNoteType.MAESTRO_TYPE_REQUEST, MaestroCommand.MAESTRO_NOTE_STOP_WORKER);
    }

    @Test
    public void serializeStopInspectorRequest() throws Exception {
        serializeTest(new StopInspector(), MaestroNoteType.MAESTRO_TYPE_REQUEST, MaestroCommand.MAESTRO_NOTE_STOP_INSPECTOR);
    }

    @Test
    public void serializeStopTestRequest() throws Exception {
        serializeTest(new StopTestRequest(), MaestroNoteType.MAESTRO_TYPE_REQUEST, MaestroCommand.MAESTRO_NOTE_STOP_TEST);
    }

    private void verifyRoleAssignRequestPayload(RoleAssign note) {
        assertEquals("Unexpected role", Role.SENDER, note.getRole());
    }

    @Test
    public void serializeRoleAssignRequest() throws Exception {
        serializeTest(new RoleAssign(Role.SENDER), MaestroNoteType.MAESTRO_TYPE_REQUEST,
                MaestroCommand.MAESTRO_NOTE_ROLE_ASSIGN, this::verifyRoleAssignRequestPayload);
    }

    @Test
    public void serializeRoleUnassignRequest() throws Exception {
        serializeTest(new RoleUnassign(), MaestroNoteType.MAESTRO_TYPE_REQUEST,
                MaestroCommand.MAESTRO_NOTE_ROLE_UNASSIGN);
    }

    private void verifyGroupJoinRequestPayload(GroupJoinRequest note) {
        assertEquals("Unexpected member name", "SomeMemberName", note.getGroupInfo().memberName());
        assertEquals("Unexpected group name", "OtherGroupName", note.getGroupInfo().groupName());
    }

    @Test
    public void serializeGroupJoinRequest() throws Exception {
        GroupJoinRequest joinRequest = new GroupJoinRequest();

        joinRequest.setGroupInfo(new GroupInfo() {
            @Override
            public String memberName() {
                return "SomeMemberName";
            }

            @Override
            public String groupName() {
                return "OtherGroupName";
            }
        });


        serializeTest(joinRequest, MaestroNoteType.MAESTRO_TYPE_REQUEST,
                MaestroCommand.MAESTRO_NOTE_GROUP_JOIN, this::verifyGroupJoinRequestPayload);
    }


    @Test
    public void serializeGroupLeaveRequest() throws Exception {
        serializeTest(new GroupLeaveRequest(), MaestroNoteType.MAESTRO_TYPE_REQUEST,
                MaestroCommand.MAESTRO_NOTE_GROUP_LEAVE);
    }

    @Test
    public void serializeProtocolError() throws Exception {
        ProtocolError note = new ProtocolError();

        note.setId("asfas45");
        note.setPeerInfo(peerInfo);

        serializeTest(note, MaestroNoteType.MAESTRO_TYPE_RESPONSE,
                MaestroCommand.MAESTRO_NOTE_PROTOCOL_ERROR);
    }

    @Test
    public void serializeStartAgentRequest() throws Exception {
        serializeTest(new StartAgent(), MaestroNoteType.MAESTRO_TYPE_REQUEST, MaestroCommand.MAESTRO_NOTE_START_AGENT);
    }

    @Test
    public void serializeStopAgentAgentRequest() throws Exception {
        serializeTest(new StopAgent(), MaestroNoteType.MAESTRO_TYPE_REQUEST, MaestroCommand.MAESTRO_NOTE_STOP_AGENT);
    }


    @Test
    public void serializeHaltRequest() throws Exception {
        serializeTest(new Halt(), MaestroNoteType.MAESTRO_TYPE_REQUEST, MaestroCommand.MAESTRO_NOTE_HALT);
    }


    private void verifyStartTestRequestPayload(StartTestRequest note) {

        TestExecutionInfo testExecutionInfo = note.getTestExecutionInfo();
        assertNotNull("Invalid test execution info", testExecutionInfo);

        assertNotNull(note.getTestExecutionInfo().getTest());

        TestDetails testDetails = note.getTestExecutionInfo().getTest().getTestDetails();
        assertNotNull("Invalid test details", testDetails);

        assertEquals("Unexpected test description", "some description",
                testDetails.getTestDescription());
        assertEquals("Unexpected test comments", "test comments",
                testDetails.getTestComments());

        SutDetails sutDetails = testExecutionInfo.getSutDetails();
        assertEquals("Invalid SUT ID", SutDetails.UNSPECIFIED, sutDetails.getSutId());
        assertEquals("Invalid SUT name", "unit test sut", sutDetails.getSutName());
        assertEquals("Invalid SUT version", "1.0.1", sutDetails.getSutVersion());

        assertEquals("Invalid lab name", "local", sutDetails.getLabName());
        assertEquals("Invalid JVM version", "1.7.0", sutDetails.getSutJvmVersion());
        assertEquals("Invalid SUT tags", "maestro,devel,test", sutDetails.getSutTags());
        assertEquals("Invalid test tags", "unit,java,junit", sutDetails.getTestTags());

        assertEquals("Invalid script name", "junit", testExecutionInfo.getTest().getScriptName());
    }

    @Test
    public void serializeStartTestRequest() throws Exception {
        TestExecutionInfo testExecutionInfo = TestExecutionInfoBuilder.newBuilder()
                .withDescription("some description")
                .withComment("test comments")
                .withSutId(SutDetails.UNSPECIFIED)
                .withSutName("unit test sut")
                .withSutVersion("1.0.1")
                .withSutJvmVersion("1.7.0")
                .withSutOtherInfo("")
                .withSutTags("maestro,devel,test")
                .withTestName("unit test")
                .withTestTags("unit,java,junit")
                .withLabName("local")
                .withScriptName("junit")
        .build();


        serializeTest(new StartTestRequest(testExecutionInfo), MaestroNoteType.MAESTRO_TYPE_REQUEST,
                MaestroCommand.MAESTRO_NOTE_START_TEST, this::verifyStartTestRequestPayload);
    }


    @Test
    public void serializeAbnormalDisconnectNotification() throws Exception {
        AbnormalDisconnect abnormalDisconnect = new AbnormalDisconnect();

        abnormalDisconnect.setMessage("Disconnected abruptly");
        setupNotification(abnormalDisconnect);

        serializeTest(abnormalDisconnect, MaestroNoteType.MAESTRO_TYPE_NOTIFICATION,
                MaestroCommand.MAESTRO_NOTE_ABNORMAL_DISCONNECT);
    }

    private void verifyTestStartedNotificationPayload(TestStartedNotification note) {
        assertNotNull("Unexpected peer Info", note.getPeerInfo());
        assertEquals("Unexpected notification message","Test started", note.getMessage());
        assertEquals("Unexpected peer ID", "asfas45", note.getId());
        verifyMockTest(note.getTest());
    }

    @Test
    public void serializeTestStartedNotification() throws Exception {
        TestStartedNotification tsn = new TestStartedNotification();

        setupNotification(tsn);

        org.maestro.common.client.notes.Test test = mockTest();

        tsn.setTest(test);
        tsn.setMessage("Test started");

        serializeTest(tsn, MaestroNoteType.MAESTRO_TYPE_NOTIFICATION,
                MaestroCommand.MAESTRO_NOTE_NOTIFY_TEST_STARTED, this::verifyTestStartedNotificationPayload);
    }

}

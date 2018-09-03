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

package org.maestro.maestro;

import org.junit.Test;
import org.maestro.client.exchange.MaestroDeserializer;
import org.maestro.client.exchange.support.DefaultGroupInfo;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.exchange.support.WorkerPeer;
import org.maestro.common.client.notes.LocationType;
import org.maestro.client.notes.LogResponse;
import org.maestro.common.Role;
import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.MaestroNoteType;
import org.maestro.contrib.utils.digest.Sha1Digest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LogResponseTest {
    private static final PeerInfo peerInfo = new WorkerPeer(Role.RECEIVER, "unittest", "localhost",
            new DefaultGroupInfo("test", "all"));

    static class TestLogResponse extends LogResponse {
        private int testChunkSize = LogResponse.LOG_RESPONSE_MAX_PAYLOAD_SIZE;

        @Override
        protected void setFileName(String fileName) {
            super.setFileName(fileName);
        }

        @Override
        protected void setFileSize(long fileSize) {
            super.setFileSize(fileSize);
        }

        @Override
        protected void setTotal(int total) {
            super.setTotal(total);
        }

        void setTestChunkSize(int testChunkSize) {
            this.testChunkSize = testChunkSize;
        }

        @Override
        protected InputStream initializeInputStream() {
            return this.getClass().getResourceAsStream("/logresponse/" + getFileName());
        }

        @Override
        public int getChunkSize(int maxChunkSize) {
            return super.getChunkSize(testChunkSize);
        }

        String calculateHash() throws IOException {
            try (InputStream inputStream = this.getClass().getResourceAsStream("/logresponse/" + getFileName())) {
                Sha1Digest digest = new Sha1Digest();

                return digest.calculate(inputStream);
            }
        }
    }

    private byte[] doSerialize(MaestroNote note) throws Exception {
        return note.serialize();
    }


    @Test
    public void serializeLogRequest() throws Exception {
        TestLogResponse logResponse = new TestLogResponse();

        logResponse.setId("testid");
        logResponse.setPeerInfo(peerInfo);

        logResponse.setFileName("test.properties");
        logResponse.setFileSize(903);
        logResponse.setTotal(1);
        logResponse.setLocationType(LocationType.ANY);
        logResponse.setFileHash(logResponse.calculateHash());

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(logResponse));

        assertTrue(parsed instanceof LogResponse);
        assertTrue("Parsed object is not a log response",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_LOG);

        final String expectedHash = "06dbd6b9a75417b7ab5aef1ad58b03c30a43dd83";

        Sha1Digest digest = new Sha1Digest();
        final String logHash = digest.calculate(((LogResponse) parsed).getLogData());

        assertEquals("The file hashes do not mach", expectedHash, logHash);
    }

    @Test
    public void serializeLogRequestLarge() throws Exception {
        TestLogResponse logResponse = new TestLogResponse();

        logResponse.setId("testid");
        logResponse.setPeerInfo(peerInfo);

        logResponse.setFileName("sample.txt");
        logResponse.setFileSize(20);
        logResponse.setTotal(2);
        logResponse.setTestChunkSize(10);
        logResponse.setLocationType(LocationType.ANY);
        logResponse.setFileHash(logResponse.calculateHash());


        byte[] firstChunk = doSerialize(logResponse);
        logResponse.next();
        assertTrue("The log response should have subsequent data", logResponse.hasNext());
        byte[] secondChunk = doSerialize(logResponse);

        @SuppressWarnings("UnusedAssignment") MaestroNote note = MaestroDeserializer.deserialize(firstChunk);
        note = MaestroDeserializer.deserialize(secondChunk);

        assertTrue(note instanceof LogResponse);

        assertTrue("Chunk1 object is not a log response",
                note.getNoteType() == MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertTrue(note.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_LOG);

        final String expectedHash = "f1b27e5c5ede29e941e3d5fb10c3ef275a0f63a8";

        LogResponse fullResponse = (LogResponse) note;

        Sha1Digest digest = new Sha1Digest();
        final String logHash = digest.calculate(fullResponse.getLogData());

        assertEquals("The file hashes do not mach", expectedHash, logHash);
    }
}

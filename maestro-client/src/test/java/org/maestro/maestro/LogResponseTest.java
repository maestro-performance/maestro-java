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
import org.maestro.client.notes.LocationType;
import org.maestro.client.notes.LogResponse;
import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.MaestroNoteType;
import org.maestro.contrib.utils.digest.Sha1Digest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LogResponseTest {

    static class TestLogResponse extends LogResponse {
        @Override
        public void setFileName(String fileName) {
            super.setFileName(fileName);
        }

        @Override
        public void setFileSize(long fileSize) {
            super.setFileSize(fileSize);
        }

        @Override
        public void setTotal(int total) {
            super.setTotal(total);
        }

        @Override
        protected InputStream initializeInputStream() throws IOException {
            InputStream ret = this.getClass().getResourceAsStream("/logresponse/" + getFileName());

            return ret;
        }

        public String calculateHash() throws IOException {
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

        logResponse.setFileName("test.properties");
        logResponse.setFileSize(333);
        logResponse.setTotal(1);
        logResponse.setLocationType(LocationType.ANY);
        logResponse.setFileHash(logResponse.calculateHash());

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(logResponse));

        assertTrue(parsed instanceof LogResponse);
        assertTrue("Parsed object is not a log response",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_LOG);

        final String expectedHash = "ab5313aa600dceff855fdbd62917b62952b39179";
        assertEquals("The file hashes do not mach", expectedHash, ((LogResponse) parsed).getFileHash());
    }
}

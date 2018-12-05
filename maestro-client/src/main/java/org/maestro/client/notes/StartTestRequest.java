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

package org.maestro.client.notes;

import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.client.notes.TestExecutionInfo;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class StartTestRequest extends MaestroRequest<MaestroEventListener> {
    private final TestExecutionInfo testExecutionInfo;

    public StartTestRequest(final TestExecutionInfo testExecutionInfo) {
        super(MaestroCommand.MAESTRO_NOTE_START_TEST);

        this.testExecutionInfo = testExecutionInfo;
    }

    public StartTestRequest(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_START_TEST, unpacker);

        testExecutionInfo = SerializationUtils.unpackTestExecutionInfo(unpacker);
    }

    public TestExecutionInfo getTestExecutionInfo() {
        return testExecutionInfo;
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        SerializationUtils.pack(packer, testExecutionInfo);

        return packer;
    }

    @Override
    public void notify(final MaestroEventListener visitor) {
        visitor.handle(this);
    }
}

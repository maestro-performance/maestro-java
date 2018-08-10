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

package org.maestro.client.notes;

import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class UserCommand1Request extends MaestroRequest<MaestroAgentEventListener> {
    private long option;
    private String payload;

    public UserCommand1Request() {
        super(MaestroCommand.MAESTRO_NOTE_USER_COMMAND_1);
    }

    public UserCommand1Request(MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_USER_COMMAND_1, unpacker);

        if (unpacker.hasNext()) {
            this.option = unpacker.unpackLong();
        }

        if (unpacker.hasNext()) {
            this.payload = unpacker.unpackString();
        }
    }

    public void set(final long option, final String value) {
        this.option = option;
        this.payload = value;
    }

    public long getOption() {
        return option;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packLong(option);

        if (payload != null) {
            packer.packString(this.payload);
        }

        return packer;
    }

    @Override
    public void notify(MaestroAgentEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    public String toString() {
        return "UserCommand1Request{" +
                "option=" + option +
                ", payload='" + payload + '\'' +
                "} " + super.toString();
    }
}

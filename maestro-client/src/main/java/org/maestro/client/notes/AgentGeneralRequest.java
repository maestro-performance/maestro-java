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

public class AgentGeneralRequest extends MaestroRequest<MaestroAgentEventListener> {


    public enum Option {
        /** Broker address */
        MAESTRO_NOTE_EXECUTE_COMMAND(0);

        private long value;

        Option(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }

        static public AgentGeneralRequest.Option from(long value) {
            switch ((int) value) {
                case 0: return MAESTRO_NOTE_EXECUTE_COMMAND;
            }

            return null;
        }
    }

    private AgentGeneralRequest.Option option;
    private String value;

    public AgentGeneralRequest() {
        super(MaestroCommand.MAESTRO_NOTE_AGENT_GENERAL_EXECUTE);
    }

    public AgentGeneralRequest(MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_AGENT_GENERAL_EXECUTE);

        this.option = Option.from(unpacker.unpackLong());
        this.value = unpacker.unpackString();
    }

    private void set(final AgentGeneralRequest.Option option, final String value) {
        this.option = option;
        this.value = value;
    }

    public void setExecuteCommand(final String value) {
        set(AgentGeneralRequest.Option.MAESTRO_NOTE_EXECUTE_COMMAND, value);
    }

    public AgentGeneralRequest.Option getOption() {
        return option;
    }

    public String getValue() {
        return value;
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packLong(option.getValue());
        packer.packString(this.value);

        return packer;
    }

    @Override
    public void notify(MaestroAgentEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    public String toString() {
        return "AgentGeneralRequest{" +
                "option=" + option +
                ", value='" + value + '\'' +
                "} " + super.toString();
    }
}

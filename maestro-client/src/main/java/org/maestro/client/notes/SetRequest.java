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

public class SetRequest extends MaestroRequest<MaestroEventListener> {
    public enum Option {
        /** Broker address */
        MAESTRO_NOTE_OPT_SET_BROKER(0),
        /** Duration type (count or duration) */
        MAESTRO_NOTE_OPT_SET_DURATION_TYPE(1),
        /** Set the log level */
        @Deprecated
        MAESTRO_NOTE_OPT_SET_LOG_LEVEL(2),
        /** Set the parallel count */
        MAESTRO_NOTE_OPT_SET_PARALLEL_COUNT(3),
        /** Set message size */
        MAESTRO_NOTE_OPT_SET_MESSAGE_SIZE(4),
        /** Set throttle */
        @Deprecated
        MAESTRO_NOTE_OPT_SET_THROTTLE(5),
        /** Set rate */
        MAESTRO_NOTE_OPT_SET_RATE(6),
        /** Set fail condition  */
        MAESTRO_NOTE_OPT_FCL(7),
        /** Sets the management interface */
        MAESTRO_NOTE_OPT_SET_MI(8);

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

        static public Option from(long value) {
            switch ((int) value) {
                case 0: return MAESTRO_NOTE_OPT_SET_BROKER;
                case 1: return MAESTRO_NOTE_OPT_SET_DURATION_TYPE;
                case 2: return MAESTRO_NOTE_OPT_SET_LOG_LEVEL;
                case 3: return MAESTRO_NOTE_OPT_SET_PARALLEL_COUNT;
                case 4: return MAESTRO_NOTE_OPT_SET_MESSAGE_SIZE;
                case 5: return MAESTRO_NOTE_OPT_SET_THROTTLE;
                case 6: return MAESTRO_NOTE_OPT_SET_RATE;
                case 7: return MAESTRO_NOTE_OPT_FCL;
                case 8: return MAESTRO_NOTE_OPT_SET_MI;
            }

            return null;
        }
    }

    private Option option;
    private String value;

    public SetRequest() {
        super(MaestroCommand.MAESTRO_NOTE_SET);
    }

    public SetRequest(MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_SET, unpacker);

        this.option = Option.from(unpacker.unpackLong());
        this.value = unpacker.unpackString();
    }

    private void set(final Option option, final String value) {
        this.option = option;
        this.value = value;
    }

    public void setBroker(final String value) {
        set(Option.MAESTRO_NOTE_OPT_SET_BROKER, value);
    }

    public void setDurationType(final String value) {
        set(Option.MAESTRO_NOTE_OPT_SET_DURATION_TYPE, value);
    }

    public void setParallelCount(final String value) {
        set(Option.MAESTRO_NOTE_OPT_SET_PARALLEL_COUNT, value);
    }

    public void setMessageSize(final String value) {
        set(Option.MAESTRO_NOTE_OPT_SET_MESSAGE_SIZE, value);
    }

    public void setRate(final String value) {
        set(Option.MAESTRO_NOTE_OPT_SET_RATE, value);
    }

    public void setFCL(final String value) {
        set(Option.MAESTRO_NOTE_OPT_FCL, value);
    }

    public void setManagementInterface(final String value) {
        set(Option.MAESTRO_NOTE_OPT_SET_MI, value);
    }

    public Option getOption() {
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
    public void notify(MaestroEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    public String toString() {
        return "SetRequest{" +
                "option=" + option +
                ", value='" + value + '\'' +
                "} " + super.toString();
    }
}

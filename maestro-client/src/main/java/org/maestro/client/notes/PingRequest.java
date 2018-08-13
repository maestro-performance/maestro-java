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
import java.time.Instant;

public class PingRequest extends MaestroRequest<MaestroEventListener> {
    private long sec;
    private long usec;

    public PingRequest() {
        super(MaestroCommand.MAESTRO_NOTE_PING);

        Instant instant = Instant.now();
        sec = instant.getEpochSecond();
        usec = instant.getNano() / 1000;
    }

    public PingRequest(MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_PING, unpacker);

        sec = unpacker.unpackLong();
        usec = unpacker.unpackLong();
    }

    public long getSec() {
        return sec;
    }

    public void setSec(long sec) {
        this.sec = sec;
    }

    public long getUsec() {
        return usec;
    }

    public void setUsec(long usec) {
        this.usec = usec;
    }

    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packLong(sec);
        packer.packLong(usec);

        return packer;
    }

    @Override
    public void notify(MaestroEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    public String toString() {
        return "PingRequest{" +
                "sec=" + sec +
                ", usec=" + usec +
                "} " + super.toString();
    }
}

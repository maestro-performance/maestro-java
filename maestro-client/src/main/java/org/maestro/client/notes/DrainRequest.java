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
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;


public class DrainRequest extends MaestroRequest<MaestroReceiverEventListener> {
    private String duration;
    private String url;
    private String parallelCount;
    private String workerName;

    public DrainRequest() {
        super(MaestroCommand.MAESTRO_NOTE_DRAIN);
    }

    public DrainRequest(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_DRAIN, unpacker);

        this.duration = unpacker.unpackString();
        this.url = unpacker.unpackString();
        this.parallelCount = unpacker.unpackString();
        this.workerName = unpacker.unpackString();
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParallelCount() {
        return parallelCount;
    }

    public void setParallelCount(String parallelCount) {
        this.parallelCount = parallelCount;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    @Override
    public void notify(MaestroReceiverEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packString(this.duration);
        packer.packString(this.url);
        packer.packString(this.parallelCount);
        packer.packString(this.workerName);

        return packer;
    }


    @Override
    public String toString() {
        return "DrainRequest{" +
                "duration='" + duration + '\'' +
                ", url='" + url + '\'' +
                ", parallelCount=" + parallelCount +
                "} " + super.toString();
    }
}

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
import org.maestro.common.client.notes.MaestroNoteType;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class MaestroResponse extends AbtractMaestroNote {
    private String id = "";
    private String name = "";

    public MaestroResponse(MaestroCommand maestroCommand) {
        super(MaestroNoteType.MAESTRO_TYPE_RESPONSE, maestroCommand);
    }

    public MaestroResponse(MaestroCommand maestroCommand, MessageUnpacker unpacker) throws IOException {
        super(MaestroNoteType.MAESTRO_TYPE_RESPONSE, maestroCommand);

        id = unpacker.unpackString();
        name = unpacker.unpackString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packString(this.id);
        packer.packString(this.name);

        return packer;
    }

    @Override
    public String toString() {
        return "MaestroResponse{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                "} " + super.toString();
    }
}

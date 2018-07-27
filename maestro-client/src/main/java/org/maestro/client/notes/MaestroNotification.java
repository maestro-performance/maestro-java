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

public abstract class MaestroNotification extends MaestroEvent<MaestroEventListener> {
    private String id;
    private String name;
    private String role;
    private String host;

    public MaestroNotification(MaestroCommand maestroCommand) {
        super(MaestroNoteType.MAESTRO_TYPE_NOTIFICATION, maestroCommand);
    }

    public MaestroNotification(MaestroCommand maestroCommand, MessageUnpacker unpacker) throws IOException {
        super(MaestroNoteType.MAESTRO_TYPE_NOTIFICATION, maestroCommand);

        id = unpacker.unpackString();
        name = unpacker.unpackString();
        role = unpacker.unpackString();
        host = unpacker.unpackString();
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packString(this.id);
        packer.packString(this.name);
        packer.packString(this.role);
        packer.packString(this.host);

        return packer;
    }

    @Override
    public String toString() {
        return "MaestroNotification{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", host='" + host + '\'' +
                "} " + super.toString();
    }
}

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
 *
 */

package org.maestro.client.notes;

import org.maestro.client.exchange.support.DefaultGroupInfo;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.exchange.support.WorkerPeer;
import org.maestro.common.Role;
import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.client.notes.MaestroNoteType;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public abstract class MaestroData<T> extends MaestroEvent<T> {
    private String id;
    private PeerInfo peerInfo;

    public MaestroData(final MaestroCommand maestroCommand) {
        super(MaestroNoteType.MAESTRO_TYPE_DATA, maestroCommand);
    }

    public MaestroData(final MaestroCommand maestroCommand, final MessageUnpacker unpacker) throws IOException {
        super(MaestroNoteType.MAESTRO_TYPE_DATA, maestroCommand, unpacker);

        id = unpacker.unpackString();


        final String memberName = unpacker.unpackString();
        final String groupName = unpacker.unpackString();

        final int role = unpacker.unpackInt();
        final String name = unpacker.unpackString();
        final String host = unpacker.unpackString();

        this.peerInfo = new WorkerPeer(Role.from(role), name, host,
                new DefaultGroupInfo(memberName, groupName));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PeerInfo getPeerInfo() {
        return peerInfo;
    }

    public void setPeerInfo(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packString(this.id);
        packer.packString(peerInfo.groupInfo().memberName());
        packer.packString(peerInfo.groupInfo().groupName());
        packer.packInt(peerInfo.getRole().getCode());
        packer.packString(peerInfo.peerName());
        packer.packString(peerInfo.peerHost());

        return packer;
    }

    @Override
    public String toString() {
        return "MaestroData{" +
                "id='" + id + '\'' +
                ", peerInfo=" + peerInfo +
                "} " + super.toString();
    }
}

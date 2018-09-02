/*
 * Copyright 2018 Otavio Rodolfo Piske
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

import org.maestro.client.exchange.support.DefaultGroupInfo;
import org.maestro.client.exchange.support.GroupInfo;
import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class GroupJoinRequest extends MaestroRequest<MaestroEventListener> {
    private GroupInfo groupInfo;

    public GroupJoinRequest() {
        super(MaestroCommand.MAESTRO_NOTE_GROUP_JOIN);
    }

    public GroupJoinRequest(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_GROUP_JOIN, unpacker);

        final String memberName = unpacker.unpackString();
        final String groupName = unpacker.unpackString();

        groupInfo = new DefaultGroupInfo(memberName, groupName);
    }

    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(final GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }

    @Override
    public void notify(MaestroEventListener visitor) {
        visitor.handle(this);
    }
}

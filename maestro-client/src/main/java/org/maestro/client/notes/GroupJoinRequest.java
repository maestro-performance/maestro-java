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

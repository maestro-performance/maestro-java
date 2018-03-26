package org.maestro.client.notes;

import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class AgentSourceRequest extends MaestroRequest<MaestroAgentEventListener> {
    private String sourceUrl;

    public AgentSourceRequest() {
        super(MaestroCommand.MAESTRO_NOTE_AGENT_SOURCE);
    }

    public AgentSourceRequest(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_AGENT_SOURCE);

        this.sourceUrl = unpacker.unpackString();
    }

    @Override
    public void notify(MaestroAgentEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packString(this.sourceUrl);

        return packer;
    }
}

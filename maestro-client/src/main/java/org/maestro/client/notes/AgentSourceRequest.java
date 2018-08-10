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

public class AgentSourceRequest extends MaestroRequest<MaestroAgentEventListener> {
    private String sourceUrl;
    private String branch;

    public AgentSourceRequest() {
        super(MaestroCommand.MAESTRO_NOTE_AGENT_SOURCE);
    }

    public AgentSourceRequest(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_AGENT_SOURCE, unpacker);

        this.sourceUrl = unpacker.unpackString();

        if (unpacker.hasNext()) {
            branch = unpacker.unpackString();
        }
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }


    @Override
    public void notify(MaestroAgentEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packString(this.sourceUrl);

        if (this.branch != null) {
            packer.packString(branch);
        }

        return packer;
    }

    @Override
    public String toString() {
        return "AgentSourceRequest{" +
                "sourceUrl='" + sourceUrl + '\'' +
                ", branch='" + branch + '\'' +
                "} " + super.toString();
    }
}

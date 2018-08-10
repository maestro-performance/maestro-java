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

import org.maestro.common.client.notes.GetOption;
import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

/**
 * A GET request queries the peers for information
 */
public class GetRequest extends MaestroRequest<MaestroEventListener> {
    private GetOption option;

    public GetRequest() {
        super(MaestroCommand.MAESTRO_NOTE_GET);
    }

    public GetRequest(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_GET, unpacker);

        this.option = GetOption.from(unpacker.unpackLong());
    }

    public void setGetOption(final GetOption option) {
        this.option = option;
    }

    public GetOption getOption() {
        return option;
    }


    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packLong(option.getValue());

        return packer;
    }

    @Override
    public void notify(MaestroEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    public String toString() {
        return "GetRequest{" +
                "option=" + option +
                "} " + super.toString();
    }
}

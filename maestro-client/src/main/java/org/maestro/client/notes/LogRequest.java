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

import org.maestro.common.client.notes.LocationType;
import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;


public class LogRequest extends MaestroRequest<MaestroEventListener> {
    private LocationType locationType;
    private String typeName;

    public LogRequest() {
        super(MaestroCommand.MAESTRO_NOTE_LOG);
    }

    public LogRequest(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_LOG, unpacker);

        this.locationType = LocationType.byCode(unpacker.unpackInt());
        if (locationType == LocationType.ANY) {
            this.typeName = unpacker.unpackString();
        }
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public void notify(MaestroEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packInt(this.locationType.getCode());

        if (locationType == LocationType.ANY) {
            packer.packString(this.typeName);
        }

        return packer;
    }


    @Override
    public String toString() {
        return "LogRequest{" +
                "locationType=" + locationType +
                ", typeName='" + typeName + '\'' +
                "} " + super.toString();
    }
}

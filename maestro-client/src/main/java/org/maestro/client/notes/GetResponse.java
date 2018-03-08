package org.maestro.client.notes;

import org.maestro.common.client.notes.GetOption;
import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;


/**
 * A GET response provide peers w/ a reponse for their GET requests
 */
public class GetResponse extends MaestroResponse {
    private GetOption option;
    private String value;

    public GetResponse() {
        super(MaestroCommand.MAESTRO_NOTE_GET);
    }

    public GetResponse(MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_GET, unpacker);

        this.option = GetOption.from(unpacker.unpackLong());
        value = unpacker.unpackString();
    }

    public GetOption getOption() {
        return option;
    }

    public void setOption(GetOption option) {
        this.option = option;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packLong(option.getValue());
        packer.packString(value);

        return packer;
    }

    @Override
    public String toString() {
        return "GetResponse{" +
                "option=" + option +
                ", value='" + value + '\'' +
                "} " + super.toString();
    }
}

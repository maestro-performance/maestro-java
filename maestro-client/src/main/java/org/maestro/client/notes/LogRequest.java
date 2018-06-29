package org.maestro.client.notes;

import org.maestro.common.client.notes.MaestroCommand;


public class LogRequest extends MaestroRequest<MaestroEventListener> {
    public LogRequest() {
        super(MaestroCommand.MAESTRO_NOTE_LOG);
    }

    @Override
    public void notify(MaestroEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    public String toString() {
        return "LogRequest{} " + super.toString();
    }
}

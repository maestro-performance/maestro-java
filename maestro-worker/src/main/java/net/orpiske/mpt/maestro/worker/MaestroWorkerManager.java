package net.orpiske.mpt.maestro.worker;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.maestro.client.AbstractMaestroPeer;
import net.orpiske.mpt.maestro.notes.MaestroNote;
import net.orpiske.mpt.maestro.notes.StartSender;

public class MaestroWorkerManager extends AbstractMaestroPeer {

    public MaestroWorkerManager(final String url, final String clientName) throws MaestroConnectionException {
        super(url, clientName);
    }


    protected void messageArrived(MaestroNote note) {

    }


    protected void messageArrived(StartSender note) {

    }
}

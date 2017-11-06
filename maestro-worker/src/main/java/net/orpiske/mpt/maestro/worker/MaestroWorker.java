package net.orpiske.mpt.maestro.worker;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.maestro.client.AbstractMaestroPeer;
import net.orpiske.mpt.maestro.notes.MaestroNote;

public class MaestroWorker extends AbstractMaestroPeer {

    public MaestroWorker(final String url, final String clientName) throws MaestroConnectionException {
        super(url, clientName);
    }


    protected void messageArrived(MaestroNote node) {

    }

}

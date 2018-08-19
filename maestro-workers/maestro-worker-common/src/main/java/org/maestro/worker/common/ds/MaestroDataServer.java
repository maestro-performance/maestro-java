package org.maestro.worker.common.ds;

public interface MaestroDataServer extends Runnable {

    /**
     * Gets the data server URL
     * @return the data server URL
     */
    String getServerURL();
}

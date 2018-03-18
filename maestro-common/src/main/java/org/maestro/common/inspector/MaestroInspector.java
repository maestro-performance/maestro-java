package org.maestro.common.inspector;

import org.maestro.common.duration.TestDuration;
import org.maestro.common.exceptions.DurationParseException;

public interface MaestroInspector extends TestDuration.TestProgress {

    void setUrl(String url);

    void setUser(String user);

    void setPassword(String password);

    void setDuration(String duration) throws DurationParseException;

    /**
     * Inspectors normally don't know how many messages have been
     * exchanged, therefore they should return 0 and handle the
     * success/failure notifications when the test is count-based
     * @return
     */
    @Override
    default long messageCount() {
        return 0;
    }

    int start() throws Exception;

    void stop() throws Exception;
}

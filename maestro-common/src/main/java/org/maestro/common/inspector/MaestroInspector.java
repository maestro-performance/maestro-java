package org.maestro.common.inspector;

public interface MaestroInspector {

    void setUrl(String url);

    void setUser(String user);

    void setPassword(String password);

    int start() throws Exception;

    void stop() throws Exception;
}

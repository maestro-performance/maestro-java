package org.maestro.agent.base;

import java.io.File;

final class ExtensionPoint {
    private final File path;
    private final boolean transientFlag;

    ExtensionPoint(File path, boolean transientFlag) {
        this.path = path;
        this.transientFlag = transientFlag;
    }


    public File getPath() {
        return path;
    }

    public boolean isTransient() {
        return transientFlag;
    }

    @Override
    public String toString() {
        return "ExtensionPoint{" +
                "path=" + path +
                ", transientFlag=" + transientFlag +
                '}';
    }
}

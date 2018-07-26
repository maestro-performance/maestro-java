package org.maestro.common.io.data.writers;

import java.io.File;
import java.io.IOException;

public interface RateWriter extends AutoCloseable {
    File reportFile();

    void write(int metadata, long count, long timestamp) throws IOException;


    void close();
}

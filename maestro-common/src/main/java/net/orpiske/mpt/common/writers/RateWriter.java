package net.orpiske.mpt.common.writers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A writer class for performance rate data. This data is saved to a compressed file in the format
 * {role}-rate.gz
 */
public class RateWriter {
    private OutputStream fileStream = null;
    private OutputStream gzipStream = null;

    private RateDataConverter converter;

    /**
     * Constructor
     * @param path file path
     * @throws IOException
     */
    public RateWriter(final File path) throws IOException {

        fileStream = new FileOutputStream(path);
        gzipStream = new GZIPOutputStream(fileStream);

        gzipStream.write(new String("eta;ata\n").getBytes());
    }

    /**
     * Gets the rate data converter
     * @return
     */
    public RateDataConverter getConverter() {
        return converter;
    }


    /**
     * Sets a rate data converter. The converter can be used to adjust
     * worker-specific data (ie.: some benchmark tools may use milliseconds instead of microseconds)
     * @param converter
     */
    public void setConverter(RateDataConverter converter) {
        this.converter = converter;
    }


    private void doWriteLine(final String eta, final String ata) throws IOException {
        String line = eta + ";" + ata + "\n";

        gzipStream.write(line.getBytes());
    }


    /**
     * Write a line in the performance data report
     * @param eta Estimated time of arrival (may be ignored in some driver implementations)
     * @param ata Actual time of arrival
     * @throws IOException
     */
    public void writeLine(final String eta, final String ata) throws IOException  {
        String actualEta = converter != null ? converter.convertEta(eta) : ata;
        String actualAta = converter != null ? converter.convertAta(ata) : ata;

        doWriteLine(actualEta, actualAta);
    }

    public void close() {
        try {
            gzipStream.flush();
            gzipStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

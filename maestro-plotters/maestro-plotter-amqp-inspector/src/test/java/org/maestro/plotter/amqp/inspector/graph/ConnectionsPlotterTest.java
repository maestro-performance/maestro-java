package org.maestro.plotter.amqp.inspector.graph;

import org.junit.Test;
import org.maestro.plotter.amqp.inspector.connections.ConnectionsDataSet;
import org.maestro.plotter.amqp.inspector.connections.ConnectionsProcessor;
import org.maestro.plotter.amqp.inspector.connections.ConnectionsReader;
import org.maestro.plotter.common.exceptions.EmptyDataSet;
import org.maestro.plotter.common.exceptions.IncompatibleDataSet;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class ConnectionsPlotterTest {
    @Test
    public void testPlot() throws IOException, EmptyDataSet, IncompatibleDataSet {
        String fileName = this.getClass().getResource("/data-ok/connections.csv").getPath();

        ConnectionsProcessor connectionsProcessor = new ConnectionsProcessor();
        ConnectionsReader connectionsReader = new ConnectionsReader(connectionsProcessor);

        ConnectionsDataSet connectionsDataSet = connectionsReader.read(fileName);

        File sourceFile = new File(fileName);
        ConnectionsPlotter plotter = new ConnectionsPlotter();

        File outputFile = new File(sourceFile.getParentFile(), ConnectionsPlotter.DEFAULT_FILENAME);
        plotter.plot(connectionsDataSet, outputFile);

        assertTrue("The output file does not exist: " + outputFile.getPath(), outputFile.exists());
    }
}

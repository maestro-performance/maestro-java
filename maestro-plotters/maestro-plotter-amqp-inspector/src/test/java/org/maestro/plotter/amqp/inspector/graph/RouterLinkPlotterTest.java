package org.maestro.plotter.amqp.inspector.graph;

import org.junit.Test;
import org.maestro.plotter.amqp.inspector.routerlink.RouterLinkDataSet;
import org.maestro.plotter.amqp.inspector.routerlink.RouterLinkProcessor;
import org.maestro.plotter.amqp.inspector.routerlink.RouterLinkReader;
import org.maestro.plotter.common.exceptions.EmptyDataSet;
import org.maestro.plotter.common.exceptions.IncompatibleDataSet;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class RouterLinkPlotterTest {
    @Test
    public void testPlot() throws IOException, EmptyDataSet, IncompatibleDataSet {
        String fileName = this.getClass().getResource("/data-ok/routerLink.csv").getPath();

        RouterLinkProcessor routerLinkProcessor = new RouterLinkProcessor();
        RouterLinkReader routerLinkReader = new RouterLinkReader(routerLinkProcessor);

        RouterLinkDataSet routerLinkDataSet = routerLinkReader.read(fileName);

        File sourceFile = new File(fileName);
        RouterLinkPlotter plotter = new RouterLinkPlotter();

        File outputFile = sourceFile.getParentFile();
        plotter.plot(routerLinkDataSet, outputFile);

        assertTrue("The output file does not exist: " + outputFile.getPath(), outputFile.exists());
    }
}

package org.maestro.plotter.amqp.inspector.graph;

import org.junit.Test;
import org.maestro.plotter.amqp.inspector.generalinfo.GeneralInfoDataSet;
import org.maestro.plotter.amqp.inspector.generalinfo.GeneralInfoProcessor;
import org.maestro.plotter.amqp.inspector.generalinfo.GeneralInfoReader;
import org.maestro.plotter.common.exceptions.EmptyDataSet;
import org.maestro.plotter.common.exceptions.IncompatibleDataSet;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class GeneralInfoPlotterTest {
    @Test
    public void testPlot() throws IOException, EmptyDataSet, IncompatibleDataSet {
        String fileName = this.getClass().getResource("/data-ok/general.csv").getPath();

        GeneralInfoProcessor generalInfoProcessor = new GeneralInfoProcessor();
        GeneralInfoReader generalInfoReader = new GeneralInfoReader(generalInfoProcessor);

        GeneralInfoDataSet generalInfoDataSet = generalInfoReader.read(fileName);

        File sourceFile = new File(fileName);
        GeneralInfoPlotter plotter = new GeneralInfoPlotter();

        File outputFile = sourceFile.getParentFile();
        plotter.plot(generalInfoDataSet, outputFile);

        assertTrue("The output file does not exist: " + outputFile.getPath(), outputFile.exists());
    }
}

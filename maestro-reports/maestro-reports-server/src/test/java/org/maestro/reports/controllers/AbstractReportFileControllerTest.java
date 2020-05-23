package org.maestro.reports.controllers;

import io.javalin.Context;
import org.junit.Test;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.controllers.common.exceptions.ReportFileNotFound;
import org.maestro.reports.dto.Report;

import java.io.File;

import static org.junit.Assert.*;

public class AbstractReportFileControllerTest {

    class UnitTestReportFileController extends AbstractReportFileController {

        @Override
        public void handle(Context context) throws Exception {

        }
    }

    @Test
    public void testGetReportFile() {
        Report report = new Report();

        report.setLocation(this.getClass().getResource("/log").getPath());

        UnitTestReportFileController testController = new UnitTestReportFileController();

        File reportFile = testController.getReportFile(report, "sender-0.dat");

        assertTrue(reportFile.exists());
    }

    @Test(expected = ReportFileNotFound.class)
    public void testGetReportFileFilesOnNonExist() {
        Report report = new Report();

        report.setLocation(this.getClass().getResource("/log").getPath());

        UnitTestReportFileController testController = new UnitTestReportFileController();

        testController.getReportFile(report, "sender-3.dat");
    }

    @Test(expected = MaestroException.class)
    public void testGetReportFileFilesOnDir() {
        Report report = new Report();

        report.setLocation(this.getClass().getResource("/").getPath());

        UnitTestReportFileController testController = new UnitTestReportFileController();

        testController.getReportFile(report, "log");
    }

}
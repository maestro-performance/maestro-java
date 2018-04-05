package org.maestro.reports;

import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class SenderReportResolverTest {
    private static final String BASE_URL = "http://localhost:5006/";

    @Test
    public void testSuccessFiles() {
        ReportResolver reportResolver = new SenderReportResolver(BASE_URL);

        List<String> successFiles = reportResolver.getSuccessFiles();
        assertEquals("List size does not match the expected size", 2, successFiles.size());

        assertEquals("The sender rate file does not match the expected sender rate file",
                BASE_URL + "/logs/tests/lastSuccessful/senderd-rate.csv.gz", successFiles.get(0));
        assertEquals("The test properties file does not match the expected file",
                BASE_URL + "/logs/tests/lastSuccessful/test.properties", successFiles.get(1));
    }

    @Test
    public void testFailedFiles() {
        ReportResolver reportResolver = new SenderReportResolver(BASE_URL);

        List<String> failedFiles = reportResolver.getFailedFiles();
        assertEquals("List size does not match the expected size", 2, failedFiles.size());

        assertEquals("The sender rate file does not match the expected sender rate file",
                BASE_URL + "/logs/tests/lastFailed/senderd-rate.csv.gz", failedFiles.get(0));
        assertEquals("The test properties file does not match the expected file",
                BASE_URL + "/logs/tests/lastFailed/test.properties", failedFiles.get(1));
    }
}

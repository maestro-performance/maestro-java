package org.maestro.reports.processors;

import org.maestro.reports.files.ReportFile;

import java.util.List;

public interface ReportFileProcessor {
    void process(List<ReportFile> reportFiles);
}

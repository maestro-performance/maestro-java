package net.orpiske.mpt.reports.processors;

import net.orpiske.mpt.reports.files.ReportFile;

import java.util.List;

public interface ReportFileProcessor {
    void process(List<ReportFile> reportFiles);
}

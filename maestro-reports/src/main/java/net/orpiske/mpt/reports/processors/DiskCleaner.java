package net.orpiske.mpt.reports.processors;

import net.orpiske.mpt.common.Constants;
import net.orpiske.mpt.reports.files.ReportFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class DiskCleaner implements ReportFileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DiskCleaner.class);
    private static final String FILE_EXTENSION_CSV = "csv";

    private void clean(ReportFile reportFile) {
        File sourceFile = reportFile.getSourceFile();

        String ext = FilenameUtils.getExtension(sourceFile.getName());

        switch (ext) {
            case Constants.FILE_EXTENSION_HDR_HISTOGRAM:
            case Constants.FILE_EXTENSION_MPT_COMPRESSED:
            case FILE_EXTENSION_CSV:
                logger.debug("Cleaning file {}", sourceFile.getPath());
                FileUtils.deleteQuietly(sourceFile);
            default:

        }
    }

    @Override
    public void process(List<ReportFile> reportFiles) {
        reportFiles.forEach(this::clean);
    }
}

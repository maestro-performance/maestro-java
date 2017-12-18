package net.orpiske.mpt.reports.files;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class BmicReportFile extends ReportFile {


    public BmicReportFile(File sourceFile, File normalizedFile) {
        super(sourceFile, normalizedFile);

        // Removes the gz
        String baseName = FilenameUtils.removeExtension(normalizedFile.getPath());
        // Removes the csv
        baseName = FilenameUtils.removeExtension(baseName);


    }


}

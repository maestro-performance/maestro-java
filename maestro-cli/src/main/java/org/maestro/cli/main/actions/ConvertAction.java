/*
 * Copyright 2018 Otavio Rodolfo Piske
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.cli.main.actions;

import org.apache.commons.cli.*;
import org.maestro.cli.main.actions.convert.legacy.LegacyRateReader;
import org.maestro.cli.main.actions.convert.legacy.RateDataProcessor;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.io.data.common.FileHeader;
import org.maestro.common.io.data.common.exceptions.InvalidRecordException;
import org.maestro.common.io.data.writers.BinaryRateWriter;
import org.maestro.plotter.rate.RateData;
import org.maestro.plotter.rate.RateRecord;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ConvertAction extends Action {
    private CommandLine cmdLine;

    private File input;
    private boolean keep = false;

    public ConvertAction(final String[] args) {
        processCommand(args);
    }

    @Override
    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("i", "input-file", true, "the legacy input file in csv.gz format");
        options.addOption("", "keep", false, "keep the old record files");

        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            help(options, -1);
        }

        if (cmdLine.hasOption("help")) {
            help(options, 0);
        }

        String fileStr = cmdLine.getOptionValue('i');
        if (fileStr == null) {
            System.err.println("The input file is a required option");
            help(options, 1);
        }

        input = new File(fileStr);

        keep = cmdLine.hasOption("keep");
    }

    @Override
    public int run() {
        RateDataProcessor legacyRateDataProcessor = new RateDataProcessor();
        LegacyRateReader legacyRateReader = new LegacyRateReader(legacyRateDataProcessor);

        try (BinaryRateWriter writer = getWriter()) {
            final RateData rateData = legacyRateReader.read(input);
            final Set<RateRecord> recordSet = rateData.getRecordSet();


            recordSet.forEach(record -> writeRecord(writer, record));

            if (!keep) {
                System.out.println("Marking legacy file for deletion");
                input.deleteOnExit();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    public BinaryRateWriter getWriter() throws IOException {
        final String name = getOutputFileName();
        File output = new File(input.getParent(), name);

        BinaryRateWriter writer;
        if (name.equals("sender.dat")) {
            writer = new BinaryRateWriter(output, FileHeader.WRITER_DEFAULT_SENDER);
        }
        else {
            writer = new BinaryRateWriter(output, FileHeader.WRITER_DEFAULT_RECEIVER);
        }

        return writer;
    }

    public String getOutputFileName() {
        String name;
        if (input.getName().equals("senderd-rate.csv.gz")) {
            name = "sender.dat";
        }
        else {
            if (input.getName().equals("receiverd-rate.csv.gz")) {
                name = "receiver.dat";
            }
            else {
                throw new MaestroException("Invalid file name");
            }
        }
        return name;
    }

    public void writeRecord(final BinaryRateWriter writer, final RateRecord record) {
        try {
            writer.write(0, record.getCount(),
                    TimeUnit.SECONDS.toMicros(record.getTimestamp().getEpochSecond()));
        } catch (IOException e) {
            System.err.println("I/O error while trying to convert the rate record: " + e.getMessage());
            e.printStackTrace();
        } catch (InvalidRecordException e) {
            System.err.println("Invalid record for entry for: " + record);
        }
    }
}

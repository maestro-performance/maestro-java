package net.orpiske.mpt.main.actions;

import net.orpiske.mpt.data.rate.RateToHistogram;
import org.apache.commons.cli.*;

import java.io.*;

public class DataAction extends Action {
    private CommandLine cmdLine;

    private String input;
    private String output;

    public DataAction(String[] args) {
        processCommand(args);
    }

    @Override
    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("a", "action", true, "the action to execute [rate-to-histogram]");
        options.addOption("i", "input", true, "the input filename");
        options.addOption("o", "output", true, "the output filename (if none, will print to stdout)");

        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            help(options, -1);
        }

        if (cmdLine.hasOption("help")) {
            help(options, 0);
        }

        input = cmdLine.getOptionValue('i');
        if (input == null) {
            System.err.println("The input filename is a required option");
            help(options, 1);
        }

        output = cmdLine.getOptionValue('o');
    }

    @Override
    public int run() {
        PrintStream ps = System.out;
        if (output != null) {
            File outputFile = new File(output);
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(outputFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();

                return 1;
            }

            ps = new PrintStream(fos);
        }

        try {
            RateToHistogram.convert(input, ps);

            return 0;
        } catch (IOException e) {
            System.err.println("Error converting rate to histogram: " + e.getMessage());

            e.printStackTrace();
            return 1;
        }
        finally {
            ps.close();
        }
    }
}

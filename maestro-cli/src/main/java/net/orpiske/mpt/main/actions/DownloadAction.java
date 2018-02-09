package net.orpiske.mpt.main.actions;

import net.orpiske.mpt.reports.ReportsDownloader;
import org.apache.commons.cli.*;

public class DownloadAction extends Action {
    private CommandLine cmdLine;

    private String directory;
    private String[] servers;
    private String result;
    private int from;
    private int to;

    public DownloadAction(String[] args) {
        processCommand(args);
    }

    protected void processCommand(String[] args) {
        CommandLineParser parser = new PosixParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("d", "directory", true, "the directory to generate the report");
        options.addOption("l", "log-level", true, "the log level to use [trace, debug, info, warn]");
        options.addOption("f", "from-test", true, "the initial test execution number");
        options.addOption("t", "to-test", true, "the final test execution number");
        options.addOption("s", "servers", true, "a command separated list of servers");
        options.addOption("r", "result", true, "the result to assign to the downloaded files [success,failed]");


        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            help(options, -1);
        }

        if (cmdLine.hasOption("help")) {
            help(options, 0);
        }

        directory = cmdLine.getOptionValue('d');
        if (directory == null) {
            System.err.println("The input directory is a required option");
            help(options, 1);
        }

        try {
            from = Integer.parseInt(cmdLine.getOptionValue('f'));
            to = Integer.parseInt(cmdLine.getOptionValue('t'));
        }
        catch (Exception e) {
            help(options, 1);
        }


        if (to < from) {
            System.err.println("The 'from' must not be smaller than 'to'");
            help(options, 1);
        }

        String serverList = cmdLine.getOptionValue('s');
        if (serverList == null) {
            System.err.println("The 'servers' option is required");
            help(options, 1);
        }

        servers = serverList.split(",");

        result = cmdLine.getOptionValue('r');
        if (result == null) {
            result = "success";
        }

        String logLevel = cmdLine.getOptionValue('l');
        if (logLevel != null) {
            configureLogLevel(logLevel);
        }
    }

    public int run() {
        try {
            for (String server : servers) {
                ReportsDownloader rd = new ReportsDownloader(directory);

                rd.setReportResultTypeDir(result);
                int i = from;
                do {
                    String resourcePath = Integer.toString(i);
                    System.out.println("Downloading reports from http://" + server + "/" + resourcePath);


                    rd.setTestNum(i);
                    rd.downloadAny(server, resourcePath +"/");
                    i++;
                } while (i <= to);
            }


            return 0;
        }
        catch (Exception e) {
            System.err.println("Unable to generate the performance test reports");
            e.printStackTrace();
            return 1;
        }
    }
}

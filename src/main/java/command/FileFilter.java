package command;

import parse_files.FileParser;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

import static picocli.CommandLine.Parameters;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;


class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new FileFilter()).execute(args);
        System.exit(exitCode);
    }

    @Command(name = "filter", description = "filter files")
    static class FileFilter implements Runnable {

        @Option(names = {"-a", "--append"}, description = "append to existing files")
        boolean appendToExistingFiles;

        @Option(names = {"-p", "--prefix"}, description = "prefix for output files")
        String prefix;

        @Option(names = {"-o", "--output"}, description = "path for output files")
        String path;

        @Option(names = {"-s", "--short"}, description = "show short statistics")
        boolean showShort;

        @Option(names = {"-f", "--full"}, description = "show full statistics")
        boolean showFull;

        @Parameters(paramLabel = "--inputFiles", description = "input files to filter")
        File[] files;

        @Override
        public void run() {
            if (showShort && showFull) {
                showShort = false; /* If the user accidentally
                 entered both options, to avoid having to
                 retype everything, let the full format be
                 used in that case
                 */
            }
            FileParser fileParser = new FileParser(files, path, prefix);

            try {
                fileParser.parseFiles(appendToExistingFiles);

                if (showShort) {
                    System.out.println(fileParser.getShortStatistics());
                } else if (showFull) {
                    System.out.println(fileParser.getFullStatistics());
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

}

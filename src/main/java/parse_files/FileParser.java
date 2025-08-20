package parse_files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class FileParser {

    private static final String INTEGER_REGEX = "-?\\d+";
    private static final String FLOAT_REGEX = "-?\\d+\\.\\d+(E-)?\\d*";

    private static final String PATH_DELIMITER = "\\";
    private static final String NEXT_LINE = "\n";

    private static final String FILES_NULL_ERROR = "You need to specify at least one file to parse.\nPlease, try again";

    private static final String INTEGERS_FILE = "integers.txt";
    private static final String FLOATS_FILE = "floats.txt";
    private static final String STRINGS_FILE = "strings.txt";

    private final List<Integer> integerList;
    private final List<Float> floatList;
    private final List<String> stringList;

    private final File[] files;

    private File integersFile;
    private File floatsFile;
    private File stringsFile;

    private final StringBuilder statisticsBuilder;

    public FileParser(File[] files, String inputPath,String outputPath, String prefix) {
        if (files == null) {
            throw new RuntimeException(FILES_NULL_ERROR);
        }

        this.integerList = new ArrayList<>();
        this.floatList = new ArrayList<>();
        this.stringList = new ArrayList<>();
        this.files = getFilesWithPath(files, inputPath);
        this.statisticsBuilder = new StringBuilder();
        initializeOutputFiles(outputPath, prefix);
    }

    public FileParser(File[] files, String bothPath, String prefix) {
        if (files == null) {
            throw new RuntimeException(FILES_NULL_ERROR);
        }

        this.integerList = new ArrayList<>();
        this.floatList = new ArrayList<>();
        this.stringList = new ArrayList<>();
        this.files = getFilesWithPath(files, bothPath);
        this.statisticsBuilder = new StringBuilder();
        initializeOutputFiles(bothPath, prefix);
    }

    public void parseFiles(boolean append) {
        findAllLines(line -> line.matches(INTEGER_REGEX), files, integersFile, append);
        findAllLines(line -> line.matches(FLOAT_REGEX), files, floatsFile, append);
        findAllLines(line -> !line.matches(INTEGER_REGEX) &&
                !line.matches(FLOAT_REGEX), files, stringsFile, append);
    }

    public String getShortStatistics() {
        addElementsToLists();

        statisticsBuilder.append(NEXT_LINE);

        if (!integerList.isEmpty()) {
            statisticsBuilder.append("Integers count: ")
                    .append(integerList.size())
                    .append(NEXT_LINE);
        }

        if (!floatList.isEmpty()) {
            statisticsBuilder.append("Floats count: ")
                    .append(floatList.size())
                    .append(NEXT_LINE);
        }

        if (!stringList.isEmpty()) {
            statisticsBuilder.append("Strings count: ")
                    .append(stringList.size())
                    .append(NEXT_LINE);
        }

        return statisticsBuilder.append(NEXT_LINE).toString();
    }

    public String getFullStatistics() {

        statisticsBuilder.append(getShortStatistics());

        if (!integerList.isEmpty()) {
            appendFullStatisticsForIntegers();
        }

        if (!floatList.isEmpty()) {
            appendFullStatisticsForFloats();
        }

        if (!stringList.isEmpty()) {
            appendFullStatisticsForStrings();
        }

        return statisticsBuilder.toString();
    }

    private void appendFullStatisticsForIntegers() {
        int sum = integerList.stream().reduce(Integer::sum).orElseThrow();


        statisticsBuilder.append("Integers max: ");
        Optional<Integer> optionalMax = integerList.stream().max(Integer::compare);
        optionalMax.ifPresent(statisticsBuilder::append);
                statisticsBuilder.append(NEXT_LINE)
                .append("Integers min: ");
                Optional<Integer> optionalMin = integerList.stream().min(Integer::compareTo);
                optionalMin.ifPresent(statisticsBuilder::append);
                statisticsBuilder.append(NEXT_LINE)
                .append("Integers sum: ")
                .append(sum)
                .append(NEXT_LINE)
                .append("Integers average: ")
                .append(sum / integerList.size())
                .append(NEXT_LINE);
    }

    private void appendFullStatisticsForFloats() {
        float sum = floatList.stream().reduce(Float::sum).orElseThrow();

        statisticsBuilder.append("Floats max: ");
        Optional<Float> optionalMax = floatList.stream().max(Float::compare);
        optionalMax.ifPresent(statisticsBuilder::append);
        statisticsBuilder.append(NEXT_LINE);
        statisticsBuilder.append("Floats min: ");
        Optional<Float> optionalMin = floatList.stream().min(Float::compareTo);
        optionalMin.ifPresent(statisticsBuilder::append);
        statisticsBuilder.append(NEXT_LINE)
                .append("Floats sum: ")
                .append(sum)
                .append(NEXT_LINE)
                .append("Floats average: ")
                .append(sum / floatList.size())
                .append(NEXT_LINE);
    }

    private void appendFullStatisticsForStrings() {
        statisticsBuilder
                .append("Strings max: ");
        Optional<String> optionalMax = stringList.stream().max(Comparator.comparingInt(String::length));
        optionalMax.ifPresent(statisticsBuilder::append);
        statisticsBuilder.append(NEXT_LINE)
                .append("Strings min: ");
        Optional<String> optionalMin = stringList.stream().min(Comparator.comparingInt(String::length));
        optionalMin.ifPresent(statisticsBuilder::append);
    }


    private File[] getFilesWithPath(File[] files, String path) {
        File[] filesWithPath = new File[files.length];

        for (int i = 0; i < files.length; i++) {
            filesWithPath[i] = new File(path + PATH_DELIMITER + files[i]);
        }

        return filesWithPath;
    }

    private void findAllLines(Predicate<String> predicate,
                              File[] files,
                              File outputFile,
                              boolean append) {
        if (!outputFile.exists()) {
            for (File file : files) {
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                    checkLine(predicate, bufferedReader, outputFile);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(file + " is not found.\nProbably, you entered incorrect path.\nPlease, try again");
                } catch (IOException e) {
                    throw new RuntimeException("Error while finding lines from " + file + " to " + outputFile);
                }
            }
        } else {
            findAllLinesIfFileExists(predicate, files, outputFile, append);
        }
    }

    private void findAllLinesIfFileExists(Predicate<String> predicate,
                                          File[] files,
                                          File outputFile,
                                          boolean append) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile, append))) {
            for (File file : files) {
                checkLineIfOutputFileExists(predicate, bufferedWriter, file);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while writing to output file " + outputFile);
        }
    }

    private void checkLine(Predicate<String> predicate,
                           BufferedReader bufferedReader,
                           File outputFile) throws IOException {
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            if (predicate.test(line)) {
                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile, true))) {
                    bufferedWriter.write(line + NEXT_LINE);
                } catch (IOException e) {
                    throw new RuntimeException("Problems with checking line " + line);
                }
            }
        }
    }

    private void checkLineIfOutputFileExists(Predicate<String> predicate, BufferedWriter bufferedWriter, File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (predicate.test(line)) {
                    bufferedWriter.write(line + NEXT_LINE);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading file " + file);
        }
    }

    private void initializeOutputFiles(String path, String prefix) {
        if (prefix != null) {
            this.integersFile = new File(path + PATH_DELIMITER + prefix + INTEGERS_FILE);
            this.floatsFile = new File(path + PATH_DELIMITER + prefix + FLOATS_FILE);
            this.stringsFile = new File(path + PATH_DELIMITER + prefix + STRINGS_FILE);
        } else {
            this.integersFile = new File(path + PATH_DELIMITER + INTEGERS_FILE);
            this.floatsFile = new File(path + PATH_DELIMITER + FLOATS_FILE);
            this.stringsFile = new File(path + PATH_DELIMITER + STRINGS_FILE);
        }
    }

    private void addElementsToLists() {
        if (integersFile.exists()) {
            addElementsToIntegerList();
        }

        if (floatsFile.exists()) {
            addElementsToFloatList();
        }

        if (stringsFile.exists()) {
            addElementsToStringList();
        }
    }

    private void addElementsToIntegerList() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(integersFile))) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                integerList.add(Integer.valueOf(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error with converting lines from " + integersFile + " to list elements");
        }
    }

    private void addElementsToFloatList() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(floatsFile))) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                floatList.add(Float.valueOf(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error with converting lines from " + floatsFile + " to list elements");
        }
    }

    private void addElementsToStringList() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(stringsFile))) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringList.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error with converting lines from " + stringsFile + " to list elements");
        }
    }

}

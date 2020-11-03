package benching;

import codeTesting.CodeTesting;
import graphical.Graph;
import io.GraphParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Benchmarker<T> {

    private static boolean benching = false;

    public Path benchmarksPath;
    public Path instancesPath;
    public Path graphsPath;
    public Path solutionsPath;
    public Path conargPath;
    public Path root;

    public Benchmarker(Path root) {
        this.root = root;

        instancesPath = root.resolve("instances");
        graphsPath = root.resolve("graphs");
        solutionsPath = root.resolve("reference-results");
        conargPath = root.resolve("conarg").resolve("distribution").resolve("conarg.exe");
        benchmarksPath = root.resolve("benchmarks");
    }

    public void benchAndSave(boolean checkResult) {
        benchAndSave(checkResult, "");
    }

    public void benchAndSave(boolean checkResult, String fileIdentifier) {
        try {
            // takes ???
            long start = System.currentTimeMillis();
            final Map<Path, Long> bench = bench(checkResult);
            System.out.println(this.getClass() + " benching took:" + (System.currentTimeMillis() - start));
            // takes 5ms
            saveBench(bench, fileIdentifier);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public Map<Path, Long> bench(boolean checkResult) throws IOException {
        benching = true;
        try ( Stream<Path> paths = Files.list(instancesPath) ) {
            String testInstance = instancesPath.resolve(CodeTesting.selfMadeApx).toString();
            /*
             * meaning of the Long value:
             *  <0 means incorrect result and abs(value) is the time spent to calculate it (includes timeouts)
             * ==0 means exception thrown
             *  >0 means correct result
             */
            final Map<Path, Long> benchingResults = paths//.parallel()
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("apx"))
                    .filter(path -> !path.toAbsolutePath().toString().equals(testInstance))
                    //.sorted(Comparator.comparingLong(path -> path.toFile().length()))
                    //.limit(20)
                    .collect(Collectors.toMap(path -> path, path -> {
                        String output = "";
                        try {
                            Graph g = GraphParser.readGraph(path);
                            long start = System.currentTimeMillis();
                            T result = calcResult(g);
                            long duration = System.currentTimeMillis() - start;
                            //do not allow 0
                            duration++;
                            if ( result == null ) duration = -duration;
                            else if ( checkResult && !isResultCorrect(result, path) ) {
                                duration = -duration;
                            }
                            output += ";" + path + ";" + duration;
                            System.out.println(output);
                            return duration;
                        } catch ( Exception e ) {
                            output += e.toString() + '\n';
                            System.out.println(output);
                            return 0L;
                        }
                    }));
            benching = false;
            return benchingResults;
        } catch ( IOException e ) {
            benching = false;
            throw e;
        }
    }

    //public abstract Set<Set<Vertex>> iterativeResults(Graph g);

    public abstract T calcResult(Graph g);

    //public abstract boolean isResultCorrect(Graph g, Path instancePath) throws IOException;

    public abstract boolean isResultCorrect(T result, Path instancePath) throws IOException;

    public void saveBench(Map<Path, Long> bench, Path outFile) throws IOException {
        StringBuilder csvContent = bench.entrySet().stream().collect(
                StringBuilder::new,
                (currentString, entry) -> currentString.append(entry.getKey()).append(";").append(entry.getValue()).append("\n"),
                StringBuilder::append
        );

        Files.write(outFile, csvContent.toString().getBytes());
    }

    public void saveBench(Map<Path, Long> bench, String filename) throws IOException {
        saveBench(bench, benchmarksPath.resolve(
                this.getClass().getName().replace("benching.", "").toLowerCase()
                        + "_"
                        + filename
                        + "_"
                        + currentDateAndTime()
                        + ".csv"
        ));
    }

    public void saveBench(Map<Path, Long> bench) throws IOException {
        saveBench(bench, "");
    }

    public static String currentDateAndTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss"));
    }

    public static boolean isBenching() {
        return benching;
    }

}

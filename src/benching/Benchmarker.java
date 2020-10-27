package benching;

import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import solver.NaiveSolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Benchmarker {

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

    public Map<Path, Long> bench() throws IOException {
        try ( Stream<Path> paths = Files.list(instancesPath) ) {
            return paths.parallel()
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("apx"))
                    //.sorted(Comparator.comparingLong(path -> path.toFile().length()))
                    /*.unordered()*/
                    //.limit(20)
                    .collect(Collectors.toMap(path -> path, path -> {
                        String output = "";
                        long start = System.currentTimeMillis();
                        try {
                            Graph g = GraphParser.readGraph(path);
                            NaiveSolver ns = new NaiveSolver(g);
                            if ( !isResultCorrect(compute(ns), solutionPath(path)) ) {
                                output += path + "\nnicht korrekt ermittelt worden." + '\n';
                                return -1L;
                            }
                            output += ((System.currentTimeMillis() - start) / 1000.) + "s: " + path + '\n';
                        } catch ( Exception e ) {
                            output += e.getMessage() + '\n';
                        }
                        //System.out.println(output);
                        return System.currentTimeMillis() - start;
                    }));
        }
    }

    public abstract Set<Set<Vertex>> compute(NaiveSolver ns);

    public abstract Path solutionPath(Path instancePath) throws IOException;

    public abstract boolean isResultCorrect(Set<Set<Vertex>> toBeTested, Path solutionPath) throws IOException;

    public void printBench(Map<Path, Long> bench, Path outFile) throws IOException {
        StringBuilder csvContent = bench.entrySet().stream().collect(
                StringBuilder::new,
                (currentString, entry) -> currentString.append(entry.getKey()).append(";").append(entry.getValue()).append("\n"),
                StringBuilder::append
        );

        Files.write(outFile, csvContent.toString().getBytes());
    }

    public void printBench(Map<Path, Long> bench, String filename) throws IOException {
        printBench(bench, benchmarksPath.resolve(filename));
    }

    public void printBench(Map<Path, Long> bench) throws IOException {
        printBench(
                bench,
                this.getClass().getName().replace("Benchmarker", "").toLowerCase() +
                        "_" +
                        currentDateAndTime() +
                        ".csv"
        );
    }

    public static String currentDateAndTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss"));
    }

    private static final String PREFIX1 = "instances-";
    private static final String PREFIX2 = "iccma-2019-";
    private static final String PREFIX3 = "sub2-";

}

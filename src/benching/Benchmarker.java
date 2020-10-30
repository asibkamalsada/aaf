package benching;

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
            String testInstance = "C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba " +
                    "baumann\\iccma19\\instances\\C-1-afinput_exp_acyclic_indvary1_step5_batch_yyy07.apx";
            return paths.parallel()
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("apx"))
                    .sequential()
                    .filter(path -> path.toAbsolutePath().toString().equals(testInstance))
                    /*sorted(Comparator.comparingLong(path -> path.toFile().length()))
                    .limit(20)*/
                    .collect(Collectors.toMap(path -> path, path -> {
                        String output = "";
                        long start = System.currentTimeMillis();
                        try {
                            Graph g = GraphParser.readGraph(path);
                            System.out.print(path + ";");
                            if ( !isResultCorrect(g, path, conargPath) ) {
                                output += path + "\nnicht korrekt ermittelt worden." + '\n';
                                return -1L;
                            }
                            System.out.println(System.currentTimeMillis() - start);
                            output += ((System.currentTimeMillis() - start) / 1000.) + "s: " + path + '\n';
                        } catch ( Exception e ) {
                            output += e.getMessage() + '\n';
                        }
                        //System.out.println(output);
                        return System.currentTimeMillis() - start;
                    }));
        }
    }

    public abstract boolean isResultCorrect(Graph g, Path instancePath, Path conargPath) throws IOException;

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

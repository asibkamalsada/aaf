package execution;

import Solver.NaiveSolver;
import Solver.Tester;
import graphical.Graph;
import io.GraphParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroundedBenchmarker {
    public Path instancesPath;
    public Path graphsPath;
    public Path solutionsPath;
    public Path root;

    public GroundedBenchmarker(Path root) {
        this.root = root;

        instancesPath = root.resolve("instances");
        graphsPath = root.resolve("graphs");
        solutionsPath = root.resolve("reference-results");
    }

    public static void main(String[] args) {
        Path root = Paths.get(
                args.length == 1 ?
                        args[ 0 ] :
                        "C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba baumann\\iccma19"
        );
        GroundedBenchmarker gb = new GroundedBenchmarker(root);
        try {
            final Map<Path, Long> bench = gb.bench();
            // takes 5ms
            gb.printBench(bench, root.resolve("grounded_bench_unlimited.csv"));
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public Map<Path, Long> bench() throws IOException {
        try ( Stream<Path> paths = Files.list(instancesPath) ) {
            return paths.parallel()
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("apx"))
                    /*.unordered()
                    .limit(20)*/
                    .collect(Collectors.toMap(path -> path, path -> {
                        String output = "";
                        long start = System.currentTimeMillis();
                        try {
                            Graph g = GraphParser.readGraph(path);
                            NaiveSolver ns = new NaiveSolver(g);
                            Tester t = new Tester(g, groundedSolutionPath(path));
                            if ( !t.testGrounded(ns.computeGrounded()) ) {
                                output += path + "\nnicht korrekt ermittelt worden." + '\n';
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

    private Path groundedSolutionPath(Path instancePath) throws IOException {
        try ( Stream<Path> paths = Files.list(solutionsPath) ) {
            return paths
                    .filter(path -> path.toString().endsWith(instancePath.getFileName() + "m-SE-GR-D.out"))
                    .collect(Collectors.toList())
                    .get(0);
        }
    }

    private void printBench(Map<Path, Long> bench, Path outFile) throws IOException {
        StringBuilder csvContent = bench.entrySet().stream().collect(
                StringBuilder::new,
                (currentString, entry) -> currentString.append(entry.getKey()).append(";").append(entry.getValue()).append("\n"),
                StringBuilder::append
        );

        Files.write(outFile, csvContent.toString().getBytes());

    }

    private static final String PREFIX1 = "instances-";
    private static final String PREFIX2 = "iccma-2019-";
    private static final String PREFIX3 = "sub2-";

}

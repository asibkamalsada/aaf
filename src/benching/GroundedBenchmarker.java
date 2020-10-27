package benching;

import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import solver.NaiveSolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroundedBenchmarker extends Benchmarker {

    public GroundedBenchmarker(Path root) {
        super(root);
    }

    public static void main(String[] args) {
        Path root = Paths.get(
                args.length == 1 ?
                        args[ 0 ] :
                        "C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba baumann\\iccma19"
        );
        GroundedBenchmarker gb = new GroundedBenchmarker(root);
        try {
            // takes roughly 18min
            final Map<Path, Long> bench = gb.bench();
            // takes 5ms
            gb.printBench(bench, root.resolve("grounded_bench_limit_20_abstract.csv"));
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<Set<Vertex>> compute(NaiveSolver ns) {
        return Collections.singleton(ns.computeGrounded());
    }

    @Override
    public Path solutionPath(Path instancePath) throws IOException {
        try ( Stream<Path> paths = Files.list(solutionsPath) ) {
            return paths
                    .filter(path -> path.toString().endsWith(instancePath.getFileName() + "m-SE-GR-D.out"))
                    .collect(Collectors.toList())
                    .get(0);
        }
    }

    @Override
    public boolean isResultCorrect(Set<Set<Vertex>> toBeTested, Path solutionPath) throws IOException {
        return Tester.testGrounded(toBeTested.stream().findFirst().get(), solutionPath);
    }

}

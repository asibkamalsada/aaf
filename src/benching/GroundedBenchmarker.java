package benching;

import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import solver.GroundedSolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroundedBenchmarker extends Benchmarker<Set<Vertex>> {

    public GroundedBenchmarker(Path root) {
        super(root);
    }

    @Override
    public Set<Vertex> calcResult(Graph g) {
        return new GroundedSolver(g).computeGrounded();
    }

    @Override
    public boolean isResultCorrect(Set<Vertex> result, Path instancePath) throws IOException {
        return Tester.testGrounded(result, instancePath, conargPath);
    }

    public static void main(String[] args) {
        GroundedBenchmarker gb = new GroundedBenchmarker(CodeTesting.root);
        gb.benchAndSave(false);
    }

    @Deprecated
    private Path solutionPath(Path instancePath) throws IOException {
        try ( Stream<Path> paths = Files.list(solutionsPath) ) {
            return paths
                    .filter(path -> path.toString().endsWith(instancePath.getFileName() + "m-SE-GR-D.out"))
                    .collect(Collectors.toList())
                    .get(0);
        }
    }
}

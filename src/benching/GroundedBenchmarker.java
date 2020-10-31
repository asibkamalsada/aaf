package benching;

import graphical.Graph;
import graphical.Vertex;
import solver.GroundedSolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        return Tester.testGrounded(result, solutionPath(instancePath));
    }

    public static void main(String[] args) {
        Path root = Paths.get(
                args.length == 1 ?
                        args[ 0 ] :
                        "C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba_baumann\\iccma19"
        );
        GroundedBenchmarker gb = new GroundedBenchmarker(root);
        gb.benchAndSave();
    }

    private Path solutionPath(Path instancePath) throws IOException {
        try ( Stream<Path> paths = Files.list(solutionsPath) ) {
            return paths
                    .filter(path -> path.toString().endsWith(instancePath.getFileName() + "m-SE-GR-D.out"))
                    .collect(Collectors.toList())
                    .get(0);
        }
    }
}

package benching;

import graphical.Graph;
import solver.GroundedSolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
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
            // 156791ms or ~3 min
            //long start = System.currentTimeMillis();
            final Map<Path, Long> bench = gb.bench();
            //System.out.println("grounded benching took:" + (System.currentTimeMillis() - start));
            // takes 5ms
            gb.printBench(bench);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private Path solutionPath(Path instancePath) throws IOException {
        try ( Stream<Path> paths = Files.list(solutionsPath) ) {
            return paths
                    .filter(path -> path.toString().endsWith(instancePath.getFileName() + "m-SE-GR-D.out"))
                    .collect(Collectors.toList())
                    .get(0);
        }
    }

    @Override
    public boolean isResultCorrect(Graph g, Path instancePath, Path conargPath) throws IOException {
        return Tester.testGrounded(new GroundedSolver(g).computeGrounded(), solutionPath(instancePath));
    }

}

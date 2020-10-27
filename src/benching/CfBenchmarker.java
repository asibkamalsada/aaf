package benching;

import graphical.Vertex;
import solver.NaiveSolver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class CfBenchmarker extends Benchmarker {

    public CfBenchmarker(Path root) {
        super(root);
    }

    @Override
    public Set<Set<Vertex>> compute(NaiveSolver ns) {
        return ns.computeConflictFree();
    }

    @Override
    public Path solutionPath(Path instancePath) throws IOException {
        return null;
    }

    @Override
    public boolean isResultCorrect(Set<Set<Vertex>> toBeTested, Path solutionPath) {
        return false;
    }

    public static void main(String[] args) {
        Path root = Paths.get(
                args.length == 1 ?
                        args[ 0 ] :
                        "C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba baumann\\iccma19"
        );
        Benchmarker cfb = new CfBenchmarker(root);
        try {
            // takes ???
            final Map<Path, Long> bench = cfb.bench();
            // takes 5ms
            cfb.printBench(bench);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

}

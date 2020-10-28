package benching;

import solver.NaiveSolver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class CfBenchmarker extends Benchmarker {

    public CfBenchmarker(Path root) {
        super(root);
    }

    @Override
    public boolean isResultCorrect(NaiveSolver naiveSolver, Path instancePath) {
        return Tester.testConflictFree(naiveSolver.computeConflictFree(), instancePath, conargPath);
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
            long start = System.currentTimeMillis();
            final Map<Path, Long> bench = cfb.bench();
            System.out.println("cf benching took:" + (System.currentTimeMillis() - start));
            // takes 5ms
            cfb.printBench(bench);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

}

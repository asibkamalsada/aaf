package benching;

import graphical.Graph;
import graphical.Vertex;
import solver.sat.AdmMaxSat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class AdmMaxSatBench extends Benchmarker<Set<Set<Vertex>>> {
    public AdmMaxSatBench(Path root) {
        super(root);
    }

    @Override
    public Set<Set<Vertex>> calcResult(Graph g) {
        return new AdmMaxSat(g).findSolutions();
    }

    @Override
    public boolean isResultCorrect(Set<Set<Vertex>> result, Path instancePath) throws IOException {
        return Tester.testAdmissible(result, instancePath, conargPath);
    }

    public static void main(String[] args) {
        Path root = Paths.get(
                args.length == 1 ?
                        args[ 0 ] :
                        "C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba_baumann\\iccma19"
        );
        Benchmarker<Set<Set<Vertex>>> admb = new AdmMaxSatBench(root);
        admb.benchAndSave();
    }

}

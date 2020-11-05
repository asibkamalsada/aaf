package benching.sat;

import benching.Benchmarker;
import verification.Tester;
import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import solver.sat.AdmMaxSat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class AdmMaxSatBench extends Benchmarker<Set<Set<Vertex>>> {
    public AdmMaxSatBench(Path root) {
        super(root);
    }

    @Override
    public Set<Set<Vertex>> calcResult(Graph g, boolean checkResult) {
        return new AdmMaxSat(g).findSolutions();
    }

    @Override
    public boolean isResultCorrect(Set<Set<Vertex>> result, Path instancePath) throws IOException {
        return Tester.testAdmissible(result, instancePath, conargPath);
    }

    public static void main(String[] args) {
        Benchmarker<Set<Set<Vertex>>> admb = new AdmMaxSatBench(CodeTesting.root);
        admb.benchAndSave(false);
    }

}

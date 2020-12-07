package benching.iterative;

import benching.Benchmarker;
import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import verification.SolutionParser;
import solver.iterative.AdmIterator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class AdmIterativeBench extends Benchmarker<Set<Set<Vertex>>> {
    public AdmIterativeBench(Path root) {
        super(root);
    }

    @Override
    public Graph getKernel(Path path) throws IOException {
        return null;
    }

    @Override
    public Set<Set<Vertex>> calcResult(Graph g, boolean checkResult) {
        return checkResult ? new AdmIterator(g).getSolutions() : new AdmIterator(g).printSolutions();
    }

    @Override
    public boolean isResultCorrect(Set<Set<Vertex>> result, Path instancePath) throws IOException {
        return SolutionParser.parseAdmissible(instancePath, conargPath).equals(result);
    }

    public static void main(String[] args) {
        Benchmarker<Set<Set<Vertex>>> admb = new AdmIterativeBench(CodeTesting.root);
        admb.benchAndSave(true, "limit20incrementalSATchecked");
    }


}

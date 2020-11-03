package benching;

import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.SolutionParser;
import solver.iterative.CfSat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class CfBenchmarker extends Benchmarker<Set<Set<Vertex>>> {

    public CfBenchmarker(Path root) {
        super(root);
    }

    @Override
    public Set<Set<Vertex>> calcResult(Graph g) {
        return new CfSat(g).getSolutions();
    }

    @Override
    public boolean isResultCorrect(Set<Set<Vertex>> result, Path instancePath) {
        return SolutionParser.parseConflictFree(instancePath, conargPath).equals(result);
    }

    public static void main(String[] args) {

        Benchmarker<Set<Set<Vertex>>> cfb = new CfBenchmarker(CodeTesting.root);
        cfb.benchAndSave(false);
    }

}

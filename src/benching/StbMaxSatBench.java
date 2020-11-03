package benching;

import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.SolutionParser;
import solver.sat.StbMaxSat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class StbMaxSatBench extends Benchmarker<Set<Set<Vertex>>> {
    public StbMaxSatBench(Path root) {
        super(root);
    }

    @Override
    public Set<Set<Vertex>> calcResult(Graph g) {
        return new StbMaxSat(g, 45).findSolutions();
    }

    @Override
    public boolean isResultCorrect(Set<Set<Vertex>> result, Path instancePath) throws IOException {
        return result.equals(SolutionParser.parseStable(instancePath, conargPath));
    }

    public static void main(String[] args) {
        Benchmarker<Set<Set<Vertex>>> stbb = new StbMaxSatBench(CodeTesting.root);
        stbb.benchAndSave(true);
    }

}

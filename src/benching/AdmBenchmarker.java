package benching;

import graphical.Graph;
import graphical.Vertex;
import io.SolutionParser;
import solver.iterative.AdmIterator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class AdmBenchmarker extends Benchmarker<Set<Set<Vertex>>> {
    public AdmBenchmarker(Path root) {
        super(root);
    }

    @Override
    public Set<Set<Vertex>> calcResult(Graph g) {
        return new AdmIterator(g).getSolutions();
    }

    @Override
    public boolean isResultCorrect(Set<Set<Vertex>> result, Path instancePath) throws IOException {
        return SolutionParser.parseAdmissible(instancePath, conargPath).equals(result);
    }

    public static void main(String[] args) {
        Path root = Paths.get(
                args.length == 1 ?
                        args[ 0 ] :
                        "C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba baumann\\iccma19"
        );
        Benchmarker<Set<Set<Vertex>>> admb = new AdmBenchmarker(root);
        admb.benchAndSave();
    }


}

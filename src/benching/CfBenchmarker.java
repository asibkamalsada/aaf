package benching;

import graphical.Graph;
import graphical.Vertex;
import io.SolutionParser;
import solver.iterative.CfIterator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Deprecated
public class CfBenchmarker extends Benchmarker<Set<Set<Vertex>>>  {

    public CfBenchmarker(Path root) {
        super(root);
    }

    @Override
    public Set<Set<Vertex>> calcResult(Graph g) {
        new CfIterator(g).printSolutions();
        return null;
    }

    @Override
    public boolean isResultCorrect(Set<Set<Vertex>> result, Path instancePath) throws IOException {
        return SolutionParser.parseConflictFree(instancePath, conargPath).equals(result);
    }

    public static void main(String[] args) {
        Path root = Paths.get(
                args.length == 1 ?
                        args[ 0 ] :
                        "C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba_baumann\\iccma19"
        );
        Benchmarker<Set<Set<Vertex>>> cfb = new CfBenchmarker(root);
        try {
            cfb.bench(false);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

}

package solver.sat;

import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;
import verification.SolutionParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CmpMaxSat extends MaxSat {

    public static void main(String[] args) throws IOException {

        Path testingInstance = CodeTesting.instances.resolve(CodeTesting.emptyGrounded);

        Graph g = GraphParser.readGraph(testingInstance);

        CmpMaxSat solver = new CmpMaxSat(g);

        Set<Set<Vertex>> solutions;
        solutions = solver.findSolutions();

        System.out.println("my cmp:");
        solutions.forEach(System.out::println);

        final Set<Set<Vertex>> cmpSolutions =
                SolutionParser.parseComplete(testingInstance, CodeTesting.conarg);

        System.out.println("correct cmp:");
        cmpSolutions.forEach(System.out::println);

        System.out.println(solutions.equals(cmpSolutions));

        //writeSolutions(solutions, System.currentTimeMillis() + ".kryo");
    }

    public CmpMaxSat(Graph graph) {
        super(graph);
        //dimacsString = graph.getAdmDimacs();
    }

    @Override
    protected void prepareSolver() throws ContradictionException {
        graph.prepareCmp(solver);
        problem = solver;
    }

    @Override
    public Set<Set<Vertex>> findSolutions() {

        if ( unsat ) return Collections.emptySet();

        long start = System.currentTimeMillis();

        Set<Set<Vertex>> solutions = new HashSet<>();

        // filename is given on the command line
        try {
            boolean unsat = true;

            while ( problem.isSatisfiable() ) {
                unsat = false;
                solutions.add(graph.interpretSolution(problem.model()));
            }
            if ( unsat ) {
            }
        } catch ( TimeoutException e ) {
            System.err.println("Timeout, sorry!");
            return null;
        } finally {
            //System.out.println(System.currentTimeMillis() - start);
        }
        return solutions;
    }


}


/*
biggest_apx;timeout10min;9518solutions
 */
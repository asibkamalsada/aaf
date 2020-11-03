package solver.sat;

import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import verification.SolutionParser;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class PrfMaxSat extends MaxSat {

    public static void main(String[] args) throws IOException {

        Path instance = CodeTesting.instances.resolve(CodeTesting.shortApx);

        Graph g = GraphParser.readGraph(instance);

        /*myPrfSolutions = new HashSet<>();
        Set<Vertex> solution = new HashSet<>();
        solution.add(new Vertex("hallo"));
        myPrfSolutions.add(solution);*/

/*
        final Set<Set<Vertex>> admSolutions = SolutionParser.parseAdmissible(instance, CodeTesting.conarg);

        System.out.println("admissible:");
        admSolutions.forEach(System.out::println);


        final Set<Vertex> grdSolution = SolutionParser.parseGrounded(instance, CodeTesting.conarg);

        System.out.println("grounded:");
        System.out.println(grdSolution);

*/
        final Set<Set<Vertex>> prfSolutions = SolutionParser.parsePreferred(instance, CodeTesting.conarg);

        /*System.out.println("preferred:");
        prfSolutions.forEach(System.out::println);*/


        long start = System.currentTimeMillis();
        final Set<Set<Vertex>> myPrfSolutions = new PrfMaxSat(g).findSolutions();
        System.out.println(instance + ";" + (System.currentTimeMillis() - start));
        /*System.out.println("my preferred:");
        myPrfSolutions.forEach(System.out::println);*/


        System.out.println(prfSolutions.equals(myPrfSolutions));

        //writeSolutions(myPrfSolutions, System.currentTimeMillis() + ".kryo");
    }

    public PrfMaxSat(Graph graph) {
        super(graph);
    }

    @Override
    protected void prepareSolver() throws ContradictionException {
        graph.preparePrf(solver);
        //solver.setDBSimplificationAllowed(false);
        problem = solver;
    }

    // TODO try to solve preferred in other ways and compare performance
    @Override
    public Set<Set<Vertex>> findSolutions() {

        if ( unsat ) return new HashSet<>();

        long start = System.currentTimeMillis();

        Set<Set<Vertex>> solutions = new HashSet<>();

        // filename is given on the command line
        try {
            boolean unsat = true;

            while ( problem.isSatisfiable() ) {
                unsat = false;

                int[] model = problem.model();
                VecInt positiveLiterals = new VecInt();
                for ( int literal : model ) {
                    if ( literal > 0 ) positiveLiterals.push(literal);
                }

                if ( !problem.isSatisfiable(positiveLiterals) ) {
                    solutions.add(graph.interpretSolution(model));
                    //System.out.println(graph.interpretSolution(model));
                }
            }
            if ( unsat ) {
                // do something for unsat case
                System.err.println("no solutions found");
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
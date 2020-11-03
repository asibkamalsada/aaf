package solver.sat;

import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import verification.SolutionParser;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static io.CustomSolutionParser.writeSolutions;

public class StbMaxSat extends MaxSat {

    public static void main(String[] args) throws IOException {

        Path instance = CodeTesting.instances.resolve(CodeTesting.longApx);

        Graph g = GraphParser.readGraph(instance);

        /*myStbSolutions = new HashSet<>();
        Set<Vertex> solution = new HashSet<>();
        solution.add(new Vertex("hallo"));
        myStbSolutions.add(solution);*/

/*
        final Set<Set<Vertex>> admSolutions = SolutionParser.parseAdmissible(instance, CodeTesting.conarg);

        System.out.println("admissible:");
        admSolutions.forEach(System.out::println);


        final Set<Vertex> grdSolution = SolutionParser.parseGrounded(instance, CodeTesting.conarg);

        System.out.println("grounded:");
        System.out.println(grdSolution);


        final Set<Set<Vertex>> prfSolutions = SolutionParser.parsePreferred(instance, CodeTesting.conarg);

        System.out.println("preferred:");
        prfSolutions.forEach(System.out::println);

*/
        final Set<Set<Vertex>> stbSolutions = SolutionParser.parseStable(instance, CodeTesting.conarg);

        System.out.println("stable:");
        stbSolutions.forEach(System.out::println);


        long start = System.currentTimeMillis();
        final Set<Set<Vertex>> myStbSolutions = new StbMaxSat(g, Integer.MAX_VALUE).findSolutions();
        System.out.println(instance + ";" + (System.currentTimeMillis() - start));
        System.out.println("my stable:");
        myStbSolutions.forEach(System.out::println);


        System.out.println(stbSolutions.equals(myStbSolutions));

        writeSolutions(stbSolutions, "stable-longApx.kryo");
    }

    public StbMaxSat(Graph graph, int timeout) {
        this(graph);
        solver.setTimeout(timeout);
    }

    public StbMaxSat(Graph graph) {
        super(graph);
    }

    @Override
    protected void prepareSolver() throws ContradictionException {
        graph.prepareStb(solver);
        //solver.setTimeout(Integer.MAX_VALUE);
        //solver.setDBSimplificationAllowed(false);
        problem = solver;
    }

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
                solutions.add(graph.interpretSolution(model));
                //System.out.println(graph.interpretSolution(model));
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
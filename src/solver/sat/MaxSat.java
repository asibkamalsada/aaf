package solver.sat;

import graphical.Graph;
import graphical.Vertex;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

import java.util.HashSet;
import java.util.Set;

public abstract class MaxSat {

    final Graph graph;
    protected IProblem problem;
    protected ISolver solver;

    boolean unsat = false;

    /**
     * in seconds
     **/
    private final int TIMEOUT = 45;

    public MaxSat(Graph graph) {
        this.graph = graph;

        ISolver solver = SolverFactory.newDefault();
        this.solver = new ModelIterator(solver);
        this.solver.setTimeout(TIMEOUT);
        //this.solver.setDBSimplificationAllowed(true);

        try {
            prepareSolver();
        } catch ( ContradictionException e ) {
            unsat = true;
        }

    }

    protected abstract void prepareSolver() throws ContradictionException;

    public Set<Set<Vertex>> findSolutions() {

        if ( unsat ) return new HashSet<>();

        long start = System.currentTimeMillis();

        Set<Set<Vertex>> solutions = new HashSet<>();

        // filename is given on the command line
        try {
            boolean unsat = true;

            while ( problem.isSatisfiable() ) {
                unsat = false;
                // do something with each model
                solutions.add(graph.interpretSolution(problem.model()));
                //System.out.println(graph.interpretSolution(problem.model()));
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

package solver.sat;

import graphical.Graph;
import graphical.Vertex;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public abstract class MaxSat {

    private final Graph graph;
    protected String dimacsString;
    /**
     * in seconds
     **/
    private final int TIMEOUT = 600;

    public MaxSat(Graph graph){
        this.graph = graph;
    }

    public Set<Set<Vertex>> findSolutions() {
        InputStream is = new ByteArrayInputStream(dimacsString.getBytes(StandardCharsets.UTF_8));

        long start = System.currentTimeMillis();

        ISolver solver = SolverFactory.newDefault();
        ModelIterator mi = new ModelIterator(solver);
        solver.setTimeout(TIMEOUT);
        Reader reader = new DimacsReader(mi);

        Set<Set<Vertex>> solutions = new HashSet<>();

        // filename is given on the command line
        try {
            boolean unsat = true;
            IProblem problem = reader.parseInstance(is);
            while ( problem.isSatisfiable() ) {
                unsat = false;
                solutions.add(graph.interpretSolution(problem.model()));
                //System.out.println(interpretSolution(model));
                // do something with each model
                //System.out.println(Arrays.toString(model));
            }
            if ( unsat ) {
                System.out.println("no solutions found");
                // do something for unsat case
            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( ParseFormatException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ContradictionException e ) {
            System.out.println("Unsatisfiable (trivial)!");
        } catch ( TimeoutException e ) {
            System.out.println("Timeout, sorry!");
        } finally {
            System.out.println(System.currentTimeMillis() - start);
        }
        return solutions;
    }

}

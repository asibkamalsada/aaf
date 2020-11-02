package solver.sat;

import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import io.SolutionParser;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class PrfMaxSat extends MaxSat {

    private Deque<IConstr> currentAdm;

    public static void main(String[] args) throws IOException {

        Path instance = CodeTesting.instances.resolve(CodeTesting.selfMadeApx);

        Graph g = GraphParser.readGraph(instance);

        /*myPrfSolutions = new HashSet<>();
        Set<Vertex> solution = new HashSet<>();
        solution.add(new Vertex("hallo"));
        myPrfSolutions.add(solution);*/


        final Set<Set<Vertex>> admSolutions = SolutionParser.parseAdmissible(instance, CodeTesting.conarg);

        System.out.println("admissible:");
        admSolutions.forEach(System.out::println);


        final Set<Vertex> grdSolution = SolutionParser.parseGrounded(instance, CodeTesting.conarg);

        System.out.println("grounded:");
        System.out.println(grdSolution);


        final Set<Set<Vertex>> prfSolutions = SolutionParser.parsePreferred(instance, CodeTesting.conarg);

        System.out.println("preferred:");
        prfSolutions.forEach(System.out::println);


        final Set<Set<Vertex>> myPrfSolutions = new PrfMaxSat(g).findSolutions();

        System.out.println("my preferred:");
        myPrfSolutions.forEach(System.out::println);


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

}


/*
biggest_apx;timeout10min;9518solutions
 */
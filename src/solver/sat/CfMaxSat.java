package solver.sat;

import verification.Tester;
import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import org.sat4j.specs.ContradictionException;

import java.io.IOException;
import java.util.Set;

public class CfMaxSat extends MaxSat {

    public static void main(String[] args) throws IOException {

        Graph g = GraphParser.readGraph(
                CodeTesting.instances.resolve(CodeTesting.emptyGrounded)
        );

        CfMaxSat solver = new CfMaxSat(g);

        Set<Set<Vertex>> solutions;
        solutions = solver.findSolutions();
        /*solutions = new HashSet<>();
        Set<Vertex> solution = new HashSet<>();
        solution.add(new Vertex("hallo"));
        solutions.add(solution);*/

        System.out.println(solutions);

        System.out.println(Tester.testConflictFree(solutions,
                CodeTesting.instances.resolve(CodeTesting.longApx), CodeTesting.conarg));

        //writeSolutions(solutions, System.currentTimeMillis() + ".kryo");

    }

    public CfMaxSat(Graph graph) {
        super(graph);
    }

    @Override
    protected void prepareSolver() throws ContradictionException {
        graph.prepareCf(solver);
        problem = solver;
    }

}

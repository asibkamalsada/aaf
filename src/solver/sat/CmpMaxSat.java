package solver.sat;

import benching.Tester;
import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import org.sat4j.specs.ContradictionException;

import java.io.IOException;
import java.util.Set;

public class CmpMaxSat extends MaxSat {

    public static void main(String[] args) throws IOException {

        Graph g = GraphParser.readGraph(
                CodeTesting.instances.resolve(CodeTesting.longApx)
        );

        CmpMaxSat solver = new CmpMaxSat(g);

        Set<Set<Vertex>> solutions;
        solutions = solver.findSolutions();

        System.out.println(solutions.size());

        /*solutions = new HashSet<>();
        Set<Vertex> solution = new HashSet<>();
        solution.add(new Vertex("hallo"));
        solutions.add(solution);*/


        System.out.println(Tester.testComplete(solutions,
                CodeTesting.instances.resolve(CodeTesting.emptyGrounded), CodeTesting.conarg));

        //writeSolutions(solutions, System.currentTimeMillis() + ".kryo");
    }

    public CmpMaxSat(Graph graph) {
        super(graph);
    }

    @Override
    protected void prepareSolver() throws ContradictionException {

    }

}


/*
biggest_apx;timeout10min;9518solutions
 */
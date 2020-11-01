package solver.sat;

import benching.Tester;
import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;

import java.io.IOException;
import java.util.Set;

public class CfMaxSat extends MaxSat {

    public static void main(String[] args) throws IOException {

        int vertexCount = 20;

        String header = "p cnf " + vertexCount + " " + vertexCount / 2 + "\n";

        StringBuilder simpleDimacs = new StringBuilder(header);
        for ( int i = 1; i < vertexCount; i = i + 2 ) {
            simpleDimacs.append(-i).append(" ").append(-(i + 1)).append(" 0 ");
        }

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

        System.out.println(Tester.testConflictFree(solutions,
                CodeTesting.instances.resolve(CodeTesting.emptyGrounded), CodeTesting.conarg));

        //writeSolutions(solutions, System.currentTimeMillis() + ".kryo");

    }

    public CfMaxSat(Graph graph) {
        super(graph);
        dimacsString = graph.getCfDimacs();
    }

}

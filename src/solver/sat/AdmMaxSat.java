package solver.sat;

import benching.Tester;
import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;

import java.io.IOException;
import java.util.Set;

import static io.CustomSolutionParser.writeSolutions;

public class AdmMaxSat extends MaxSat {

    public static void main(String[] args) throws IOException {

        Graph g = GraphParser.readGraph(
                CodeTesting.instances.resolve(CodeTesting.longApx)
        );

        AdmMaxSat solver = new AdmMaxSat(g);

        Set<Set<Vertex>> solutions;
        solutions = solver.findSolutions();

        System.out.println(solutions.size());

        /*solutions = new HashSet<>();
        Set<Vertex> solution = new HashSet<>();
        solution.add(new Vertex("hallo"));
        solutions.add(solution);*/


        /*System.out.println(Tester.testAdmissible(solutions,
                CodeTesting.instances.resolve(CodeTesting.longApx), CodeTesting.conarg));*/

        //writeSolutions(solutions, System.currentTimeMillis() + ".kryo");
    }

    public AdmMaxSat(Graph graph) {
        super(graph);
        dimacsString = graph.getAdmDimacs();
    }

}


/*
biggest_apx;timeout10min;9518solutions
 */
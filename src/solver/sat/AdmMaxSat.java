package solver.sat;

import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import org.sat4j.specs.ContradictionException;
import verification.SolutionParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class AdmMaxSat extends MaxSat {

    public static void main(String[] args) throws IOException {

        Path instance = CodeTesting.instances.resolve(CodeTesting.shortApx);


        final Set<Set<Vertex>> admSolutions = SolutionParser.parseAdmissible(instance, CodeTesting.conarg);

        System.out.println("adm:");
        admSolutions.forEach(System.out::println);

        Graph g = GraphParser.readGraph(instance);

        AdmMaxSat solver = new AdmMaxSat(g);

        Set<Set<Vertex>> mySolutions;
        mySolutions = solver.findSolutions();

        System.out.println("my adm:");
        mySolutions.forEach(System.out::println);

        System.out.println(mySolutions.equals(admSolutions));

        /*System.out.println(Tester.testAdmissible(mySolutions,
                CodeTesting.instances.resolve(CodeTesting.longApx), CodeTesting.conarg));*/

        //writeSolutions(mySolutions, System.currentTimeMillis() + ".kryo");
    }

    public AdmMaxSat(Graph graph) {
        super(graph);
        //dimacsString = graph.getAdmDimacs();
    }

    @Override
    protected void prepareSolver() throws ContradictionException {
        graph.prepareAdm(solver);
        solver.setTimeout(Integer.MAX_VALUE);
        problem = solver;
    }

}


/*
biggest_apx;timeout10min;9518solutions
 */
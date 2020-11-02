package solver.sat;

import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import io.SolutionParser;
import org.sat4j.specs.ContradictionException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class CmpMaxSat extends MaxSat {

    public static void main(String[] args) throws IOException {

        Path instance = CodeTesting.instances.resolve(CodeTesting.emptyGrounded);

        Graph g = GraphParser.readGraph(instance);

        /*myCmpSolution = new HashSet<>();
        Set<Vertex> solution = new HashSet<>();
        solution.add(new Vertex("hallo"));
        myCmpSolution.add(solution);*/


        final Set<Set<Vertex>> admSolutions = SolutionParser.parseAdmissible(instance, CodeTesting.conarg);

        System.out.println("admissible:");
        admSolutions.forEach(System.out::println);


        final Set<Vertex> grdSolution = SolutionParser.parseGrounded(instance, CodeTesting.conarg);

        System.out.println("grounded:");
        System.out.println(grdSolution);


        final Set<Set<Vertex>> cmpSolutions = SolutionParser.parseComplete(instance, CodeTesting.conarg);

        System.out.println("complete:");
        cmpSolutions.forEach(System.out::println);


        final Set<Set<Vertex>> myCmpSolution = new CmpMaxSat(g).findSolutions();

        System.out.println("my complete:");
        myCmpSolution.forEach(System.out::println);


        System.out.println(cmpSolutions.equals(myCmpSolution));

        //writeSolutions(myCmpSolution, System.currentTimeMillis() + ".kryo");
    }

    public CmpMaxSat(Graph graph) {
        super(graph);
    }

    @Override
    protected void prepareSolver() throws ContradictionException {
        graph.prepareCmp(solver);
        problem = solver;
    }

}


/*
biggest_apx;timeout10min;9518solutions
 */
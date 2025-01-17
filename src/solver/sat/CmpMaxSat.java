package solver.sat;

import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import solver.GroundedSolver;
import verification.SolutionParser;
import verification.Tester;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CmpMaxSat extends MaxSat {

    public static void main(String[] args) throws IOException {

        Path testingInstance = CodeTesting.instances.resolve(CodeTesting.shortApx);

        Graph g = GraphParser.readGraph(testingInstance);

        System.out.println(Tester.testGrounded(new GroundedSolver(g).computeGrounded(), testingInstance,
                CodeTesting.conarg));
        System.out.println(Tester.testConflictFree(new CfMaxSat(g).findSolutions(), testingInstance,
                CodeTesting.conarg));
        System.out.println(Tester.testAdmissible(new AdmMaxSat(g).findSolutions(), testingInstance,
                CodeTesting.conarg));

        CmpMaxSat solver = new CmpMaxSat(g);

        Set<Set<Vertex>> solutions;
        solutions = solver.findSolutions();

        System.out.println("my cmp:");
        solutions.forEach(System.out::println);

        final Set<Set<Vertex>> cmpSolutions =
                SolutionParser.parseComplete(testingInstance, CodeTesting.conarg);

        System.out.println("correct cmp:");
        cmpSolutions.forEach(System.out::println);

        System.out.println(solutions.equals(cmpSolutions));

        //writeSolutions(solutions, System.currentTimeMillis() + ".kryo");
    }

    public CmpMaxSat(Graph graph) {
        super(graph);
        //dimacsString = graph.getAdmDimacs();
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
package solver.sat;

import codeTesting.CodeTesting;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import org.sat4j.specs.ContradictionException;
import verification.SolutionParser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class AdmMaxSat extends MaxSat {

    public static void main(String[] args) throws IOException {

        Path instance = CodeTesting.instances.resolve(CodeTesting.emptyGrounded);
        instance = Paths.get("C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba_baumann\\iccma19" +
                "\\instances\\A-2-afinput_exp_cycles_depvary_step4_batch_yyy03.apx");

        final Set<Set<Vertex>> admSolutions = SolutionParser.parseAdmissible(instance, CodeTesting.conarg);

        System.out.println("adm:");
        admSolutions.forEach(System.out::println);

        Graph g = GraphParser.readAdmKernelGraph(instance);

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
        solver.setTimeout(15 * 60);
        problem = solver;
    }

}


/*
biggest_apx;timeout10min;9518solutions
 */
package codeTesting;

import verification.Tester;
import graphical.Graph;
import io.GraphParser;
import verification.SolutionParser;
import solver.iterative.AdmIterator;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CodeTesting {

    public static Path root = Paths.get("C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba_baumann\\iccma19");

    public static Path instances = root.resolve("instances");

    public static Path graphs = root.resolve("graphs");

    public static Path solutions = root.resolve("reference-results");

    public static Path conarg = root.resolve("conarg").resolve("distribution").resolve("conarg.exe");

    public static Path customSolutions = root.resolve("custom-solutions");


    public static String longTgf = "T-4-grd_8020_3_4.tgf";
    /*
     * build: 2.431s
     * 1. run successors: 127.624s
     * 1. run predecessors: 142.606s
     *
     * calcPreSucc: 0.728s
     *
     * serializable without pre/succ:
     * writing: 18.387s
     * reading: 44.465s
     *
     * kryo without pre/succ:
     * writing: 0.270s
     * reading: 1.802s
     *
     * naive grounded: 2.743s
     *
     */
    public static String emptyGrounded = "Small-result-b83.apx";
    public static String smallGrounded = "T-1-cascadespoint-or-us.gml.20.apx";

    public static String surabsVariable = "A-3-afinput_exp_acyclic_depvary_step6_batch_yyy04.apx";

    public static String longApx = "T-4-grd_8020_3_4.apx";
    public static String shortApx = "B-1-BA_40_60_2.apx";

    public static String selfMadeApx = "test.apx";

    public static void main(String[] args) throws Exception {

        String currentFileName = emptyGrounded;

        Path currentInstance = instances.resolve(currentFileName);
        Path currentGraphFile = graphs.resolve(currentFileName + ".bin");
        Path currentGroundedSolution = solutions.resolve("instances-" + currentFileName + "m-SE-GR-D.out");

        long start0 = System.currentTimeMillis();
        Graph normalGraph = GraphParser.readGraph(currentInstance);
        System.out.println("Graph-building: " + (System.currentTimeMillis() - start0));

        /*
        Graph copiedGraph = Graph.copy(normalGraph);
        for ( int i = 1; i <= 2; i++ ) {
            long start1 = System.currentTimeMillis();
            normalGraph.getVertices().forEach(normalGraph::predecessors);
            System.out.println(i + ". run predecessors: " + (System.currentTimeMillis() - start1));
        }
        for ( int i = 1; i <= 2; i++ ) {
            long start1 = System.currentTimeMillis();
            normalGraph.getVertices().forEach(normalGraph::successors);
            System.out.println(i + ". run successors: " + (System.currentTimeMillis() - start1));
        }

        long start4 = System.currentTimeMillis();
        copiedGraph.calcPreSucc();
        System.out.println("calcPreSucc: " + (System.currentTimeMillis() - start4));

        normalGraph = Graph.copy(copiedGraph);


        long start2 = System.currentTimeMillis();
        writeGraphToFile(normalGraph, currentGraphFile.toString());
        System.out.println("writing: " + (System.currentTimeMillis() - start2));

        long start3 = System.currentTimeMillis();
        Graph graphFromFile = readGraphFromFile(currentGraphFile.toString());
        System.out.println("reading: " + (System.currentTimeMillis() - start3));

        System.out.println("vertices: " + copiedGraph.getVertices());
        System.out.println("predecessors:");
        copiedGraph.getAllPredecessors().forEach((key, value) -> System.out.println(key + "->" + value));
        System.out.println("successors:");
        copiedGraph.getAllSuccessors().forEach((key, value) -> System.out.println(key + "->" + value));
        */
/*
        GroundedSolver groundedSolver = new GroundedSolver(normalGraph);

        long start5 = System.currentTimeMillis();
        naiveSolver.computeGrounded();
        System.out.println("grounded naive time: " + (System.currentTimeMillis() - start5));

        GroundedBenchmarker gb = new GroundedBenchmarker(root);

        System.out.println(groundedSolver.computeGrounded());

        System.out.println(Tester.testGrounded(groundedSolver.computeGrounded(), gb.solutionPath(currentInstance)));
        */
/*
        Path currentConflictFreeSolution = null;
        long start6 = System.currentTimeMillis();

        final Set<Set<Vertex>> mySolution = naiveSolver.computeConflictFree();

        final Set<Set<Vertex>> correctSolution = SolutionParser.parseConflictFree(currentInstance, conarg);

        System.out.println("my solution:[");
        mySolution.forEach(result -> System.out.println("\t" + result));
        System.out.println("]\n");

        System.out.println("my\\correct:[");
        mySolution.stream().filter(o -> !correctSolution.contains(o)).forEach(result -> System.out.println("\t" +
                result));
        System.out.println("]\n");


        System.out.println("correct\\my:[");
        correctSolution.stream().filter(o -> !mySolution.contains(o)).forEach(result -> System.out.println("\t" +
                result));
        System.out.println("]");

        System.out.println(mySolution.equals(correctSolution));
*/
        //System.out.println(Tester.testConflictFree(, currentInstance, conarg));

        //test whether predecessor and successor work:
        /*{
            Set<Vertex> vertices = new HashSet<>(normalGraph.getVertices());
            Set<Edge> edges = new HashSet<>();

            normalGraph.getAllSuccessors().forEach((attacker, attackeds) -> {
                attackeds.forEach(attacked -> {
                    edges.add(new Edge(attacker, attacked));
                });
            });

            Graph successorGraph = new Graph(vertices, edges);

            System.out.println(normalGraph.equals(successorGraph));
        }

        {
            Set<Vertex> vertices = new HashSet<>(normalGraph.getVertices());
            Set<Edge> edges = new HashSet<>();

            normalGraph.getVertices().forEach(attacked -> {
                normalGraph.predecessors(attacked).forEach(attacker -> {
                    edges.add(new Edge(attacker, attacked));
                });
            });

            Graph predecessorGraph = new Graph(vertices, edges);

            System.out.println(normalGraph.equals(predecessorGraph));
        }*/
        /*
        for ( Vertex a : normalGraph.getVertices() ) {
            System.out.print(normalGraph.predecessors(a));
            System.out.println(naiveSolver.legalOptions(normalGraph.predecessors(a)).count());
        }
        */
/*
        StringBuilder sb = new StringBuilder("[\n");

        naiveSolver.iterativerAnsatz().forEach((depth, resultLayer) -> {
            sb.append("\t").append(resultLayer.size()).append(" ");
            resultLayer.forEach(result -> sb.append(result).append(" "));
            sb.append("\n");
        });
        sb.append("]");

        System.out.println(sb);

        System.out.println("computeConflictFree: " + (System.currentTimeMillis() - start6));
*/

        System.out.println(
                Tester.iterativeTest(
                        new AdmIterator(normalGraph),
                        SolutionParser.parseAdmissible(currentInstance, conarg)
                )
        );

        //System.out.println(naiveSolver.printPreferred());

    }

}

package io;

import solver.NaiveSolver;
import benching.Tester;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import graphical.Edge;
import graphical.Graph;
import graphical.Vertex;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraphParser {

    private static final Kryo kryo = new Kryo();

    static {
        kryo.register(HashSet.class);
        kryo.register(HashMap.class);

        kryo.register(Graph.class);
        kryo.register(Vertex.class);
        kryo.register(Edge.class);
    }

    private static final String l = "([^,\\s]+)";

    private static final Pattern apxVertex = Pattern.compile("arg\\(" + l + "\\)\\.");
    private static final Pattern apxEdge = Pattern.compile("att\\(" + l + "," + l + "\\)\\.");

    private static final Pattern tgfVertex = Pattern.compile("a" + l);
    private static final Pattern tgfEdge = Pattern.compile("a" + l + "\\sa" + l);

    public static Graph readGraph(Path instancesPath, String filename) throws Exception {
        return readGraph(instancesPath.resolve(filename));
    }

    public static Graph readGraph(Path absolutePath) throws Exception {
        String fileExtension = absolutePath.toString().substring(absolutePath.toString().lastIndexOf('.') + 1);
        Pattern vertexPattern;
        Pattern edgePattern;
        switch ( fileExtension ) {
            case "apx":
                vertexPattern = apxVertex;
                edgePattern = apxEdge;
                break;
            case "tgf":
                vertexPattern = tgfVertex;
                edgePattern = tgfEdge;
                break;
            default:
                throw new Exception("File was not readable (maybe incorrect file extension)");
        }

        return Files.lines(absolutePath).collect(
                Graph::new,
                (graph, line) -> {
                    Matcher vertexMatcher = vertexPattern.matcher(line);
                    Matcher edgeMatcher = edgePattern.matcher(line);
                    if ( vertexMatcher.matches() ) {
                        graph.addVertex(vertexMatcher.group(1));
                    } else if ( edgeMatcher.matches() ) {
                        graph.addEdge(edgeMatcher.group(1), edgeMatcher.group(2));
                    }
                },
                Graph::addGraph
        );
    }

    public static void main(String[] args) throws Exception {

        Path root = Paths.get("C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba baumann\\iccma19");

        Path instances = root.resolve("instances");

        Path graphs = root.resolve("graphs");

        Path solutions = root.resolve("reference-results");

        String longTgf = "T-4-grd_8020_3_4.tgf";
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
        String longApx = "T-4-grd_8020_3_4.apx";
        String shortApx = "B-1-BA_40_60_2.apx";

        String currentFileName = longApx;

        Path currentInstance = instances.resolve(currentFileName);
        Path currentGraphFile = graphs.resolve(currentFileName + ".bin");
        Path currentGroundedSolution = solutions.resolve("instances-" + currentFileName + "m-SE-GR-D.out");

        long start0 = System.currentTimeMillis();
        Graph normalGraph = readGraph(currentInstance);
        System.out.println("Graph-building: " + (System.currentTimeMillis() - start0));

        Graph copiedGraph = Graph.copy(normalGraph);
        /*
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
        */
        long start4 = System.currentTimeMillis();
        copiedGraph.calcPreSucc();
        System.out.println("calcPreSucc: " + (System.currentTimeMillis() - start4));

        normalGraph = Graph.copy(copiedGraph);

        /*
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

        NaiveSolver naiveSolver = new NaiveSolver(normalGraph);

        long start5 = System.currentTimeMillis();
        naiveSolver.computeGrounded();
        System.out.println("grounded naive time: " + (System.currentTimeMillis() - start5));

        System.out.println(Tester.testGrounded(naiveSolver.computeGrounded(), currentGroundedSolution));

    }

    private static void writeGraphToFile(Graph g, String kryoPath) throws FileNotFoundException {
        try ( Output output = new Output(new FileOutputStream(kryoPath)) ) {
            kryo.writeObject(output, g);
        }
    }

    private static Graph readGraphFromFile(String kryoPath) throws FileNotFoundException {
        try ( Input input = new Input(new FileInputStream(kryoPath)) ) {
            return kryo.readObject(input, Graph.class);
        }
    }
}

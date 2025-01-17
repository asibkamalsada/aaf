package io;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import graphical.Edge;
import graphical.Graph;
import graphical.Vertex;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GraphParser {

    private static final Kryo kryo = new Kryo();

    static {
        kryo.register(HashSet.class);
        kryo.register(HashMap.class);
        kryo.register(ArrayList.class);

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

    public static Graph readGraph(Path absolutePath) throws IOException {
        Graph result = readUnprocessedGraph(absolutePath);
        result.calcIndices();
        result.calcPreSucc();
        return result;
    }

    public static Graph readUnprocessedGraph(Path absolutePath) throws IOException {
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
                throw new FileNotFoundException("File was not readable (maybe incorrect file extension)");
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
                Graph::mergeVerticesAndEdges
        );
    }

//----------------------------------------------------------------------------------------------------------------------

    public static Graph readStbKernelGraph(Path absolutePath) throws IOException {
        final Graph graph = readUnprocessedGraph(absolutePath);
        final Set<Vertex> selfAttacked = graph.getVertices().parallelStream()
                .filter(v -> graph.getEdges().contains(new Edge(v, v)))
                .collect(Collectors.toSet());
        graph.getEdges().removeIf(edge -> !edge.isSelfAttack() && selfAttacked.contains(edge.attacker()));
        graph.calcIndices();
        graph.calcPreSucc();
        return graph;
    }

//----------------------------------------------------------------------------------------------------------------------

    public static Graph readAdmKernelGraph(Path absolutePath) throws IOException {
        final Graph graph = readUnprocessedGraph(absolutePath);
        final Set<Edge> newEdges = graph.getEdges().parallelStream()
                .filter(edge -> {
                    final Vertex attacker = edge.attacker();
                    final Vertex attacked = edge.attacked();
                    final Set<Edge> edges = graph.getEdges();
                    return !edge.isSelfAttack() &&
                            edges.contains(new Edge(attacker, attacker)) &&
                            (edges.contains(new Edge(attacked, attacker)) ||
                                    edges.contains(new Edge(attacked, attacked)));
                })
                .collect(Collectors.toSet());
        graph.getEdges().removeAll(newEdges);
        graph.calcIndices();
        graph.calcPreSucc();
        return graph;
    }

//----------------------------------------------------------------------------------------------------------------------

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

package graphical;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Graph implements Serializable, Externalizable {

    private final static long serialVersionUID = -3393718104825315346L;

    private final Set<Vertex> vertices;
    private final Set<Edge> edges;

    private final Map<Vertex, Set<Vertex>> successors;
    private final Map<Vertex, Set<Vertex>> predecessors;

    public Graph(Set<Vertex> vertices, Set<Edge> edges) {
        this.vertices = vertices;
        this.edges = edges;
        successors = new HashMap<>();
        predecessors = new HashMap<>();
    }

    public Graph() {
        vertices = new HashSet<>();
        edges = new HashSet<>();
        successors = new HashMap<>();
        predecessors = new HashMap<>();
    }

    public void addVertex(Vertex n) {
        vertices.add(n);
    }

    public void addVertex(String label) {
        addVertex(new Vertex(label));
    }

    public void addEdge(Edge e) {
        edges.add(e);
    }

    public void addEdge(Vertex n1, Vertex n2) {
        addEdge(new Edge(n1, n2));
    }

    public void addEdge(String label1, String label2) {
        addEdge(new Vertex(label1), new Vertex(label2));
    }

    public static Graph copy(Graph g) {
        return new Graph(new HashSet<>(g.getVertices()), new HashSet<>(g.getEdges()));
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public Set<Vertex> successors(Vertex n) {
        Set<Vertex> oldValue = successors.get(n);
        if ( oldValue == null ) {
            Set<Vertex> newValue = edges.parallelStream()
                    .filter(e -> e.getAttacker().equals(n))
                    .map(Edge::getAttacked)
                    .collect(Collectors.toSet());
            successors.put(n, newValue);
            return newValue;
        }
        return oldValue;
    }

    public Set<Vertex> predecessors(Vertex n) {
        Set<Vertex> oldValue = predecessors.get(n);
        if ( oldValue == null ) {
            Set<Vertex> newValue = edges.parallelStream()
                    .filter(e -> e.getAttacked().equals(n))
                    .map(Edge::getAttacker)
                    .collect(Collectors.toSet());
            predecessors.put(n, newValue);
            return newValue;
        }
        return oldValue;
    }

    public void addGraph(Graph graph2) {
        vertices.addAll(graph2.getVertices());
        edges.addAll(graph2.getEdges());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}

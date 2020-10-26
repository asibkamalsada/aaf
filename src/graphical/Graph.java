package graphical;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Graph implements Serializable {

    private final static long serialVersionUID = -3393718104825315346L;

    private final Set<Vertex> vertices;
    private final Set<Edge> edges;

    private final Map<Vertex, Set<Vertex>> allSuccessors;
    private final Map<Vertex, Set<Vertex>> allPredecessors;

    public Graph(Set<Vertex> vertices, Set<Edge> edges, Map<Vertex, Set<Vertex>> allSuccessors, Map<Vertex,
            Set<Vertex>> allPredecessors) {
        this.vertices = vertices;
        this.edges = edges;
        this.allSuccessors = allSuccessors;
        this.allPredecessors = allPredecessors;
    }

    public Graph(Set<Vertex> vertices, Set<Edge> edges) {
        this.vertices = vertices;
        this.edges = edges;
        allSuccessors = new HashMap<>();
        allPredecessors = new HashMap<>();
    }

    public Graph() {
        vertices = new HashSet<>();
        edges = new HashSet<>();
        allSuccessors = new HashMap<>();
        allPredecessors = new HashMap<>();
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

    public Set<Vertex> getVertices() {
        return vertices;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public Map<Vertex, Set<Vertex>> getAllSuccessors() {
        return allSuccessors;
    }

    public Map<Vertex, Set<Vertex>> getAllPredecessors() {
        return allPredecessors;
    }

    //----------------------------------------------------------------------------------------------------------------------

    public static Graph copy(Graph g) {
        HashMap<Vertex, Set<Vertex>> newPredecessors = new HashMap<>();
        g.getAllPredecessors().forEach((key, value) -> newPredecessors.put(key, new HashSet<>(value)));
        HashMap<Vertex, Set<Vertex>> newSuccessors = new HashMap<>();
        g.getAllSuccessors().forEach((key, value) -> newSuccessors.put(key, new HashSet<>(value)));
        return new Graph(new HashSet<>(g.getVertices()), new HashSet<>(g.getEdges()), newPredecessors, newSuccessors);
    }

    public void addGraph(Graph graph2) {
        vertices.addAll(graph2.getVertices());
        edges.addAll(graph2.getEdges());
    }

    public void removeVertex(Vertex vertex) {
        vertices.remove(vertex);
        edges.removeIf(edge -> edge.getAttacker().equals(vertex) || edge.getAttacked().equals(vertex));
    }

    public void removeIf(Predicate<? super Vertex> filter) {
        Set<Vertex> toBeRemoved = vertices.parallelStream().filter(filter).collect(Collectors.toSet());
        vertices.removeAll(toBeRemoved);
        edges.removeIf(edge -> toBeRemoved.contains(edge.getAttacker()) || toBeRemoved.contains(edge.getAttacked()));
    }

//----------------------------------------------------------------------------------------------------------------------

    public Set<Vertex> successors(Vertex n) {
        Set<Vertex> oldValue = allSuccessors.get(n);
        if ( oldValue == null ) {
            Set<Vertex> newValue = edges.parallelStream()
                    .filter(e -> e.getAttacker().equals(n))
                    .map(Edge::getAttacked)
                    .collect(Collectors.toSet());
            allSuccessors.put(n, newValue);
            return newValue;
        }
        return oldValue;
    }

    public Set<Vertex> predecessors(Vertex n) {
        Set<Vertex> oldValue = allPredecessors.get(n);
        if ( oldValue == null ) {
            Set<Vertex> newValue = edges.parallelStream()
                    .filter(e -> e.getAttacked().equals(n))
                    .map(Edge::getAttacker)
                    .collect(Collectors.toSet());
            allPredecessors.put(n, newValue);
            return newValue;
        }
        return oldValue;
    }

    public Set<Vertex> getUnattacked() {
        return vertices.parallelStream()
                .filter(vertex -> predecessors(vertex).isEmpty())
                .collect(Collectors.toSet());
    }

    public void calcPreSucc() {
        List<HashMap<Vertex, Set<Vertex>>> l = edges.stream().collect(
                () -> new ArrayList<>(Arrays.asList(new HashMap<>(), new HashMap<>())),
                (maps, edge) -> {
                    HashMap<Vertex, Set<Vertex>> tempSuccessors = maps.get(0);
                    if ( tempSuccessors.containsKey(edge.getAttacker()) ) {
                        tempSuccessors.get(edge.getAttacker()).add(edge.getAttacked());
                    } else {
                        tempSuccessors.put(edge.getAttacker(),
                                new HashSet<>(Collections.singleton(edge.getAttacked())));
                    }

                    HashMap<Vertex, Set<Vertex>> tempPredecessors = maps.get(1);
                    if ( tempPredecessors.containsKey(edge.getAttacked()) ) {
                        tempPredecessors.get(edge.getAttacked()).add(edge.getAttacker());
                    } else {
                        tempPredecessors.put(edge.getAttacked(),
                                new HashSet<>(Collections.singleton(edge.getAttacker())));
                    }
                },
                (maps1, maps2) -> {
                    maps1.get(0).putAll(maps2.get(0));
                    maps1.get(1).putAll(maps2.get(1));
                }
        );

        vertices.forEach(vertex -> {
            allSuccessors.putIfAbsent(vertex, new HashSet<>());
            allPredecessors.putIfAbsent(vertex, new HashSet<>());
        });

        allSuccessors.putAll(l.get(0));
        allPredecessors.putAll(l.get(1));
    }

//----------------------------------------------------------------------------------------------------------------------


    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Graph graph = (Graph) o;
        return vertices.equals(graph.vertices) &&
                edges.equals(graph.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertices, edges);
    }
}

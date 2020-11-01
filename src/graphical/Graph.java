package graphical;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Graph implements Serializable {

    private final static long serialVersionUID = -3393718104825315346L;

    private String cfDimacs = null;

    protected final Set<Vertex> vertices;
    protected final Set<Edge> edges;

    protected Map<Vertex, Set<Vertex>> allSuccessors;
    protected Map<Vertex, Set<Vertex>> allPredecessors;

    private Map<Vertex, Integer> vertexToIndex;
    private Map<Integer, Vertex> indexToVertex;
    private List<Vertex> orderedVertices;

    public Graph(Set<Vertex> vertices, Set<Edge> edges, Map<Vertex, Set<Vertex>> allSuccessors, Map<Vertex,
            Set<Vertex>> allPredecessors) {
        this.vertices = vertices;
        this.edges = edges;
        this.allSuccessors = allSuccessors;
        this.allPredecessors = allPredecessors;
        calcIndices();
        calcPreSucc();
    }

    public Graph() {
        this.vertices = new HashSet<>();
        this.edges = new HashSet<>();
    }

    public void calcIndices() {
        if ( orderedVertices == null || vertexToIndex == null || indexToVertex == null ) {
            orderedVertices = vertices.parallelStream().sorted().collect(Collectors.toList());
            vertexToIndex = orderedVertices.parallelStream()
                    .collect(Collectors.toMap(Function.identity(), o -> orderedVertices.indexOf(o) + 1));
            indexToVertex = vertexToIndex.entrySet().parallelStream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        } else {
            System.err.println("Indices for graph already calculated");
        }
    }

//----------------------------------------------------------------------------------------------------------------------

    public static TemporaryGraph temporaryCopy(Graph g) {
        return new TemporaryGraph(g);
    }

    public void mergeVerticesAndEdges(Graph graph2) {
        vertices.addAll(graph2.getVertices());
        edges.addAll(graph2.getEdges());
    }

//----------------------------------------------------------------------------------------------------------------------

    public Set<Vertex> successors(Vertex n) {
        return allSuccessors.get(n);
    }

    public Set<Vertex> predecessors(Vertex n) {
        return allPredecessors.get(n);
    }

    public void calcPreSucc() {
        if ( allPredecessors == null && allSuccessors == null ) {
            allPredecessors = new HashMap<>();
            allSuccessors = new HashMap<>();

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
        } else {
            System.err.println("PreSucc already calculated");
        }
    }

    public Set<Vertex> getUnattacked() {
        return vertices.parallelStream()
                .filter(vertex -> predecessors(vertex).isEmpty())
                .collect(Collectors.toSet());
    }

    public Set<Vertex> getFreeVertices() {
        return vertices.parallelStream()
                .filter(vertex -> predecessors(vertex).isEmpty() && successors(vertex).isEmpty())
                .collect(Collectors.toSet());
    }

//----------------------------------------------------------------------------------------------------------------------

    public String getCfDimacs() {
        if ( cfDimacs != null ) return cfDimacs;
        String header = "p cnf " + vertices.size() + " " + edges.size() + "\n";

        String body = edges.stream()
                .collect(StringBuilder::new,
                        (sb, edge) -> sb
                                .append(-vertexToIndex.get(edge.getAttacker()))
                                .append(" ")
                                .append(-vertexToIndex.get(edge.getAttacked()))
                                .append(" 0 "),
                        StringBuilder::append)
                .toString();

        return (cfDimacs = header + body);
    }

    public String getAdmDimacs() {
        return null;
    }

    public Set<Set<Vertex>> interpretSolutions(Set<int[]> models) {
        return models.parallelStream()
                .map(this::interpretSolution)
                .collect(Collectors.toSet());
    }

    public Set<Vertex> interpretSolution(int[] model) {
        return Arrays.stream(model).boxed().parallel()
                .filter(value -> value > 0)
                .map(indexToVertex::get)
                .collect(Collectors.toSet());
    }

//----------------------------------------------------------------------------------------------------------------------


    public static class TemporaryGraph extends Graph {
        public TemporaryGraph(Graph g) {
            super();
            mergeVerticesAndEdges(g);
            if ( g.allPredecessors == null || g.allSuccessors == null ) {
                calcPreSucc();
            } else {
                this.allPredecessors = new HashMap<>();
                g.allPredecessors.forEach((key, value) -> this.allPredecessors.put(key, new HashSet<>(value)));
                this.allSuccessors = new HashMap<>();
                g.allSuccessors.forEach((key, value) -> this.allSuccessors.put(key, new HashSet<>(value)));
            }
        }

        public void removeVertex(Vertex vertex) {
            vertices.remove(vertex);
            edges.removeIf(edge -> edge.getAttacker().equals(vertex) || edge.getAttacked().equals(vertex));

            allPredecessors.values().forEach(predecessor -> predecessor.remove(vertex));
            allPredecessors.remove(vertex);

            allSuccessors.values().forEach(successor -> successor.remove(vertex));
            allSuccessors.remove(vertex);
        }

        public boolean removeIf(Predicate<? super Vertex> filter) {
            Set<Vertex> toBeRemoved = vertices.parallelStream().filter(filter).collect(Collectors.toSet());
            return removeVertices(toBeRemoved);
        }

        public boolean removeVertices(Collection<Vertex> toBeRemoved) {
            final boolean b = vertices.removeAll(toBeRemoved);
            edges.removeIf(edge -> toBeRemoved.contains(edge.getAttacker()) || toBeRemoved.contains(edge.getAttacked()));

            allPredecessors.values().forEach(predecessor -> predecessor.removeAll(toBeRemoved));
            allPredecessors.keySet().removeAll(toBeRemoved);

            allSuccessors.values().forEach(successor -> successor.removeAll(toBeRemoved));
            allSuccessors.keySet().removeAll(toBeRemoved);

            return b;

        }

    }

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

    @Override
    public String toString() {
        return "Graph{" +
                "vertices=" + vertices +
                ", edges=" + edges +
                '}';
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

}

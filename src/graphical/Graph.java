package graphical;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import solver.GroundedSolver;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Graph implements Serializable {

    private final static long serialVersionUID = -3393718104825315346L;

    private String cfDimacs = null;
    private String admDimacs = null;
    private String cfDimacsBody = null;

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
                        if ( tempSuccessors.containsKey(edge.attacker()) ) {
                            tempSuccessors.get(edge.attacker()).add(edge.attacked());
                        } else {
                            tempSuccessors.put(edge.attacker(),
                                    new HashSet<>(Collections.singleton(edge.attacked())));
                        }

                        HashMap<Vertex, Set<Vertex>> tempPredecessors = maps.get(1);
                        if ( tempPredecessors.containsKey(edge.attacked()) ) {
                            tempPredecessors.get(edge.attacked()).add(edge.attacker());
                        } else {
                            tempPredecessors.put(edge.attacked(),
                                    new HashSet<>(Collections.singleton(edge.attacker())));
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

    // TODO this is executed twice, second time just for the size
    public Set<Vertex> getFreeVertices() {
        return vertices.parallelStream()
                .filter(vertex -> predecessors(vertex).isEmpty() && successors(vertex).isEmpty())
                .collect(Collectors.toSet());
    }

//----------------------------------------------------------------------------------------------------------------------

    public int prepareCf(ISolver solver) throws ContradictionException {
        solver.newVar(vertices.size());
        int nClauses = getFreeVertices().size() + edges.size();
        solver.setExpectedNumberOfClauses(nClauses);

        addCfClauses(solver);

        return nClauses;
    }

    private void addCfClauses(ISolver solver) throws ContradictionException {
/*
        Vec<IVecInt> atomarClauses = getFreeVertices().stream()//.parallel().unordered()
                .map(vertex -> new VecInt(new int[]{ vertexToIndex.get(vertex), -vertexToIndex.get(vertex) }))
                .collect(Vec::new, Vec::push, Vec::moveTo);

        Vec<IVecInt> cfClauses = edges.stream()//.parallel().unordered()
                .map(edge -> new VecInt(new int[]{
                        -vertexToIndex.get(edge.attacker()),
                        -vertexToIndex.get(edge.attacked())
                }))
                .collect(Vec::new, Vec::push, Vec::moveTo);

        atomarClauses.moveTo(cfClauses);

        solver.addAllClauses(cfClauses);
*/
        for ( Vertex vertex : getFreeVertices() ) {
            solver.addClause(new VecInt(new int[]{ vertexToIndex.get(vertex), -vertexToIndex.get(vertex) }));
        }

        for ( Edge edge : edges ) {
            solver.addClause(new VecInt(new int[]{
                    -vertexToIndex.get(edge.attacker()),
                    -vertexToIndex.get(edge.attacked())
            }));
        }
    }

    public int prepareAdm(ISolver solver) throws ContradictionException {
        int nClauses = prepareCf(solver);
        nClauses += getAllPredecessors().entrySet().parallelStream()
                .mapToInt(entry -> entry.getValue().size())
                .sum();
        solver.setExpectedNumberOfClauses(nClauses);

        for ( Vertex vertex : vertices ) {
            for ( Vertex attacker : predecessors(vertex) ) {
                IVecInt vecInt = new VecInt(new int[]{ -vertexToIndex.get(vertex) });
                for ( Vertex defender : predecessors(attacker) ) {
                    vecInt.push(vertexToIndex.get(defender));
                }
                solver.addClause(vecInt);
            }
        }

        return nClauses;
    }

    public void prepareCmp(ISolver solver) throws ContradictionException {
        int nClauses = prepareAdm(solver);

        GroundedSolver groundedSolver = new GroundedSolver(this);
        final Set<Vertex> grounded = groundedSolver.computeGrounded();

        if ( !grounded.isEmpty() ) {

            nClauses += grounded.size();

            solver.setExpectedNumberOfClauses(nClauses);

            for ( Vertex vertex : grounded ) {
                solver.addClause(new VecInt(new int[]{ vertexToIndex.get(vertex) }));
            }
        } else {
            throw new ContradictionException("grounded is empty :(");
        }

    }

    @Deprecated
    public String getCfDimacs() {
        if ( cfDimacs != null ) return cfDimacs;
        String header = "p cnf " + vertices.size() + " " + getFreeVertices().size() + edges.size() + "\n";

        return (cfDimacs = header + getCfDimacsBody());
    }

    @Deprecated
    public String getCfDimacsBody() {
        if ( cfDimacsBody != null ) return cfDimacsBody;

        String freeVertices = getFreeVertices().parallelStream().unordered()
                .map(vertex -> {
                    int index = vertexToIndex.get(vertex);
                    return index + " " + -index + " 0";
                })
                .collect(Collectors.joining(" "));

        return cfDimacsBody = " " + freeVertices + " " + edges.parallelStream().unordered()
                .collect(StringBuilder::new,
                        (dimacs, edge) -> dimacs
                                .append(-vertexToIndex.get(edge.attacker()))
                                .append(" ")
                                .append(-vertexToIndex.get(edge.attacked()))
                                .append(" 0 "),
                        StringBuilder::append)
                .toString();
    }

    @Deprecated
    public String getAdmDimacs() {
        if ( admDimacs != null ) return admDimacs;

        int clauseCount = edges.size() + getFreeVertices().size();

        int predecessorsCount = getAllPredecessors().entrySet().parallelStream()
                .mapToInt(entry -> entry.getValue().size())
                .sum();

        String header = "p cnf " + vertices.size() + " " + (clauseCount + predecessorsCount) + "\n";

        String admDimacsBody = " " +
                vertices.parallelStream().unordered()
                        .collect(StringBuilder::new,
                                (dimacs, vertex) -> dimacs.append(defenders(vertex)),
                                StringBuilder::append).toString();

        return (admDimacs = header + getCfDimacsBody() + admDimacsBody);
    }

    @Deprecated
    private String defenders(Vertex vertex) {
        return predecessors(vertex).parallelStream()
                .collect(StringBuilder::new,
                        (clauses, attacker) -> clauses.append(attackers(vertex, attacker)),
                        StringBuilder::append)
                .toString();
    }

    @Deprecated
    private String attackers(Vertex vertex, Vertex attacker) {
        return -vertexToIndex.get(vertex) + " " + attackersOf(attacker) + " 0 ";
    }

    @Deprecated
    private String attackersOf(Vertex vertex) {
        return predecessors(vertex).parallelStream().unordered()
                .map(attacker -> vertexToIndex.get(attacker).toString())
                .collect(Collectors.joining(" "));
    }

//----------------------------------------------------------------------------------------------------------------------

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
            edges.removeIf(edge -> edge.attacker().equals(vertex) || edge.attacked().equals(vertex));

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
            edges.removeIf(edge -> toBeRemoved.contains(edge.attacker()) || toBeRemoved.contains(edge.attacked()));

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

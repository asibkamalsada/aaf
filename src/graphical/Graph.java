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
    protected Set<Edge> edges;

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
/*
        Vec<IVecInt> atomarClauses = getFreeVertices().stream()//.parallel().unordered()
                .map(vertex -> new VecInt(new int[]{ vertexToInt(vertex), -vertexToInt(vertex) }))
                .collect(Vec::new, Vec::push, Vec::moveTo);

        Vec<IVecInt> cfClauses = edges.stream()//.parallel().unordered()
                .map(edge -> new VecInt(new int[]{
                        -vertexToInt(edge.attacker()),
                        -vertexToInt(edge.attacked())
                }))
                .collect(Vec::new, Vec::push, Vec::moveTo);

        atomarClauses.moveTo(cfClauses);

        solver.addAllClauses(cfClauses);
*/
        for ( Vertex vertex : getFreeVertices() ) {
            solver.registerLiteral(vertexToInt(vertex));
        }

        for ( Edge edge : edges ) {
            solver.addClause(new VecInt(new int[]{
                    -vertexToInt(edge.attacker()),
                    -vertexToInt(edge.attacked())
            }));
        }
        return nClauses;
    }

    public int prepareStb(ISolver solver) throws ContradictionException {

        int nClauses = prepareCf(solver);

        nClauses += orderedVertices.size();

        solver.setExpectedNumberOfClauses(nClauses);

        for ( int index = 0; index < orderedVertices.size(); index++ ) {
            Vertex vertex = orderedVertices.get(index);

            Set<Vertex> predecessors = predecessors(vertex);
            IVecInt clause = new VecInt(1 + predecessors.size());
            // wenn ein Knoten nicht enthalten ist, muss mindestens einer der Angreifer enthalten sein
            // -index -> p1 oder p2 oder p3
            // index oder p1 oder p2 oder p3
            clause.push(index + 1);

            for ( Vertex attacker : predecessors ) {
                clause.push(vertexToIndex.get(attacker));
            }

            solver.addClause(clause);

        }

        return nClauses;

    }

    //TODO test if that didnt actually break it
    public int preparePrf(ISolver solver) throws ContradictionException {

        return addGrounded(solver, prepareAdm(solver));
        // without preprocessing grounded, it is exponentially slower
        //return prepareAdm(solver);
    }

    public int prepareAdm(ISolver solver) throws ContradictionException {
        int nClauses = prepareCf(solver);
        nClauses += getAllPredecessors().entrySet().parallelStream()
                .mapToInt(entry -> entry.getValue().size())
                .sum();
        solver.setExpectedNumberOfClauses(nClauses);

        // TODO try to replace this by an indexed for loop over orderedVertices
        for ( Vertex vertex : vertices ) {
            for ( Vertex attacker : predecessors(vertex) ) {
                IVecInt clause = new VecInt(new int[]{ -vertexToInt(vertex) });
                for ( Vertex defender : predecessors(attacker) ) {
                    clause.push(vertexToInt(defender));
                }
                solver.addClause(clause);
            }
        }

        return nClauses;
    }

    // TODO not working
    public void prepareCmp(ISolver solver) throws ContradictionException {

        int nClauses = 0;
        try {
            nClauses = prepareCf(solver);
        } catch ( ContradictionException e ) {
            System.err.println("contradiction building cf");
            throw e;
        }

        GroundedSolver groundedSolver = new GroundedSolver(this);
        final Set<Vertex> grounded = groundedSolver.computeGrounded();

        if ( grounded.isEmpty() ) throw new ContradictionException("empty grounded, sry");

        nClauses += grounded.size();

        solver.setExpectedNumberOfClauses(nClauses);

        long start = System.currentTimeMillis();
        for ( Vertex vertex : grounded ) {
            try {
                solver.addClause(new VecInt(new int[]{ vertexToInt(vertex) }));
            } catch ( ContradictionException e ) {
                System.err.println("contradiction adding grounded");
                throw e;
            }
        }

        //TODO find correct expected number of clauses

        nClauses += 2 * getAllPredecessors().entrySet().parallelStream()
                .mapToInt(entry -> entry.getValue().size())
                .sum();

        solver.setExpectedNumberOfClauses(nClauses);

        for ( Vertex vertex : orderedVertices ) {
            IVecInt cmpClause = new VecInt(new int[]{ vertexToInt(vertex) });
            for ( Vertex attacker : predecessors(vertex) ) {

                cmpClause.push(vertexToInt(attacker));

                IVecInt admClause = new VecInt(new int[]{ -vertexToInt(vertex) });
                for ( Vertex defender : predecessors(attacker) ) {
                    admClause.push(vertexToInt(defender));
                }
                try {
                    solver.addClause(admClause);
                } catch ( ContradictionException e ) {
                    System.err.println("contradiction adding admissible");
                    throw e;
                }
            }
            try {
                solver.addClause(cmpClause);
            } catch ( ContradictionException e ) {
                System.err.println("contradiction adding complete");
                e.printStackTrace();
                throw e;
            }
            System.out.println(vertex + " is done");
        }
        System.out.println("took " + (System.currentTimeMillis() - start));

        /*final Map<Vertex, Set<Set<Vertex>>> humongousMapping = orderedVertices.parallelStream()
                .collect(Collectors.toMap(vertex -> vertex,
                        vertex -> predecessors(vertex).parallelStream()
                                .map(this::predecessors)
                                .collect(Collectors.toSet())));

        humongousMapping.replaceAll((key, value) -> computeCombinations3(value));*/


    }

    public static <T> Set<Set<T>> appendElements(Set<Set<T>> combinations, Set<T> extraElements) {
        return combinations.parallelStream().flatMap(oldCombination -> extraElements.parallelStream().map(extra -> {
            Set<T> combinationWithExtra = new HashSet<>(oldCombination);
            combinationWithExtra.add(extra);
            return combinationWithExtra;
        })).collect(Collectors.toSet());
    }

    public static <T> Set<Set<T>> computeCombinations3(Set<Set<T>> Sets) {
        Set<Set<T>> currentCombinations = Collections.singleton(Collections.emptySet());
        for ( Set<T> set : Sets ) {
            currentCombinations = appendElements(currentCombinations, set);
        }
        return currentCombinations;
    }

    public static void main(String[] args) {
        Set<Vertex> a = new HashSet<>(Arrays.asList(new Vertex("a"), new Vertex("b"), new Vertex("c")));
        Set<Vertex> b = new HashSet<>(Arrays.asList(new Vertex("1"), new Vertex("2"), new Vertex("3")));
        System.out.println(computeCombinations3(new HashSet<>(Arrays.asList(a, b))));
    }


    public int addGrounded(ISolver solver, int nClauses) throws ContradictionException {
        final Set<Vertex> grdSolution = new GroundedSolver(this).computeGrounded();

        nClauses += grdSolution.size();

        solver.setExpectedNumberOfClauses(nClauses);

        for ( Vertex grd : grdSolution ) {
            solver.addClause(new VecInt(new int[]{ vertexToInt(grd) }));
        }

        return nClauses;
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
                    int index = vertexToInt(vertex);
                    return index + " " + -index + " 0";
                })
                .collect(Collectors.joining(" "));

        return cfDimacsBody = " " + freeVertices + " " + edges.parallelStream().unordered()
                .collect(StringBuilder::new,
                        (dimacs, edge) -> dimacs
                                .append(-vertexToInt(edge.attacker()))
                                .append(" ")
                                .append(-vertexToInt(edge.attacked()))
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
        return -vertexToInt(vertex) + " " + attackersOf(attacker) + " 0 ";
    }

    @Deprecated
    private String attackersOf(Vertex vertex) {
        return predecessors(vertex).parallelStream().unordered()
                .map(attacker -> String.valueOf(vertexToInt(attacker)))
                .collect(Collectors.joining(" "));
    }

//----------------------------------------------------------------------------------------------------------------------

    public Set<Set<Vertex>> interpretClauses(List<IVecInt> clauses) {
        return clauses.parallelStream().map(this::interpretClause).collect(Collectors.toSet());
    }

    public Set<Vertex> interpretClause(IVecInt clause) {
        return Arrays.stream(clause.toArray()).mapToObj(this::intToVertex).collect(Collectors.toSet());
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

    public Vertex intToVertex(int i) {
        return indexToVertex.get(Math.abs(i));
    }

    public int vertexToInt(Vertex v) {
        return vertexToIndex.get(v);
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

    public void setEdges(Set<Edge> edges) {
        this.edges = edges;
    }

    public Map<Vertex, Set<Vertex>> getAllSuccessors() {
        return allSuccessors;
    }

    public Map<Vertex, Set<Vertex>> getAllPredecessors() {
        return allPredecessors;
    }

    public List<Vertex> getOrderedVertices() {
        return orderedVertices;
    }

}

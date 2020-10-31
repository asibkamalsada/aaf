package solver.iterative;

import graphical.Graph;
import graphical.SearchTree;
import graphical.Vertex;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SemanticIterator {

    protected final List<Vertex> orderedVertices;
    protected final Graph graph;
    protected final Map<Vertex, Integer> blacklist;
    protected List<Vertex> currentResult;

    /**
     * abgelaufene Pfade des vollstaendigen Suchbaumes
     */
    protected final SearchTree donePaths;

    public SemanticIterator(Graph g) {
        graph = g;

        orderedVertices = graph.getVertices().stream().sorted().collect(Collectors.toList());
        donePaths = new SearchTree(graph);

        blacklist = new HashMap<>(graph.getVertices().size() + 1);

        for ( Vertex vertex : graph.getVertices() ) {
            if ( graph.successors(vertex).contains(vertex) ) {
                blacklist.put(vertex, 1);
            } else {
                blacklist.put(vertex, 0);
            }
        }
    }

    public Set<Vertex> next() {
        if ( currentResult == null ) {
            currentResult = new ArrayList<>(graph.getVertices().size());
        } else {

            // next chosen Vertex must not be blacklisted and not lead to a visited Path

            final Optional<Vertex> nextO = getAllowedMoves().stream().findAny();

            if ( nextO.isPresent() ) {
                move(nextO.get());
            } else {
                return null;
            }
        }
        return new HashSet<>(currentResult);
    }

    public void move(Vertex nextO) {
        blacklistRelatedVertices(nextO);
        currentResult.add(nextO);
    }

    public Set<Vertex> getAllowedMoves() {
        Set<Vertex> allowedMoves;

        while ( (allowedMoves =
                orderedVertices.parallelStream().filter(this::isAllowed).collect(Collectors.toSet()))
                .isEmpty()
                && donePaths.addDoneBranch(currentResult)
                && !currentResult.isEmpty() ) {
            Vertex lastPoppedVertex = currentResult.remove(currentResult.size() - 1);

            whitelistRelatedVertices(lastPoppedVertex);
        }
        return allowedMoves;
    }

    protected boolean isAllowed(Vertex vertex) {
        return (blacklist.get(vertex) == 0) && !donePaths.isDone(currentResult, vertex) && additionalRestriction(vertex);
    }

    protected abstract boolean additionalRestriction(Vertex vertex);

    private void blacklistRelatedVertices(Vertex v) {
        blacklistVertex(v);
        for ( Vertex succ : graph.successors(v) ) {
            blacklistVertex(succ);
        }
        for ( Vertex pre : graph.predecessors(v) ) {
            blacklistVertex(pre);
        }
    }

    public void whitelistRelatedVertices(Vertex v) {
        whitelistVertex(v);
        for ( Vertex succ : graph.successors(v) ) {
            whitelistVertex(succ);
        }
        for ( Vertex pre : graph.predecessors(v) ) {
            whitelistVertex(pre);
        }
    }

    private void blacklistVertex(Vertex v) {
        blacklist.merge(v, 1, Integer::sum);
    }

    private void whitelistVertex(Vertex v) {
        blacklist.merge(v, -1, Integer::sum);
    }

    public Set<Set<Vertex>> getSolutions() {
        Set<Set<Vertex>> results = new HashSet<>();
        Set<Vertex> result;
        while ( (result = next()) != null ) {
            results.add(result);
        }
        return results;
    }

    public void printSolutions() {
        while ( next() != null);
    }

}

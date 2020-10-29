package solver;

import graphical.Graph;
import graphical.SearchTree;
import graphical.Vertex;

import java.util.*;
import java.util.stream.Collectors;

public class CfIterator {

    protected final List<Vertex> orderedVertices;
    protected final Graph graph;
    protected final List<Vertex> blacklist;
    protected Stack<Vertex> currentResult;

    /**
     * abgelaufene Pfade des vollstaendigen Suchbaumes
     */
    protected final SearchTree donePaths;

    public CfIterator(Graph g) {
        graph = g;

        orderedVertices = graph.getVertices().stream().sorted().collect(Collectors.toList());
        donePaths = new SearchTree(graph);
        blacklist = graph.getAllSuccessors().entrySet().parallelStream()
                .filter(entry -> entry.getValue().contains(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Set<Vertex> next() {
        if ( currentResult == null ) {
            currentResult = new Stack<>();
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
        blacklist.add(nextO);

        blacklist.addAll(graph.successors(nextO));
        blacklist.addAll(graph.predecessors(nextO));
        currentResult.push(nextO);
    }

    public Set<Vertex> getAllowedMoves() {
        Set<Vertex> allowedMoves;

        while ( (allowedMoves =
                orderedVertices.parallelStream().filter(this::isAllowed).collect(Collectors.toSet()))
                .isEmpty()
                && donePaths.addDonePath(currentResult)
                && !currentResult.isEmpty() ) {
            Vertex lastPoppedVertex = currentResult.pop();

            blacklist.remove(lastPoppedVertex);

            for ( Vertex successor : graph.successors(lastPoppedVertex) ) {
                blacklist.remove(successor);
            }
            for ( Vertex predecessor : graph.predecessors(lastPoppedVertex) ) {
                blacklist.remove(predecessor);
            }
        }
        return allowedMoves;
    }

    protected boolean isAllowed(Vertex vertex) {
        return !blacklist.contains(vertex) && !donePaths.isDone(currentResult, vertex) && additionalRestriction(vertex);
    }

    protected boolean additionalRestriction(Vertex vertex) {
        return true;
    }
}

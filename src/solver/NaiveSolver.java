package solver;

import graphical.Graph;
import graphical.Vertex;

import java.util.*;
import java.util.stream.Collectors;

public class NaiveSolver {

    private final Graph graph;
    private Set<Vertex> grounded;


    public NaiveSolver(Graph graph) {
        this.graph = graph;
    }

//----------------------------------------------------------------------------------------------------------------------

    public Set<Vertex> computeGrounded() {
        if ( grounded == null ) {
            Graph copiedGraph = Graph.copy(graph);
            Set<Vertex> accepted;

            do {
                accepted = copiedGraph.getUnattacked();
            } while (
                    copiedGraph.removeVertices(
                            accepted.parallelStream()
                                    .flatMap(vertex -> copiedGraph.successors(vertex).stream())
                                    .collect(Collectors.toSet())
                    )
            );

            grounded = accepted;

        }
        return grounded;
    }

//----------------------------------------------------------------------------------------------------------------------

    public Set<Set<Vertex>> computeConflictFree() {

        Set<Vertex> looped = graph.getAllSuccessors().entrySet().parallelStream().unordered()
                .filter(entry -> entry.getValue().contains(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<Vertex> notLooped = new HashSet<>(graph.getVertices());
        notLooped.removeAll(looped);

        Set<Set<Vertex>> result = notLooped.parallelStream().unordered()
                .map(vertex ->
                        computeOneConflictFree(
                                Collections.singleton(vertex),
                                initializeBlacklist(looped, graph.successors(vertex), graph.predecessors(vertex)))
                )
                .reduce(
                        new HashSet<>(),
                        (s1, s2) -> {
                            s1.addAll(s2);
                            return s2;
                        }
                );

        // empty set is always conflict free
        result.add(new HashSet<>());
        return result;
    }

    private Set<Set<Vertex>> computeOneConflictFree(Set<Vertex> cf, Map<Vertex, Integer> blacklist) {
        Set<Set<Vertex>> result = new HashSet<>();
        result.add(cf);
        return result;
    }

    @SafeVarargs
    private final Map<Vertex, Integer> initializeBlacklist(Set<Vertex>... sets) {
        return Arrays.stream(sets)
                .flatMap(Collection::stream)
                .collect(
                        HashMap::new,
                        this::addToBlacklist,
                        (map1, map2) -> map2.forEach((key, value) -> addToBlacklist(map1, key, value))
                );
    }

    private void addToBlacklist(Map<Vertex, Integer> blacklist, Vertex vertex) {
        addToBlacklist(blacklist, vertex, 1);
    }

    private void addToBlacklist(Map<Vertex, Integer> blacklist, Vertex vertex, Integer value) {
        blacklist.merge(vertex, value, Integer::sum);
    }

    private void removeFromBlacklist(Map<Vertex, Integer> blacklist, Vertex vertex) {
        Integer count = blacklist.get(vertex);
        if ( count != null ) {
            if ( count > 1 ) {
                blacklist.put(vertex, count - 1);
            } else {
                blacklist.remove(vertex);
            }
        }
    }

}

package solver;

import graphical.Graph;
import graphical.Vertex;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return iterativerAnsatz().entrySet().parallelStream().<Set<Set<Vertex>>>collect(
                HashSet::new,
                (set, entry) -> set.addAll(entry.getValue()),
                Set::addAll
        );
    }

    public Map<Integer, Set<Set<Vertex>>> iterativerAnsatz() {
        /*
         * Start: leere Menge
         */

        Map<Integer, Set<Set<Vertex>>> results = new HashMap<>();
        Set<Set<Vertex>> result0 = new HashSet<>();
        result0.add(new HashSet<>());
        results.put(0, result0);

        for ( int i = 0; i <= graph.getVertices().size(); i++ ) {
            for ( Set<Vertex> previousCf : results.getOrDefault(i, Collections.emptySet()) ) {
                Set<Set<Vertex>> resultOfCurrentDepth = new HashSet<>();
                legalOptions(previousCf).forEach(vertex -> {
                    Set<Vertex> foundCf = new HashSet<>(previousCf);
                    foundCf.add(vertex);
                    resultOfCurrentDepth.add(foundCf);
                });
                results.put(i + 1, resultOfCurrentDepth);
            }
        }

        return results;
    }

    public Stream<Vertex> legalOptions(Set<Vertex> currentCf) {
        return graph.getVertices().parallelStream().filter(generalVertex ->
                !(
                        (
                                currentCf.contains(generalVertex)
                        ) || (
                                currentCf.parallelStream().anyMatch(cfVertex ->
                                        (
                                                graph.predecessors(cfVertex).contains(generalVertex)
                                        ) || (
                                                graph.successors(cfVertex).contains(generalVertex)
                                        )
                                )
                        )
                )
        );
    }

    @Deprecated
    public Set<Set<Vertex>> computeConflictFreeDeprecated() {

        Set<Vertex> looped = graph.getAllSuccessors().entrySet().parallelStream().unordered()
                .filter(entry -> entry.getValue().contains(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<Vertex> notLooped = new HashSet<>(graph.getVertices());
        notLooped.removeAll(looped);

        Set<Set<Vertex>> result = notLooped.parallelStream().unordered()
                .flatMap(vertex ->
                        computeOneConflictFree(
                                Collections.singleton(vertex),
                                initializeBlacklist(vertex, looped)
                        ).stream()
                )
                .collect(Collectors.toSet());

        // empty set is always conflict free
        result.add(new HashSet<>());
        return result;
    }


    private Set<Set<Vertex>> computeOneConflictFree(Set<Vertex> cf, Set<Vertex> blacklist) {
        return null;
    }

    private Set<Set<Vertex>> computeOneConflictFree(Set<Vertex> cf, Map<Vertex, Integer> blacklist) {
        return null;
    }

    private Map<Vertex, Integer> initializeBlacklist(Vertex initialVertex, Set<Vertex> looped) {
        return Stream.of(Collections.singleton(initialVertex), graph.predecessors(initialVertex),
                graph.successors(initialVertex), looped)
                .flatMap(Collection::stream)
                .distinct()
                .<Map<Vertex, Integer>>collect(
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

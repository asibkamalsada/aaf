package solver;

import graphical.Graph;
import graphical.Vertex;

import java.util.*;
import java.util.function.Predicate;
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
                System.out.print(previousCf + " with following options: ");
                for ( Vertex generalVertex : graph.getVertices() ) {

                    boolean isAttacked = false;
                    boolean isAttacker = false;

                    for ( Vertex possibleAttackerOrAttacked : previousCf ) {
                        if ( graph.successors(possibleAttackerOrAttacked).contains(generalVertex) ) {
                            isAttacked = true;
                        }
                        if ( graph.predecessors(possibleAttackerOrAttacked).contains(generalVertex) ) {
                            isAttacker = true;
                        }
                    }

                    if ( !previousCf.contains(generalVertex) && !isAttacked && !isAttacker ) {
                        System.out.print(generalVertex + " ");
                        Set<Vertex> foundCf = new HashSet<>(previousCf);
                        foundCf.add(generalVertex);
                        resultOfCurrentDepth.add(foundCf);
                    }
                }
                System.out.println();
                results.merge(i + 1, resultOfCurrentDepth, (s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                });
            }
        }

        return results;
    }

    public Stream<Vertex> legalOptions(Set<Vertex> currentCf) {
        return graph.getVertices().stream().filter(predicate2(currentCf));
    }

    public Predicate<Vertex> predicate2(Set<Vertex> currentCf) {
        return vertex -> true;
    }

    public Predicate<Vertex> predicate(Set<Vertex> currentCf) {
        return generalVertex -> !(currentCf.contains(generalVertex)
                || currentCf.parallelStream().flatMap(cfVertex -> Stream.concat(graph.predecessors(cfVertex).stream()
                , graph.successors(cfVertex).stream())).anyMatch(generalVertex::equals));
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

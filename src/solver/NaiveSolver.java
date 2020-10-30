package solver;

import graphical.Graph;
import graphical.Vertex;

import java.util.*;
import java.util.function.Predicate;
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

    public long printConflictFree() {
        return printFromIterator(new CfIterator(graph));
    }

    public Set<Set<Vertex>> computeConflictFree() {
        return breitensuche().entrySet().parallelStream().<Set<Set<Vertex>>>collect(
                HashSet::new,
                (set, entry) -> set.addAll(entry.getValue()),
                Set::addAll
        );
    }

    public Map<Integer, Set<Set<Vertex>>> breitensuche() {
        /*
         * Start: leere Menge
         */

        Map<Integer, Set<Set<Vertex>>> results = new HashMap<>();
        Set<Set<Vertex>> result0 = new HashSet<>();
        result0.add(new HashSet<>());
        results.put(0, result0);

        for ( int i = 0; i <= graph.getVertices().size(); i++ ) {
            Set<Set<Vertex>> previousLayer = results.getOrDefault(i, Collections.emptySet());
            if ( previousLayer.isEmpty() ) {
                break;
            }

            results.put(i + 1, previousLayer.parallelStream()
                    .map(previousCf ->
                            graph.getVertices().parallelStream()
                                    .filter(noAttackNorContained(previousCf))
                                    .map(filteredVertex -> {
                                        Set<Vertex> cfSet = new HashSet<>(previousCf);
                                        cfSet.add(filteredVertex);
                                        return cfSet;
                                    })
                                    .collect(Collectors.toSet()))
                    .<Set<Set<Vertex>>>collect(
                            HashSet::new,
                            Set::addAll,
                            Set::addAll
                    )
            );
            /* above Stream API translates to:
            for ( Set<Vertex> previousCf : previousLayer ) {
                StringBuilder output = new StringBuilder().append(previousCf)
                        .append(" with following options: ");

                Set<Set<Vertex>> resultOfCurrentDepth = graph.getVertices().parallelStream()
                        .filter(noAttackNorContained(previousCf))
                        .<Set<Vertex>>map(
                                filteredVertex -> {
                                    Set<Vertex> cfSet = new HashSet<>(previousCf);
                                    cfSet.add(filteredVertex);
                                    return cfSet;
                                }
                        )
                        .collect(Collectors.toSet());

                Set<Set<Vertex>> resultOfCurrentDepth = new HashSet<>();
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
                        output.append(generalVertex).append(" ");
                        Set<Vertex> foundCf = new HashSet<>(previousCf);
                        foundCf.add(generalVertex);
                        resultOfCurrentDepth.add(foundCf);
                    }
                }

                output.append("\n");
                //System.out.println("innerLoop");
                results.merge(i + 1, resultOfCurrentDepth, (s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                });

            }
            */
        }

        return results;
    }

    public Predicate<Vertex> noAttackNorContained(Set<Vertex> currentCf) {
        return generalVertex -> {
            boolean isContained = currentCf.contains(generalVertex);

            if ( isContained ) {
                return false;
            }

            Set<Vertex> isConnected = new HashSet<>();
            isConnected.addAll(graph.successors(generalVertex));
            isConnected.addAll(graph.predecessors(generalVertex));

            return Collections.disjoint(currentCf, isConnected);

            /* above Stream API translates to:
            for ( Vertex possibleAttackerOrAttacked : currentCf ) {
                if ( graph.successors(possibleAttackerOrAttacked).contains(generalVertex) ) {
                    // generalVertex.isAttacked == true
                    return false;
                }
                if ( graph.predecessors(possibleAttackerOrAttacked).contains(generalVertex) ) {
                    // generalVertex.isAttacker == true
                    return false;
                }
            }
            return true;
             */
        };
    }

//----------------------------------------------------------------------------------------------------------------------

    public long printPreferred() {
        return printFromIterator(new PrfIterator(graph));
    }

//----------------------------------------------------------------------------------------------------------------------

    public long printAdmissible() {
        return printFromIterator(new AdmIterator(graph));
    }

//----------------------------------------------------------------------------------------------------------------------

    public long printComplete() {
        return printFromIterator(new CompIterator(graph));
    }

//----------------------------------------------------------------------------------------------------------------------

    public long printFromIterator(SemanticIterator solver) {
        Set<Vertex> solution;
        long solutionCount = 0;
        while ( (solution = solver.next()) != null ) {
            System.out.println(solution);
            solutionCount++;
        }
        return solutionCount;
    }

}

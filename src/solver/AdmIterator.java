package solver;

import graphical.Graph;
import graphical.Vertex;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * eine Menge S ist admissible gdw<p>
 * 1. S konfliktfrei ist<p>
 * 2. alle e in S durch ein f in S verteidigt werden.
 */
public class AdmIterator extends SemanticIterator {

    protected Set<Vertex> unattacked;

    public AdmIterator(Graph g) {
        super(g);
        unattacked = g.getAllPredecessors().entrySet().parallelStream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    // muesste korrekt isAdmissible() heißen
    @Override
    protected boolean additionalRestriction(Vertex vertex) {
        // nur hinzugefuegte Argumente muessen ueberprueft werden, da vorherige schon adm erfuellen

        if ( unattacked.contains(vertex) ) return true;

        /*return Stream.concat(currentResult.parallelStream(), Stream.of(vertex).parallel())
                .flatMap(result -> graph.successors(result).parallelStream())
                .filter(attacked -> graph.predecessors(vertex).contains(attacked))
                .distinct()
                .count() == graph.predecessors(vertex).size();*/

        /* abyssmal performance
        final Set<Vertex> attacked = currentResult.parallelStream()
                .flatMap(result -> graph.successors(result).stream())
                .collect(Collectors.toSet());
        attacked.addAll(graph.successors(vertex));

        return attacked.containsAll(graph.predecessors(vertex));*/

        /*return Stream.concat(currentResult.parallelStream(), Stream.of(vertex).parallel())
                .allMatch(result -> attacked.containsAll(graph.predecessors(result)));*/

        return graph.predecessors(vertex).stream()
                .allMatch(attacker -> Stream.concat(currentResult.parallelStream(), Stream.of(vertex)).parallel()
                        .anyMatch(result -> graph.predecessors(attacker).contains(result)));
    }
}

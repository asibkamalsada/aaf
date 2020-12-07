package solver.iterative;

import graphical.Graph;
import graphical.Vertex;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
/* incremental SAT
        Set<Vertex> notInResult = new HashSet<>(graph.getVertices());
        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(2);
        try {
            for ( Vertex vertex_ : currentResult ) {
                notInResult.remove(vertex_);
                addClauses(solver, vertex_);
            }
            notInResult.remove(vertex);
            addClauses(solver, vertex);
            for ( Vertex vertex__ : notInResult ) {
                solver.addClause(new VecInt(new int[]{-graph.vertexToInt(vertex__)}));
            }
        } catch ( ContradictionException e ) {
            return false;
        }
        try {
            return solver.isSatisfiable();
        } catch ( TimeoutException e ) {
            e.printStackTrace();
            return false;
        }
*/
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

    private void addClauses(ISolver solver, Vertex vertex) throws ContradictionException {
        solver.addClause(new VecInt(new int[]{ graph.vertexToInt(vertex) }));
        for ( Vertex attacker : graph.predecessors(vertex) ) {
            solver.addClause(new VecInt(new int[]{ -graph.vertexToInt(attacker) }));
            IVecInt clause = new VecInt(graph.predecessors(attacker).size());
            for ( Vertex defender : graph.predecessors(attacker) ) {
                clause.push(graph.vertexToInt(defender));
            }
            solver.addClause(clause);
        }
    }
}

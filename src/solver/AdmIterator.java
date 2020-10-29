package solver;

import graphical.Graph;
import graphical.Vertex;

import java.util.stream.Stream;

public class AdmIterator extends CfIterator {

    public AdmIterator(Graph g) {
        super(g);
    }

    @Override
    protected boolean additionalRestriction(Vertex vertex) {
        return super.additionalRestriction(vertex) && admissibleRestriction(vertex);
    }

    private boolean admissibleRestriction(Vertex vertex) {
        // nur hinzugefuegte Argumente muessen ueberprueft werden, da vorherige schon adm erfuellen
        return graph.predecessors(vertex).parallelStream()
                .allMatch(attacker -> Stream.concat(currentResult.parallelStream(), Stream.of(vertex))
                        .anyMatch(result -> graph.predecessors(attacker).contains(result)));
    }
}

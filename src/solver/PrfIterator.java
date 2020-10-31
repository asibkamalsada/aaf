package solver;

import graphical.Graph;
import graphical.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PrfIterator extends AdmIterator {
    public PrfIterator(Graph g) {
        super(g);
        currentResult = new ArrayList<>(g.getVertices().size());
    }

    @Override
    public Set<Vertex> next() {

        // next chosen Vertex must not be blacklisted and not lead to a visited Path

        Set<Vertex> allowedMoves;

        boolean forwardPeeked = false;

        // laufe so viele Schritte, wie moeglich
        while ( !(allowedMoves =
                orderedVertices.parallelStream().filter(this::isAllowed).collect(Collectors.toSet()))
                .isEmpty() ) {
            forwardPeeked = true;
            move(allowedMoves.stream().findAny().get());
        }

        if ( forwardPeeked ) return new HashSet<>(currentResult);

        final Optional<Vertex> nextO = getAllowedMoves().stream().findAny();

        if ( nextO.isPresent() ) {
            move(nextO.get());
            return next();
        } else {
            return null;
        }
    }
}

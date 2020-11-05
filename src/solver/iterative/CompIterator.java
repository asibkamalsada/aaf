package solver.iterative;

import graphical.Graph;
import graphical.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CompIterator extends AdmIterator {
    public CompIterator(Graph g) {
        super(g);
        currentResult = new ArrayList<>(g.getVertices().size());
    }

    @Override
    public Set<Vertex> next() {

        // next chosen Vertex must not be blacklisted and not lead to a visited Path

        final Optional<Vertex> nextO = getAllowedMove();

        if ( nextO.isPresent() ) {
            move(nextO.get());
            if ( !isComplete() ) {
                return next();
            }
        } else {
            return null;
        }

        return new HashSet<>(currentResult);
    }

    // doesnt work :(
    private boolean isComplete() {
        Set<Vertex> defendedArguments =
                currentResult.parallelStream()
                        .flatMap(result ->
                                graph.successors(result).parallelStream()
                                        //.flatMap(attacked -> graph.successors(attacked).parallelStream())
                                        .flatMap(attacked -> graph.successors(attacked).parallelStream()))
                        .collect(Collectors.toSet());

        System.out.println("\n" + currentResult + " " + defendedArguments);

        return currentResult.containsAll(defendedArguments);
    }
}

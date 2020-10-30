package solver;

import graphical.Graph;
import graphical.Vertex;

import java.util.Set;
import java.util.stream.Collectors;

public class GroundedSolver {

    private final Graph graph;
    private Set<Vertex> grounded;


    public GroundedSolver(Graph graph) {
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

}

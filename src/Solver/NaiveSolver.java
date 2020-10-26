package Solver;

import graphical.Edge;
import graphical.Graph;
import graphical.Vertex;

import java.util.HashSet;
import java.util.Set;

public class NaiveSolver {

    private Graph graph;
    private Set<Vertex> grounded;


    public NaiveSolver(Graph graph) {
        this.graph = graph;
        grounded = new HashSet<>();
    }

    public Set<Vertex> computeGrounded() {
        if ( grounded == null ) {
            Set<Vertex> vertices = new HashSet<>(graph.getVertices());
            Set<Edge> edges = new HashSet<>(graph.getEdges());

            /*
            vertices = vertices.parallelStream()
                    .filter(vertex -> edges.parallelStream().noneMatch(e -> e.getAttacked().equals(vertex)))
                    .collect(Collectors.toSet());
            */

            while ( vertices.removeIf(vertex -> edges.parallelStream().anyMatch(e -> e.getAttacked().equals(vertex))) ) {
                edges.removeIf(e -> !(vertices.contains(e.getAttacker()) && vertices.contains(e.getAttacked())));
            }

            grounded = vertices;
        }
        return grounded;
    }

}

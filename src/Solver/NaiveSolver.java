package Solver;

import graphical.Graph;
import graphical.Vertex;

import java.util.HashSet;
import java.util.Set;

public class NaiveSolver {

    private Graph graph;
    private Set<Vertex> grounded;


    public NaiveSolver(Graph graph) {
        this.graph = graph;
    }

    public Set<Vertex> computeGrounded() {
        if ( grounded == null ) {
            Graph gG = Graph.copy(graph);
            Set<Vertex> accepted = new HashSet<>();
        }
        return grounded;
    }

}

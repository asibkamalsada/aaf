package Solver;

import graphical.Graph;
import graphical.Vertex;
import io.SolutionParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class Tester {


    private final Graph graph;
    private final Path groundedSolution;

    public Tester(Graph graph, Path groundedSolution) {
        this.graph = graph;
        this.groundedSolution = groundedSolution;
    }

    public boolean testGrounded(Collection<Vertex> toBeTestedGrounded) throws IOException {
        return SolutionParser.parseGrounded(groundedSolution).equals(toBeTestedGrounded);
    }




}

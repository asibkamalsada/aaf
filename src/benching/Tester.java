package benching;

import graphical.Graph;
import graphical.Vertex;
import io.SolutionParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class Tester {

    public static boolean testGrounded(Set<Vertex> toBeTestedGrounded, Path groundedSolutionPath) throws IOException {
        return SolutionParser.parseGrounded(groundedSolutionPath).equals(toBeTestedGrounded);
    }

    public static boolean testConflictFree(Set<Set<Vertex>> toBeTestedConflictFree, Graph graph) {
        return true;
    }
}

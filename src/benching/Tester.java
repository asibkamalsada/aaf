package benching;

import graphical.Vertex;
import io.SolutionParser;
import solver.iterative.SemanticIterator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class Tester {

    public static boolean testGrounded(Set<Vertex> toBeTestedGrounded, Path groundedSolutionPath) throws IOException {
        return toBeTestedGrounded.equals(SolutionParser.parseGrounded(groundedSolutionPath));
    }

    public static boolean testConflictFree(Set<Set<Vertex>> results, Path instancePath, Path conargPath) {
        return results.equals(SolutionParser.parseConflictFree(instancePath, conargPath));
    }

    public static boolean iterativeTest(SemanticIterator solver, Set<Set<Vertex>> correctResult) {
        Set<Vertex> oneSolution;
        long counter = 0;

        while ( (oneSolution = solver.next()) != null ) {
            counter++;
            if ( !correctResult.contains(oneSolution) ) {
                return false;
            }
        }
        return counter == correctResult.size();
    }
}

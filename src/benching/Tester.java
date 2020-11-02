package benching;

import graphical.Vertex;
import io.SolutionParser;
import solver.iterative.SemanticIterator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class Tester {

    public static boolean testGrounded(Set<Vertex> result, Path groundedSolutionPath, Path conargPath) throws IOException {
        return result.equals(SolutionParser.parseGrounded(groundedSolutionPath, conargPath));
    }

    public static boolean testConflictFree(Set<Set<Vertex>> results, Path instancePath, Path conargPath) {
        return results.equals(SolutionParser.parseConflictFree(instancePath, conargPath));
    }

    public static boolean testAdmissible(Set<Set<Vertex>> results, Path instancePath, Path conargPath) {
        return results.equals(SolutionParser.parseAdmissible(instancePath, conargPath));
    }

    public static boolean testComplete(Set<Set<Vertex>> results, Path instancePath, Path conargPath) {
        return results.equals(SolutionParser.parseComplete(instancePath, conargPath));
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

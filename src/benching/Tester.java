package benching;

import graphical.Vertex;
import io.SolutionParser;
import solver.AdmIterator;
import solver.SemanticIterator;
import solver.CompIterator;
import solver.PrfIterator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class Tester {

    public static boolean testGrounded(Set<Vertex> toBeTestedGrounded, Path groundedSolutionPath) throws IOException {
        return SolutionParser.parseGrounded(groundedSolutionPath).equals(toBeTestedGrounded);
    }

    public static boolean testConflictFree(Set<Set<Vertex>> results, Path instancePath, Path conargPath) {
        return SolutionParser.parseConflictFree(instancePath, conargPath).equals(results);
    }

    public static boolean testAdmissible(AdmIterator solver, Path admissibleSolutionPath, Path conargPath) {
        Set<Set<Vertex>> correctResult = SolutionParser.parseAdmissible(admissibleSolutionPath, conargPath);

        return testSemantic(solver, correctResult);
    }

    public static boolean testComplete(CompIterator solver, Path currentInstance, Path conargPath) {
        Set<Set<Vertex>> correctResult = SolutionParser.parseComplete(currentInstance, conargPath);

        return testSemantic(solver, correctResult);
    }

    public static boolean testPreferred(PrfIterator solver, Path currentInstance, Path conargPath) {
        return false;
    }

    private static boolean testSemantic(SemanticIterator solver, Set<Set<Vertex>> correctResult) {
        Set<Vertex> oneSolution;
        long counter = 0;

        while ((oneSolution = solver.next()) != null) {
            counter++;
            if (!correctResult.contains(oneSolution)){
                return false;
            }
        }
        return counter == correctResult.size();
    }
}

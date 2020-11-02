package solver.iterative;

import graphical.Graph;
import graphical.Vertex;
import solver.Cadical;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CfSat extends SemanticIterator {

    private final Map<Vertex, Integer> index;
    private final String dimacsPrefix;
    private final String dimacsMiddle;

    public CfSat(Graph graph) {
        super(graph);

        index = orderedVertices.parallelStream()
                .collect(Collectors.toMap(Function.identity(), o -> orderedVertices.indexOf(o) + 1));

        dimacsPrefix = "p cnf " + graph.getVertices().size() + " $";
        dimacsMiddle = prepareCnfDimacs();

    }

    private String prepareCnfDimacs() {
        return graph.getEdges().stream()
                .collect(
                        StringBuilder::new,
                        (sb, edge) -> sb
                                .append(-index.get(edge.attacker()))
                                .append(" ")
                                .append(-index.get(edge.attacked()))
                                .append(" 0 "),
                        StringBuilder::append
                ).toString();
    }

    @Override
    protected boolean isAllowed(Vertex vertex) {
            return !currentResult.contains(vertex)
                    && additionalRestriction(vertex)
                    && !donePaths.isDone(currentResult, vertex);
    }

    @Override
    protected boolean additionalRestriction(Vertex vertex) {

        Path cadical = Paths.get("C:\\Users\\Kamalsada\\Documents\\Asib" +
                "\\uni\\ba_baumann\\software\\cadical-master\\build\\cadical.exe");

        String dimacsSuffix = Stream.concat(currentResult.stream(), Stream.of(vertex))
                .map(index::get)
                .collect(
                        () -> new StringBuilder(dimacsMiddle),
                        (sb, value) -> sb.append(value).append(" 0 "),
                        StringBuilder::append
                )
                .toString();

        String newPrefix = dimacsPrefix.replaceFirst(
                "\\$",
                Integer.toString(graph.getEdges().size() + currentResult.size() + 1)
        );

        Boolean isSatisfiable = Cadical.isSatisfiable(cadical, newPrefix, dimacsSuffix);

        if ( isSatisfiable == null ) throw new Error("could not verify via cadical");
        return isSatisfiable;
    }

}

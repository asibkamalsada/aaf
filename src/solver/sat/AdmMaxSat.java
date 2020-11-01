package solver.sat;

import benching.Tester;
import codeTesting.CodeTesting;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import graphical.Graph;
import graphical.Vertex;
import io.GraphParser;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AdmMaxSat {

    private final Graph graph;
    private final String dimacsString;
    private final Map<Vertex, Integer> vertexToIndex;
    private final Map<Integer, Vertex> indexToVertex;
    private final List<Vertex> orderedVertices;
    /**
     * in seconds
     **/
    private final int TIMEOUT = 600;

    public static void main(String[] args) throws IOException {

        int vertexCount = 20;

        String header = "p cnf " + vertexCount + " " + vertexCount / 2 + "\n";

        StringBuilder simpleDimacs = new StringBuilder(header);
        for ( int i = 1; i < vertexCount; i = i + 2 ) {
            simpleDimacs.append(-i).append(" ").append(-(i + 1)).append(" 0 ");
        }

        Graph g = GraphParser.readGraph(
                CodeTesting.instances.resolve(CodeTesting.emptyGrounded)
        );

        CfMaxSat solver = new CfMaxSat(g);

        Set<Set<Vertex>> solutions;
        solutions = solver.findSolutions();
        /*solutions = new HashSet<>();
        Set<Vertex> solution = new HashSet<>();
        solution.add(new Vertex("hallo"));
        solutions.add(solution);*/

        System.out.println(Tester.testConflictFree(solutions,
                CodeTesting.instances.resolve(CodeTesting.emptyGrounded), CodeTesting.conarg));

        writeSolution(solutions);

    }

    public static void writeSolution(Set<Set<Vertex>> solutions) throws FileNotFoundException {
        Kryo kryo = new Kryo();

        kryo.register(Vertex.class);
        kryo.register(HashSet.class);

        Output o = new Output(new FileOutputStream(
                CodeTesting.customSolutions.resolve("cfmaxsat_test.kryo").toString()
        ));
        kryo.writeObject(o, solutions);
        o.close();
    }

    public AdmMaxSat(Graph graph) {
        this.graph = graph;

        orderedVertices = graph.getVertices().stream().sorted().collect(Collectors.toList());

        vertexToIndex = orderedVertices.parallelStream()
                .collect(Collectors.toMap(Function.identity(), o -> orderedVertices.indexOf(o) + 1));

        indexToVertex = vertexToIndex.entrySet().parallelStream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));


        String header = "p cnf "
                + graph.getVertices().size()
                + " "
                + graph.getEdges().size()
                //+ graph.getFreeVertices()
                + "\n";

        dimacsString = header + prepareCnfDimacs();

    }

    private String prepareCnfDimacs() {
        return graph.getEdges().stream()
                .collect(
                        StringBuilder::new,
                        (sb, edge) -> sb
                                .append(-vertexToIndex.get(edge.getAttacker()))
                                .append(" ")
                                .append(-vertexToIndex.get(edge.getAttacked()))
                                .append(" 0 "),
                        StringBuilder::append
                ).toString();
    }

    public Set<Set<Vertex>> findSolutions() {
        InputStream is = new ByteArrayInputStream(dimacsString.getBytes(StandardCharsets.UTF_8));

        long start = System.currentTimeMillis();

        ISolver solver = SolverFactory.newDefault();
        ModelIterator mi = new ModelIterator(solver);
        solver.setTimeout(TIMEOUT);
        Reader reader = new DimacsReader(mi);

        Set<Set<Vertex>> solutions = new HashSet<>();

        // filename is given on the command line
        try {
            boolean unsat = true;
            IProblem problem = reader.parseInstance(is);
            while ( problem.isSatisfiable() ) {
                unsat = false;
                solutions.add(interpretSolution(problem.model()));
                //System.out.println(interpretSolution(model));
                // do something with each model
                //System.out.println(Arrays.toString(model));
            }
            if ( unsat ) {
                System.out.println("no solutions found");
                // do something for unsat case
            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( ParseFormatException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ContradictionException e ) {
            System.out.println("Unsatisfiable (trivial)!");
        } catch ( TimeoutException e ) {
            System.out.println("Timeout, sorry!");
        } finally {
            System.out.println(System.currentTimeMillis() - start);
        }
        return solutions;
    }

    private Set<Set<Vertex>> interpretSolutions(Set<int[]> models) {
        return models.parallelStream()
                .map(this::interpretSolution)
                .collect(Collectors.toSet());
    }

    private Set<Vertex> interpretSolution(int[] model) {
        return Arrays.stream(model).boxed().parallel()
                .filter(value -> value > 0)
                .map(indexToVertex::get)
                .collect(Collectors.toSet());
    }

}

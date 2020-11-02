package io;

import benching.Benchmarker;
import graphical.Vertex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SolutionParser {

    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("[^,\\[\\] ]+");

    @Deprecated
    public static Set<Vertex> parseGrounded(Path path) throws IOException {
        return parseSingleSet(Files.lines(path).collect(Collectors.toList()).get(1));
    }

    @Deprecated
    private static Set<Vertex> parseSingleSet(String input) {
        Set<Vertex> solution = new HashSet<>();

        Matcher m = ARGUMENT_PATTERN.matcher(input);

        while ( m.find() ) {
            solution.add(new Vertex(m.group()));
        }
        return solution;
    }

    public static Set<Vertex> parseGrounded(Path instancePath, Path conargPath) {
        final Set<Set<Vertex>> grdSolutions = parse(instancePath, conargPath, "grounded");
        if ( grdSolutions == null ) return null;
        return grdSolutions.stream().findAny().orElse(null);
    }

    public static Set<Set<Vertex>> parseConflictFree(Path instancePath, Path conargPath) {
        return parse(instancePath, conargPath, "conflictfree");
    }

    public static Set<Set<Vertex>> parseAdmissible(Path instancePath, Path conargPath) {
        return parse(instancePath, conargPath, "admissible");
    }

    public static Set<Set<Vertex>> parseComplete(Path instancePath, Path conargPath) {
        return parse(instancePath, conargPath, "complete");
    }

    public static Set<Set<Vertex>> parsePreferred(Path instancePath, Path conargPath) {
        return parse(instancePath, conargPath, "preferred");
    }

    public static Set<Set<Vertex>> parseStable(Path instancePath, Path conargPath) {
        return parse(instancePath, conargPath, "stable");
    }

    private static Set<Set<Vertex>> parse(Path instancePath, Path conargPath, String keyWord) {
        String[] commandArray = new String[]{
                escapePath(conargPath),
                "-e",
                keyWord,
                escapePath(instancePath)
        };

        return getConargSets(commandArray);
    }

    public static Set<Set<Vertex>> getConargSets(String[] command) {
        long start = System.currentTimeMillis();
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())) ) {
                List<String> lines = new ArrayList<>();
                {
                    String line;
                    while ( (line = reader.readLine()) != null ) {
                        lines.add(line);
                        //System.err.println(line);
                    }
                }
                process.waitFor();

                final Set<Set<Vertex>> result = lines.stream()
                        .filter(line -> line.startsWith("\""))
                        .map(line ->
                                Arrays.stream(line.replace("\"", "").split(" "))
                                        .map(label -> {
                                            if ( label.isEmpty() ) {
                                                return null;
                                            } else {
                                                return new Vertex(label);
                                            }
                                        })
                                        .<Set<Vertex>>collect(
                                                HashSet::new,
                                                (oldSet, newValue) -> {
                                                    if ( newValue != null ) {
                                                        oldSet.add(newValue);
                                                    }
                                                },
                                                Set::addAll
                                        )
                        )
                        .collect(Collectors.toSet());
                if ( Benchmarker.isBenching() ) System.out.print(System.currentTimeMillis() - start);
                return result;
            }
        } catch ( IOException | InterruptedException e ) {
            e.printStackTrace();
            if ( Benchmarker.isBenching() ) System.out.print(System.currentTimeMillis() - start);
            return null;
        }
    }

    private static String escapePath(Path path) {
        return ("\"" + path.toAbsolutePath().toString() + "\"").replace("\\", "\\\\");
    }
}

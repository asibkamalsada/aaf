package io;

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

    public static Set<Vertex> parseGrounded(Path path) throws IOException {
        return parseSingleSet(Files.lines(path).collect(Collectors.toList()).get(1));
    }

    private static Set<Vertex> parseSingleSet(String input) {
        Set<Vertex> solution = new HashSet<>();

        Matcher m = ARGUMENT_PATTERN.matcher(input);

        while ( m.find() ) {
            solution.add(new Vertex(m.group()));
        }
        return solution;
    }

    public static Set<Set<Vertex>> parseConflictFree(Path instancePath, Path conargPath) {
        try {
            String[] commandArray = new String[]{
                    escapePath(conargPath),
                    "-e",
                    "conflictfree",
                    escapePath(instancePath)
            };

            ProcessBuilder pb = new ProcessBuilder(commandArray);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())) ) {
                List<String> lines = new ArrayList<>();
                {
                    String line;
                    while ( (line = reader.readLine()) != null ) {
                        lines.add(line);
                    }
                }
                process.waitFor();

                return lines.stream()
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
            }
        } catch ( IOException | InterruptedException e ) {
            e.printStackTrace();
            return null;
        }
    }

    private static String escapePath(Path conargPath) {
        return ("\"" + conargPath.toAbsolutePath().toString() + "\"").replace("\\", "\\\\");
    }
}

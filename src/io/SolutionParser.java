package io;

import graphical.Vertex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SolutionParser {

    private static final Pattern argument = Pattern.compile("[^,\\[\\] ]+");

    public static Set<Vertex> parseGrounded(Path path) throws IOException {
        return parseSingleSet(Files.lines(path).collect(Collectors.toList()).get(1));
    }

    private static Set<Vertex> parseSingleSet(String input) {
        Set<Vertex> solution = new HashSet<>();

        Matcher m = argument.matcher(input);

        while ( m.find() ) {
            solution.add(new Vertex(m.group()));
        }
        return solution;
    }

}

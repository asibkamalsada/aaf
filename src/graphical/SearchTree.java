package graphical;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SearchTree {

    Set<List<Vertex>> doneBranches;

    public SearchTree(Graph g) {
        doneBranches = new HashSet<>();
    }

    public boolean addDoneBranch(List<Vertex> currentResult, Vertex potentialStep) {
        List<Vertex> doneBranch = new ArrayList<>(currentResult);
        doneBranch.add(potentialStep);
        return doneBranches.add(doneBranch);
    }

    public boolean addDoneBranch(List<Vertex> visited) {
        doneBranches = doneBranches.parallelStream()
                .filter(doneBranch -> {
                    if ( doneBranch.size() <= visited.size() ) return true;
                    for ( int i = 0; i < visited.size(); i++ ) {
                        if ( !doneBranch.get(i).equals(visited.get(i)) ) return true;
                    }
                    return false;
                })
                .collect(Collectors.toSet());
        return doneBranches.add(new ArrayList<>(visited));
    }

    public boolean isDone(List<Vertex> visited, Vertex vertex) {
        return doneBranches.parallelStream()
                .anyMatch(doneBranch -> {
                    // wenn der bereits abgeschlossene Pfad laenger ist, als der zu ueberpruefende, kann der zu
                    // ueberpruefende gar nicht von diesem Pfad abgedeckt sein.
                    if ( doneBranch.size() > visited.size() + 1 ) return false;
                    for ( int i = 0; i < doneBranch.size() - 1; i++ ) {
                        // wenn sie bis zur vorletzten Stelle uebereinstimmen koennte es ein Treffer sein
                        if ( !doneBranch.get(i).equals(visited.get(i)) ) return false;
                    }
                    return doneBranch.get(doneBranch.size() - 1).equals(vertex);
                });
    }

}

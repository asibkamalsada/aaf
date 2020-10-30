package graphical;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchTree {

    Set<Vertex> possibleVertices;
    List<Set<Vertex>> donePaths;

    public SearchTree(Graph g) {
        possibleVertices = g.getVertices();
        donePaths = new ArrayList<>();
    }

    public boolean addDonePath(List<Vertex> visited) {
        return donePaths.add(new HashSet<>(visited));
    }

    public void addDonePath(List<Vertex> visited, Vertex lastPopped) {
        Set<Vertex> temp = new HashSet<>(visited);
        temp.add(lastPopped);
        donePaths.add(temp);
    }

    public boolean isDone(List<Vertex> visited, Vertex vertex) {
        // should return true, if some part of the walking graph is contained in visited
        Set<Vertex> move = new HashSet<>(visited);
        move.add(vertex);
        return donePaths.contains(move);
        //return donePaths.parallelStream().anyMatch(move::containsAll);
    }

}

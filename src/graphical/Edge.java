package graphical;

import java.io.Serializable;
import java.util.Objects;

public class Edge implements Serializable {

    private final static long serialVersionUID = 7377897378777781517L;

    private Vertex attacker;
    private Vertex attacked;

    public Edge(Vertex attacker, Vertex attacked) {
        this.attacker = attacker;
        this.attacked = attacked;
    }

    public Edge(String attacker, String attacked) {
        this.attacker = new Vertex(attacker);
        this.attacked = new Vertex(attacked);
    }

    public Edge() {

    }

    public Vertex getAttacker() {
        return attacker;
    }

    public Vertex getAttacked() {
        return attacked;
    }

    @Override
    public String toString() {
        return attacker + "->" + attacked;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Edge edge = (Edge) o;
        return attacker.equals(edge.attacker) &&
                attacked.equals(edge.attacked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attacker, attacked);
    }
}

package graphical;

import java.io.Serializable;
import java.util.Objects;

public class Vertex implements Serializable, Comparable<Vertex> {

    private final static long serialVersionUID = 3667875158165427227L;

    private String label;

    public Vertex() {
    }

    public Vertex(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Vertex vertex = (Vertex) o;
        return label.equals(vertex.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }


    @Override
    public int compareTo(Vertex o) {
        return this.label.compareTo(o.label);
    }
}

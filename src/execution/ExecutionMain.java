package execution;

import graphical.Vertex;

import java.util.HashSet;
import java.util.Set;

public class ExecutionMain {

    public static void main(String[] args) {
        Set<Set<Vertex>> asib = new HashSet<>();
        Set<Set<Vertex>> hamza = new HashSet<>();

        Set<Vertex> leer = new HashSet<>();
        Set<Vertex> einkauf = new HashSet<>();
        einkauf.add(new Vertex("Eier"));


        hamza.add(new HashSet<>());
        hamza.add(einkauf);

        asib.add(new HashSet<>());
        asib.add(einkauf);

        System.out.println(asib.equals(hamza));

    }

}

package io;

import codeTesting.CodeTesting;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import graphical.Vertex;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

public class CustomSolutionParser {
    public static void writeSolutions(Set<Set<Vertex>> solutions, String filename) throws FileNotFoundException {
        Kryo kryo = new Kryo();

        kryo.register(Vertex.class);
        kryo.register(HashSet.class);

        Output o = new Output(new FileOutputStream(
                CodeTesting.customSolutions.resolve(filename).toString()
        ));
        kryo.writeObject(o, solutions);
        o.close();
    }

    //TODO not tested
    @Deprecated
    public static Set<Set<Vertex>> readSolution( String filename ) throws FileNotFoundException {
        Kryo kryo = new Kryo();

        kryo.register(Vertex.class);
        kryo.register(HashSet.class);

        Input i = new Input(new FileInputStream(
                CodeTesting.customSolutions.resolve(filename).toString()
        ));
        Set<Set<Vertex>> solutions = (Set<Set<Vertex>>) kryo.readObject(i, HashSet.class);
        i.close();

        return solutions;
    }
}

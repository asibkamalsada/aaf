package execution;

import graphical.Graph;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ExecutionMain {

    public static Path instancesPath;

    public static void main(String[] args) {
        instancesPath = Paths.get(
                args.length == 1 ?
                        args[ 0 ] :
                        "C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba baumann\\iccma19\\instances"
        );

    }

}

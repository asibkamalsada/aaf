package solver;

import benching.Benchmarker;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class Cadical {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Collections.nCopies(100, null).parallelStream().forEach(o ->
                Cadical.isSatisfiable(
                        Paths.get("C:\\Users\\Kamalsada\\Documents\\Asib\\uni\\ba_baumann\\software\\cadical-master" +
                                "\\build\\cadical.exe"),
                        "p cnf 1 2", "1 0 -1 0"
                ));
        System.out.println(System.currentTimeMillis() - start);
    }

    public static Boolean isSatisfiable(Path cadical, String dimacsPrefix, String dimacsBody) {
        String[] cmdArray = new String[]{
                escapePath(cadical),
                "-q",
                "--no-colors"
        };
        return askCadical(cmdArray, dimacsPrefix, dimacsBody);
    }

    private static Boolean askCadical(String[] command, String dimacsPrefix, String dimacsBody) {
        long start = System.currentTimeMillis();
        ProcessBuilder pb = new ProcessBuilder(command);

        try {
            File f = File.createTempFile("dimacs", ".cache");

            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writer.write(dimacsPrefix);
            writer.newLine();
            writer.write(dimacsBody);
            writer.newLine();
            writer.flush();
            writer.close();
            f.deleteOnExit();

            pb.redirectInput(f);
            pb.redirectErrorStream(true);
            Process process = null;
            try {
                process = pb.start();
                //pb.inheritIO();

                InputStream is = process.getInputStream();

                //OutputStream os = process.getOutputStream();

                try ( BufferedReader reader = new BufferedReader(new InputStreamReader(is))/*;
                      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))*/ ) {

                    new Thread(null, () -> {
                        try {
                            //List<String> lines = new ArrayList<>();
                            String line;
                            while ( (line = reader.readLine()) != null ) {
                                //lines.add(line);
                                //System.out.println(line);
                            }
                        } catch ( IOException e ) {
                            e.printStackTrace();
                        }
                    }, "dump_cadical_output" + System.currentTimeMillis()).start();


                    int exitStatus = process.waitFor();

                    if ( Benchmarker.isBenching() ) System.out.print(System.currentTimeMillis() - start + ";");
                    if ( exitStatus == 10 ) {
                        return true;
                    } else if ( exitStatus == 20 ) {
                        return false;
                    }
                } catch ( IOException | InterruptedException e ) {
                    process.destroyForcibly();
                    e.printStackTrace();
                }
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        if ( Benchmarker.isBenching() ) System.out.print(System.currentTimeMillis() - start + ";");
        return null;
    }

    private static String escapePath(Path path) {
        return ("\"" + path.toAbsolutePath().toString() + "\"").replace("\\", "\\\\");
    }

}

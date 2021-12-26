package configuration;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.util.*;

public class RunScript {
    private static Map<String, String> env;
    private String path;

    public RunScript(String path){
        this.path = path;
        run();
    }

    public File run(){
        // String path = "C:/Users/shane/Desktop/SFSU/csc667-internet-application-design-and-development/webserver-csc667_server_project-shanew-matthewm/public_html/cgi-bin/perl_env";
        ProcessBuilder pb = new ProcessBuilder(path);
        try {
            // * Place the environment variables in a map
            env = pb.environment();
            // * Print the env variables to stdout
            env.forEach((key, value) -> System.out.println(key + value));
            
            // * Create two new file objects: env variables, errors
            File outputTxt = new File("standard-output.txt");

            // * Run script
            pb.command(path);

            // * Append output to text and log files respectively
            pb.redirectOutput(Redirect.appendTo(outputTxt));
            pb.start();

            return outputTxt;
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

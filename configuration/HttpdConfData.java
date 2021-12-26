package configuration;

import java.util.Scanner;
import java.util.Set;
import java.util.Hashtable;
import java.io.*;

public class HttpdConfData {
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_ACCESS_FILE = ".htaccess";
    private static final String DEFAULT_DIRECTORY_INDEX = "index.html";
    
    private Hashtable<String,String> aliasHashtable = new Hashtable<>();
    private Hashtable<String,String> scriptAliasHashtable = new Hashtable<>();
    private Hashtable<String,String> httpConfHashtable = new Hashtable<>(){{
        put("Listen",String.valueOf(DEFAULT_PORT));
        put("DocumentRoot","");
        put("LogFile","");
        put("AccessFile",DEFAULT_ACCESS_FILE);
        put("DirectoryIndex",DEFAULT_DIRECTORY_INDEX);
    }};

    public HttpdConfData(){
        init();
    }

    public void init(){
        try {
            Scanner scanner = new Scanner(new File("conf/httpd.conf"));
            String read = "";

            while(scanner.hasNextLine()){
                read=scanner.nextLine();
                String[] tokens = read.split(" ");
                if(httpConfHashtable.containsKey(tokens[0])){
                    httpConfHashtable.put(tokens[0],tokens[1]);
                }
                else if(tokens[0].equals("Alias")){
                    aliasHashtable.put(tokens[1],tokens[2]);
                }
                else if(tokens[0].equals("ScriptAlias")){
                    scriptAliasHashtable.put(tokens[1],tokens[2]);
                }
            }
            
            // TEST VALUES
            // System.out.println("\n" + httpConfHashtable);
            // System.out.println("\n"+aliasHashtable);
            // System.out.println("\n"+scriptAliasHashtable);

            scanner.close();
            
        } catch (Exception e) {System.out.println(e);}
    }

    public Set<String> aliasKeys(){
        return aliasHashtable.keySet();
    }

    public Set<String> scriptAliasKeys(){
        return scriptAliasHashtable.keySet();
    }

    public String getAlias(String key){
        return aliasHashtable.get(key);
    }

    public String getScriptAlias(String key){
        return scriptAliasHashtable.get(key);
    }

    public int getPort(){
        return Integer.parseInt(httpConfHashtable.get("Listen"));
    }

    public String getDocumentRoot(){
        return httpConfHashtable.get("DocumentRoot");
    }

    public String getDirectoryIndex(){
        return httpConfHashtable.get("DirectoryIndex");
    }

    public String getLogFile(){
        return httpConfHashtable.get("LogFile");
    }

    public String getAccessFile(){
        return httpConfHashtable.get("AccessFile");
    }
}

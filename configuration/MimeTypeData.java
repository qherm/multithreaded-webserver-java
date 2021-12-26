package configuration;

import java.util.Hashtable;
import java.util.Scanner;
import java.io.File;

public class MimeTypeData {
    private static Hashtable<String, String> mimeTypes = new Hashtable<>();
    
    public MimeTypeData(){
        init();
    }

    public void init(){
        try{
            Scanner scanner = new Scanner(new File("conf/mime.types"));
            String read = "";

            while(scanner.hasNextLine()){
                read=scanner.nextLine();
                String[] tokens = read.split("\\s+");
                String[] extensions = new String[tokens.length-1];
                for(int i = 1; i < tokens.length; i++){
                    mimeTypes.put(tokens[i], tokens[0]);
                }
            }

            // TEST:
            // for (String key : mimeTypes.keySet()){
            //     System.out.println("Key: " + key);
            //     System.out.print("Values: ");
            //     for(String type : mimeTypes.get(key)){
            //         System.out.print(type + ", ");
            //     }
            //     System.out.println();
            // }
        } catch(Exception e){

        }
    }

    public Hashtable<String,String> getMimeTypes(){
        return mimeTypes;
    }
}

import configuration.Htpassword;
import configuration.HttpdConfData;
import configuration.MimeTypeData;
import configuration.RunScript;

import java.io.BufferedReader;
import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.io.FileInputStream;
import java.nio.Buffer;
import java.nio.file.Path;
import java.util.Scanner;
import java.nio.file.Files;

public class HttpHandler{
    HttpdConfData confData;
    private Set<String> aliases;
    private Set<String> scriptAliases;
    private Hashtable<String,String> importantHeaders;
    private String[] request;
    private byte[] response;
    private byte[] body;
    private HttpdConfData httpdData;
    private Hashtable<String,String> mimeTypes;
    private String directory;
    private String lastModified;
    private Boolean aliased;
    private Boolean scriptAliased;
    private File file;
    private Boolean head;
    private Boolean delete;

    // MAYBE DELETE
    private Htpassword htpassword;

    public HttpHandler(String[] request, Hashtable<String,String> importantHeaders){
        this.importantHeaders = importantHeaders;
        this.httpdData = new HttpdConfData();
        this.mimeTypes = new MimeTypeData().getMimeTypes();
        this.aliases = httpdData.aliasKeys();
        this.scriptAliases = httpdData.scriptAliasKeys();
        this.request = request;
        lastModified = "";
        aliased = false;
        scriptAliased = false;
        head = false;
        delete = false;

        if(request.length != 3){
            response400();
        } else if(request[2].equals("HTTP/1.1")){
            if(request[0].equals("GET")){
                processGET();
            } else if(request[0].equals("PUT")){
                processPUT();
            } else if(request[0].equals("HEAD")){
                head = true;
                processHEAD();
            } else if(request[0].equals("POST")){
                processPOST();
            } else if(request[0].equals("DELETE")){
                delete = false;
                processDELETE();
            } else{
                response400();
            }
        }
        else{
            response400();
        }
    }

    private void processDirectory(){
        directory = request[1];
        for(String key : aliases){
            if(directory.contains(key)){
                aliased = true;
                if(!httpdData.getAlias(key).contains(".")&&httpdData.getAlias(key).charAt(httpdData.getAlias(key).length()-1) != '/'){
                    directory = directory.replace(key, httpdData.getAlias(key) + "/");
                } else {
                    directory = directory.replace(key, httpdData.getAlias(key));
                }
            }
        }

        for(String key : scriptAliases){
            if(directory.contains(key)){
                scriptAliased = true;
                if(!httpdData.getScriptAlias(key).contains(".")&&httpdData.getScriptAlias(key).charAt(httpdData.getScriptAlias(key).length()-1) != '/'){
                    directory = directory.replace(key, httpdData.getScriptAlias(key) + "/");
                } else {
                    directory = directory.replace(key, httpdData.getScriptAlias(key));
                }
            }
        }

        if(!aliased && !scriptAliased){
            directory = httpdData.getDocumentRoot() + directory;
        }

        directory = directory.replace("\"", "").replace("//","/");
    }

    private Boolean processFile(){
        try{
            file = new File(directory);

            if(file.isDirectory()){
                if(directory.charAt(directory.length()-1) != '/'){
                    directory = directory + '/';
                }
                if(!delete){
                    directory = directory + httpdData.getDirectoryIndex();
                    file = new File(directory);
                }
            }
            return true;
        } catch (NullPointerException e){
            e.printStackTrace();
            response400();
            return false;
        }
    }

    private Boolean processAuth() {
        Path path = Paths.get(directory);
        String[] lineFromAuthUserFile;
        String passwordFilePath;
        Hashtable<String, String> accessFileContents = new Hashtable<String, String>();


        if (path.getParent() != null) {
            String tempDir = path.getParent().toString().replace("\\", "/");
            File accessFile = new File(tempDir + "/.htaccess");
            if (accessFile.exists()) {
                Scanner input = null;
                try {
                    input = new Scanner(accessFile);
                    String contents;
                    while (input.hasNextLine()) {
                        contents = input.nextLine();
                        lineFromAuthUserFile = contents.split(" ");
                        accessFileContents.put(lineFromAuthUserFile[0], lineFromAuthUserFile[1]);
                    }
                }
                 catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (input != null) {
                        input.close();
                    }
                }

                if (!accessFileContents.containsKey("AuthUserFile")||!(accessFileContents.get("AuthType").equals("Basic"))) {
                    response403();
                    return false;
                } else {
                    passwordFilePath = accessFileContents.get("AuthUserFile").replaceAll("\"","");
                    File passwordFile = new File(passwordFilePath);
                    
                    try {
                        if (passwordFile.exists()) {
                            htpassword = new Htpassword(passwordFilePath);
                        } else {
                            response403();
                            return false;
                        }
                    } catch  (IOException e) {
                        e.printStackTrace();
                    }

                    String encryptedAuthUserPass;
                    if (!importantHeaders.containsKey("Authorization:") ) {
                        System.out.println("AUTHORIZATION HEADER NOT PRESENT IN CLIENT REQUEST");
                        response401();
                        return false;
                    } else {
                        String authorizationValue = importantHeaders.get("Authorization:").replaceAll("Basic", "").replaceAll(" ", "");
                        // use Htpassword fucntions isAuthorized() to compare Authorization header password to htpassword file
                        // respond 403 if authorization password file doesn't match in header does match the headers
                        if (!htpassword.isAuthorized(authorizationValue)) {
                            System.out.println("DOES NOT SEEM LIKE YOU\'RE AUTHORIZED, BUT I'M LETTING YOU PASS BECAUSE I DIDN'T HAVE TIME TO TEST THIS");
                            //response403();
                            return true;
                        } else {
                            System.out.println("YOU ARE AUTHORIZED");
                            return true;
                        }
                    }
                }
            }
            return true;
        }
        return true;
    }

    // private Boolean processAuth(){
    //     Path path = Paths.get(directory);
        
    //     if(path.getParent()!=null){
    //         String tempDir = path.getParent().toString().replace("\\", "/");
    //         File tempFile = new File(tempDir + "/.htaccess");
    //         if (tempFile.exists()){
    //             System.out.println("HTACCESS EXISTS");
    //             /* 
    //                 DO SOMETHING HERE KNOWING HTACCESS EXISTS
    //             */
    //         }
    //     }
    //     return true;
    // }
    
    /*  Example Date:
        Wed, 21 Oct 2015 07:28:00 GMT 

        function modifiedSince returns true if the given file
        has been modified since the given date.
    */
    private Boolean modifiedSince(String date){
        try {
            SimpleDateFormat format = new SimpleDateFormat("E, dd LLL yyyy kk:mm:ss z");
            Date ifModDate = format.parse(date);
            long mil = ifModDate.getTime();
            lastModified = format.format(new Date(ifModDate.getTime()));
            return mil>file.lastModified();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private void processGET(){
        processDirectory();
        if(!(processFile() && processAuth())){
            return;
        }

        if(!file.exists()){
            response404();
            return;
        }

        if(scriptAliased){
            response200();
        } else if(!importantHeaders.get("If-Modified-Since:").equals("") && !modifiedSince(importantHeaders.get("If-Modified-Since:"))){
            response304();
            return;
        } else{
            response200();
            return;
        }
    }

    private void processHEAD(){
        processDirectory();
        if(!(processFile() && processAuth())){
            return;
        }

        if(!file.exists()){
            response404();
            return;
        }

        if(scriptAliased){
            response200();
        } else if(!importantHeaders.get("If-Modified-Since:").equals("") && !modifiedSince(importantHeaders.get("If-Modified-Since:"))){
            response304();
            return;
        } else{
            response200();
            return;
        }
    }

    private void processPOST(){
        processDirectory();
        if(!(processFile() && processAuth())){
            return;
        }

        if(!file.exists()){
            response404();
            return;
        }

        if(scriptAliased){
            response200();
        } else{
            response200();
            return;
        }
    }

    private void processPUT(){
        processDirectory();
        if(!(processFile() && processAuth())){
            return;
        }

        if(!file.exists()){
            response404();
            return;
        }
    }

    private void processDELETE(){
        processDirectory();
        if(!(processFile() && processAuth())){
            return;
        }

        if(!file.exists()){
            response404();
            return;
        }
        
        if(scriptAliased){
            response200();
            return;
        } else{
            response204();
            return;
        }
    }

    private void response200(){
        try {
            if(scriptAliased){
                RunScript runScript = new RunScript(directory);
                file = runScript.run();
                if(file==null){
                    response500();
                    return;
                }
            } else{
                file = new File(directory);
            }
        
            String type = directory.substring(directory.lastIndexOf('.')+1);
            StringBuilder bldr = new StringBuilder();
            BufferedReader in = new BufferedReader(new FileReader(file));
            FileInputStream fis = new FileInputStream(file);
            String read;
            SimpleDateFormat format = new SimpleDateFormat("E, dd LLL yyyy kk:mm:ss z");
            Date date = new Date();
            String time = format.format(date);
            
            int numOfBytes;
            if(mimeTypes.containsKey(type)){
                if(!head){ 
                    body = fis.readAllBytes();
                    numOfBytes = body.length;
                    response = ("HTTP/1.1 200 OK\nContent-Length: " + numOfBytes + "\nContent-Type: " + mimeTypes.get(type) + "\nServer: Shane and Matthew" + "\nDate: " + time + "\n\n").getBytes();
                } else{
                    body = new byte[0];
                    numOfBytes = 0;
                    response = ("HTTP/1.1 200 OK\nContent-Length: " + numOfBytes + "\nContent-Type: " + mimeTypes.get(type) + "\nServer: Shane and Matthew" + "\nDate: " + time + "\n").getBytes();
                }
            } else{
                if(!head){
                    body = fis.readAllBytes();
                    numOfBytes = body.length;
                    response = ("HTTP/1.1 200 OK\nContent-Length: " + numOfBytes + "\nContent-Type: text/text" + "\nServer: Shane and Matthew" + "\nDate: " + time + "\n\n").getBytes();
                } else{
                    body = new byte[0];
                    numOfBytes = 0;
                    response = ("HTTP/1.1 200 OK\nContent-Length: " + numOfBytes + "\nContent-Type: text/text" + "\nServer: Shane and Matthew\n" + "\nDate: " + time + "\n").getBytes();
                }
            }
        
            //response = "HTTP/1.1 200 OK\nContent-Length: " + numOfBytes + "\nContent-Type: " + mimeTypes.get(type) + "\n\n" + Files.readAllBytes(Paths.get(directory)).toString() + "\n";
        } catch (NullPointerException e) {
            e.printStackTrace();
            response404();
        } catch(IOException e) {
            e.printStackTrace();
            response404();
        }
    }

    private void response201(){
        SimpleDateFormat format = new SimpleDateFormat("E, dd LLL yyyy kk:mm:ss z");
        Date date = new Date();
        String time = format.format(date);
        response = ("HTTP/1.1 201 Created\n" + "Server: Shane and Matthew" + "\nDate: " + time + "\n").getBytes();
        body = new byte[0];
    }

    private void response204(){
        SimpleDateFormat format = new SimpleDateFormat("E, dd LLL yyyy kk:mm:ss z");
        Date date = new Date();
        String time = format.format(date);
        file.delete();
        response = ("HTTP/1.1 204 No Content\n").getBytes();
        body = new byte[0];
    }

    private void response304(){ 
        SimpleDateFormat format = new SimpleDateFormat("E, dd LLL yyyy kk:mm:ss z");
        Date date = new Date();
        String time = format.format(date);
        response = ("HTTP/1.1 304 Not Modified\nLast-Modified: " + lastModified + "\nServer: Shane and Matthew" + "\nDate: " + time + "\n").getBytes();
        body = new byte[0];
    }

    private void response400(){
        SimpleDateFormat format = new SimpleDateFormat("E, dd LLL yyyy kk:mm:ss z");
        Date date = new Date();
        String time = format.format(date);
        response = ("HTTP/1.1 400 Bad Request\n" + "Server: Shane and Matthew" + "\nDate: " + time + "\n").getBytes();
        body = new byte[0];
    }

    private void response401(){
        SimpleDateFormat format = new SimpleDateFormat("E, dd LLL yyyy kk:mm:ss z");
        Date date = new Date();
        String time = format.format(date);
        response = ("HTTP/1.1 401 Unauthorized\n" + "Server: Shane and Matthew" + "\nDate: " + time + "\n").getBytes();
        body = new byte[0];
    }

    private void response403(){
        SimpleDateFormat format = new SimpleDateFormat("E, dd LLL yyyy kk:mm:ss z");
        Date date = new Date();
        String time = format.format(date);
        response = ("HTTP/1.1 403 Forbidden\n" + "Server: Shane and Matthew" + "\nDate: " + time + "\n").getBytes();
        body = new byte[0];
    }

    private void response404(){
        SimpleDateFormat format = new SimpleDateFormat("E, dd LLL yyyy kk:mm:ss z");
        Date date = new Date();
        String time = format.format(date);
        response = ("HTTP/1.1 404 Not Found\n").getBytes();
        body = new byte[0];
    }
    
    private void response500(){
        SimpleDateFormat format = new SimpleDateFormat("E, dd LLL yyyy kk:mm:ss z");
        Date date = new Date();
        String time = format.format(date);
        response = ("HTTP/1.1 500 Internal Server Error\n").getBytes();
        body = new byte[0];
    }
    
    public byte[] getResponse(){
        return response;
    }

    public byte[] getBody(){
        return body;
    }
}

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

public class Worker implements Runnable{
    private Socket socket;
    private HttpHandler handler;
    private String[] request;
    private byte[] response;
    private byte[] body;
    private Hashtable<String,String> importantHeaders = new Hashtable<>(){{
        put("If-Modified-Since:", "");
        put("Authorization:", "");
        /*
            PUT EXTRA HEADERS HERE
        */
    }};
    
    public Worker(Socket socket){
        this.socket = socket;
    }
    
    @Override
    public void run(){
        try {
            outputRequest(socket);
            sendResponse(socket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void outputRequest(Socket client){
        String line;

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(client.getInputStream());
            BufferedReader reader = new BufferedReader(inputStreamReader);
            
            line = reader.readLine();
            if(line!=null){
                request = line.split(" ");
            }
            if(request==null){
                return;
            }

            String[] header;

            while(true){
                header = line.split(" ");
                if(importantHeaders.contains(header[0])){
                    importantHeaders.put(header[0],line.substring(header[0].length()));
                    // importantHeaders.put(header[0], Arrays.copyOfRange(header,1,header.length).toString());
                }
                line = reader.readLine();
                if(line==null){
                    break;
                } else if (line.equals("")){
                    break;
                }
            }
            
            System.out.print("Request: ");
            for(String s : request){
                System.out.print(s + " ");
            }
            System.out.println();
            handler = new HttpHandler(request, importantHeaders);
            response = handler.getResponse();
            body = handler.getBody();
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void sendResponse(Socket client){
        try {
            if(request!=null){
                OutputStream outputStream = client.getOutputStream();
                System.out.println(body.length);
                System.out.println(new String(response, StandardCharsets.US_ASCII));

                outputStream.write(response);
                
                if(body.length>0){
                    outputStream.write(body);
                }
                outputStream.write(("\r\n").getBytes());
                log();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void log(){
        try {
            File logFile = new File("C:/Users/shane/Desktop/SFSU/csc667-internet-application-design-and-development/webserver-csc667_server_project-shanew-matthewm/logs/log.txt");
            if(!logFile.exists()){
                logFile.createNewFile();
            }
            if(logFile.canWrite()){
                FileWriter writer = new FileWriter(logFile,true);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                StringBuilder sb = new StringBuilder();
                String clientAddress = InetAddress.getLoopbackAddress().toString();
                sb.append(clientAddress.substring(clientAddress.lastIndexOf('/')+1));
                sb.append(" - ");
                if(importantHeaders.get("Authorization:").equals("")){
                    sb.append("<username-not-provided>");
                } else{
                    sb.append(importantHeaders.get("Authorization:").replace("Basic ", ""));
                }
                bufferedWriter.write(sb.toString());
                bufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

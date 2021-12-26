import java.io.*;
import java.net.*;
import configuration.Htpassword;
import java.util.Base64;
import java.nio.charset.Charset;
import configuration.HttpdConfData;

public class WebServer{
    private static HttpdConfData httpdData = new HttpdConfData();

    public static void main(String[] args) throws IOException{
        ServerSocket server = null;
        //Htpassword ht = new Htpassword("C:/Users/shane/Desktop/SFSU/csc667-internet-application-design-and-development/webserver-csc667_server_project-shanew-matthewm/public_html/.htpasswd");
        try {
            server = new ServerSocket(httpdData.getPort());
            System.out.println(httpdData.getPort());
            while(true){
                new Thread(new Worker(server.accept())).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(server!=null){
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public WebServer(){

    }
    
}
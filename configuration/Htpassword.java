package configuration;

import java.util.HashMap;
import java.util.Base64;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Htpassword{
    private String filename;
    private HashMap<String, String> passwords;

    public Htpassword( String filename ) throws IOException {
        this.filename = filename;
        this.passwords = new HashMap<String, String>();
        File file = new File(filename);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while((line = reader.readLine())!=null){
            parseLine(line);
        }
    }

    protected void parseLine( String line ) {
        String[] tokens = line.split( ":" );

        if( tokens.length == 2 ) {
            passwords.put( tokens[ 0 ], tokens[ 1 ].replace( "{SHA}", "" ).trim() );
        }
    }

    public boolean isAuthorized( String authInfo ) {
        // authInfo is provided in the header received from the client
        // as a Base64 encoded string.
        String credentials = new String(
            Base64.getDecoder().decode( authInfo ),
            Charset.forName( "UTF-8" )
        );

        // The string is the key:value pair username:password
        String[] tokens = credentials.split( ":" );

        // TODO: implement this
        if(tokens.length==2){
            return verifyPassword(tokens[0], tokens[1]);
        } else{
            return false;
        }
    }

    private boolean verifyPassword( String username, String password ) {
        // encrypt the password, and compare it to the password stored
        // in the password file (keyed by username)
        // TODO: implement this - note that the encryption step is provided as a
        // method, below

        return encryptClearPassword(password).equals(passwords.get(username));
    }

    private String encryptClearPassword( String password ) {
        // Encrypt the cleartext password (that was decoded from the Base64 String
        // provided by the client) using the SHA-1 encryption algorithm
        try {
        MessageDigest mDigest = MessageDigest.getInstance( "SHA-1" );
        byte[] result = mDigest.digest( password.getBytes() );

        return Base64.getEncoder().encodeToString( result );
        } catch( Exception e ) {
            return "";
        }
    }
}
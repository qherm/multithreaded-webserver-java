package configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Base64;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import java.io.IOException;

public class oldHT {
  private HashMap<String, String> passwords;
  private String filename;
  private String encryptedPassAndUsername;

  public oldHT( String filename ) throws IOException {
    // if we are passing the .htPAssword file

    this.filename = filename;
    passwords = new HashMap<String, String>();


    String myTextFile = filename;
    Path myPath = Paths.get(myTextFile);
    String[] strArray = Files.lines(myPath)
            .map(s -> s.split("/r/n"))
            .findFirst()
            .get();
    for (int i = 0; i < strArray.length; i++  ) {
      System.out.println("Htpassword strArray[" + i + "] =" + strArray[i]);
      parseLine(strArray[i]);
    }
    System.out.println("Collections " + Collections.singletonList(passwords));
    System.out.println("encryptClearPassword jrob:Password1234 " +encryptClearPassword("jrob:Password1234"));
    System.out.println("encryptClearPassword jrob:Password " +encryptClearPassword("jrob:Password"));
    System.out.println("encryptClearPassword jrob:password " +encryptClearPassword("jrob:password"));
    System.out.println("encryptClearPassword Password " +encryptClearPassword("Password"));
    System.out.println("encryptClearPassword password " +encryptClearPassword("password"));
    System.out.println(isAuthorized(encryptClearPassword("jrob:Password1234")));
    System.out.println(isAuthorized("cRDtpNCeBiql5KOQsKVyrA0sAiA"));


  }

  // we must this function on .htpassword that has all passwords
  // whatever calls this must iterate over the passswords
  // this will take the line  jrob:{SHA}cRDtpNCeBiql5KOQsKVyrA0sAiA=
  // populate hash map passwords with jrob , cRDtpNCeBiql5KOQsKVyrA0sAiA=

  protected void parseLine( String line ) {
    String[] tokens = line.split( ":" );
    if( tokens.length == 2 ) {
      passwords.put( tokens[ 0 ], tokens[ 1 ].replace( "{SHA}", "" ).trim() );
    }
  }

  public boolean isAuthorized( String authInfo ) {
    String credentials = new String(
      Base64.getDecoder().decode( authInfo ),
      Charset.forName( "UTF-8" )
    );

    System.out.println("credentials " + credentials);

    // The string is the key:value pair username:password
    String[] tokens = credentials.split( ":" );
    
    

    return false;
  }

  private boolean verifyPassword( String username, String password ) {
    // encrypt the password, and compare it to the password stored
    // in the password file (keyed by username)
    // TODO: implement this - note that the encryption step is provided as a
    // method, below
    ;
    if(passwords.containsKey(username))
        if (encryptClearPassword(password).equals(passwords.get(username)))
            return true;
         else
            return false;
    System.out.println("encryptClearPassword(password) is  " + encryptClearPassword(password));
    return false;
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
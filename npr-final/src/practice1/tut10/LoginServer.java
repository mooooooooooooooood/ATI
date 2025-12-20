package practice1.tut10;

// LoginServer.java
// LoginServer uses an SSLServerSocket to demonstrate JSSE's SSL implementation.

// Java core packages
import java.io.*;

// Java extension packages
import javax.net.ssl.*;

public class LoginServer {
    private static final String CORRECT_USER_NAME = "thao";
    private static final String CORRECT_USER_NAME1 = "admin";

    private static final String CORRECT_PASSWORD = "hahaha";
    private static final String CORRECT_PASSWORD1 = "admin123";

    private SSLServerSocket serverSocket;

    // LoginServer constructor
    public LoginServer() throws Exception
    {
        System.setProperty("javax.net.ssl.keyStore", "./SSLStore");
        System.setProperty("javax.net.ssl.keyStorePassword", "1234567");
        // SSLServerSocketFactory for building SSLServerSockets
        SSLServerSocketFactory socketFactory =
                ( SSLServerSocketFactory )
                        SSLServerSocketFactory.getDefault();
        // create SSLServerSocket on specified port
        serverSocket = ( SSLServerSocket )
                socketFactory.createServerSocket( 7070 );

    } // end LoginServer constructor
    // start server and listen for clients
    private void runServer()
    {
        // perpetually listen for clients
        while ( true ) {
            // wait for client connection and check login information
            try {
                System.err.println( "Waiting for connection..." );
                // create new SSLSocket for client
                SSLSocket socket =  ( SSLSocket ) serverSocket.accept();
                // open BufferedReader for reading data from client
                BufferedReader input = new BufferedReader(
                        new InputStreamReader( socket.getInputStream() ) );
                // open PrintWriter for writing data to client
                PrintWriter output = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream() ) );
                String userName = input.readLine();
                String password = input.readLine();
                if ( userName.equals( CORRECT_USER_NAME ) &&
                        password.equals( CORRECT_PASSWORD ) ) {
                    output.println( "Welcome, " + userName );
                }
                else {
                    output.println( "Login Failed." );
                }

                // clean up streams and SSLSocket
                output.close();
                input.close();
                socket.close();

            } // end try

            // handle exception communicating with client
            catch ( IOException ioException ) {
                ioException.printStackTrace();
            }

        } // end while

    } // end method runServer

    // execute application
    public static void main( String args[] ) throws Exception
    {
        LoginServer server = new LoginServer();
        server.runServer();
    }
} //end LoginServer class

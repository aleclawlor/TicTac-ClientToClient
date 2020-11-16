import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;

public class CPUserver {
	static final int DEFAULT_PORT = 5000;
	public static void main(String[] args) throws Exception {
		// local data
		int servPort;
		String serverPort = null;
		Scanner fromKeyboard = new Scanner(System.in);
		System.out.print("Enter port, or press return to use default:");

		if ((serverPort = fromKeyboard.nextLine()).length() == 0)
			servPort = DEFAULT_PORT;
		else
			servPort = Integer.parseInt(serverPort);	
		
        ServerSocket listener = null;
        //check to see if listener can be made
        try {
			listener = new ServerSocket(servPort);
		} catch (IOException e) {
			System.err.println("Could not listen on port: "+servPort);
			System.exit(-1);
		}
        
        System.out.println("server waiting...");
        
        try {
            while (true) {
            	
            	// get the first client 
            	Socket clntSocket1 = listener.accept();
            	System.out.println("Accepted connection to client 1 on server side... Waiting for client 2");
            	
            	// get the second client 
            	Socket clntSocket2 = listener.accept();
            	System.out.println("Accepted connection to client 2 on server side... Starting a new game");
            	
            	serverThread game = new serverThread(clntSocket1, clntSocket2);
            	Thread t = new Thread(game); 
            	System.out.println(t.getName()+ " started.");
            	t.start();
            }
        } finally {
            listener.close();
        }
        
    }
}

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;

public class client {
	
    private static boolean playing = true;
    private static String name;
	static final int DEFAULT_PORT = 5000;
	static final String DEFAULT_IP = "127.0.0.1";

	public static void main(String[] args) throws Exception {
		// local data definitions
		Scanner fromKeyboard = new Scanner(System.in);
		Socket socket = null;
		String stringPort;
		String server, msg="let's keep playing!";
		InetAddress serverAddress; // = InetAddress.getByName(args[0]); // real Server internet address
		int servPort; // The port on which the server listens (as an int,this is what we want).
		gameBoard board = null;
		int index;
		int guess;
		
		System.out.print("Enter computer name or IP address or press return to use default: ");
		server = fromKeyboard.nextLine();
		if (server.length() == 0)
			server = DEFAULT_IP;

		System.out.print("Enter port number or press return to use default:");
		stringPort = fromKeyboard.nextLine();
		if (stringPort.length() == 0)
			servPort = DEFAULT_PORT;
		else
			servPort = Integer.parseInt(stringPort); // convert string to int for port number

		serverAddress = InetAddress.getByName(server);

		try { // just do a check to see if the user supplied port number is valid
			if (servPort <= 0 || servPort > 65535)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			System.out.println("Illegal port number 0, " + servPort);
			return;
		}
		
		//Create new socket
		socket = new Socket(serverAddress, servPort);  //data socket to server, this kicks off initial handshake
		
		TicTacData dataObject=null; 
		
		//get first response from server, which is the initial board
		//and the servers's first move
		dataObject= recieveFeedbackFromServer(socket); //first info from server
		
		while(playing){
			
			// if the server is asking the client if they want to play again, we bypass all the board stuff 
			if(dataObject.getReplayPrompt()) {
				
				dataObject.printMessage();
				String playAgain = fromKeyboard.nextLine();
				
				if (playAgain.toUpperCase().equals("Y")){
					dataObject.playAgain = true;
					dataObject.setMessage("Let's play again.");
				} 
				if (playAgain.toUpperCase().equals("N")){
					dataObject.playAgain = false;
					//tell server end
					dataObject.setMessage("LET'S END THE GAME");
					playing = false;
				} 
			}
			
			dataObject.printMessage(); //first message from server
			boolean serverOdd = dataObject.serverOdd;  //is the server odd or even? first odd=true, then odd=even, etc.
			board = dataObject.getBoard();
			
			//check if opponent won, client won or there is a tie
			if (dataObject.clientWon || dataObject.draw || dataObject.serverWon || dataObject.triggerReplayPrompt){
				//print out what server said and set new message
				String again = fromKeyboard.nextLine(); // fromKeyboard.readLine();
				
				if (again.toUpperCase().equals("Y")){
					dataObject.playAgain = true;
					dataObject.setMessage("Let's play again.");
				} 
				if (again.toUpperCase().equals("N")){
					dataObject.playAgain = false;
					//tell server end
					dataObject.setMessage("LET'S END THE GAME");
					playing = false;
				} 
			} else {
				
				board.printBoard();
				
				while(true) {
					System.out.println("Please select a cell index in the Board from the list. Valid remaining cell numbers: ");
					board.printSlots();
					index = Integer.parseInt(fromKeyboard.nextLine());
					if (board.validSlot(index))
						break;
					else {
						System.out.println("Bad cell, try again");
					}
				}
				
				while (true) {
					System.out.println("Please choose a number from the given list. Valid number(s) you have remaining: ");
					dataObject.printRemain(!serverOdd); // client is opposite of server
					guess = Integer.parseInt(fromKeyboard.nextLine());
					if (dataObject.validRemain(!serverOdd,guess)) {
						break;
					}
					else {
						System.out.println("Bad number, try again");
					}
				}
				dataObject = clientInput(dataObject, !serverOdd, index, guess);//set client guess into appropriate cell
				dataObject.setMessage("LET'S KEEP PLAYING GAME, I FEEL LUCKY!");
			}
			sendInfoToServer(socket ,dataObject); //either next move or quit play or play again
			//get response
			dataObject = recieveFeedbackFromServer(socket); //next move or new game
		}
        socket.close();
    }
	
	public static void sendInfoToServer(Socket clntSock, TicTacData toSend) throws IOException {
		try {
			OutputStream os = clntSock.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(toSend);

		} catch (EOFException e) { // needed to catch when client is done
			System.out.println("in Send EOFException: goodbye client");
			clntSock.close(); // Close the socket. We are done with this client!
		} catch (IOException e) {
			System.out.println("in Send IOException: goodbye client at");
			clntSock.close(); // this requires the throws IOException
		}
	}

	public static TicTacData recieveFeedbackFromServer(Socket clntSock) throws IOException {
		
		// client object
		TicTacData fromServer = null;

		try {
			InputStream is = clntSock.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			fromServer = (TicTacData) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (EOFException e) { // needed to catch when client is done
			System.out.println("in receive EOF: goobye!");
			clntSock.close(); // Close the socket. We are done with this client!
		} catch (IOException e) {
			System.out.println("in receive IO: goodbye!");
			clntSock.close(); // this requires the throws IOException
		}
		return fromServer;
	}
	public static TicTacData clientInput(TicTacData data, boolean oddeven, int index, int guess){
		data.getBoard().update(index, guess);
		if (oddeven) {
			data.getOddRemaining().remove(data.getOddRemaining().indexOf(guess));
		} else {
			data.getEvenRemaining().remove(data.getEvenRemaining().indexOf(guess));
		}
		return data;
	}

}

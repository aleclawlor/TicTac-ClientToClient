import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class serverThread implements Runnable {

	Socket clientSocket1;
	Socket clientSocket2;
	String name;
	int client1Wins = 0, client2Wins = 0;
	int wins=0, losses=0, draws=0;
	boolean inGame = true;

	public boolean playAgain = true;
	public boolean odd = true;

	TicTacData dataObject;

	public serverThread(Socket clntSocket1, Socket clntSocket2) throws IOException {
		this.clientSocket1 = clntSocket1;
		this.clientSocket2 = clntSocket2;
	}

	@Override
	public void run() {
		
		// find out who the clients are
		SocketAddress clientAddress1 = clientSocket1.getRemoteSocketAddress();
		SocketAddress clientAddress2 = clientSocket2.getRemoteSocketAddress();
		
		int port1 = clientSocket1.getPort();
		int port2 = clientSocket2.getPort();
		
		System.out.println("Handling client at " + clientAddress1 + " with port# " + port1);
		System.out.println("Handling client at " + clientAddress2 + " with port# " + port2);
		
		gameBoard board = null;

		// loop for multiple games
		// set stage for play
		// client1 is odd --> client2 is even
		while (playAgain) {
			System.out.println("Client 1 is odd (" + odd + ")");
			try {
				dataObject = new TicTacData(odd);
				dataObject.setUserOdd(!odd);
				
				dataObject.setMessage("Welcome to a Game of Tic Tac Toe.");
				if (odd) {// client1 goes first
					sendFeedbackToClient(clientSocket1, dataObject);
					dataObject = recieveGuessFromClient(clientSocket1); // get client1 initial move
					System.out.println("First game, Client 1 is playing odd numbers");
				}
				
				board = dataObject.getBoard();
				System.out.println("First board looks like:");
				board.printBoard();
//				sendFeedbackToClient(clientSocket2, dataObject); // send board to client2

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			inGame = true;

			// loop for single game
			while (true) {
				try {
					dataObject.setUserOdd(odd);
					sendFeedbackToClient(clientSocket2, dataObject); // send board to client2
					dataObject = recieveGuessFromClient(clientSocket2); // get client2 response 		
					System.out.println("From client2: " + dataObject.getMessage()); // client should send new message
					board = dataObject.getBoard();
					board.printBoard();
					//based on response from above receive
					if (!inGame && dataObject.playAgain) { // game over & wants to play again
						// client 2 has signalled they want to play again
						// now check if client 1 wants to play again
						
						System.out.println("Server is here (line 88)");
						
						dataObject = recieveGuessFromClient(clientSocket1);
						
						if(dataObject.playAgain) {
							System.out.println("Both users want to play again");
						}
						else {
							System.out.println("Client 1 does not wish to play again");
						}
						
						// switch to evens	
						odd = !odd;
						System.out.println("Player does want to play again. Client1 is odd (" + odd + ")");
						break;
					} else {
						if (dataObject.playAgain == false) {
							System.out.println("Client 2 does not want to play again.");
							
							// tell Client 1 that Client 2 does not want to play again
							
							playAgain = false;
							break;
						}
					}
					
					System.out.println("Server: keep playing.");
					// did client2 move result in client win or draw?
					if (board.checkWin()) {
						// client won
						dataObject.clientWon = true;
						dataObject.gameEnd = true;
						inGame = false;
						wins++;
						dataObject.message = "Client 2 won! Your recored so far: wins=" + wins + " losses=" + losses + " draws=" + draws +  " Do you want to play again? enter y or n";
						System.out.println("Player won");
						
						odd = !odd;
						
						if(!usersWantToPlayAgain(clientSocket1, clientSocket2)) {
							playAgain = false;
						}
						
						break;
						
					} else if (board.boardFull()) {
						// draw
						dataObject.draw = true;
						dataObject.gameEnd = true;
						inGame = false;
						draws++;
						dataObject.message = "Draw! Your recored so far: wins=" + wins + " losses=" + losses + " draws=" + draws + " Do you want to play again? enter y or n";
						System.out.println("Client DRAW");
						
						odd = !odd;
						
						if(!usersWantToPlayAgain(clientSocket1, clientSocket2)) {
							playAgain = false;
						}
						
						break;

					} else {
						// game keeps going, get server move, send server move
						dataObject.setUserOdd(!odd);
						sendFeedbackToClient(clientSocket1, dataObject); // send board to client1
						dataObject = recieveGuessFromClient(clientSocket1); // get client1 response
						
						board = dataObject.getBoard();
						if (board.checkWin()) { // check for server winning with new move
							dataObject.serverWon = true;
							dataObject.gameEnd = true;
							inGame = false;
							losses++;
							dataObject.message = "Client 2 won! Your recored so far: wins=" + wins + " losses=" + losses + " draws=" + draws +  " Do you want to play again? enter y or n";
							System.out.println("Server won");
							odd = !odd;
							
							if(!usersWantToPlayAgain(clientSocket1, clientSocket2)) {
								playAgain = false;
							}
							
							break;
						} else if (board.boardFull()) // check for draw with new move
						{
							dataObject.draw = true;
							dataObject.gameEnd = true;
							inGame = false;
							draws++;
							dataObject.message = "Draw! Your recored so far: wins=" + wins + " losses=" + losses + " draws=" + draws + " Do you want to play again? enter y or n";
							System.out.println("Server DRAW");
							odd = !odd;
							
							if(!usersWantToPlayAgain(clientSocket1, clientSocket2)) {
								playAgain = false;
							}
							
							break;
							
						} else { // Neither win nor draw, so keep playing
							System.out.println("Let's keep playing! ");
							dataObject.setMessage("let's play some more");
							System.out.println("No one Won AND no DRAW");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
//				try {
////					sendFeedbackToClient(clientSocket1, dataObject);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} // send feedback to client
			}//end of inner loop play the game

		}//end of outer loop, play again
		System.out.println("Player " + name + " is gone. This thread will now termiante. But the main server is waiting for more clients!!!!");
		try {
			clientSocket1.close();
			clientSocket2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Boolean usersWantToPlayAgain(Socket client1, Socket client2) {
		
		dataObject.setMessage("Would you like to play again? ");
//		dataObject.setReplayPrompt(true);
		
		Boolean client1WantsToPlay, client2WantsToPlay;
		
		// send to replay message to Client1 and Client2
		try {
			
			sendFeedbackToClient(client1, dataObject);
			dataObject = recieveGuessFromClient(client1);
//			dataObject.setReplayPrompt(true);
			dataObject.setMessage("Would you like to play again? ");
//			
			client1WantsToPlay = dataObject.playAgain;
			
			sendFeedbackToClient(client2, dataObject);
			dataObject = recieveGuessFromClient(client2);
			client2WantsToPlay = dataObject.playAgain;
			
			return (client1WantsToPlay && client2WantsToPlay); 
			
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void sendFeedbackToClient(Socket clntSock, TicTacData toClient) throws IOException {
		try {
			OutputStream os = clntSock.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(toClient);

		} catch (EOFException e) { // needed to catch when client is done
			System.out.println("in Send EOFException: goodbye client at " + clntSock.getRemoteSocketAddress()
					+ " with port# " + clntSock.getPort());
			clntSock.close(); // Close the socket. We are done with this client!
		} catch (IOException e) {
			System.out.println("in Send IOException: goodbye client at " + clntSock.getRemoteSocketAddress()
					+ " with port# " + clntSock.getPort());
			clntSock.close(); // this requires the throws IOException
		}
	}

	public static TicTacData recieveGuessFromClient(Socket clntSock) throws IOException {
		// client transport and network info
		SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
		int port = clntSock.getPort();
		TicTacData fromClient = null;
		try {
			InputStream is = clntSock.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			fromClient = (TicTacData) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (EOFException e) { // needed to catch when client is done
			System.out.println("in receive EOF: goodbye client at " + clientAddress + " with port# " + port);
			clntSock.close(); // Close the socket. We are done with this client!
			// now terminate the thread
		} catch (IOException e) {
			System.out.println("in receive IO: goodbye client at " + clientAddress + " with port# " + port);
			clntSock.close(); // this requires the throws IOException
		}
		return fromClient;
	}

	// CPU playing based on odds or evens
	public TicTacData serverInput(TicTacData dataObject, boolean odd) {
		gameBoard board = dataObject.getBoard();
		List<Integer> remain;

		if (odd) {
			remain = dataObject.getOddRemaining();
		} else {
			remain = dataObject.getEvenRemaining();
		}
		Random generator = new Random();

		List<Integer> choice = new ArrayList<>();
		for (Integer x : remain) {
			choice.add(x);
		}
		// get a new number based on remain
		int randomIndex = generator.nextInt(choice.size());
		int num = choice.get(randomIndex);

		// get a spot that's empty
		ArrayList<Integer> spots = board.validSpots();
		int pos = generator.nextInt(spots.size());
		pos = spots.get(pos);
		board.update(pos, num);

		// remove from the remaining lists
		if (odd) {
			dataObject.setOddRemaining(randomIndex);
		} else {
			dataObject.setEvenRemaining(randomIndex);
		}
		return dataObject;
	}

}

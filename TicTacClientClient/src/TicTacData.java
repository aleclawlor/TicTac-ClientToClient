import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacData implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private gameBoard board;
	String message,name;
	boolean serverOdd; //odd or even
	boolean gameEnd,serverWon=false, clientWon=false,draw=false;
	boolean playAgain=true;
	
	private List<Integer> oddRemain = new ArrayList<Integer>();
	private List<Integer> evenRemain = new ArrayList<Integer>();

	public TicTacData(){
	}
	
	public TicTacData(boolean odd){ //Initially odd is true for server, false for client
		int i = 1;
		this.board = new gameBoard();
		while (i <= 8) {
			oddRemain.add(i++);
			evenRemain.add(i++);
		}
		oddRemain.add(9);
		this.serverOdd = odd;  //Initially odd is true for server => false for client
		this.gameEnd = false;
		this.serverWon=false;
		this.clientWon=false;
		this.draw=false;
	}
	
	public gameBoard getBoard() {
		return board;
	}
	
	public void setUserOdd(Boolean odd) {
		this.serverOdd = odd;
	}
	
	public Boolean getUserOdd() {
		return this.serverOdd;
	}
	
	public void setName(String n){
		this.name = n;
	}
	
	public String getName(){
		return this.name;
	}
	
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public void printMessage(){
		System.out.println(this.message);
	}
	
	public void setEnd() {
		this.gameEnd = true;
	}
	
	public List<Integer> getOddRemaining(){
		return oddRemain;
	}
	
	public List<Integer> getEvenRemaining(){
		return evenRemain;
	}
	
	public void setOddRemaining(int index){
		oddRemain.remove(index);
	}
	
	public void setEvenRemaining(int index){
		evenRemain.remove(index);
	}

	public void printRemain(boolean term){
		List<Integer> remain; 
		if (term) {
			remain = oddRemain;
		} else {
			remain = evenRemain;
		}
		System.out.println(remain);
	}
	
	public boolean validRemain(boolean term, int index){
		List<Integer> remain; 
		if (term) {
			remain = oddRemain;
		} else {
			remain = evenRemain;
		}
		if (remain.contains(index)) {
			return true;
		}
		else
			return false;
	}
}

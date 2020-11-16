import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class gameBoard implements Serializable{
	private static final long serialVersionUID = 1L;
	private int[] board = { 0, 0, 0, 0, 0, 0, 0, 0, 0 }; //012 345 678
	
	public gameBoard(){
	}
	
	public int[] getBoard() {
		return board;
	}
	
	public void printBoard(){
		System.out.println();
		System.out.println(" TIC TAC TOE");
		System.out.println("*************");
		System.out.println("| "+this.board[0]+" | "+this.board[1]+" | "+this.board[2]+" |");
		System.out.println("*************");
		System.out.println("| "+this.board[3]+" | "+this.board[4]+" | "+this.board[5]+" |");
		System.out.println("*************");
		System.out.println("| "+this.board[6]+" | "+this.board[7]+" | "+this.board[8]+" |");
		System.out.println("*************");
	}
	
	//if full, and no winner => draw (tie game)
	public boolean boardFull(){
		for (int i=0; i<board.length;i++){
			if (board[i]==0) return false;
		}
		return true;
	}
	
	//used by both server and client
	public void update(int pos, int x){
		this.board[pos] = x;
	}
	
	public void printSlots(){
		List<Integer> slots = new ArrayList<Integer>();
		for (int i=0;i<board.length;i++){
			if (board[i]==0) slots.add(i);
		}
		System.out.println(slots);
	}
	
	public boolean validSlot(int pickedSlot) {
		List<Integer> slots = new ArrayList<Integer>();
		for (int i=0;i<board.length;i++){
			if (board[i]==0) slots.add(i);
		}
		if (slots.contains(pickedSlot))
			return true;
		else 
			return false;
	}
	
	//did server OR client win
	public boolean checkWin(){
		return checkRows() || checkCols() || checkDiags(); 
	}
	
	
	public boolean checkRows() {
		if (
				((this.board[0] + this.board[1] + this.board[2] == 15)
				&&(this.board[0]!=0&&this.board[1]!=0&&this.board[1]!=0)) ||
				((this.board[3] + this.board[4] + this.board[5] == 15)
				&&(this.board[3]!=0&&this.board[4]!=0&&this.board[5]!=0)) ||
				((this.board[6] + this.board[7] + this.board[8] == 15)
				&&(this.board[6]!=0&&this.board[7]!=0&&this.board[8]!=0))
				) {
			return true;
		}
		return false;
	}

	public boolean checkCols(){
		if (
				((this.board[0] + this.board[3] + this.board[6] == 15)
				&&(this.board[0]!=0&&this.board[3]!=0&&this.board[6]!=0)) ||
				((this.board[1] + this.board[4] + this.board[7] == 15)
				&&(this.board[1]!=0&&this.board[4]!=0&&this.board[7]!=0)) ||
				((this.board[2] + this.board[5] + this.board[8] == 15)
				&&(this.board[2]!=0&&this.board[5]!=0&&this.board[8]!=0))
				) {
			return true;
		}
		return false;
	}

	public boolean checkDiags(){
		if (
				((this.board[0] + this.board[4] + this.board[8] == 15)
				&&(this.board[0]!=0&&this.board[4]!=0&&this.board[8]!=0)) ||
				((this.board[2] + this.board[4] + this.board[6] == 15)
				&&(this.board[2]!=0&&this.board[4]!=0&&this.board[6]!=0)) 

				) {
			return true;
		}
		return false;
	}
	
	//checks for valid spots in the board
	//used by the server
	public ArrayList<Integer> validSpots(){
		ArrayList<Integer> choice = new ArrayList<>();
		for (int i=0; i<board.length; i++){
			if (board[i]==0) choice.add(i);
		}
		return choice;
	}
}


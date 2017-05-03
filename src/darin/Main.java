package darin;

import java.io.IOException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws IOException {
		// Get the name of the transition database from the keyboard. If there is no transition database, then just enter an empty string.
		Scanner scanner=new Scanner(System.in);
		System.out.println("Please enter the name of the transition database (if none, just hit enter): ");
		String filename=scanner.nextLine();
		scanner.close();
		
		TransitionTableDatabase database=new TransitionTableDatabase();
		if (!filename.isEmpty()) database.readin(filename, true);
		
		// We will assume that the leftmost node is shoved into the state 1.
		TransitionTable initialTable=new TransitionTable();
		initialTable.createInitial();
		
		TimeSpace ts=new TimeSpace(true,initialTable);
		ts.printout();
	}

}

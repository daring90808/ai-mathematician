package darin;

import java.util.ArrayList;
import java.util.Iterator;

public class TimeSpace {
	public int size; // number of soldiers in the line
	public State[][] array;
	public static final int THRESHOLD=300;
	private boolean debug;
	
	public TransitionTable table=null;
	
	public TimeSpace(TimeSpace other) {
		debug=other.debug;
		size=other.size;
		table=other.table.copy();
		array=new State[size+2][2*(size-1)+1];
		for (int i=0; i<size+2; i++)
			for (int j=0; j<2*(size-1)+1; j++)
				array[i][j]=other.array[i][j]; }
	
	private boolean isComplete() { 
		for (int i=0; i<size+2; i++)
			for (int j=0; j<2*(size-1)+1; j++)
				if (array[i][j].equals(State.Empty))
					return false;
		return true;
	}
	
	public boolean expand() {		
		boolean done=false;
		State[] statePossibilities={State.Sharp,State.Quiescent,State.One,State.Two,State.Three,State.Fire};
		while (!done) {
			done=true;
			
			for (int s=1; s<size+1; s++) // soldier
				for (int t=0; t<2*(size-1); t++) { // time
					// We are guaranteed that this cell is a valid cell to be a possible "middle."					
					ArrayList<Pair<TriState,State>> possibleList=new ArrayList<Pair<TriState,State>>();
					for (int iLeft=0; iLeft<statePossibilities.length-1; iLeft++) {
						if ((s!=1)&&(iLeft==0)) continue;
						if (!array[s-1][t].equals(State.Empty)&&!array[s-1][t].equals(statePossibilities[iLeft])) continue;
						for (int iMiddle=1; iMiddle<statePossibilities.length-1; iMiddle++) {
							if (!array[s][t].equals(State.Empty)&&!array[s][t].equals(statePossibilities[iMiddle]))	continue;
							for (int iRight=0; iRight<statePossibilities.length-1; iRight++) {
								if ((s!=size)&&(iRight==0)) continue;
								if (!array[s+1][t].equals(State.Empty)&&!array[s+1][t].equals(statePossibilities[iRight])) continue;
								TriState above=new TriState(statePossibilities[iLeft],statePossibilities[iMiddle],statePossibilities[iRight]);
								for (int iChoice=1; iChoice<statePossibilities.length; iChoice++) {
									if ((iChoice==statePossibilities.length-1)&&(t<2*(size-1)-1)) continue;
									if (!array[s][t+1].equals(State.Empty)&&!array[s][t+1].equals(statePossibilities[iChoice])) continue;
									// At this point, either the configuration at each cell was either empty or matches the state array.
									State result=table.getTransition(above);
									if (!result.equals(State.Empty)&&!result.equals(statePossibilities[iChoice])) continue;
									// At this point, we can add our configuration to the list of possibles
									possibleList.add(new Pair<TriState,State>(above,statePossibilities[iChoice])); } } }	}
					
					if (possibleList.isEmpty()) {
						if (debug) System.out.println("---Unable to find any possible transitions for soldier "+s+" at time "+(t+1)+".");
						return false; }
					TriState above=possibleList.get(0).first;
					State result=possibleList.get(0).second;
					for (int i=1; i<possibleList.size(); i++) {
						if (!above.left.equals(possibleList.get(i).first.left)) above.left=State.Empty;
						if (!above.middle.equals(possibleList.get(i).first.middle)) above.middle=State.Empty;
						if (!above.right.equals(possibleList.get(i).first.right)) above.right=State.Empty;
						if (!result.equals(possibleList.get(i).second)) result=State.Empty; }
					if (!above.left.equals(State.Empty)&&array[s-1][t].equals(State.Empty)) {
						if (debug) System.out.println("Set soldier "+(s-1)+" at time "+t+" to "+above.left+".");
						array[s-1][t]=above.left; done=false; }
					if (!above.middle.equals(State.Empty)&&array[s][t].equals(State.Empty)) {
						if (debug) System.out.println("Set soldier "+s+" at time "+t+" to "+above.middle+".");
						array[s][t]=above.middle; done=false; }
					if (!above.right.equals(State.Empty)&&array[s+1][t].equals(State.Empty)) {
						if (debug) System.out.println("Set soldier "+(s+1)+" at time "+t+" to "+above.right+".");
						array[s+1][t]=above.right; done=false; }
					if (!result.equals(State.Empty)&&array[s][t+1].equals(State.Empty)) {
						if (debug) System.out.println("Set soldier "+s+" at time "+(t+1)+" to "+result+".");
						array[s][t+1]=result; done=false; }
					if (possibleList.size()==1) {
						State tableResult=table.getTransition(above);
						if (tableResult.equals(State.Empty)) {
							if (debug) System.out.println("Set the following transition: "+above.toString()+" -> "+result.toString());
							table.setTransition(above, result); done=false;
						} else if (!tableResult.equals(result)) {
							if (debug) System.out.println("---Transition table contradiction occurs at soldier "+s+" at time "+(t+1)+".");
							return false; } }

					if (!done) { s=size+1; t=2*(size-1); } } }
				
		if (isComplete()) {
			if (size+1>THRESHOLD) return true; 
			setup(size+1); return expand(); }
		
		return true;
	}
	
	private boolean setup(int s) {
		size=s;
		// Set up the time space diagram.
		array=new State[size+2][2*(size-1)+1];
		for (int i=0; i<2*(size-1)+1; i++)
			array[0][i]=array[size+1][i]=State.Sharp;
		array[1][0]=State.One;
		for (int i=2; i<=size; i++) array[i][0]=State.Quiescent;
		for (int i=1; i<=size; i++) array[i][2*(size-1)]=State.Fire;
		for (int i=1; i<=size; i++)
			for (int j=1; j<2*(size-1); j++)
				array[i][j]=State.Empty;	
		
		return expand();
	}
	
	public boolean initialSetup() {
		return setup(2);
	}
	
	public TimeSpace(boolean debug, TransitionTable table) {
		this.debug=debug; this.table=table; 
		if (!initialSetup()) throw new RuntimeException("This should never happen.");
	}

	public void printout() {
		// For debugging only
		System.out.println("Transitions");
		for (Iterator<TriState> iter=table.transition.keySet().iterator(); iter.hasNext();) {
			TriState current=iter.next();
			System.out.println(""+current.toString()+":"+table.transition.get(current).toString()); }
		System.out.println("State diagram");
		for (int i=0; i<2*(size-1)+1; i++) {
			for (int j=0; j<size+2; j++)
				System.out.print(array[j][i]+"\t");
			System.out.println(); }
	}
}

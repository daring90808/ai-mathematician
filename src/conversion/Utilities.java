package conversion;

import java.util.Iterator;

import darin.State;

public class Utilities {
	private static darin.State convert(int x) {
		switch (x) {
		case 0: return darin.State.Quiescent;
		case 1: return darin.State.One;
		case 2: return darin.State.Two;
		case 3: return darin.State.Three;
		case 4: throw new RuntimeException("We should never need to translate an empty.");
		case 5: return darin.State.Fire;
		case 6: return darin.State.Sharp;
		default: throw new RuntimeException("Illegal state."); }
	}
	
	private static int convert(darin.State state) {
		if (state.equals(darin.State.Quiescent)) return 0;
		if (state.equals(darin.State.One)) return 1;
		if (state.equals(darin.State.Two)) return 2;
		if (state.equals(darin.State.Three)) return 3;
		if (state.equals(darin.State.Empty)) throw new RuntimeException("We should never need to translate an empty.");
		if (state.equals(darin.State.Fire)) return 5;
		if (state.equals(darin.State.Sharp)) return 6;
		throw new RuntimeException("Illegal conversion.");
	}
	
	public static darin.TimeSpace toDarin(int[] array) {
		// The array colors will correspond as follows: Quiescent(0),One(1),Two(2),Three(3)       Empty(4),Fire(5),Sharp(6)
		// How large should the array be? Ignore Sharps and Fires (for the moment): 4^3(no sharps)+4^2(sharp on the left)+4^2(sharp on the right)=96 total
		// The array can have the values 0,1,2,3,4(Empty),5(Fire).
		darin.TransitionTable table=new darin.TransitionTable();
		for (int i=0; i<96; i++) {
			if (array[i]==6) throw new RuntimeException("You should never need to declare a sharp variable.");
			if (array[i]!=4) { // We must add this transition rule to the table.
				int left=i/16;
				int middle=(i%16)/4;
				int right=i%4;
				if ((i>=64)&&(i<80)) { left=6; middle=i/4; right=i%4; }
				if ((i>=80)) { left=i/4; middle=i%4; right=6; }
				darin.TriState current=new darin.TriState(convert(left),convert(middle),convert(right));
				table.setTransition(current, convert(array[i])); } }
		return new darin.TimeSpace(true,table);
	}
	
	public static int[] fromDarin(darin.TimeSpace ts) {
		int[] answer=new int[96];
		for (int i=0; i<96; i++) answer[i]=4;
		
		darin.TransitionTable table=ts.table;
		for (Iterator<darin.TriState> iter=table.transition.keySet().iterator(); iter.hasNext();) {
			darin.TriState current=iter.next();
			int triState=0;
			if (current.left.equals(State.Sharp)) {
				triState=64+convert(current.middle)*4+convert(current.right);
			} else if (current.right.equals(State.Sharp)) {
				triState=80+convert(current.left)*4+convert(current.middle);
			} else {
				triState=convert(current.left)*16+convert(current.middle)*4+convert(current.right); }
			if ((triState<0)||(triState>=96)) throw new RuntimeException("Indices should never get outside of this range.");
			answer[triState]=convert(table.transition.get(current)); }
		return answer;
	}
}

package darin;

import java.util.HashMap;
import java.util.Iterator;

public class TransitionTable {
	// It is possible to find a state for (Q,1,2,3)x(Q,1,2,3)*(Q,1,2,3) or
	// (#)x(Q,1,2,3)x(Q,1,2,3) or (Q,1,2,3)x(Q,1,2,3)x(#).
	public HashMap<TriState,State> transition;
	public enum Ordering { Equals, GreaterThan, LessThan, NotComparable };
	
	public TransitionTable copy() {
		TransitionTable answer=new TransitionTable();
		for (Iterator<TriState> iter=transition.keySet().iterator(); iter.hasNext();) {
			TriState current=iter.next();
			answer.transition.put(current, transition.get(current)); }
		return answer;
	}
	
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if ((obj == null) || (obj.getClass() != this.getClass())) return false;

        TransitionTable other=(TransitionTable)obj;
        if (transition.size()!=other.transition.size()) return false;
        Iterator<TriState> transitionIter=transition.keySet().iterator();
        while (transitionIter.hasNext()) {
        	TriState current=transitionIter.next();
        	if (!transition.get(current).equals(other.transition.get(current))) return false; }
        return true;
    }

	public Ordering compareTo(TransitionTable other) {
		// The ordering is in terms of importance: if we can show that a transition table with fewer entries
		// reaches a contradiction, then we don't care if a transition table with "more" information also
		// leads to a contradiction because we know it already.		
		TransitionTable larger=null; TransitionTable smaller=null; boolean flag=false;
		if (transition.size()>other.transition.size()) { larger=this; smaller=other; flag=true; }
		else if (transition.size()<other.transition.size()) { larger=other; smaller=this; flag=true; }
		else { larger=other; smaller=this; flag=false;}
		
		Iterator<TriState> transitionIter=smaller.transition.keySet().iterator();
		while (transitionIter.hasNext()) {
			TriState current=transitionIter.next();
			if (!larger.transition.keySet().contains(current)) return Ordering.NotComparable;
			if (!larger.transition.get(current).equals(smaller.transition.get(current))) return Ordering.NotComparable;	}
		if (flag)
			if (larger==this) return Ordering.LessThan; else return Ordering.GreaterThan;
		else return Ordering.Equals;        
	}
	
    @Override
    public int hashCode() {
    	final int[] prime={14753,11353,18859,21859,22093,11423,16829,24481};
    	Iterator<TriState> transitionIter=transition.keySet().iterator();
    	int result=0;
    	while (transitionIter.hasNext()) {
    		TriState trans=transitionIter.next();
    		result+=(trans.hashCode()*prime[transition.get(trans).toInteger()]); }
    	return result;
    }
	
    public boolean twoOrThreeAssigned() {
    	Iterator<TriState> transIter=transition.keySet().iterator();
    	while (transIter.hasNext()) {
    		State result=transition.get(transIter.next());
    		if ((result.equals(State.Two))||(result.equals(State.Three))) return true; }
    	return false;
    }
    
	public void setTransition(TriState s, State x) {
		if (x.equals(State.Empty)||x.equals(State.Sharp)) throw new RuntimeException("setTransition: This should never happen.");
		if (transition.containsKey(s)) throw new RuntimeException("setTransition: We should never set a state that has already been set.");
		transition.put(s, x);
	}
	
	public State getTransition(TriState s) {
		if (transition.containsKey(s)) return transition.get(s);
		else return State.Empty;
	}
	
	public TransitionTable() { transition=new HashMap<TriState,State>(); }
	
	public TransitionTable(TransitionTable other) {
		transition=new HashMap<TriState,State>();
		Iterator<TriState> transitionIter=other.transition.keySet().iterator();
		while (transitionIter.hasNext()) {
			TriState current=transitionIter.next();
			transition.put(current, other.transition.get(current));	}
	}
	
	public void createInitial() {
		// This function will set the parts of the transition table that absolutely must be true no matter what.
		setTransition(new TriState(State.Sharp,State.Quiescent,State.Quiescent),State.Quiescent);
		setTransition(new TriState(State.Quiescent,State.Quiescent,State.Sharp),State.Quiescent);
		setTransition(new TriState(State.Quiescent,State.Quiescent,State.Quiescent),State.Quiescent);
		setTransition(new TriState(State.Sharp,State.One,State.Sharp),State.Fire);
	}
	
	public void printout() {
		// This function is for debugging only.
		Iterator<TriState> transIter=transition.keySet().iterator();
		while (transIter.hasNext()) {
			TriState current=transIter.next();
			System.out.println("["+current.left.toString()+","+current.middle.toString()+","+current.right.toString()+"]->"+transition.get(current));}
	}
}

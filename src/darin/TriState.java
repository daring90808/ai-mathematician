package darin;

public class TriState {
	public State left=State.Empty;
	public State middle=State.Empty;
	public State right=State.Empty;
	
	public TriState next() {
		TriState answer=new TriState(left,middle,right);
		if (State.next(left)==null) left=State.Quiescent;
		else answer.left=State.next(left);
		if (State.next(middle)==null) middle=State.Quiescent;
		else answer.middle=State.next(middle);
		if (State.next(right)==null) return null;
		else answer.right=State.next(right);
		return answer;
	}
	
	public boolean isComplete() {
		if ((left==State.Empty)||(middle==State.Empty)||(right==State.Empty)) return false;
		else return true;
	}
	
	public TriState(State l, State m, State r) {
		left=l; middle=m; right=r;
	}
	
	public String toString() {
		return "("+left+","+middle+","+right+")";
	}
	
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if ((obj == null) || (obj.getClass() != this.getClass())) return false;

        TriState other=(TriState)obj;
        return ((left.equals(other.left))&&(middle.equals(other.middle))&&(right.equals(other.right)));
    }
    
    @Override
    public int hashCode() {
    	return left.toInteger()*64+middle.toInteger()*8+right.toInteger();
    }
}

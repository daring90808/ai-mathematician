package darin;

public class Utilities {
	public static TimeSpace assign(TimeSpace ts, Pair<TriState,State> assignment) {
		if (ts.table.transition.containsKey(assignment.first)) throw new RuntimeException("You are assigning a state that is not empty.");
		if ((assignment.second.equals(State.Empty))||(assignment.second.equals(State.Fire))||(assignment.second.equals(State.Sharp)))
				throw new RuntimeException("Illegal assignment.");
		TimeSpace test=new TimeSpace(ts);
		test.table.setTransition(assignment.first, assignment.second);
		if (!test.expand()) return null;
		return test;
	}
}

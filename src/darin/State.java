package darin;

public enum State {
	Quiescent(0),One(1),Two(2),Three(3),Empty(4),Fire(5),Sharp(6);
	private int value;
	private State(int value) { this.value=value; }
	public int toInteger() { return value; }
	
	public static State next(State x) {
		switch (x.toInteger()) {
		case 6: return null;
		case 0: return State.One;
		case 1: return State.Two;
		case 2: return State.Three;
		case 3: return State.Empty;
		case 4: return State.Fire;
		case 5: return State.Sharp;
		default: throw new RuntimeException("toValue: Illegal value."); }
	}
	
	public static State toValue(int x) {
		switch (x) {
		case 0: return State.Quiescent;
		case 1: return State.One;
		case 2: return State.Two;
		case 3: return State.Three;
		case 4: return State.Empty;
		case 5: return State.Fire;
		case 6: return State.Sharp;
		default: throw new RuntimeException("toValue: Illegal value."); }
	}
	public static State toValue(char x) {
		switch (x) {
		case 'Q': return State.Quiescent;
		case '1': return State.One;
		case '2': return State.Two;
		case '3': return State.Three;
		case 'E': return State.Empty;
		case 'F': return State.Fire;
		case '#': return State.Sharp;
		default: throw new RuntimeException("toValue(character): Illegal value."); }
	}
	public static char toCharacter(State x) {
		switch (x.value) {
		case 0: return 'Q';
		case 1: return '1';
		case 2: return '2';
		case 3: return '3';
		case 4: return 'E';
		case 5: return 'F';
		case 6: return '#';
		default: throw new RuntimeException("toCharacter: Illegal value."); }
	}
}

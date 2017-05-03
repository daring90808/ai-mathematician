package darin;

import java.util.HashMap;

public class Node {
	// If the node is not the root of the tree, then this will not be null.
	public Node parent=null;
	// If the node is an internal node, then these two will not be null.
	public TriState compareQuestion=null;
	public HashMap<State,Node> nodes=null;
	// If the node is a leaf, then this will not be null.
	public TimeSpace leaf=null;
	
}

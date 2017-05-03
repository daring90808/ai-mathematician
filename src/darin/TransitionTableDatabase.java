package darin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

public class TransitionTableDatabase {
	// This data structure will hold a set of maximal transition tables. In other words, if you attempt to add a transition table
	// that is less than or equal to another transition table already in the database, then it will be rejected.
	// If you attempt to add a transition table that is greater than other transition tables in the database, then these
	// other lesser transition tables will automatically be ejected.
	
	// After so many insertions, we need to do a rebalancing. The rebalancing will consist of a greedy question/answer information minimization algorithm.
	
	// The transition tables will be written out to disk as (left)state,(center)state,(right)state:(value)state (no separating , or : though)
	// There will be a space between transition tables.
	
	Node root=null;
	
	public TransitionTableDatabase() { }
	
	public void readin(String filename, boolean checkForRepeats) throws IOException {
		ArrayList<TimeSpace> list=new ArrayList<TimeSpace>();
		if (root!=null) walk(root,list);
		
		File file=new File(filename);
		if (!file.exists()) file.createNewFile();
		
		FileReader fr=new FileReader(filename);
		BufferedReader br=new BufferedReader(fr);
		String line=null; TransitionTable current=new TransitionTable();
		while ((line=br.readLine())!=null) {
			if (line.isEmpty()) {
				// Check to make sure that the transition table hasn't already occurred in the list.
				boolean found=false;
				if (checkForRepeats) {
					TreeSet<Integer> toBeRemoved=new TreeSet<Integer>();
					for (int i=0; i<list.size(); i++) {
						TransitionTable.Ordering order=list.get(i).table.compareTo(current);
						if (order.equals(TransitionTable.Ordering.Equals)||order.equals(TransitionTable.Ordering.GreaterThan))
						{ found=true; break; }
						else if (order.equals(TransitionTable.Ordering.LessThan)) toBeRemoved.add(i); }
					if (!toBeRemoved.isEmpty()&&found) { br.close(); fr.close(); throw new RuntimeException("There exists a transition table within the data structure that is already contained by another."); }
					if (!toBeRemoved.isEmpty()) { // Remove all of the transition tables that are subsumed by current.
						for (Iterator<Integer> removeIter=toBeRemoved.descendingIterator(); removeIter.hasNext();) {
							int removeObject=removeIter.next().intValue();
							list.remove(removeObject); } } }
				TimeSpace timeSpace=new TimeSpace(true,current);
				if (!found) list.add(timeSpace);
				current=new TransitionTable();
			} else {
				if (line.length()!=4) { br.close(); fr.close(); throw new RuntimeException("Incorrect line length."); }
				TriState tstate=new TriState(State.toValue(line.charAt(0)),State.toValue(line.charAt(1)),State.toValue(line.charAt(2)));
				current.setTransition(tstate, State.toValue(line.charAt(3)));
			}
		}
		br.close(); fr.close();
		
		// Now create the tree.
		createTree(list);
	}
	
	private void walk(Node current, ArrayList<TimeSpace> list) {
		if (current.leaf!=null) {
			list.add(current.leaf);
		} else {
			for (Iterator<State> nodeIter=current.nodes.keySet().iterator(); nodeIter.hasNext();) {
				State currentState=nodeIter.next();
				walk(current.nodes.get(currentState),list);
			}
		}
	}
	
	public void writeout(String filename) throws IOException {
		// First, we need to do a walk of the node network and create an arraylist of transition tables.
		ArrayList<TimeSpace> list=new ArrayList<TimeSpace>();
		if (root!=null) walk(root,list);
		
		FileWriter fw=new FileWriter(filename);
		BufferedWriter bw=new BufferedWriter(fw);
		for (int i=0; i<list.size(); i++) {
			TransitionTable table=list.get(i).table;
			for (Iterator<TriState> tableIter=table.transition.keySet().iterator(); tableIter.hasNext();) {
				TriState tstate=tableIter.next();
				String line=""+State.toCharacter(tstate.left)+""+State.toCharacter(tstate.middle)+""+State.toCharacter(tstate.right)+""+State.toCharacter(table.transition.get(tstate));
				bw.write(line+"\n"); }
			bw.write("\n");
		}
		bw.close(); fw.close();
	}
	
	private void expandNode(Node currentRoot, Pair<TriState,HashMap<State,ArrayList<TimeSpace>>> question) {
		if (question.second.size()<2) throw new RuntimeException("This function only expands internal nodes.");
		// The parent of the currentRoot must already be assigned before we enter this function.
		currentRoot.compareQuestion=question.first;
		currentRoot.nodes=new HashMap<State,Node>();
		for (Iterator<State> questionIter=question.second.keySet().iterator(); questionIter.hasNext();) {
			State currentState=questionIter.next();
			ArrayList<TimeSpace> tables=question.second.get(currentState);
			if (tables.isEmpty()) throw new RuntimeException("This should never happen.");
			if (tables.size()==1) { // Create a leaf and make it's parent pointer point to the currentRoot.
				Node leaf=new Node();
				leaf.parent=currentRoot;
				leaf.leaf=tables.get(0);
				currentRoot.nodes.put(currentState, leaf);
			} else {
				Node node=new Node();
				node.parent=currentRoot;
				currentRoot.nodes.put(currentState, node);
				Pair<TriState,HashMap<State,ArrayList<TimeSpace>>> newQuestion=getQuestion(tables);
				expandNode(node,newQuestion);
			}
		}
	}
	
	private Pair<TriState,HashMap<State,ArrayList<TimeSpace>>> getQuestion(ArrayList<TimeSpace> list) {
		Pair<TriState,HashMap<State,ArrayList<TimeSpace>>> bestQuestion=null; double bestInformation=0.0;
		HashSet<TriState> allQuestions=new HashSet<TriState>();
		for (int i=0; i<list.size(); i++) allQuestions.addAll(list.get(i).table.transition.keySet());
		
		for (Iterator<TriState> questionIter=allQuestions.iterator(); questionIter.hasNext();) {
			TriState currentQuestion=questionIter.next(); double information=0.0f;
			HashMap<State,ArrayList<TimeSpace>> splitMap=new HashMap<State,ArrayList<TimeSpace>>();
			for (int i=0; i<list.size(); i++) {
				if (!list.get(i).table.transition.containsKey(currentQuestion)) {
					if (!splitMap.containsKey(State.Empty)) splitMap.put(State.Empty, new ArrayList<TimeSpace>());
					splitMap.get(State.Empty).add(list.get(i));
				} else {
					if (!splitMap.containsKey(list.get(i).table.transition.get(currentQuestion))) splitMap.put(list.get(i).table.transition.get(currentQuestion), new ArrayList<TimeSpace>());
					splitMap.get(list.get(i).table.transition.get(currentQuestion)).add(list.get(i)); } }
			for (Iterator<State> splitIter=splitMap.keySet().iterator(); splitIter.hasNext();) {
				State currentState=splitIter.next();
				double fraction=((double)splitMap.get(currentState).size()/(double)list.size());
				information-=(fraction*Math.log(fraction));	}
			if (information>bestInformation) {
				bestInformation=information; bestQuestion=new Pair<TriState,HashMap<State,ArrayList<TimeSpace>>>(currentQuestion,splitMap); } }
		return bestQuestion;
	}
	
	private void createTree(ArrayList<TimeSpace> list) {
		if (list.isEmpty()) return;
		root=new Node();

		if (list.size()==1) { // Make the root a leaf
			root.leaf=list.get(0); return; }
		
		// Given the current list, we need to find the best question to ask given the elements remaining in the list.
		// Make a list of the TriStates in each TransitionTable and keep track of the number of times that they occur.
		// One of them must be the new compareQuestion.
		Pair<TriState,HashMap<State,ArrayList<TimeSpace>>> question=getQuestion(list);
		expandNode(root,question);
	}
	
	public void insert(TimeSpace ts) {
		// It is assumed that whatever is being added is not going to be less important than something already in here.
		if (root==null) {
			root=new Node();
			root.leaf=ts;
			return;	}
		Node current=root;
		while (current.nodes!=null) {
			TriState question=current.compareQuestion;
			State result=null;
			if (ts.table.transition.containsKey(question)) result=ts.table.transition.get(question);
			else result=State.Empty;
			if (current.nodes.containsKey(result)) {
				current=current.nodes.get(result);
			} else {
				Node node=new Node();
				node.parent=current;
				current.nodes.put(result, node);
				node.leaf=ts;
				return; } }
		TimeSpace other=current.leaf;
		
		// At this point, either other and table are comparable or not. If they are comparable, no additional nodes need to be defined.
		// Otherwise, they do and a branch needs to be created.
		TransitionTable.Ordering order=ts.table.compareTo(other.table);
		if (order.equals(TransitionTable.Ordering.NotComparable)) { // Make a new node that separates these two with a question.
			// Run through the two transition tables to find a spot where they are not in common. There must be one.
			for (Iterator<TriState> stateIter=ts.table.transition.keySet().iterator(); stateIter.hasNext();) {
				TriState currentState=stateIter.next();
				State otherState=null;
				if (!other.table.transition.keySet().contains(currentState)) {
					otherState=State.Empty;
				} else if (!other.table.transition.get(currentState).equals(ts.table.transition.get(currentState))) {
					otherState=other.table.transition.get(currentState); }
				if (otherState!=null) {
					Node tableNode=new Node(); Node otherNode=new Node();
					tableNode.leaf=ts; tableNode.parent=current;
					otherNode.leaf=other; otherNode.parent=current;
					current.leaf=null;
					current.compareQuestion=currentState;
					current.nodes=new HashMap<State,Node>();
					current.nodes.put(otherState, otherNode);
					current.nodes.put(ts.table.getTransition(currentState), tableNode);
					return;
				} }
			throw new RuntimeException("This should never happen.");
		} else {
			if (order.equals(TransitionTable.Ordering.GreaterThan)) { // table should replace other
				current.leaf=ts;
			} // if the other is at least as important as table, then table should just be ignored
		}
	}
}

package dfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import automaton.Automaton;

public class State implements Comparable<State> {
	private String id;
	// Keys are symbols of alphabet, values are sets of next states
	private HashMap<Character, HashSet<State>> transitions;
	private Set<Integer> name;
	private boolean marked, acceptable;
	
	public State(int id) {
		this.id=id+"";
		transitions=new HashMap<>();
	}
	
	public String getID() {
		return id;
	}
	
	public void addTransition(char symbol, HashSet<State> nextStates) {
		transitions.put(symbol, nextStates);
	}
	
	public void addAllTransitions(Automaton automaton, char symbol, ArrayList<String> id) {
		String string = id.toString().replace("[", "");
		string = string.replace("]", ""); 
		String[] parts=string.split(",");
		HashSet<State> nextStates=new HashSet<>();
		for (String s : parts) {
			if(!s.equals("null"))
				nextStates.add(automaton.getStateByID(s));
		}
		addTransition(symbol, nextStates);
	}
	
	public HashSet<State> getAllTransitions(){
		HashSet<State> temp= new HashSet<>();
		for (HashSet<State> set : transitions.values()) {
			temp.addAll(set);
		}
		return temp;
	}
	
	public void printTransitions() {
		System.out.println("ID: "+id);
		transitions.entrySet().stream()
										.forEach(System.out::println);
		System.out.println(" acceptable = "+acceptable);
	}
	
	public void changeTransitions(Automaton automaton, State newState) {
		// At this point, in dfa states there will be only new state, which contains all transitions of old states, from which new state is created.
		for (char key : transitions.keySet()) {
			HashSet<State> nextStates = move(key);
			if(nextStates==null) {		// Ova linija je dodata zbog konvertovanja NFA u DFA, jer tada postoji mogucnost da nema prelaza u novo stanje
				addTransition(key, new HashSet<>(Arrays.asList(newState)));
			}
				// Method retainAll will deleted all transitions from this state to old states, which are already deleted from states of automaton.
				// Transitions into the states which are still in automaton, will not be deleted.
			else if(nextStates.retainAll(automaton.getAllStates())) {  
				nextStates.add(newState);
				addTransition(key, nextStates);			// Then, into transitions of this state, put transition into newly created state, if necessary. 
			}
		}
	}
	
	public ArrayList<Character> getKeys(){
		return new ArrayList<Character> (transitions.keySet());
	}
	
	public HashSet<State> move(char symbol){
		return transitions.get(symbol);
	}	
	
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(!(o instanceof State))
			return false;
		
		State state=(State) o;
		return this.id.equals(state.id);
	}
	
	public int hashCode() {
		int hash= 17;
		hash = 31*hash + id.hashCode();
		return hash;
	}
	
	public String toString() {
		return id;
	}

	@Override
	public int compareTo(State o) {
		return this.id.compareTo(o.id);
	}
	
	public Set<Integer> getName() {
		return name;
	}

	public void setName(Set<Integer> name) {
		this.name = name;
	}

	public HashMap<Character, HashSet<State>> getTransitions() {
		return transitions;
	}

	public void setTransitions(HashMap<Character, HashSet<State>> transitions) {
		this.transitions = transitions;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}
	
	public boolean isAcceptable() {
		return acceptable;
	}
	
	public void setAcceptable(boolean accept) {
		acceptable=accept;
	}
}

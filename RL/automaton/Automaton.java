package automaton;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;

import dfa.DFA;
import dfa.State;

public abstract class Automaton {
	
	public int ID;
	protected ArrayList<State> states;
	protected State startState;
	protected HashSet<State> finalStates;
	protected ArrayList<Character> alphabet; 
	private String createdFromRegex;
	
	public Automaton() {
		states=new ArrayList<>();
		finalStates=new HashSet<>();
		alphabet=new ArrayList<>();
	}
	
	public ArrayList<State> getAllStates(){
		return states;
	}
	
	public void setAllStates(ArrayList<State> states) {
		this.states.addAll(states);
	}
	
	public State getStartState() {
		return startState;
	}
	
	public void setStartStateByID(String id) {
		startState=getStateByID(id);
	}
	
	public State getStateByID(String id) {
		return states.stream()
								.filter(state -> state.getID().equals(id))
								.findAny().get();
	}
	
	public State getStateByOrder(int i) {
		return states.get(i);
	}
	
	public HashSet<State> getFinalStates() {
		return finalStates; 
	}
	
	public void setFinalStates(HashSet<State> finalStates) {
		this.finalStates=finalStates;
	}
	
	public void setFinalState(String id) {
		//This method use for setting single state as final or not final when double click on jtable cell.
		if(id.contains("->"))
			id=id.split("->")[1];
		State state=getStateByID(id);
		
		if(finalStates.contains(state))
			finalStates.remove(state);
		else
			finalStates.add(state);
	}
	
	public void setAlphabet(ArrayList<Character> alphabet) {
		this.alphabet.addAll(alphabet);
	}
	
	public ArrayList<Character> getAlphabet(){
		return alphabet;
	}
	
	public String getFromRegex() {
		return createdFromRegex;
	}
	
	public void setFromRegex(String s) {
		createdFromRegex=s;
	}
	
	public boolean machineAcceptInput(HashSet<State> states) {
		if(states==null)
			return false;
		// Check is input string is accepted. 
		//Find intersection of states in wich we ended with input string and final states
		System.out.println("We ended up in state(s): "+states);
		states.retainAll(finalStates);
		if(states.isEmpty())
			return false;
		else
			return true;
	}
	
	public HashSet<State> execute(State currentState, String input) {
		//Stack is holding input string, last character is on top of stack
		Deque<Character> stack = new ArrayDeque<>();
		for (Character sign : input.toCharArray()) 
			stack.push(sign);
		// The extended transition function is started with start state and stack which hold input string. 
		// Start state is pass as HashSet temp.
		HashSet<State> temp= new HashSet<>();
		temp.add(currentState);
		HashSet<State> result= deltaFunction(temp, stack);
		return result;
	}
														// EXTENDED TRANSITION FUNCTION
		// Exact same implementation for NFA and DFA, only difference is this: in case of DFA HashSet currentStates will have only one element, and set 
		// which is return by last call of this function wont be empty. The empty set will be return when in NFA there is no transition to next state for 
		// particular symbol of alfabet. 
	public HashSet<State> deltaFunction(HashSet<State> currentStates,Deque<Character> stack){
		// In each recursion pop up one character from stack, until it reaches to bottom, which is first letter in input string.
		Character sign=stack.poll();
		// If (sign==null) is condition for break recursion and that means we are reaches to bottom of stack (stack is empty and input string has been 
		// read to the end. Recursion start to unwind and the executions of algorithm begins. 
		// In this case, if true, function return start state (which is set of state because of my implementation) and will forward to that set the first
		// character in input string.
		if(sign==null)
			return epsilonClosure(currentStates);
		// In all other cases, the extended transition function will return set of states which are returned by the previous function call, and on them 
		// will apply basic function transition with current character of input string.
		return epsilonClosure(deltaFunction(deltaFunction(currentStates, stack), sign));
	}
											// BASIC TRANSITION FUNCTION
	public HashSet<State> deltaFunction(HashSet<State> currentStates, char sign){
		System.out.println("Na skup stanja "+currentStates+" primjenjujem osn. funkciju prelaza za simbol: "+sign);
		// If statement testing return value of previous function call of extended function. If return value is null, then makes no sense to apply basic 
		// delta function for this particular symbol. 
		// In the case of DFA, previous call of delta function will always will return exactly one state, but in the case of NFA return value could be 
		// set or null, because NFA for the same symbol may go to set of multiple states or may go nowhere. 
		if(currentStates==null)
			return null;
			//This for loop get all transitions from this set of states for this sign. In case of NFA, method state.move(sign) may return empty set, which means
			// there is no transition for this sign. 
		HashSet<State> nextStates= new HashSet<>();
		for (State state : currentStates) 
			if(state.move(sign)!=null)
				nextStates.addAll(state.move(sign));
			//In case DFA, nextStates will be always one element and never will be null. 
			//In case NFA, may be one state, multiple states or null (empty set).
		if(nextStates.size()==0)
			return nextStates;
		else
			return epsilonClosure(nextStates);
	}
	
	public void print() {
		states.stream().forEach(State::printTransitions);
	}	
	
	public abstract void minimize();
		
	public abstract DFA convert();
	
	public abstract HashSet<State> epsilonClosure (HashSet<State> set);
	
}

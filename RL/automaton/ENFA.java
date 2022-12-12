package automaton;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import states.State;

public class ENFA extends Automaton {
	public static final char epsilon = 'ε';
	private State deadState=null;
	private DFA dfa;
	private ENFA newAutomaton;
	private HashSet<State> newFinalStates;
	
	@Override
	public void minimize() {
		
	}
	
	private void newTransitionFunction () {
							// 	CONVERTING E-NFA to regular NFA
		// First, make new NFA without epsilon transitions. The new NFA will have same number of states with same names. Things that will be change:
		// alphabet (new nfa will have new alphabet, without epsilon symbol);
		// new transition function (without epsilon transitions);
		// set of final states could change, if epsilon closure of some state contains state from final set.
		newAutomaton=new ENFA();
		newAutomaton.setAlphabet(this.alphabet);
		int indeks = newAutomaton.alphabet.indexOf(epsilon);
		newAutomaton.alphabet.remove(indeks);
		newFinalStates=new HashSet<>();
		newAutomaton.ID=this.ID;
		for (State state : this.states) {
			newAutomaton.states.add(new State(Integer.parseInt(state.getID())));
		}
		newAutomaton.setStartStateByID(this.startState.getID());
								// Start converting...
		HashSet<State> set= new HashSet<>();
		for (State state : this.states) {
			//1. step
			// For each state of automaton, find epsilon closure. Each set is pass to method epsilonClosure as a set of states. 
			set=epsilonClosure(new HashSet<>(Arrays.asList(state)));
			for (State state2 : set) {  // check if new state should be final state
				if(this.finalStates.contains(state2))
					newFinalStates.add(state2);
			}
			secondStep(state, set);
		}	
		for (State state : newFinalStates) {		//making new final set of new automaton
			newAutomaton.setFinalState(state.getID());
		}
		// Copy new NFA to old instance of epsilon NFA
		this.states.clear();
		this.setAllStates(newAutomaton.states);
		this.startState=newAutomaton.startState;
		this.finalStates.clear();
		this.setFinalStates(newAutomaton.finalStates);
		this.alphabet=newAutomaton.alphabet;
				// Conversion is over!
		System.out.println("=====================");
		System.out.println("NFA without ε transitions");
		this.print();
		System.out.println("Final states of new NFA are: "+this.finalStates);
		System.out.println("Alphabet is: "+this.alphabet);
		System.out.println("=====================\n");
	}	
	
	public HashSet<State> epsilonClosure (HashSet<State> currentStates) {
		// This method will find e-closure for parameter currentStates, and returns e-closure as set. 
		// Method is using basic transition function deltaFunction, as a helper function.
		HashSet<State> nextStates = deltaFunction(currentStates, epsilon);
		// If there is no epsilon move from currentStates...
		if(nextStates==null || nextStates.isEmpty() 
				|| currentStates.containsAll(nextStates)) {	// ...or there is no expansion of the set(no new state in currentStates)
			return currentStates;						    // it will return the same set.
		}
		// Else, again we need to find epsilon closure for each state from next states
		else {
			currentStates.addAll(epsilonClosure(nextStates));
			return currentStates;
		}
	}	
	
	private void secondStep(State state, HashSet<State> set){
		State newState=newAutomaton.getStateByID(state.getID());
		//2. step
		// When we find epsilon closure of given set, to the each state from that set we are passing symbols of alphabet and looking for transitions.
		for (char sym : newAutomaton.alphabet) {
			// For each symbol of alphabet, beside epsilon, we should to see where lead us basic transtion function.
			HashSet<State> nextStates= new HashSet<>();
			nextStates=deltaFunction(set, sym);
			//System.out.println("Set of states after second step: "+newSet);
								//3. step
			// Finally, on given set nextStates we again looking for epsilon closure. That will be new transition function for tha symbol. 
			nextStates=epsilonClosure(nextStates);
			// System.out.println("Finally, epsilon closure of state "+state+" for symbol "+sym+" is set: "+newSet);
			// Then, adding new transitions into newly created state.
			HashSet<State> newSet=new HashSet<>();
			for (State state2 : nextStates) 
				newSet.add(newAutomaton.getStateByID(state2.getID()));
			newState.addTransition(sym, newSet);
		}
	}
	
	public DFA convert() {
		if(this.alphabet.contains(epsilon)) {
			newTransitionFunction();
		}

		dfa=new DFA();
		// Start state of NFA and DFA will be the same, as alphabet. 
		dfa.getAllStates().add(startState);
		dfa.setStartStateByID(startState.getID());
		dfa.setAlphabet(alphabet);
		HashMap <String, HashSet<State>> map= new HashMap<>();
		// This deque will hold reached states, it will be create new combined name. For now, states don't have transitions. This deque is use for while loop, 
		// to process only reached states and no one else. 
		Deque<State> markedStates = new ArrayDeque<>();
		// visitedStates is use for avoiding putting of the same state twice into set reachedStates. 
		// On the end, it will hold all states which need to put into new DFA. 
		HashSet<State> visitedStates= new HashSet<>();			
		markedStates.add(startState);	
				// Starting from the start state and put them into deque. Deque will have only array of states, not array of set of states because we always
				// first create new state and then put him into deque. 
		while(!markedStates.isEmpty()) {
			// Poll state from stack and for each state from marked states we need to test his transitions. 
			State currentState= markedStates.pollFirst();
			visitedStates.add(currentState);
			System.out.println("From stack pop up "+currentState.getID()+" and testing his transitions");
			// For each symbol we check transition from current state.
			for (char sym : alphabet) {
				HashSet<State> nextStates = currentState.move(sym);
				// Possible three outcome for currentState.move(): to return null, meaning there is no transition for this symbol and we need to make new 
				// state, called dead state, if dead state is not already created. 
				// After creating dead state, we adding current state transition to dead state. 
				if(nextStates==null || nextStates.size()==0) {
					makeDeadState(visitedStates, markedStates);
					currentState.addTransition(sym, new HashSet<>(Arrays.asList(deadState)));
				}
				// Second case, ir return only one state, we need to process them if he has null transition (if that state has not been already processed).
				else if(nextStates.size()==1) {
					if(!visitedStates.contains(nextStates.stream().findAny().get())) {
						markedStates.addFirst(nextStates.stream().findAny().get());
					}
				}
				// Third case: return set of states, then we need to create new state from that set. For now, transitions remaining as transitions into set of
				// states
				else {
					if (nextStates.contains(deadState))
						nextStates.removeAll(new HashSet<>(Arrays.asList(deadState)));
					// Making new state from the set only if that set haven't been already processed. 
					if(!map.containsValue(nextStates)) {
						State novoStanje= makeNewState(nextStates, visitedStates, markedStates);
						map.put(novoStanje.getID(), nextStates);
						markedStates.addFirst(novoStanje);
					}
				}
			}
		}
		// Add remaining states from the old NFA which has not been changed.
		for (State state : visitedStates) {
			if (!dfa.getAllStates().contains(state)) {
				dfa.getAllStates().add(state);
				if(finalStates.contains(state))
					dfa.getFinalStates().add(state);
			}
		}
		// Connecting old states and new states (which are created from set of states as union). 
		conectNewStates(dfa,map);
		
		System.out.println("New dfa");
		dfa.print();
		dfa.ID=ID;
		return dfa;
	}
	
	private void conectNewStates(DFA dfa, HashMap<String, HashSet<State>> map) {
		for (State state : dfa.getAllStates()) {
			for (char symbol : dfa.getAlphabet()) {
				HashSet<State> set=state.move(symbol);
				if(set.size()==1)					// if move only into one state, should not be changed. 
					continue;
				
				for (Entry<String, HashSet<State>> entry : map.entrySet()) {
			        if (entry.getValue().equals(set)) {
						String id=entry.getKey();
						State newState=dfa.getStateByID(id);
			            state.addTransition(symbol, new HashSet<>(Arrays.asList(newState)));
			            break;
			        }
			    }
			}
		}
	}

	private State makeNewState(HashSet<State> set, HashSet<State> visitedSets, Deque<State> dostignutaStanja) {
		if(set==null)
			return null;
		State newState=new State(ID++);
		dfa.getAllStates().add(newState);
		// If one of states, which will be merged into one state, is final state, then new state also should be add to set of final states.
		for (State state : set)
			if(finalStates.contains(state)) {
				dfa.getFinalStates().add(newState);
				break;
			}
		
		// Mapping transitions of each state should have been merged into new state. 
		for(char symbol : alphabet) {
			HashSet<State> prelazi = deltaFunction(set, symbol);		
			if(prelazi.size()>1)
				prelazi.remove(deadState);
							
			if(prelazi.isEmpty()) {
				makeDeadState(visitedSets, dostignutaStanja);
				prelazi.add(deadState);
			}
			newState.addTransition(symbol, prelazi);
		}
		System.out.println("A new state is created "+newState.getID()+" from this set of states: "+set.toString());
		newState.printTransitions();
		return newState;
	}
	
	private void makeDeadState(HashSet<State> visitedStates, Deque<State> reachedStates) {
		if (deadState!=null)
			return;
		
		// Making dead state. 
		deadState=new State(ID++);
		for (char symbol : alphabet) {
			deadState.addTransition(symbol, new HashSet<>(Arrays.asList(deadState)));
		}
		dfa.getAllStates().add(deadState);
		visitedStates.add(deadState);
		reachedStates.addFirst(deadState);
	}
}

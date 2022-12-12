package states;

import java.util.HashSet;

import automaton.Automaton;

// This class represents set of equivalent states of which we will make single state 
public class StateSet {
	HashSet<State> equivalentStates;
	
	public StateSet() {
		equivalentStates=new HashSet<>();
	}
	
	public StateSet(HashSet<State> states) {
		equivalentStates=states;
	}
	
	public void addPair(StatePair pair) {
		equivalentStates.add(pair.getFirstState());
		equivalentStates.add(pair.getSecondState());
	}
	
	public State makeNewStateFromEquivalent(Automaton automaton){
		if(equivalentStates.isEmpty())
			return null;
		System.out.println("Making new state from these: "+equivalentStates.toString());
		State newState=new State(automaton.ID++);  // renaming new state
		newState.printTransitions();
		automaton.getAllStates().add(newState);		// add new state to automaton
		automaton.getAllStates().removeAll(equivalentStates);		//from automaton states we remove all states that will be merged
		// If one of states which will be merged is start state, then new state also should be mark as start state
		if(equivalentStates.contains(automaton.getStartState()))
			automaton.setStartStateByID(newState.getID());
		// If one of states, which will be merged into one state, is final state, then new state also should be add to set of final states.
		// Then old final state is removed from set of final states. 
		// For loop should not be break after we find first final state, because there could be more final states in set equivalentStates. All old final states
		// should be removed from set of final states.
		boolean add=false;
		for (State state : equivalentStates) {
			if(automaton.getFinalStates().contains(state)) {
				automaton.getFinalStates().remove(state);
				add=true;
			}
		}
		if(add)
			automaton.setFinalState(newState.getID());
		// Now, map all transitions of equivalent state which need to merge into new state
		boolean stop=false;
		for (State state : equivalentStates) {
			if(stop)
				break;
			for (char symbol : automaton.getAlphabet()) {
				HashSet<State> nextStates = state.move(symbol);
				if(nextStates.contains(state)) {								// in case of autotransition 
					HashSet<State> autoTransition=new HashSet<>();
					autoTransition.add(newState);
					newState.addTransition(symbol, autoTransition);
					stop=true;
				}
				else
					newState.addTransition(symbol, nextStates);
			}
		}
		// Now, we need to change and transitions of all other states in automaton and set them to move to new state, if necessary. 
		automaton.getAllStates().forEach(state -> state.changeTransitions(automaton, newState));
		return newState;
//		System.out.println("New DFA, with new states and transitions");
//		dfa.print();
	}
	
	public String toString() {
		return equivalentStates.toString();
	}
}

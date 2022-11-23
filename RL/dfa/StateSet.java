package dfa;

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
		System.out.println("pravim novo stanje od ovih: "+equivalentStates.toString());
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
		//Sad treba mapirati tranzicije svakog stanja koje treba sazeti u novo stanje
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
		//sad treba promijeniti i tranzicije svih ostalih stanja, ako je potrebno
		automaton.getAllStates().forEach(state -> state.changeTransitions(automaton, newState));
		return newState;
//		System.out.println("Nova stanja i prelazi DFA su");
//		dfa.print();
	}
	
	public String toString() {
		return equivalentStates.toString();
	}
}

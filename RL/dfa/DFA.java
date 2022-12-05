package dfa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import automaton.Automaton;

public class DFA extends Automaton {
		
	public void minimize() {
        //First, remove unreachable states
		Deque<State> stack = new ArrayDeque<>();
		HashSet<State> visited = new HashSet<>();
		stack.push(startState);
		
		while(stack.size()>0) {
			State state=stack.pop();
			visited.add(state);
			for (State nextState : state.getAllTransitions()) {
				if(!visited.contains(nextState))
					stack.push(nextState);
			}
		}
		ArrayList<State> minimizedStates= new ArrayList<>(visited);
		Collections.sort(minimizedStates);
		states.retainAll(minimizedStates);
		finalStates.retainAll(states);
		System.out.println("Number of states after removing unreachable states: "+states.size());
		merge();
	}
	
	public void merge() {
		//Second, find equivalent classes and merge states into one states if they belong equivalent class
        HashSet<StatePair> pairs = new HashSet<>();
        HashSet<StatePair> unmarkedPairs = new HashSet<>();
        //First, if pair of states are not equivalent (one state is final, while other is not), put that pair into Set pairs.
        //If that is not the case, put that pair into Set of remainingPairs.
        for(int i=0; i<states.size()-1; i++)
        	for(int j=i+1; j<states.size(); j++) {
        		if(finalStates.contains(states.get(i)) != finalStates.contains(states.get(j))) 
        			pairs.add(new StatePair(states.get(i), states.get(j)));
        		else
        			unmarkedPairs.add(new StatePair(states.get(i), states.get(j)));
        	}
        System.out.println("Marked pairs after first iteration:"+pairs.toString());
        System.out.println("Unmarked pairs after first iteration: "+unmarkedPairs.toString());
        //Testing each pair in set remainingPairs for each symbol of alphabet. If resulting pair of transition function of every state in that pair for same 
        //symbol is in Set pairs, then we put testing pair to set pairs and remove from remainingPairs. While loop will stop when no new pair has been found.        
        while (true)
        {
            HashSet<StatePair> tempPairs=new HashSet<>();
            for (StatePair pair : unmarkedPairs) {
				for (char symbol : alphabet) {
					HashSet<State> transitionsFirstState=pair.getFirstState().move(symbol);
					State first=transitionsFirstState.stream().findAny().get();
					HashSet<State> transitionsSecondState=pair.getSecondState().move(symbol);
					State second=transitionsSecondState.stream().findAny().get();
                    StatePair newPair = new StatePair(first, second);
                    for (StatePair statePair : pairs) {
                    	if(statePair.equals(newPair)) {
    						tempPairs.add(pair);
    					}
					}
				}
			}
            //After each iteration through remainingPairs, if we found new pairs, put them to Set pairs.
            //If there is no new pairs, while loop will stop. 
            pairs.addAll(tempPairs);
            unmarkedPairs.removeAll(tempPairs);
            if(tempPairs.isEmpty())
            	break;
        }
        System.out.println("Marked pairs after end of alghoritm"+pairs.toString());
        System.out.println("Unmarked pairs after end of alghoritm"+unmarkedPairs.toString());
        System.out.println("Unmarked pairs are classes of equivalence.");
        //Now we combined all the remaining pairs and make them as single state in minimized DFA if they are in same class of equivalence.
        // newStates sadrzi listu StateSet-ova, tj. listu skupova u kojima su ekvivalentna stanja. 
        LinkedList<StateSet> newStates=new LinkedList<>();
        while(true) {
        	if(unmarkedPairs.isEmpty()) 
        		break;
        	HashSet<StatePair> equivalentPairs=new HashSet<>();
        	StatePair pair= unmarkedPairs.stream().findAny().get();
        	unmarkedPairs.remove(pair);
        	equivalentPairs.add(pair);
        	for (StatePair other : unmarkedPairs) {
				if(other.containsState(pair.getFirstState()) || other.containsState(pair.getSecondState()))
		        	equivalentPairs.add(other);	
			}
        	unmarkedPairs.removeAll(equivalentPairs);
        	//Make new set of equivalent pairs, if any.
        	if(!equivalentPairs.isEmpty()) {
        		StateSet ss=new StateSet();
        		equivalentPairs.stream().forEach(p -> ss.addPair(p));
        		newStates.add(ss);
        	}
        }
        if(!newStates.isEmpty()) {
        	// Making new state if there is set of equivalent pairs. 
        	System.out.println("From this sets of states (which is sets of equivalent states) we need to make one new state: ");
            newStates.stream().forEach(System.out::println);
            newStates.stream().forEach(s -> s.makeNewStateFromEquivalent(this));
        }
        setShortestWord(findShortestWord());
	}	
	
	public DFA convert() {
		return null;
	}
	
	public HashSet<State> epsilonClosure (HashSet<State> currentStates) {
		// For DFA, there is no epsilon closure, meaning that is only this one, current state
		return currentStates;
	}
	
	public int findShortestWord() {
		// Using BFS traversing the graph of DFA until we end up in final state. 
		int length=0;
		HashSet<State> neighbors = new HashSet<>();
		neighbors.add(startState);
		while(neighbors.size()!=0) {
			if(machineAcceptInput(neighbors))
				return length;
			
			HashSet<State> temp=new HashSet<>();
			for (State state : neighbors) 
				temp.addAll(state.getAllTransitions());
			neighbors.clear();
			neighbors.addAll(temp);
			length++;
		}
		return length;
	}
}

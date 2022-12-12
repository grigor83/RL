package automaton;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import states.State;
import states.StatePair;
import states.StateSet;

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
        isInfinite();
        findShortestWord();
        findLongestWord();
	}	
	
	public DFA convert() {
		return null;
	}
	
	public HashSet<State> epsilonClosure (HashSet<State> currentStates) {
		// For DFA, there is no epsilon closure, meaning that is only this one, current state
		return currentStates;
	}
	
	public void findShortestWord() {
		// Using BFS traversing the graph of DFA until we end up in final state. 
		int length=0;
		HashSet<State> neighbors = new HashSet<>();
		neighbors.add(startState);
		while(neighbors.size()!=0) {
			if(machineAcceptInput(neighbors))
				break;
			
			HashSet<State> temp=new HashSet<>();
			for (State state : neighbors) 
				temp.addAll(state.getAllTransitions());
			neighbors.clear();
			neighbors.addAll(temp);
			length++;
		}
		
		setShortestWord(length);
	}
	
	//The language accepted by a DFA is infinite if and only if there exists some cycle on some path from which a final state is reachable.
	// First, I use some variation of DFS to find if graph of DFA has any cycle. Then, check if final state is reachable from any state in that cycle.
	public void isInfinite() {
		int[][] matrix = createAdjacencyMatrix();
		// Mark all the vertices as not visited and not part of recursion stack
		boolean[] visited = new boolean[states.size()];
		boolean[] stack =  new boolean[states.size()];
		LinkedList<LinkedList<Integer>> cycles=new LinkedList<>();
		// Start search for cycles in the graph from first node, which is start state of automaton
		checkCycle(matrix, visited, stack, 0, cycles);
//		System.out.println("Cycles found:");
//		ciklusi.forEach(System.out::println);
		if (!cycles.isEmpty() && reachFinalStateFromCycle(matrix, cycles)) {
			System.out.println("Language is infinite!");
			setInfiniteLanguage(true);
		}
	}
	
	private int[][] createAdjacencyMatrix() {
		int [][] matrix = new int[states.size()] [states.size()];
		
		for(int i=0;i<states.size();i++) {
			State state=states.get(i);
			HashSet<State> nextStates = state.getAllTransitions();
			if(!nextStates.isEmpty()) {
				for (State state2 : nextStates) {
					int j= states.indexOf(state2);
					matrix[i][j]=1;
				}
			}
		}
		
		return matrix;
	}
	
	private void checkCycle(int[][] matrix, boolean[] visited, boolean[] stack, int node, LinkedList<LinkedList<Integer>> cycles) {
		if(stack[node])
			saveCycle(stack, node, cycles);
		// If node is already visited, it means that he is already processed
		if(visited[node])
			return;
		
		stack[node]=true;
		visited[node]= true;
		// Now, process children of current node and check if there is a cycle. 
		for(int j=0; j<states.size(); j++) 
			if(matrix[node][j]>0)
				checkCycle(matrix, visited, stack, j, cycles);
		// After processing current node and all of his children, remove current node from stack. 
		stack[node]=false;
	}

	private void saveCycle(boolean[] stack, int node, LinkedList<LinkedList<Integer>> cycles) {
		// Save the found cycle from first node in the cycle to the end of that cycle. 
		LinkedList<Integer> list=new LinkedList<>();
		for(int i=node; i<states.size();i++)
			if(stack[i]) 
				list.add(i);
		cycles.add(list);
	}

	private boolean reachFinalStateFromCycle(int[][] matrix, LinkedList<LinkedList<Integer>> cycles) {
		// First, find index of all final states because adjacency matrix.
		HashSet<Integer> indexesOfFinalStates= new HashSet<>();
		for (State state : finalStates)
			indexesOfFinalStates.add(states.indexOf(state));
		//System.out.println("Indexes of final states: "+indexesOfFinalStates);
		// Check if we can reach final states from any state in the cycle. If we can reach, it means language is infinite. 
		for (LinkedList<Integer> list : cycles) 
			for (int i : list) {
				boolean[] visited = new boolean[states.size()];
				if(DFSvisit(indexesOfFinalStates, matrix, visited, i)) 
					return true;
			}
		
		return false;
	}
		// Using DFS we traversed the graph to check if final state(s) are reachable from any state of cycle. 
	private boolean DFSvisit(HashSet<Integer> indexesOfFinalStates, int[][] matrix, boolean[] visited, int i) {
		if(visited[i])
			return false;
		if(indexesOfFinalStates.contains(i))
			return true;
		visited[i]=true;

		boolean result=false;
		for(int j=0; j<states.size();j++)
			if(matrix[i][j]>0 && DFSvisit(indexesOfFinalStates, matrix, visited, j)) {
				result=true;
				break;
			}
		
		return result;
	}
	
	public void findLongestWord() {
		if(isInfiniteLanguage())
			return;
		// Using BFS traversing the graph of DFA until we end up in final state. 
		int length=0, maxLength=0;
		HashSet<State> neighbors = new HashSet<>();
		neighbors.add(startState);
		
		while(neighbors.size()!=0) {
			if(machineAcceptInput(neighbors)) {
				if (length>maxLength)
					maxLength=length;
			}
			
			HashSet<State> temp=new HashSet<>();
			for (State state : neighbors) {
				if (state.getAllTransitions().size()==1 && state.getAllTransitions().contains(state))  
					// This will be the case of dead state. Only the dead state will have autotransitions, because just in case of automata with finite
					// language only state that will have autotransitions is dead state. Otherwise, that means there would be cycles in the graph and
					// language would not be finite. 
					// This is also a condition to break a while loop. 
					continue;
				else
					temp.addAll(state.getAllTransitions());	
			}
			neighbors.clear();
			neighbors.addAll(temp);
			length++;
		}
		
		setLongestWord(maxLength);
	}
}

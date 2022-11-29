package regex_to_dfa;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import automaton.Automaton;
import dfa.DFA;
import dfa.State;

// Direct method is used to convert given regular expression directly into DFA. Uses augmented regular expression r#.
// Regular expression is represented as syntax tree where interior nodes correspond to operators representing union, concatenation and closure operations.
// Leaf nodes corresponds to the input symbols.
// Construct DFA directly from a regular expression by computing the functions nullable(n), firstpos(n), lastpos(n) andfollowpos(i) from the syntax tree.

public class RegexToDFA {
	public static final ArrayList<Character> operators = new ArrayList<>(Arrays.asList('*', '|', '.', '(', ')'));
    public static Set<Character> alphabet = new HashSet<Character>();
	//private static String regex = "(a|b)*abb";   //      (a|b)*abb    aba(a|b)*b*a    (a|b)*|(ac)*
    private static HashMap<Integer, String> positionOfSymbolInRegex = new HashMap<>();
	private static ArrayList<State> states = new ArrayList<>();
	private static State startState;
	private static ArrayList<Set<Integer>> followPos;
	
	public static Automaton convert(Automaton automaton, String regex) {
		alphabet = regex.chars().mapToObj(symbol->(char)symbol).collect(Collectors.toSet());
	    alphabet.removeAll(operators);
	    alphabet.add('#');
		SyntaxTree st = new SyntaxTree(regex);   			// This will create binary tree and then syntax tree
		followPos=st.getFollowPos();
		System.out.println("Broj cvorova listova u sintaksnom stablu je "+followPos.size());
		System.out.println("tablic followpos za svaki cvor:"); 
		int i=0;
		for (Set<Integer> set : followPos) {
			System.out.println(i+": "+set);
			i++;
		}
		getPositions(st);
		startState = createDFA(st.getRoot());	
		System.out.println("tablica prelaza novog DFA");
		states.stream().forEach(State::printTransitions);
		automaton = createDFA(automaton, regex);
		return automaton;
	}

	private static void getPositions(SyntaxTree st) {
		// Getting position of each symbol in input regex.
		st.postOrder(st.getRoot(), positionOfSymbolInRegex);
//		System.out.println("pozicije simbola ");
//		positionOfSymbolInRegex.entrySet().stream().forEach(System.out::println);
	}

	private static State createDFA(Node root) {
			// Initialize states to contain only the unmarked state q0, where q0 is the root of syntax tree st for (r)#;
		int id=0;
		State q0 = new State(id++);  							// start state of dfa will be firstpos of root node of syntax tree
		q0.setName(root.getFirstPos());
		isFinalState(q0);
		states.add(q0);  										// q0 is unmarked
		// while(there is an unmarked state s in states)
		while(true) {
			State state=null;
			for (State s : states) 
				if(!s.isMarked()) {
					state=s;
					break;
				}
			if(state==null)
				break;		// break while loop
			
			System.out.println("Uzimam stanje "+state.getName()+" i testiram ga");
			state.setMarked(true);		// mark s
			for (char symbol : alphabet) {			// for each symbol in alphabet
				Set<Integer> U = new HashSet<>();	
				for (int p : state.getName()) {
					if(positionOfSymbolInRegex.get(p).equals(symbol+"")) // if p(leaf node id) in state name correspond to symbol,
						U.addAll(followPos.get(p));						//	let U be the union of followpos(p) for all 
				}
				
				if(!U.isEmpty()) {
					if (checkIfContains(U)==null){
						System.out.println("kreiram novo stanje za "+U+" u koje prelazim za simbol "+symbol+" i dodajem ga u states");
						State q = new State(id++);
		                q.setName(U);
		                isFinalState(q);
		                states.add(q);
			            state.getTransitions().put(symbol,new HashSet<State>(Arrays.asList(q)));
					}
					else {
						System.out.println("Tranzicija u stanje "+U+" koje je vec u states,moze biti i autotranzicija, za simbol "+symbol);
						state.getTransitions().put(symbol, new HashSet<State>(Arrays.asList(checkIfContains(U))));
					}
				}
			}
			System.out.println("=================");
		}
		
		return q0;
	}

	private static State checkIfContains(Set<Integer> U) {
		for (State state : states) {
			if(state.getName().equals(U))
				return state;
		}
		return null;
	}
	
	private static void isFinalState(State state) {
		if(state.getName().contains(followPos.size()-1))
			state.setAcceptable(true);
	}
	
	private static Automaton createDFA(Automaton automaton, String regex) {
		automaton=new DFA();
		automaton.setAllStates(states);
		automaton.ID=states.size();
		automaton.setStartStateByID(startState.getID());
		automaton.getAlphabet().addAll(alphabet);
		int indeks = automaton.getAlphabet().indexOf('#');
		automaton.getAlphabet().remove(indeks);
		for (State state : automaton.getAllStates()) 
			if(state.isAcceptable())
				automaton.getFinalStates().add(state);
		automaton=makeCompleteDFA(automaton);
		automaton.setFromRegex("\n DFA was generated from regex: "+ regex);
		return automaton;
	}
	
	private static Automaton makeCompleteDFA(Automaton automaton) {
		// Because dfa created through direct method is incomplete, we have to create dead state. 
		State deadState = new State(automaton.ID++);
		automaton.getAllStates().add(deadState);
		
		for (char symbol : automaton.getAlphabet()) {
			for (State state : automaton.getAllStates()) {
				if (state.move(symbol)==null)
					state.addTransition(symbol, new HashSet<State>(Arrays.asList(deadState)));
			}
		}
		return automaton;
	}
}

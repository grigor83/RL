package enfa;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import automaton.Automaton;
import dfa.DFA;
import dfa.State;

public class ENFA extends Automaton {
	
	State deadState=null;
	DFA dfa;

	@Override
	public void minimize() {
		
	}
	
	public DFA convert() {
		dfa=new DFA();
		// pocetno stanje nfa i dfa ce biti isto, kao i alfabet
		dfa.getAllStates().add(startState);
		dfa.setStartStateByID(startState.getID());
		dfa.setAlphabet(alphabet);
		HashMap <String, HashSet<State>> map= new HashMap<>();
		// U ovom redu ce se nalaziti dostignuta stanja, bice kreirano novo kombinovano ime. Za sada jos nemaju prelaze. Sluzi nam za while petlju, da bismo
		// obradili samo sva dostizna stanja i ni jedna druga. 
		Deque<State> dostignutaStanja = new ArrayDeque<>();
		HashSet<State> visitedStates= new HashSet<>();					// za kontrolu, da izbjegnemo dvostruko dodavanje istog stanja u skup dostiznih stanja.
		dostignutaStanja.add(startState);								// Na kraju ce se u visited nalaziti sva stanja koja treba dodati u novi dfa
				// Krecemo od pocetnog stanja i dodajemo ga u red. Za svako stanje koje se nalazi na steku, treba provjeriti tranziciju za svaki simbol.
				// u redu ce se uvijek nalaziti niz stanja, a ne niz skupova stanja, jer cemo skup stanja uvijek sazimati u novo stanje.
		while(!dostignutaStanja.isEmpty()) {
			State currentState= dostignutaStanja.pollFirst();
			visitedStates.add(currentState);
			System.out.println("Sa steka uzimam "+currentState.getID()+" i testiram njegove prelaze");
			// Provjeravamo tranziciju za svaki simbol iz trenutnog stanja.
			for (char sym : alphabet) {
				HashSet<State> nextStates = currentState.move(sym);
				// Moze se desiti tri slucaja: da vrati null, sto znaci da nema tranzicije u iduce stanje za ovaj simbol. Trebamo napraviti novo stanje,
				// tj. dead state. Ono treba da bude samo jedno, i kad ga kreiramo, trenutnom stanju cemo dodati tranziciju u njega. 
				if(nextStates==null || nextStates.size()==0) {
					makeDeadState(visitedStates, dostignutaStanja);
					System.out.println("Stavljam novo stanje "+deadState+" iz nextstates==null. To je tranzicija iz "+currentState);
					currentState.addTransition(sym, new HashSet<>(Arrays.asList(deadState)));
				}
				// Drugi slucaj je da vrati samo jedno stanje, moramo ga obraditi u slucaju da je njegov prelaz u null, ako vec nije obradjen
				else if(nextStates.size()==1) {
					if(!visitedStates.contains(nextStates.stream().findAny().get())) {
						//visitedStates.add(nextStates.stream().findAny().get());
						System.out.println("Stavljam novo stanje "+nextStates.stream().findAny().get()+" iz nextstates==1");
						dostignutaStanja.addFirst(nextStates.stream().findAny().get());
					}
				}
				// Treci slucaj je da vrati skup stanja, tad moramo kreirati novo stanje od tog skupa. Za sada ce tranzicije ostati prelazi u skup stanja
				else {
					if (nextStates.contains(deadState))
						nextStates.removeAll(new HashSet<>(Arrays.asList(deadState)));
					// Pravimo novo stanje od skupa koji je vracen samo u slucaju da taj skup vec nismo dohvatili
					if(!map.containsValue(nextStates)) {
						State novoStanje= napraviNovoStanje(nextStates, visitedStates, dostignutaStanja);
						map.put(novoStanje.getID(), nextStates);
						dostignutaStanja.addFirst(novoStanje);
						System.out.println("Stavljam novo stanje "+novoStanje+" iz nextstates==vise");
					}
				}
			}
		}
		// Dodavanje preostalih stanja iz starog NFA koja nisu promijenjena
		for (State state : visitedStates) {
			if (!dfa.getAllStates().contains(state)) {
				dfa.getAllStates().add(state);
				if(finalStates.contains(state))
					dfa.getFinalStates().add(state);
			}
		}
		// Nove tranzicije u nova stanja koja su nastala kao unije
		mapirajUnije(dfa,map);
		
		System.out.println("Novi dfa");
		dfa.print();
		dfa.ID=ID;
		return dfa;
	}
	
	private void mapirajUnije(DFA dfa, HashMap<String, HashSet<State>> map) {
		for (State state : dfa.getAllStates()) {
			for (char symbol : dfa.getAlphabet()) {
				HashSet<State> set=state.move(symbol);
				if(set.size()==1)					// ako prelazi samo u jedno stanje,ne treba mijenjati
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

	private State napraviNovoStanje(HashSet<State> set, HashSet<State> visitedSets, Deque<State> dostignutaStanja) {
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
		
		//Sad treba mapirati tranzicije svakog stanja koje treba sazeti u novo stanje		
		for(char symbol : alphabet) {
			HashSet<State> prelazi = new HashSet<>();
			for (State state : set) 
				if(state.move(symbol)!=null) {
					prelazi.addAll(state.move(symbol));
					if(prelazi.size()>1)
						prelazi.remove(deadState);
				}
					
				if(prelazi.isEmpty()) {
					makeDeadState(visitedSets, dostignutaStanja);
					prelazi.add(deadState);
				}
			newState.addTransition(symbol, prelazi);
		}
		System.out.println("Napravio sam novo stanje "+newState.getID()+" od ovih: "+set.toString());
		newState.printTransitions();
		return newState;
	}
	
	
	private void makeDeadState(HashSet<State> visitedStates, Deque<State> dostignutaStanja) {
		if (deadState!=null)
			return;
		
		// Making dead state. 
		deadState=new State(ID++);
		for (char symbol : alphabet) {
			deadState.addTransition(symbol, new HashSet<>(Arrays.asList(deadState)));
		}
		dfa.getAllStates().add(deadState);
		visitedStates.add(deadState);
		dostignutaStanja.addFirst(deadState);
	}
}

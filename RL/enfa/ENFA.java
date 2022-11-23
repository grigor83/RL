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
	public static final char epsilon = 'ε';
	private State deadState=null;
	private DFA dfa;
	private ENFA novi;
	private HashSet<State> newFinalStates;
	
	@Override
	public void minimize() {
		
	}
	
	private void epsilonClosure () {
		// Bilo bi dobro da napravimo novi nfa automat. Novi automat nece imati nova stanja, nego samo nove prelaze, bez epsilon. Alfabet takodje nece biti isti,
		// nece imati epsilon. Moze doci jedino do promjene u broju finalnih stanja
		novi=new ENFA();
		novi.setAlphabet(this.alphabet);
		int indeks = novi.alphabet.indexOf(epsilon);
		novi.alphabet.remove(indeks);
		newFinalStates=new HashSet<>();
		novi.ID=this.ID;
		for (State state : this.states) {
			novi.states.add(new State(Integer.parseInt(state.getID())));
		}
		novi.setStartStateByID(this.startState.getID());
		//1. korak
		// Za svako stanje pronadji epsilon zatvorenje. Svako stanje se prosljedjuje kao skup preklopljenom metodu epsilonClosure
		HashSet<State> set= new HashSet<>();
		for (State state : this.states) {
			set=epsilonClosure(new HashSet<>(Arrays.asList(state)));
			//System.out.println("Za epsilon iz stanja "+state+" smo zavrsili u skupu: "+ set);
			// Ovdje dodaj provjeru za finalna stanja
			for (State state2 : set) {
				if(this.finalStates.contains(state2))
					newFinalStates.add(state2);
			}
			secondStep(state, set);
		}	
		for (State state : newFinalStates) {
			novi.setFinalState(state.getID());
		}
//		System.out.println("=====================");
//		System.out.println("enfa nakon konverzije u nfa");
//		novi.print();
		
		this.states.clear();
		this.setAllStates(novi.states);
		this.startState=novi.startState;
		this.finalStates.clear();
		this.setFinalStates(novi.finalStates);
		this.alphabet=novi.alphabet;
		System.out.println("=====================");
		System.out.println("drugi ispis");
		this.print();
		System.out.println("finalna stanja su "+this.finalStates);
		System.out.println("Alfabet je "+this.alphabet);
		System.out.println("=====================");
	}	
	
	private HashSet<State> epsilonClosure (HashSet<State> currentStates) {
		// Primijeni osnovnu funkciju prelaza deltaFunction za epsilon, ona uzima skup stanja i za svako stanje gleda kud ce otici sa epsilon
		HashSet<State> nextStates = deltaFunction(currentStates, epsilon);
		// Ako nema epsilon prelaza, vratice proslijedjeni skup, tj. u njemu ce biti samo jedno stanje
		if(nextStates==null || nextStates.isEmpty() 
				|| currentStates.containsAll(nextStates)) {	// ili nema prosirenja skupa
			return currentStates;
		}
		// U suprotnom, opet treba za svako stanje iz skupa nextStates pronaci epsilon closure od njega
		else {
			currentStates.addAll(epsilonClosure(nextStates));
			return currentStates;
		}
	}	
	
	private void secondStep(State state, HashSet<State> set){
		State newState=novi.getStateByID(state.getID());
		//2. korak
		//Kad smo pronasli epsilon zatvorenje datog stanja, svakom stanju iz tog skupa treba da proslijedimo simbole alfabeta i da vidimo 
		//kuda ce otici sa njima.
		for (char sym : novi.alphabet) {
			//Za svaki simbol alfabeta, osim epsilon, treba da vidimo kuda nas vodi osnovna funkcija prelaza
			HashSet<State> newSet= new HashSet<>();
			newSet=deltaFunction(set, sym);
			//System.out.println("Skup stanja nakon drugog koraka: "+newSet);
			//3. korak
			//Na dobijeni skup, opet treba primijeniti epsilon zatvorenje. To ce biti nova funkcija prelaza za taj simbol.
			newSet=epsilonClosure(newSet);
			//System.out.println("Konacno, epsilon closure stanja "+state+" za simbol "+sym+" je skup: "+newSet);
			// U novo stanje novog automata dodajem nove tranzicije
			HashSet<State> newSet1=new HashSet<>();
			for (State state2 : newSet) {
				newSet1.add(novi.getStateByID(state2.getID()));
			}
			newState.addTransition(sym, newSet1);
		}
	}
	
	public DFA convert() {
		if(this.alphabet.contains(epsilon)) {
			epsilonClosure();
		}
		// Za sada samo konvertuje nfa u dfa
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
					currentState.addTransition(sym, new HashSet<>(Arrays.asList(deadState)));
				}
				// Drugi slucaj je da vrati samo jedno stanje, moramo ga obraditi u slucaju da je njegov prelaz u null, ako vec nije obradjen
				else if(nextStates.size()==1) {
					if(!visitedStates.contains(nextStates.stream().findAny().get())) {
						dostignutaStanja.addFirst(nextStates.stream().findAny().get());
					}
				}
				// Treci slucaj je da vrati skup stanja, tad moramo kreirati novo stanje od tog skupa. Za sada ce tranzicije ostati prelazi u skup stanja
				else {
					if (nextStates.contains(deadState))
						nextStates.removeAll(new HashSet<>(Arrays.asList(deadState)));
					// Pravimo novo stanje od skupa koji je vracen samo u slucaju da taj skup vec nismo dohvatili
					if(!map.containsValue(nextStates)) {
						State novoStanje= makeNewState(nextStates, visitedStates, dostignutaStanja);
						map.put(novoStanje.getID(), nextStates);
						dostignutaStanja.addFirst(novoStanje);
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
		conectNewStates(dfa,map);
		
		System.out.println("Novi dfa");
		dfa.print();
		dfa.ID=ID;
		return dfa;
	}
	
	private void conectNewStates(DFA dfa, HashMap<String, HashSet<State>> map) {
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
		
		//Sad treba mapirati tranzicije svakog stanja koje treba sazeti u novo stanje		
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

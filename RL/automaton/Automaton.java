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
	
	public boolean machineAcceptInput(HashSet<State> states) {
		if(states==null)
			return false;
		// Check is input string is accepted. 
		//Find intersection of states in wich we ended with input string and final states
		System.out.println("zavrsili smo u stanju "+states);
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
		//Prosirena funkcija prelaza se startuje sa inicijalnim stanjem i stekom na kome se nalazi cijeli ulazni string.
		//Startno stanje se prosljedjuje kao skup HashSet
		HashSet<State> temp= new HashSet<>();
		temp.add(currentState);
		HashSet<State> result= deltaFunction(temp, stack);
		return result;
	}
	
														// PROSIRENA FUNKCIJA PRELAZA 
		// Potpuno ista implementacija i za NFA i DFA, jedina razlika je sto ce u slucaju DFA skup currentStates imati samo jedan element i sto skup
		// vracen prethodnim pozivom funkcije nece biti prazan. Prazan skup ce biti vracen samo kod NFA, kad za odredjeni simbol ne postoji prelaz u novo stanje.
	public HashSet<State> deltaFunction(HashSet<State> currentStates,Deque<Character> stack){
			//U svakoj rekurziji se skida karakter sa steka, sve dok se ne dodje do dna steka, tj. do prvog slova u stringu.
		Character sign=stack.poll();
			//If naredba je uslov za prekid rekurzije i to znaci da smo dosli do dna steka(stek je prazan i string je procitan do kraja).
			//Rekurzija se odmotava i pocinje izvrsavanje algoritma. 
			//U tom slucaju, vratice pocetno stanje(tj. skup stanja jer sam tako implementirao) i proslijedice mu prvi znak u ulaznom stringu. 
		if(sign==null)
			return epsilonClosure(currentStates);
			//U svim ostalim slucajevima, prosirena funkcija prelaza ce vratiti skup stanja koja su dobijena prethodnim pozivom rekurzije i na njih ce primijeniti
			//osnovnu funkciju sa trenutnim znakom inputa. 
		//currentStates=epsilonClosure(currentStates);
		return epsilonClosure(deltaFunction(deltaFunction(currentStates, stack), sign));
	}
											// OSNOVNA FUNKCIJA PRELAZA
		// IF simulira situaciju kad je prethodni poziv prosirene funkcije vratio prazan skup i tada nema smisla primjenjivati osnovnu funkciju prelaza za 
		// proslijedjeni simbol. Varijabla currentStates predstavlja skup stanja u koja smo stigli prethodnim pozivima prosirene finkcije.
		// U slucaju DFA, to ce uvijek biti samo jedan element, dok u slucaju NFA to moze biti i skup stanja ili null, jer NFA za isti simbol moze preci u 
		// vise stanja, a ne mora preci ni u jedno. 
	public HashSet<State> deltaFunction(HashSet<State> currentStates, char sign){
		System.out.println("Na skup stanja "+currentStates+" primjenjujem osn. funkciju prelaza za simbol: "+sign);
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
//		return nextStates;
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

package states;

//This class is use for finding and merging equivalent states of DFA
public class StatePair {
	
	private State firstState, secondState;
	
	public StatePair(State first, State second) {
		firstState=first;
		secondState=second;
	}

	public State getFirstState() {
		return firstState;
	}

	public State getSecondState() {
		return secondState;
	}
	
	public boolean equals(StatePair o) {
		if(this == o)
			return true;
		if(!(o instanceof StatePair)) {
			return false;
		}
		
		StatePair otherPair=(StatePair) o;
		if((firstState==otherPair.firstState && secondState==otherPair.secondState) ||
			(firstState==otherPair.secondState && secondState==otherPair.firstState))
			return true;
		else
			return false;
	}
	
	public int hashCode() {
		int value= 17;
		value =31*value + firstState.hashCode();
		value = 31*value +secondState.hashCode();
		return value;
	}
	
	public boolean containsState(State state) {
		if(firstState==state || secondState==state)
			return true;
		else
			return false;
	}

	public String toString() {
		return "("+firstState.getID()+","+secondState.getID()+")";
	}
}

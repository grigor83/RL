package regex_to_dfa;


import java.util.HashSet;
import java.util.Set;

public class LeafNode extends Node {
		//	All the leaf nodes are labeled with integers from 1 to n (n is number of characters in input string), which would be used as the
		//	information to construct the DFA later.
		// 	After creation, firstPos and lastPos of leaf node are set with his id, and nullable set to false in the parent constructor
	private int id;
	private Set<Integer> followPos;

	public LeafNode(String symbol, int num) {
		super(symbol);
	    id = num;
	    followPos = new HashSet<>();
	    
	    addToFirstPos(num);
	    addToLastPos(num);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Set<Integer> getFollowPos() {
		return followPos;
	}

	public void addToFollowPos(int number){
        followPos.add(number);
    }
	
	public void addAllToFollowPos(Set<Integer> followPos) {
		this.followPos = followPos;
	}
}

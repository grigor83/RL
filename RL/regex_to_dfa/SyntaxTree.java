package regex_to_dfa;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SyntaxTree {
	private BinaryTree bt;
	private Node root;										// root of syntax tree
	private ArrayList<Set<Integer>> followPos;				// this list contains followPos of each leaf node in syntax tree
	
	public SyntaxTree(String regex) {
        // First, generates the binary tree
		bt = new BinaryTree(regex);
        root = bt.createBinaryTree();
        // Then, creating syntax tree from this binary tree by computing nullable, firstpos, lastpos and followpos. 
        calculateNullable(root);
        calculateFirstposAndLastpos(root);
        followPos= new ArrayList<>();
        for(int i=0; i<bt.getNumberOfLeafNodes(); i++)
        	followPos.add(new HashSet<>());
        calculateFollowposition(root);
    }

	private void calculateNullable(Node node) {
		// This method evaluate nullable status for each node. Every node who may generate empty word (like star node) will have nullable = true. 
		// Leaf node, witch represent character of input string, will have nullable = false. 
		// Others interior nodes, like cat node and union node, will evaluate their nullable using this rules:
		// - for cat node: 	 nullable = nullable(left child) AND nullable(right child);
		// - for union node: nullable = nullable(left child) OR  nullable(right child);
		if (node == null)
			return;
		
        if (!(node instanceof LeafNode)) {
            Node left = node.getLeft();
            Node right = node.getRight();
            calculateNullable(left);
            calculateNullable(right);
            switch (node.getSymbol()) {
                case "|":
                    node.setNullable(left.isNullable() || right.isNullable());
                    break;
                case ".":
                    node.setNullable(left.isNullable() && right.isNullable());
                    break;
                case "*":
                    node.setNullable(true);
                    break;
            }
        }
    }
	
	private void calculateFirstposAndLastpos(Node node) {
		// This method evaluate firstpos and lastpos status for each node, except for leaf node. Leaf node has firstpos and lastpos same as his id and that 
		// was set during creation of leaf node. 
		// Others interior nodes, like star node, cat node and union node, will evaluate their firstpos and lastpos using this rules:
		// - for star node:  firstpos = firstpos(left child)            lastpos = lastpos(left child);
		// - for cat node: 	 firstpos = if (nullable(left child))  then  firstpos(left child) UNION firstpos(right child)
		//								else							 firstpos(left child)
		//					 lastpos = if (nullable(right child))  then  lastpos(left child) UNION lastpos(right child)
		//								else							 lastpos(right child)
		// - for union node: firstpos = firstpos(left child)  UNION firstpos(right child) 
		//					 lastpos  = lastpos(left child)  UNION lastpos(right child) 
		if (node == null)
			return;
		
		Node left = node.getLeft();
        Node right = node.getRight();
        // First, we need to calculate firstpos and lastpos of children nodes, if we wont to calculate firstpos and lastpos of his parent.
        calculateFirstposAndLastpos(left);
        calculateFirstposAndLastpos(right);
        switch (node.getSymbol()) {
            case "|":
                node.addAllToFirstPos(left.getFirstPos());
                node.addAllToFirstPos(right.getFirstPos());
                node.addAllToLastPos(left.getLastPos());
                node.addAllToLastPos(right.getLastPos());
                break;
            case ".":
                if (left.isNullable()) {
                    node.addAllToFirstPos(left.getFirstPos());
                    node.addAllToFirstPos(right.getFirstPos());
                } else
                    node.addAllToFirstPos(left.getFirstPos());
                //
                if (right.isNullable()) {
                    node.addAllToLastPos(left.getLastPos());
                    node.addAllToLastPos(right.getLastPos());
                } else 
                    node.addAllToLastPos(right.getLastPos());
                break;
            case "*":
                node.addAllToFirstPos(left.getFirstPos());
                node.addAllToLastPos(left.getLastPos());
                break;
        }
	}	
	
	private void calculateFollowposition(Node temp) {
		// If firstpos and lastpos has been computed for each node, then followpos of each leaf node can be computed by making one depth-first traversal of the
		// syntax tree. For finding followpos, only star and concat nodes will be considered.
		// For star node: for each leaf node(i) in lastpos(star node) 
		//						followpos(i) = followpos(i) UNION firstpos(star node)
		// For cat node:  for each leaf node(i) in lastpos(left child of cat node) 
		//						followpos(i) = followpos(i) UNION firstpos(right child of cat node)
		if (temp == null)
            return;
            
        Node left = temp.getLeft();
        Node right = temp.getRight();
        switch (temp.getSymbol()) {
        	case "*":	Set<Integer> firstpos_Starnode = temp.getFirstPos();        				
        				Set<Integer> lastPos_Starnode = temp.getLastPos();
        				for (int leafNode : lastPos_Starnode)
        					followPos.get(leafNode).addAll(firstpos_Starnode);
        				break;
            case ".":	Set<Integer> firstpos_Rightchild = right.getFirstPos();
						Set<Integer> lastpos_Leftchild = left.getLastPos();
						for (int leafNode : lastpos_Leftchild) {
        					followPos.get(leafNode).addAll(firstpos_Rightchild);
						}
            			break;
        }
        calculateFollowposition(temp.getLeft());
        calculateFollowposition(temp.getRight());
	}	
	
	public Node getRoot() {
		return root;
	}
	
	public BinaryTree getBinaryTree() {
		return bt;
	}
	
	public ArrayList<Set<Integer>> getFollowPos(){
		return followPos;
	}
	
	public void postOrder(Node node, HashMap<Integer, String> positionOfSymbolInRegex) {
		if (node == null)
            return;
        postOrder(node.getLeft(),positionOfSymbolInRegex);
        postOrder(node.getRight(),positionOfSymbolInRegex);
        if(node instanceof LeafNode) {
			LeafNode leaf = (LeafNode) node;
			positionOfSymbolInRegex.put(leaf.getId(), leaf.getSymbol());
		}
	}
}

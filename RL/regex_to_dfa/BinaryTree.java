package regex_to_dfa;
import java.util.HashMap;
import java.util.Stack;

// I am using method of converting infix expression to postfix for creating binary tree. Leaf nodes will be chars of input string and interior nodes will
// be operators (*, | or .)
public class BinaryTree {
	private int numberOfLeafNodes;
	private String augmentedRegex;
	private String postfixRegex="";
    private Stack<Node> stackNode= new Stack<>();
	
	public BinaryTree (String regex) {
	    // Add symbol . as concatenation symbol to input regular expression
        concatRegex(regex);
	}

	public Node createBinaryTree () {
		Stack<Character> operatorsStack = new Stack<Character>();
		for (Character character : augmentedRegex.toCharArray()) {
			if(RegexToDFA.alphabet.contains(character)) {  // If character in input string is symbol of alphabet, create leaf node of binary tree. 
				postfixRegex+=character;
				createLeafNode(character+"");
			}
			else {										// Else, then character is operator or some kind of parenthesis
				// Empty the stack to the bottom, or while op on the stack has bigger priority from new op
				while(!operatorsStack.isEmpty() && checkPriority(character, operatorsStack.peek())) {  
					char operator = operatorsStack.pop();
					postfixRegex+=operator;							
					createInteriorNode(operator);
				}
				
				if(operatorsStack.isEmpty() || character != ')')  // Put new operator on stack only if not ')'
					operatorsStack.push(character);
				else
					operatorsStack.pop();    							// Pop the '(' left parenthesis
			}				
		}
		// emptying the stack with operators
		while(!operatorsStack.isEmpty()) {
			char operator = operatorsStack.pop();
			postfixRegex+=operator;		
			createInteriorNode(operator);
		}
		System.out.println("Postfix expresion: "+postfixRegex);
		return getRoot();
	}
	
	// Create leaf node from input symbol and push node into stackNode
    private void createLeafNode (String symbol) {
        Node node = new LeafNode(symbol, numberOfLeafNodes++);
        // Put node to stackNode
        stackNode.push(node);
    }
	
	private void createInteriorNode(char operator) {
		System.out.println("Making interior node for operator: "+operator);
		 switch (operator) {
         	case ('|'):	union();
 						break;
         	case ('.'):	concatenation();
         				break;
         	case ('*'):	star();
         				break;
         	default:	System.out.println(">>" + operator);
         				System.out.println("Unkown Symbol !");
         				System.exit(1);
         				break;
		 }
	}	
	
	// Do the star operation
    private void star() {
        // Retrieve top Node from Stack
        Node node = stackNode.pop();
        // Create star node
        Node starNode = new Node("*");
        starNode.setLeft(node);
        node.setParent(starNode);
        // Put star node in the stackNode
        stackNode.push(starNode);
    }

    // Do the concatenation operation
    private void concatenation() {
        // retrieve node 1 and 2 from stackNode
        Node node2 = stackNode.pop();
        Node node1 = stackNode.pop();

        Node catNode = new Node(".");
        catNode.setLeft(node1);
        catNode.setRight(node2);
        node1.setParent(catNode);
        node2.setParent(catNode);
        // Put concatenation node to stackNode
        stackNode.push(catNode);
    }

    // Makes union of sub Node 1 with sub Node 2
    private void union() {
        // retrieve node 1 and 2 from stackNode
        Node node2 = stackNode.pop();
        Node node1 = stackNode.pop();

        Node unionNode = new Node("|");
        unionNode.setLeft(node1);
        unionNode.setRight(node2);
        node1.setParent(unionNode);
        node2.setParent(unionNode);
        // Put union node to stack
        stackNode.push(unionNode);
    }
	
	private boolean checkPriority (char inputSymbol, char symbolOnStack) {
		HashMap<Character, Integer> inputPriority = new HashMap<>();
		inputPriority.put('|', 2);
		inputPriority.put('.', 3);
		inputPriority.put('*', 5);
		inputPriority.put('(', 6);
		inputPriority.put(')', 1);
		HashMap<Character, Integer> stackPriority = new HashMap<>();
		stackPriority.put('|', 2);
		stackPriority.put('.', 3);
		stackPriority.put('*', 4);
		stackPriority.put('(', 0);
		stackPriority.put(')', -1);
		// Input operator should pop up from stack all operators which have stack priority greater or equal to his. 
		if(inputPriority.get(inputSymbol) <= stackPriority.get(symbolOnStack))
			return true;
		else
			return false;
	}

	private void concatRegex(String regex) {
		augmentedRegex = "";

        for (int i = 0; i < regex.length() - 1; i++) {
        	if(RegexToDFA.alphabet.contains(regex.charAt(i)) && RegexToDFA.alphabet.contains(regex.charAt(i+1))) {  // in the case of ab, then put . between 
        		augmentedRegex+=regex.charAt(i)+".";
        	}
        	else if (RegexToDFA.alphabet.contains(regex.charAt(i)) && regex.charAt(i+1)=='(') {  // in the case of a(, then change to a.(
        		augmentedRegex+=regex.charAt(i)+".";
        	}
        	else if (regex.charAt(i)==')' && RegexToDFA.alphabet.contains(regex.charAt(i+1))) {   // in the case of )a, then change to ).a
        		augmentedRegex+=regex.charAt(i)+".";
        	}        	
        	else if (regex.charAt(i)=='*' && RegexToDFA.alphabet.contains(regex.charAt(i+1))) {  // in the case of *a, then change to *.a
        		augmentedRegex+=regex.charAt(i)+".";
        	}
        	else if (regex.charAt(i)=='*' && regex.charAt(i+1)=='('){				// in the case of *(, then change to *.(
        		augmentedRegex+=regex.charAt(i)+".";
        	}
        	else if (regex.charAt(i) == ')' && regex.charAt(i + 1) == '(') {		// in the case of )(, then change to ).(
        		augmentedRegex+=regex.charAt(i)+".";
        	}
        	else
        		augmentedRegex+=regex.charAt(i);

        }
        augmentedRegex += regex.charAt(regex.length() - 1);   // add last symbol in input string
        augmentedRegex= '('+augmentedRegex+").#";
		System.out.println("Augmented regex are: "+ augmentedRegex);
	}
	
	public Node getRoot () {
		// Get the root node of binary tree
        return stackNode.peek();
	}
	
	public int getNumberOfLeafNodes() {
		return numberOfLeafNodes;
	}
	
	public void printInorder(Node node) {
        if (node == null)
            return;
        printInorder(node.getLeft());
        System.out.print(node);
        printInorder(node.getRight());
    }
	
	public void printPreorder(Node node) {
        if (node == null)
            return;
        System.out.print(node.getSymbol() + " ");
        printPreorder(node.getLeft());
        printPreorder(node.getRight());
    }
	
	public void postOrder (Node node) {
        if (node == null)
            return;
        postOrder(node.getLeft());
        postOrder(node.getRight());
        System.out.print(node);
    }
}

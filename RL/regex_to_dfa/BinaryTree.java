package regex_to_dfa;
import java.util.HashMap;
import java.util.Stack;

public class BinaryTree {
	private int numberOfLeafNodes;
	private String newRegex;
	private String postfixRegex="";
    private Stack<Node> stackNode= new Stack<>();
	
	public BinaryTree (String regex) {
	    // Add symbol . as concatenation symbol to input regular expression
        concatRegex(regex);
	}

	public Node createBinaryTree () {
		Stack<Character> operatorsStack = new Stack<Character>();
		for (Character character : newRegex.toCharArray()) {
			if(RegexToDFA.alphabet.contains(character)) {  // ako je u pitanju simbol, odmah ga saljemo u postfix
				postfixRegex+=character;
				createLeafNode(character+"");
			}
			else {										// onda je u pitanju operator ili neka zagrada
				while(!operatorsStack.isEmpty() && checkPriority(character, operatorsStack.peek())) {  // praznimo stek sve dok ne ispraznimo stek
					char operator = operatorsStack.pop();
					postfixRegex+=operator;								// ili dok je operator na steku veceg prioriteta od novog operatora
					createInteriorNode(operator);
				}
				
				if(operatorsStack.isEmpty() || character != ')')  // stavljamo novi operator na stek samo ako nije desna zagrada
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
		System.out.println("Postfix je: "+postfixRegex);
		return getRoot();
	}
	
	// Create leaf node from input symbol and push node into stackNode
    private void createLeafNode (String symbol) {
        Node node = new LeafNode(symbol, numberOfLeafNodes++);
        // Put node to stackNode
        stackNode.push(node);
    }
	
	private void createInteriorNode(char operator) {
		System.out.println("pravim unutrasnji za operator "+operator);
		System.out.println("postfix: "+postfixRegex);
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
		newRegex = "";

        for (int i = 0; i < regex.length() - 1; i++) {
        	if(RegexToDFA.alphabet.contains(regex.charAt(i)) && RegexToDFA.alphabet.contains(regex.charAt(i+1))) {  // in the case of ab, then put . between 
        		newRegex+=regex.charAt(i)+".";
        	}
        	else if (RegexToDFA.alphabet.contains(regex.charAt(i)) && regex.charAt(i+1)=='(') {  // in the case of a(, then change to a.(
        		newRegex+=regex.charAt(i)+".";
        	}
        	else if (regex.charAt(i)==')' && RegexToDFA.alphabet.contains(regex.charAt(i+1))) {   // in the case of )a, then change to ).a
        		newRegex+=regex.charAt(i)+".";
        	}        	
        	else if (regex.charAt(i)=='*' && RegexToDFA.alphabet.contains(regex.charAt(i+1))) {  // in the case of *a, then change to *.a
        		newRegex+=regex.charAt(i)+".";
        	}
        	else if (regex.charAt(i)=='*' && regex.charAt(i+1)=='('){				// in the case of *(, then change to *.(
        		newRegex+=regex.charAt(i)+".";
        	}
        	else if (regex.charAt(i) == ')' && regex.charAt(i + 1) == '(') {		// in the case of )(, then change to ).(
        		newRegex+=regex.charAt(i)+".";
        	}
        	else
        		newRegex+=regex.charAt(i);

        }
        newRegex += regex.charAt(regex.length() - 1);   // add last symbol in input string
        newRegex= '('+newRegex+").#";
		System.out.println("Novi regex sa ubacenom . je: "+ newRegex);
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

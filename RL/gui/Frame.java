package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import automaton.Automaton;
import dfa.DFA;
import dfa.State;
import enfa.ENFA;
import regex_to_dfa.RegexToDFA;


public class Frame extends JFrame {
	JPanel buttonPanel;
	JTable table;
	JTextArea headerField;
	JTextField inputField;
	JButton executeButton, minimizeButton;
	Automaton automaton;
	
	public Frame(String name) {
		super(name);
		loadMenu();
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private void loadMenu() {
		String[] options = {"DFA", "E-NFA", "Regular exspresion"};
		JList<String> list=new JList<>(options);
	    JOptionPane.showMessageDialog(null, list, "Choose option", JOptionPane.PLAIN_MESSAGE);
	    int index= list.getSelectedIndex();
	    if(index==-1)
	    	System.exit(1);
	    
	    switch(index) {
	    	case 0: menu("DFA");
	    		break;
	    	case 1: menu("e-NFA");
				break;
	    	case 2: menu();
				break;
	    }
	}
	
	private void menu() {
		String[] options = {"Execute regex ", "Test saved regex"};
		JList<String> list=new JList<>(options);
	    JOptionPane.showMessageDialog(null, list, "Choose option", JOptionPane.PLAIN_MESSAGE);
	    int index= list.getSelectedIndex();
	    if(index==-1)
	    	System.exit(1);
	    
	    switch(index) {
	    	case 0: String regex = JOptionPane.showInputDialog("Enter regular expresion:");
	    			convertRegexToDFA(regex);
	    			break;
	    	case 1: loadSavedRegex();
					break;
	    }
	}

	private void convertRegexToDFA(String regex) {
		automaton = RegexToDFA.convert(automaton, regex.trim());
		drawAutomatonFrame(true);
		minimizeButton.hide();
	}
	
	private void loadSavedRegex() {
		
	}

	private void menu(String machine) {
		String[] options = {"Create "+machine, "Test saved "+machine};
		JList<String> list=new JList<>(options);
	    JOptionPane.showMessageDialog(null, list, "Choose option", JOptionPane.PLAIN_MESSAGE);
	    int index= list.getSelectedIndex();
	    if(index==-1)
	    	System.exit(1);
	    
	    switch(index) {
	    	case 0: loadDataGUI(machine);
	    		break;
	    	case 1: loadSavedAutomaton(machine);
				break;
	    }
	}
	
	private void loadDataGUI(String machine) {
		switch(machine) {
		case "DFA": automaton=new DFA();
					break;
		case "e-NFA": automaton=new ENFA();
					break;
		}
		// User enter states  and alphabet of DFA, and choose start state
		int numberOfStates=Integer.parseInt(JOptionPane.showInputDialog("Enter number of states:"));
		ArrayList<State> states=new ArrayList<>();
		for(int i=0; i<numberOfStates;i++)
			states.add(new State(automaton.ID++));
		automaton.setAllStates(states);
		while(true) {
			String id=JOptionPane.showInputDialog("Enter name of start state:");
			int i=Integer.parseInt(id);
			if(i>=0 && i<numberOfStates) {
				automaton.setStartStateByID(id);
				break;
			}
		}
		ArrayList<Character> alphabet=new ArrayList<>();
		String s= JOptionPane.showInputDialog("Enter symbols of alphabet separated by commas:");
		String[] symbols=s.split(",");
		// Alphabet is initial created as Set to avoid duplicate symbols in alphabet.
		for (String string : symbols) {
			alphabet.add(string.charAt(0));
		}
		automaton.setAlphabet(alphabet);
		if(automaton instanceof ENFA)
			automaton.getAlphabet().add(ENFA.epsilon);
		// Final state(s) will be choose using GUI, after creating DFA.
		drawAutomatonFrame(false);
	}
	
	private void drawAutomatonFrame(boolean test) {
		// First create info header for DFA and add to frame
		headerField=new JTextArea("  Enter transitions in table:");
		headerField.setBackground(Color.GRAY);
		headerField.setFont(new Font("SansSerif", Font.BOLD, 15));
		headerField.setBorder(BorderFactory.createLineBorder(Color.black,3));
		headerField.setBounds(5, 5, 5, 5);
		headerField.setEnabled(false);
		add(headerField, BorderLayout.NORTH);
		//Then create panel with two buttons and one text field; this panel will be added to bottom of frame.		
		buttonPanel=new JPanel(new BorderLayout());
		inputField=new JTextField("  Enter input string");
		inputField.setBackground(Color.LIGHT_GRAY);
		inputField.setBorder(BorderFactory.createLineBorder(Color.black,3));
		inputField.setFont(new Font("MONOSPACED", Font.BOLD, 15));
		inputField.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
        		inputField.setBackground(Color.LIGHT_GRAY);
                inputField.setText("");
            }
        });
		executeButton=new JButton("End of entries");
		executeButton.setBackground(Color.GREEN);
		executeButton.setBorder(BorderFactory.createLineBorder(Color.black,3));
		executeButton.setFont(new Font("SansSerif", Font.BOLD, 20));
		buttonPanel.add(executeButton,BorderLayout.CENTER);
		minimizeButton=new JButton();
		createSecondButton(buttonPanel);
		add(buttonPanel, BorderLayout.SOUTH);
		//Next method will create jTable and put them to center of frame
		drawJTableForAutomaton(test);
	}
	
	private void drawJTableForAutomaton(boolean test) {
		//This method create JTable witch represent transitions table for our DFA
		table=new JTable(new MyTableModel(automaton));   
		table.setBounds(30,40,200,300);  
		table.setRowSelectionAllowed(false);
		table.getTableHeader().setBackground(Color.DARK_GRAY);
	    table.getTableHeader().setForeground(Color.green);
	    DefaultTableCellRenderer render = new DefaultTableCellRenderer();
	    render.setBackground(Color.DARK_GRAY);
	    render.setForeground(Color.green);
	    table.getColumnModel().getColumn(0).setCellRenderer(render);
	    if(!test) {
	    	 // MouseListener is use to choose final states by double click on cell. 
			 table.addMouseListener(new MouseAdapter() {
		         public void mouseClicked(MouseEvent evt) {	        	 
		             if (evt.getClickCount() == 2) {	
		            	 if(executeButton.getText().equals("EXECUTE DFA") || executeButton.getText().equals("EXECUTE E-NFA"))
			        		 return;
		            	 
		                 Point pnt = evt.getPoint();
		                 int row = table.rowAtPoint(pnt);
		                 int col = table.columnAtPoint(pnt);
		                 String id = (String) table.getValueAt(row, col);
		                 if(id.contains("*")) {
		                	 id=id.split("\\*")[0];
			                 automaton.setFinalState(id);
			                 table.setValueAt(id, row, col);
		                 }
		                 else {
			                 automaton.setFinalState(id);
			                 table.setValueAt(id+"*", row, col);
		                 }
		             }
		         }
		     });
			// Creating drop-down menu for each state of DFA. This menu will be shown when user click on JTable cell.
			if(automaton instanceof DFA) {
				JComboBox<String> comboBox = new JComboBox<>();
				for (State state : automaton.getAllStates()) {
					 comboBox.addItem(state.getID());
				}
				for(int i=1; i<table.getColumnCount();i++) {
					 TableColumn testColumn = table.getColumnModel().getColumn(i);
					 testColumn.setCellEditor(new DefaultCellEditor(comboBox));
				}	
			}
	    } 
		 JScrollPane sp=new JScrollPane(table);    
		 sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		 add(sp, BorderLayout.CENTER);
		 //Button has two function: first click will mapping JTable content to transitions map of each DFA state;
		 // second click will execute DFA. 
		 if(test) {
			buttonPanel.add(inputField,BorderLayout.NORTH);
			if(automaton instanceof DFA)
				executeButton.setText("EXECUTE DFA");
			else 
				executeButton.setText("EXECUTE E-NFA");
			minimizeButton.setEnabled(true);
			minimizeButton.setBackground(Color.GREEN);
		 }
		 executeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				if(!checkTable(table, test))
					return;
				else	{
					String name;
					if(automaton instanceof DFA)
						name="DFA";
					else
						name="E_NFA";
					table.setEnabled(false);		// Once table is created, meaning DFA is created, there is no more option to change content of JTable. 
					headerField.setText(" Start state of this DFA is: "+automaton.getStartState().getID() +
							". \n Final state(s) of this DFA are: "+automaton.getFinalStates()+automaton.getFromRegex());
					
					if(!test)
						buttonPanel.add(inputField,BorderLayout.NORTH);
					
					if(executeButton.getText().equals("End of entries")) {
						mapJTableToAutomaton(table);
						minimizeButton.setEnabled(true);
						minimizeButton.setBackground(Color.GREEN);
						executeButton.setText("EXECUTE "+name);
					}
					else {
						String input=inputField.getText();
						HashSet<State> result = automaton.execute(automaton.getStartState(), input);
						if(automaton.machineAcceptInput(result)) {
							inputField.setText("  String "+input+" is accepted!");
							inputField.setBackground(Color.green);
						}
						else {
							inputField.setText("  String "+input+" is not accepted!");
							inputField.setBackground(Color.red);
						}
					}
				}
			}
		});	 
		if(test)
			 mapAutomatonToJTable(table);
	}
	
	private boolean checkTable(JTable table, boolean test) {
		//This method is checking is Jtable filled and are final states are chosen. 
		if(test)
			return true;
		
		if(automaton.getFinalStates().size()==0) {
			JOptionPane.showMessageDialog(null, "You must enter final state!");
			return false;
		}
		
		if(automaton instanceof ENFA)
			return true;
		
		for(int i=0; i<table.getRowCount(); i++)
			for(int j=1;j<table.getColumnCount(); j++)
				if(table.getValueAt(i, j)==null) {
					JOptionPane.showMessageDialog(null, "You must fill table!");
					return false;
				}
		
		return true;
	}
	
	private void mapJTableToAutomaton(JTable table) {
		 //When user click on button, JTable content is mapping to HashMap transitions of every state. 
		for(int row=0; row<table.getRowCount(); row++)  											// for each row of JTable/each state of DFA
			for(int col=1;col<table.getColumnCount(); col++) {										//for each symbol of alphabet
				//Map transition to next states for state i and symbol
				String id=getIDfromJTable(table, row);
				Character symbol=automaton.getAlphabet().get(col-1);
				String cellValue=(String) table.getValueAt(row, col);
				getAllTransitionsForThisSymbol(automaton, automaton.getStateByID(id), symbol, cellValue);
			}
		
		automaton.print();
		System.out.println();
	}
	
	private void getAllTransitionsForThisSymbol(Automaton automaton, State state, char simbol, String cellValue){
		//U slucaju NFA, polje JTabele ce sadrzati vise stanja pa treba particionisati string cellValue
		if(cellValue!=null) {
			String[] statesID=cellValue.split(",");
			HashSet<State> nextStates=new HashSet<>();
			for (String id : statesID) {
				nextStates.add(automaton.getStateByID(id));
			}
			// Mapiram prelaz za stanje state i simbol
			state.addTransition(simbol, nextStates);
		}
		else {
			// If automaton is E_NFA, then cellvalue may be null
			state.addTransition(simbol, null);
		}
	}
	
	private void mapAutomatonToJTable(JTable table) {
		//This method map transition table of minimized DFA(or converted E-NFA to DFA)  to minimized JTable
		table.setModel(new MyTableModel(automaton));
		DefaultTableCellRenderer render = new DefaultTableCellRenderer();
	    render.setBackground(Color.DARK_GRAY);
	    render.setForeground(Color.green);
	    table.getColumnModel().getColumn(0).setCellRenderer(render);
		
		for(int i=0;i<automaton.getAllStates().size();i++) {
			int j=1;
			for (char symbol : automaton.getAlphabet()) {
				HashSet<State> nextStates = automaton.getStateByOrder(i).move(symbol);
				String s="";
				for (State state : nextStates) {
					s+=state.getID();
				}
				
				table.setValueAt(s, i, j++);
			}
		}
		if(automaton instanceof DFA) {
			headerField.setText(" Start state of this DFA is: "+automaton.getStartState().getID() +
					". \n Final state(s) of this DFA are: "+automaton.getFinalStates()+automaton.getFromRegex());
			executeButton.setText("EXECUTE DFA");
		}
		else
			headerField.setText(" Start state of this E-NFA is: "+automaton.getStartState().getID() +
					". \n Final state(s) of this E-NFA are "+automaton.getFinalStates());
	}
	
	private String getIDfromJTable(JTable table, int row) {
		String id = (String) table.getValueAt(row, 0);
		if(id.contains("->"))
			id=id.split("->")[1];
		if(id.contains("*"))
			id=id.split("\\*")[0];
		return id;
	}
	
	public void createSecondButton(JPanel buttonPanel) {
		if(automaton instanceof DFA) {
			minimizeButton.setText("Minimize DFA");
			minimizeButton.setEnabled(false);
			minimizeButton.setBackground(Color.lightGray);
			minimizeButton.setBorder(BorderFactory.createLineBorder(Color.black,3));
			minimizeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					automaton.minimize();
					mapAutomatonToJTable(table);
					minimizeButton.setBackground(Color.lightGray);
					minimizeButton.setEnabled(false);
				}
			});
		}
		else {
			minimizeButton.setText("Convert eNFA to DFA");
			minimizeButton.setEnabled(false);
			minimizeButton.setBackground(Color.lightGray);
			minimizeButton.setBorder(BorderFactory.createLineBorder(Color.black,3));
			minimizeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					automaton = automaton.convert();
					mapAutomatonToJTable(table);
					setMinimizedButton();
				}
			});
		}
		buttonPanel.add(minimizeButton,BorderLayout.SOUTH);
	}
	
	private void setMinimizedButton() {
		buttonPanel.remove(minimizeButton);
		minimizeButton=null;
		minimizeButton=new JButton();
		minimizeButton.setBackground(Color.GREEN);
		minimizeButton.setBorder(BorderFactory.createLineBorder(Color.black,3));
		minimizeButton.setText("Minimize DFA");
		minimizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				automaton.minimize();
				mapAutomatonToJTable(table);
				minimizeButton.setBackground(Color.lightGray);
				minimizeButton.setEnabled(false);
			}
		});
		buttonPanel.add(minimizeButton, BorderLayout.SOUTH);
	}
	
	private void loadSavedAutomaton(String machine) {
		if(machine.equals("DFA"))
			loadSavedDFA();
		else
			loadSavedNFA();
	}
	
	private void loadSavedDFA() {
		// This saved DFA is from lecture 2, slide 54
		int numberOfStates = 11;
		int startStateID=0;
		ArrayList<Integer> finalStatesID=new ArrayList<>(Arrays.asList (1,4,8));
		ArrayList<Character> alphabet = new ArrayList<>(Arrays.asList('a','b'));
		
		DFA dfa;
		dfa=new DFA();
		ArrayList<State> states=new ArrayList<>();
		for(int i=0; i<numberOfStates;i++)
			states.add(new State(dfa.ID++));
		dfa.setAllStates(states);
		dfa.setStartStateByID(startStateID+"");
		dfa.setAlphabet(alphabet);
		HashSet<State> finalStates=new HashSet<>();
		for (Integer id : finalStatesID) {
			finalStates.add(dfa.getStateByID(id+""));
		}
		dfa.setFinalStates(finalStates);
		dfa.getStateByID(0+"").addAllTransitions(dfa, 'a', new ArrayList<>(Arrays.asList ("6")));
		dfa.getStateByID(0+"").addAllTransitions(dfa, 'b', new ArrayList<>(Arrays.asList ("1")));
		dfa.getStateByID(1+"").addAllTransitions(dfa, 'a', new ArrayList<>(Arrays.asList ("2")));
		dfa.getStateByID(1+"").addAllTransitions(dfa, 'b', new ArrayList<>(Arrays.asList ("4")));
		dfa.getStateByID(2+"").addAllTransitions(dfa, 'a', new ArrayList<>(Arrays.asList ("5")));
		dfa.getStateByID(2+"").addAllTransitions(dfa, 'b', new ArrayList<>(Arrays.asList ("3")));
		dfa.getStateByID(3+"").addAllTransitions(dfa, 'a', new ArrayList<>(Arrays.asList ("8")));
		dfa.getStateByID(3+"").addAllTransitions(dfa, 'b', new ArrayList<>(Arrays.asList ("3")));
		dfa.getStateByID(4+"").addAllTransitions(dfa, 'a', new ArrayList<>(Arrays.asList ("6")));
		dfa.getStateByID(4+"").addAllTransitions(dfa, 'b', new ArrayList<>(Arrays.asList ("8")));
		dfa.getStateByID(5+"").addAllTransitions(dfa, 'a', new ArrayList<>(Arrays.asList ("2")));
		dfa.getStateByID(5+"").addAllTransitions(dfa, 'b', new ArrayList<>(Arrays.asList ("3")));
		dfa.getStateByID(6+"").addAllTransitions(dfa, 'a', new ArrayList<>(Arrays.asList ("4")));
		dfa.getStateByID(6+"").addAllTransitions(dfa, 'b', new ArrayList<>(Arrays.asList ("7")));
		dfa.getStateByID(7+"").addAllTransitions(dfa, 'a', new ArrayList<>(Arrays.asList ("8")));
		dfa.getStateByID(7+"").addAllTransitions(dfa, 'b', new ArrayList<>(Arrays.asList ("7")));
		dfa.getStateByID(8+"").addAllTransitions(dfa, 'a', new ArrayList<>(Arrays.asList ("7")));
		dfa.getStateByID(8+"").addAllTransitions(dfa, 'b', new ArrayList<>(Arrays.asList ("4")));
		dfa.getStateByID(9+"").addAllTransitions(dfa, 'a', new ArrayList<>(Arrays.asList ("10")));
		dfa.getStateByID(9+"").addAllTransitions(dfa, 'b', new ArrayList<>(Arrays.asList ("8")));
		dfa.getStateByID(10+"").addAllTransitions(dfa, 'a', new ArrayList<>(Arrays.asList ("3")));
		dfa.getStateByID(10+"").addAllTransitions(dfa, 'b', new ArrayList<>(Arrays.asList ("9")));
		automaton=dfa;
		drawAutomatonFrame(true);
	}
	
	private void loadSavedNFA() {
		// This saved NFA (regular NFA,without e-transitions) is from lecture 3, slide 38
		int numberOfStates = 6;
		int startStateID=0;
		ArrayList<Integer> finalStatesID=new ArrayList<>(Arrays.asList (5));
		ArrayList<Character> alphabet = new ArrayList<>(Arrays.asList('a','b'));
		
		ENFA nfa;
		nfa=new ENFA();
		ArrayList<State> states=new ArrayList<>();
		for(int i=0; i<numberOfStates;i++)
			states.add(new State(nfa.ID++));
		nfa.setAllStates(states);
		nfa.setStartStateByID(startStateID+"");
		nfa.setAlphabet(alphabet);
		HashSet<State> finalStates=new HashSet<>();
		for (Integer id : finalStatesID) {
			finalStates.add(nfa.getStateByID(id+""));
		}
		nfa.setFinalStates(finalStates);
		nfa.getStateByID(0+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("1,2,4")));
		nfa.getStateByID(0+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("0")));
		nfa.getStateByID(1+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("2,5")));
		nfa.getStateByID(1+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("1,2,3,4,5")));
		nfa.getStateByID(2+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("2")));
		nfa.getStateByID(2+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("5")));
		nfa.getStateByID(3+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("4")));
		nfa.getStateByID(3+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("null")));
		nfa.getStateByID(4+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("5")));
		nfa.getStateByID(4+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("3")));
		nfa.getStateByID(5+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("null")));
		nfa.getStateByID(5+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("null")));
		automaton=nfa;
		drawAutomatonFrame(true);
	}
	
	private void loadSavedNFA(int a) {
		// This saved e-NFA is from lecture 3, slide 37
		int numberOfStates = 6;
		int startStateID=0;
		ArrayList<Integer> finalStatesID=new ArrayList<>(Arrays.asList (5));
		ArrayList<Character> alphabet = new ArrayList<>(Arrays.asList('a','b', ENFA.epsilon));
		
		ENFA nfa;
		nfa=new ENFA();
		ArrayList<State> states=new ArrayList<>();
		for(int i=0; i<numberOfStates;i++)
			states.add(new State(nfa.ID++));
		nfa.setAllStates(states);
		nfa.setStartStateByID(startStateID+"");
		nfa.setAlphabet(alphabet);
		HashSet<State> finalStates=new HashSet<>();
		for (Integer id : finalStatesID) {
			finalStates.add(nfa.getStateByID(id+""));
		}
		nfa.setFinalStates(finalStates);
		nfa.getStateByID(0+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("1")));
		nfa.getStateByID(0+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("0")));
		nfa.getStateByID(0+"").addAllTransitions(nfa, ENFA.epsilon, new ArrayList<>(Arrays.asList ("null")));
		nfa.getStateByID(1+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("null")));
		nfa.getStateByID(1+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("1")));
		nfa.getStateByID(1+"").addAllTransitions(nfa, ENFA.epsilon, new ArrayList<>(Arrays.asList ("2,4")));
		nfa.getStateByID(2+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("2")));
		nfa.getStateByID(2+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("5")));
		nfa.getStateByID(2+"").addAllTransitions(nfa, ENFA.epsilon, new ArrayList<>(Arrays.asList ("null")));
		nfa.getStateByID(3+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("4")));
		nfa.getStateByID(3+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("null")));
		nfa.getStateByID(3+"").addAllTransitions(nfa, ENFA.epsilon, new ArrayList<>(Arrays.asList ("null")));
		nfa.getStateByID(4+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("5")));
		nfa.getStateByID(4+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("3")));
		nfa.getStateByID(4+"").addAllTransitions(nfa, ENFA.epsilon, new ArrayList<>(Arrays.asList ("null")));
		nfa.getStateByID(5+"").addAllTransitions(nfa, 'a', new ArrayList<>(Arrays.asList ("null")));
		nfa.getStateByID(5+"").addAllTransitions(nfa, 'b', new ArrayList<>(Arrays.asList ("null")));
		nfa.getStateByID(5+"").addAllTransitions(nfa, ENFA.epsilon, new ArrayList<>(Arrays.asList ("null")));
		automaton=nfa;
		drawAutomatonFrame(true);
	}
	
}

package gui;

import java.util.LinkedList;
import javax.swing.table.AbstractTableModel;

import automaton.Automaton;
import dfa.DFA;
import dfa.State;

public class MyTableModel extends AbstractTableModel {
	private LinkedList<Character> columnNames;
	private String[][] data;
	
	public MyTableModel(Automaton automaton) {
		 data = new String[automaton.getAllStates().size()] [automaton.getAlphabet().size()+1];
		 int i=0;
		 for (State state : automaton.getAllStates()) {
			if(state.equals(automaton.getStartState()))
				data[i++][0]="->"+state.getID();
			else if(automaton.getFinalStates().contains(state))
				data[i++][0]=state.getID()+"*";				
			else
				 data[i++][0]=state.getID();
		}
		 columnNames=new LinkedList<>(automaton.getAlphabet());
		 columnNames.addFirst('*');
	}

	public int getColumnCount() {
        return columnNames.size();
    }

    public int getRowCount() {
        return data.length;
    }
    
    public String getColumnName(int col) {
        return columnNames.get(col)+"";
    }

    public String getValueAt(int row, int col) {
        return data[row][col];
    }
    
    public boolean isCellEditable(int row, int col) {
    	if(col>0)
    		return true; 
    	else
    		return false;
    }
    
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = (String) value;
        fireTableCellUpdated(row, col);
    }
}

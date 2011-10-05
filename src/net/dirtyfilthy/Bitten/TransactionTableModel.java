package net.dirtyfilthy.Bitten;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.TreeTableModel;

import com.google.bitcoin.core.SqlTransaction;
import com.google.bitcoin.core.Transaction;

public class TransactionTableModel implements TreeTableModel {
	private String[] columnNames = {"id","time","from","to"};
	private ArrayList<SqlTransaction> transactions;
	
	TransactionTableModel(ArrayList<SqlTransaction> transactions){
		this.transactions=transactions;
		
	}
	
	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return transactions.size();
	}
	
	public String getColumnName(int col) {
        return columnNames[col];
    }
	
	public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	}

	private Object getTransactionValueAt(SqlTransaction t, int arg1){
		switch(arg1){
		case 0:
			return t.getHash();
		case 1:
			return new Date((long) t.createdAt*1000);
		case 2:
			return t.getInputs();
		case 3:
			return t.getOutputs();
		default:
			return null;
		}
	}
	
	
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return getTransactionValueAt(transactions.get(arg0),arg1);
	}

	@Override
	public void addTreeModelListener(TreeModelListener arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getChild(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getChildCount(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getIndexOfChild(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLeaf(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeTreeModelListener(TreeModelListener arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getHierarchicalColumn() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getValueAt(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCellEditable(Object arg0, int arg1) {
		if(arg1==6){
			return true;
		}
		return false;
	}

	@Override
	public void setValueAt(Object arg0, Object arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
	
	

}

package net.dirtyfilthy.Bitten;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.google.bitcoin.core.SqlTransaction;
import com.google.bitcoin.core.Transaction;

public class TransactionTableModel extends AbstractTableModel {
	private String[] columnNames = {"id","time","from","to"};
	private ArrayList<SqlTransaction> transactions;
	
	TransactionTableModel(ArrayList<SqlTransaction> transactions){
		this.transactions=transactions;
		
	}
	
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
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
			return t.createdAt;
		case 2:
			return t.getInputs();
		case 3:
			return t.getOutputs();
		default:
			return null;
		}
	}
	
	@Override
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return getTransactionValueAt(transactions.get(arg0),arg1);
	}
	
	

}

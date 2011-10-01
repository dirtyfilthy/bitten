package net.dirtyfilthy.Bitten;

import java.util.ArrayList;

import javax.swing.JTable;

import com.google.bitcoin.core.SqlTransaction;

public class TransactionTable extends JTable {
	
	TransactionTable(ArrayList<SqlTransaction> transactions){
		super(new TransactionTableModel(transactions));
	}

}

package net.dirtyfilthy.Bitten;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import com.google.bitcoin.core.AddressListable;
import com.google.bitcoin.core.SqlBlockStore;
import com.google.bitcoin.core.SqlTransaction;

public class TransactionTable extends JTable {
	
	
	TransactionTable(ArrayList<SqlTransaction> transactions){
		super(new TransactionTableModel(transactions));
		TableColumn from=this.getColumn("from");
		TableColumn to=this.getColumn("to");
		from.setCellRenderer(new AddressListRenderer());
		to.setCellRenderer(new AddressListRenderer());
		
	}


}

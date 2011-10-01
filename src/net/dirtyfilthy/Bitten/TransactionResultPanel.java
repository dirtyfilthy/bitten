package net.dirtyfilthy.Bitten;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.google.bitcoin.core.SqlBlockStore;
import com.google.bitcoin.core.SqlTransaction;

public class TransactionResultPanel extends ResultSetPanel {
	protected ArrayList<SqlTransaction> transactions;
	protected SqlBlockStore store;
	protected JTable table;
	
	TransactionResultPanel(PreparedStatement query,SqlBlockStore s){
		super();
		this.query=query;
		this.store=s;
		transactions=new ArrayList<SqlTransaction>();
	}
	
	@Override
	protected void processResultSet(ResultSet rs) {
		System.out.println("process transactions");
		try {
			while(rs.next()){
				transactions.add(store.loadTransactionFromResultSet(rs));
				System.out.println("add transaction");
			};
			rs.close();
			System.out.println("transactions "+transactions.size() );
			table=new TransactionTable(transactions);
			DefaultTableModel m= new DefaultTableModel(new Object[][] {{"one","two"},{"three","four"}}, new Object[] {"high","low"}); 
			//table=new JTable(m);
			table.setVisible(true);
			this.add(new JScrollPane(table));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

	}
	
	

}
package net.dirtyfilthy.Bitten;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.google.bitcoin.core.SqlAddress;
import com.google.bitcoin.core.SqlBlockStore;
import com.google.bitcoin.core.SqlTransaction;
import com.google.bitcoin.core.SqlTransactionInput;
import com.google.bitcoin.core.TransactionInput;

public class SearchBlockStoreTask extends SwingWorker<ArrayList<SqlTransaction>, Object> {

	private PreparedStatement query;
	private String name;
	private SqlBlockStore store;

	SearchBlockStoreTask(SqlBlockStore store, PreparedStatement s, String name){
		query=s;
		this.store=store;
		this.name=name;
	}
	
	protected ArrayList<SqlTransaction> doInBackground() throws Exception {
		ResultSet rs=query.executeQuery();
		ArrayList<SqlTransaction> transactions=new ArrayList<SqlTransaction>();
		ArrayList<SqlAddress> addresses=new ArrayList<SqlAddress>();
		while(rs.next()){
			SqlTransaction t=store.loadTransactionFromResultSet(rs);
			transactions.add(t);
			
		}
		rs.close();
		store.walletStore().addFromTransactions(transactions);
		return transactions;
		
	}
	
	protected void done(){
		try {
			firePropertyChange(name,null,get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

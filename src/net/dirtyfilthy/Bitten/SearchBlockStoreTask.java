package net.dirtyfilthy.Bitten;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.google.bitcoin.core.SqlBlockStore;

public class SearchBlockStoreTask extends SwingWorker<ResultSet, Object> {

	private PreparedStatement query;

	SearchBlockStoreTask(PreparedStatement s){
		query=s;
	}
	
	protected ResultSet doInBackground() throws Exception {
		return query.executeQuery();
	}
	
	protected void done(){
		try {
			firePropertyChange("resultset",null,get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

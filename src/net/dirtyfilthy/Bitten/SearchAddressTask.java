package net.dirtyfilthy.Bitten;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.google.bitcoin.core.GraphAddress;
import com.google.bitcoin.core.GraphTransaction;

public class SearchAddressTask extends SwingWorker<ArrayList<GraphTransaction>, Object> {
	
	private GraphAddress address;
	
	SearchAddressTask(GraphAddress addr){
		address=addr;
	}
	
	@Override
	protected ArrayList<GraphTransaction> doInBackground() throws Exception {
		return address.transactions();
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

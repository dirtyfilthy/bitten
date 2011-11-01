package net.dirtyfilthy.Bitten;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.google.bitcoin.core.GraphTransaction;
import com.google.bitcoin.core.GraphTransactionOutput;
import com.google.bitcoin.core.GraphWallet;

public class SearchTaintedTask extends
		SwingWorker<ArrayList<GraphTransaction>, Object> {

	private GraphTransactionOutput output;
	private int depth;

	SearchTaintedTask(GraphTransactionOutput o, int depth){
		this.output=o;
		this.depth=depth;
	}
	
	@Override
	protected ArrayList<GraphTransaction> doInBackground() throws Exception {
		return output.followTainted(depth);
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

package net.dirtyfilthy.Bitten;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.google.bitcoin.core.GraphAddress;
import com.google.bitcoin.core.GraphPayment;
import com.google.bitcoin.core.GraphTransaction;
import com.google.bitcoin.core.GraphWallet;

public class SearchWalletTask extends SwingWorker<ArrayList<GraphPayment>, Object> {
	
	private GraphWallet wallet;
	
	SearchWalletTask(GraphWallet w){
		wallet=w;
	}
	
	@Override
	protected ArrayList<GraphPayment> doInBackground() throws Exception {
		return wallet.payments();
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

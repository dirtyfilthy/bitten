package net.dirtyfilthy.Bitten;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.bitcoin.core.GraphTransaction;


public class VisibilityManager {
	
	protected HashMap<GraphTransaction,ArrayList<TransactionTreeTable>> transactionMap;
	protected HashMap<GraphTransaction,Boolean> visibilityMap;
	protected WalletView view;
	
	VisibilityManager(WalletView view){
		this.view=view;
		this.transactionMap=new HashMap<GraphTransaction,ArrayList<TransactionTreeTable>>();
		this.visibilityMap=new HashMap<GraphTransaction,Boolean>();
	}
	
	public synchronized void registerTable(TransactionTreeTable table){
		for(GraphTransaction t : ((RootTransactionTreeNode) table.getTreeTableModel().getRoot()).list){
			if(transactionMap.containsKey(t)){
				transactionMap.get(t).add(table);
				setTransactionVisibilityForTable(table,t,visibilityMap.get(t).booleanValue());
			}
			else{
				ArrayList<TransactionTreeTable> a=new ArrayList<TransactionTreeTable>();
				a.add(table);
				transactionMap.put(t, a);
				visibilityMap.put(t, false);
			}
		}
		table.repaint();
		
	}
	
	
	public synchronized void unregisterTable(TransactionTreeTable table){
		 for(Iterator<Entry<GraphTransaction, ArrayList<TransactionTreeTable>>> it = transactionMap.entrySet().iterator(); it.hasNext(); ) {
			 Map.Entry<GraphTransaction, ArrayList<TransactionTreeTable>> entry = it.next();
			ArrayList a=entry.getValue();
			if(a.contains(table)){
				a.remove(table);
				if(a.size()==0){
					GraphTransaction t=entry.getKey();
					it.remove();
					if(visibilityMap.get(t)){
						view.removeTransaction(t);
					}
					visibilityMap.remove(t);
				}
			}
		}
	}
	
	public void setTransactionVisibilityForTable(TransactionTreeTable table, GraphTransaction t, boolean visible){
		((RootTransactionTreeNode) table.getTreeTableModel().getRoot()).findNodeBySqlTransaction(t).setValueAtNoTrigger(visible, 6);
	}
	
	public void setTransactionVisibility(GraphTransaction t, boolean visible){
		if(!transactionMap.containsKey(t) || visibilityMap.get(t).booleanValue()==visible){
			return;
		}
		ArrayList<TransactionTreeTable> tables=transactionMap.get(t);
		for(TransactionTreeTable table : tables){
			setTransactionVisibilityForTable(table,t,visible);
			table.repaint();
		}
		if(visible){
			view.addTransaction(t);
		}
		else{
			view.removeTransaction(t);
		}
		visibilityMap.put(t, visible);
	}
	


}

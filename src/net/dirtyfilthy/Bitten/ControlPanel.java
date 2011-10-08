package net.dirtyfilthy.Bitten;

import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.google.bitcoin.core.SqlBlockStore;

public class ControlPanel extends JTabbedPane implements TableModelListener {
	private SearchPanel searchPanel;
	private SqlBlockStore store;
	private WalletView view;
	
	
	public ControlPanel(SqlBlockStore store, WalletView view) {
		super();
		this.store=store;
		this.view=view;
		searchPanel=new SearchPanel(this);
		searchPanel.setVisible(true);
		this.addTab("Search", searchPanel);
		
	}
	
	public void registerTransactionPanel(TransactionResultPanel p){
		
	}
	
	public void searchAddress(String address){
		AddressSearchResultPanel results=new AddressSearchResultPanel(this,store,address);
		this.addTab(address,results);
	}

	@Override
	public void tableChanged(TableModelEvent arg0) {
		System.out.println("event: "+arg0);
		System.out.println("source "+arg0.getSource());
		System.out.println("row "+arg0.getFirstRow());
		System.out.println("column "+arg0.getColumn());
		System.out.println("type "+arg0.getType());
	}

	public void notifyVisibilityChange(TransactionTreeNode transactionTreeNode) {
		if(transactionTreeNode.visible){
			view.addTransaction(transactionTreeNode.transaction);
			
		}
		
	}

	
}

package net.dirtyfilthy.Bitten;

import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.google.bitcoin.core.SqlBlockStore;
import com.google.bitcoin.core.SqlWallet;

public class ControlPanel extends JTabbedPane implements TableModelListener {
	private SearchPanel searchPanel;
	private SqlBlockStore store;
	private WalletView view;
	private VisibilityManager visibilityManager;
	
	
	public ControlPanel(SqlBlockStore store, WalletView view) {
		super();
		this.store=store;
		this.view=view;
		this.visibilityManager=new VisibilityManager(view);
		searchPanel=new SearchPanel(this);
		searchPanel.setVisible(true);
		this.addTab("Search", searchPanel);
		
	}
	
	public void registerTransactionPanel(TransactionResultPanel p){
	}
	
	
	public void searchAddress(String address){
		AddressSearchResultPanel results=new AddressSearchResultPanel(this,store,address);
		this.addTab(StringUtils.truncateText(address,8),results);
		setTabComponentAt(this.getTabCount()-1,new ButtonTabComponent(this));
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
		visibilityManager.setTransactionVisibility(transactionTreeNode.transaction, transactionTreeNode.visible);
		
	} 
	
	public void registerTransactionTreeTable(TransactionTreeTable t){
		visibilityManager.registerTable(t);
	}
	
	public void unregisterTransactionTreeTable(TransactionTreeTable t){
		visibilityManager.unregisterTable(t);
	}

	public WalletView getView() {
		return view;
	}

	public WalletStore getWalletStore() {
		// TODO Auto-generated method stub
		return store.walletStore();
	}

	
}

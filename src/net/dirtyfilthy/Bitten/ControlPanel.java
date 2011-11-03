package net.dirtyfilthy.Bitten;

import java.awt.Component;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.google.bitcoin.core.GraphBlockStore;
import com.google.bitcoin.core.GraphTransactionOutput;
import com.google.bitcoin.core.GraphWallet;

public class ControlPanel extends JTabbedPane implements TableModelListener {
	private SearchPanel searchPanel;
	private GraphBlockStore store;
	private WalletView view;
	private VisibilityManager visibilityManager;
	private HashMap<String,Integer> tabMap;
	
	public ControlPanel(GraphBlockStore store, WalletView view) {
		super();
		this.store=store;
		this.view=view;
		this.view.panel=this;
		// this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		this.visibilityManager=new VisibilityManager(view);
		searchPanel=new SearchPanel(this);
		searchPanel.setVisible(true);
		tabMap=new HashMap<String,Integer>();
		this.addTab("Search", searchPanel);
		
	}
	
	public int autoViz(){
		if(searchPanel.autoViz.isSelected()){
			return new Integer(searchPanel.autoVizNum.getText());
		}
		return 0;
		
	}
	
	public void registerTransactionPanel(TransactionResultPanel p){
	}
	
	
	public void searchAddress(String address){
		String full="A"+address.trim();
		if(!switchToTab(full)){
			AddressSearchResultPanel results=new AddressSearchResultPanel(this,store,address);
			this.addSwitchableTab(results,StringUtils.truncateText(address,8),full);
		}
	}
	
	public void addSwitchableTab(Component c, String title, String full){
		this.addTab(title,c);
		int tab=this.getTabCount()-1;
		setTabComponentAt(tab,new ButtonTabComponent(this));
		this.setSelectedIndex(tab);
		tabMap.put(full,tab);
	}
	
	public boolean switchToTab(String full){
		if(tabMap.containsKey(full)){
			this.setSelectedIndex(tabMap.get(full));
			return true;
		}
		return false;
	}
	
	public void remove(int i){
		if (i != -1) {
        	Component c=getComponentAt(i);
        	if(c instanceof Closeable){
        		((Closeable) c).close();
        	}
        	Iterator<Entry<String,Integer>> it = tabMap.entrySet().iterator();
        	while(it.hasNext()){
        		Entry<String,Integer> e=it.next();
        		if(e.getValue()==i){
        			it.remove();
        		}
        		if(e.getValue()>i){
        			e.setValue(i-1);
        		}
        	}
            super.remove(i);
        }
	}
	
	
	public void showWallet(GraphWallet w){
		String full="W"+w.node().getId();
		if(!switchToTab(full)){
			WalletSearchResultPanel results=new WalletSearchResultPanel(this,w);
			addSwitchableTab(results,StringUtils.truncateText(w.label(),8),full);
		}
	}
	
	public void searchTainted(GraphTransactionOutput o, Integer steps){
		String full="T"+o.node().getId();
		if(!switchToTab(full)){
			TaintSearchResultPanel results=new TaintSearchResultPanel(this,o,steps);
			addSwitchableTab(results,StringUtils.truncateText("Tainted",8),full);
		}
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

	

	public GraphBlockStore getBlockStore() {
		// TODO Auto-generated method stub
		return store;
	}

	
}

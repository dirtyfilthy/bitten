package net.dirtyfilthy.Bitten;

import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.google.bitcoin.core.SqlBlockStore;

public class ControlPanel extends JTabbedPane {
	private SearchPanel searchPanel;
	private SqlBlockStore store;
	
	
	public ControlPanel(SqlBlockStore store) {
		super();
		this.store=store;
		searchPanel=new SearchPanel(this);
		searchPanel.setVisible(true);
		this.addTab("Search", searchPanel);
		
	}
	
	public void searchAddress(String address){
		AddressSearchResultPanel results=new AddressSearchResultPanel(address,store);
		this.addTab(address,results);
	}

	
}

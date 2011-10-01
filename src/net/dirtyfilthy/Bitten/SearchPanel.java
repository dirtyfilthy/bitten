package net.dirtyfilthy.Bitten;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SearchPanel extends JPanel  implements ActionListener {

	ControlPanel controlPanel;
	
	JTextField addressTextField;
	
	public SearchPanel(ControlPanel cp) {
		super();
			controlPanel=cp;
			create();
		// TODO Auto-generated constructor stub
	}

	public SearchPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public SearchPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public SearchPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}
	
	 public void actionPerformed(ActionEvent e) {
	        if ("search_address".equals(e.getActionCommand())) {
	        	controlPanel.searchAddress(addressTextField.getText());
	        }
	    }
	
	private void create(){
		JPanel addressSearchPanel=new JPanel();
		addressSearchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Address search"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		 addressTextField=new JTextField(20);
		 addressSearchPanel.add(addressTextField);
		 JButton b1=new JButton("Search");
		 b1.setActionCommand("search_address");
		 b1.addActionListener(this);
		 addressSearchPanel.add(b1);
		 this.add(addressSearchPanel);
	}

}

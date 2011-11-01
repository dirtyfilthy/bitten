package net.dirtyfilthy.Bitten;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.google.bitcoin.core.GraphTransactionOutput;

public class OutputPopup extends JPopupMenu implements ActionListener {
	
	JMenuItem anItem;
	ControlPanel panel;
	GraphTransactionOutput output;
    public OutputPopup(ControlPanel p, GraphTransactionOutput o){
        anItem = new JMenuItem("Click Me!");
        add(anItem);
        anItem.addActionListener(this);
        output=o;
        panel=p;
    }
	@Override
	public void actionPerformed(ActionEvent e) {
		panel.searchTainted(output);
		
	}

}

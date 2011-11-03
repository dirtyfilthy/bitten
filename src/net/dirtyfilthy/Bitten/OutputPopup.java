package net.dirtyfilthy.Bitten;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import com.google.bitcoin.core.GraphTransactionOutput;

public class OutputPopup extends JPopupMenu implements ActionListener {
	
	JMenuItem anItem;
	ControlPanel panel;
	GraphTransactionOutput output;
	JSpinner spinner;
    public OutputPopup(ControlPanel p, GraphTransactionOutput o){
    	JPanel pp=new JPanel();
    	JLabel j=new JLabel("Depth:");
    	pp.add(j);
    	 panel=p;
         SpinnerModel model = new SpinnerNumberModel(5, //initial value
                                        1, //min
                                        15, //max
                                        1); 
         spinner=new JSpinner(model);
         pp.add(spinner);
         add(pp);
        anItem = new JMenuItem("Follow the money");
        add(anItem);
        anItem.addActionListener(this);
        output=o;
       
    }
	@Override
	public void actionPerformed(ActionEvent e) {
		panel.searchTainted(output,(Integer) spinner.getValue());
		
	}

}

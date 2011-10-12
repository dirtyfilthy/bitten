package net.dirtyfilthy.Bitten;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.google.bitcoin.core.Accountable;
import com.google.bitcoin.core.WalletIdable;


public class TransactionOptionsPanel extends JPanel implements ItemListener {
	
	private JCheckBox visIncoming;
	private JCheckBox visOutgoing;
	private TransactionTreeTableModel model;
	private Accountable target;
	private ControlPanel panel;
	
	TransactionOptionsPanel(ControlPanel p, Accountable target, TransactionTreeTableModel model){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.model=model;
		this.target=target;
		this.panel=p;
		visIncoming=new JCheckBox("Show Incoming");
		visOutgoing=new JCheckBox("Show Outgoing");
		
		visIncoming.addItemListener(this);
		visOutgoing.addItemListener(this);
		this.add(visIncoming);
		this.add(visOutgoing);
		
		
		
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		Object source=arg0.getItemSelectable();
		int change = arg0.getStateChange();
		if(source==visIncoming){
			if(change==ItemEvent.DESELECTED){
				model.setIncomingVisible(target,false);
			}
			if(change==ItemEvent.SELECTED){
				model.setIncomingVisible(target,true);
			}
			
		}
		if(source==visOutgoing){
			if(change==ItemEvent.DESELECTED){
				model.setOutgoingVisible(target,false);
			}
			if(change==ItemEvent.SELECTED){
				model.setOutgoingVisible(target,true);
			}
			
		}
		if(target instanceof WalletIdable){
			// panel.getView().panToWallet((WalletIdable) target);
		}
					
	}

}

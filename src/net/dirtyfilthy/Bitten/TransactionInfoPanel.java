package net.dirtyfilthy.Bitten;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.bitcoin.core.Accountable;
import com.google.bitcoin.core.SqlTransaction;
import com.google.bitcoin.core.Utils;

public class TransactionInfoPanel extends JPanel {
	
	TransactionInfoPanel(Accountable target,ArrayList<SqlTransaction> transactions){
		long incomingAmount=0;
		long outgoingAmount=0;
		long unspent=0;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		for(SqlTransaction t : transactions){
			incomingAmount+=target.incomingAmount(t);
			outgoingAmount+=target.outgoingAmount(t);
		}
		unspent=incomingAmount-outgoingAmount;
		this.add(new JLabel("Incoming amount: "+Utils.btcToDouble(incomingAmount)));
		this.add(new JLabel("Outgoing amount: "+Utils.btcToDouble(outgoingAmount)));
		this.add(new JLabel("Est unspent: "+Utils.btcToDouble(unspent)));
		
	}

}

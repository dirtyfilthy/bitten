package net.dirtyfilthy.Bitten;

import java.math.BigInteger;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.bitcoin.core.Accountable;
import com.google.bitcoin.core.GraphTransaction;
import com.google.bitcoin.core.Utils;

public class TransactionInfoPanel extends JPanel {
	
	TransactionInfoPanel(Accountable target,ArrayList<GraphTransaction> transactions){
		BigInteger incomingAmount=BigInteger.ZERO;
		BigInteger outgoingAmount=BigInteger.ZERO;
		BigInteger unspent;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if(target.cachedTotalIncoming()!=null){
                       incomingAmount=target.cachedTotalIncoming();
                     outgoingAmount=target.cachedTotalOutgoing();
               }
               else{
                       for(GraphTransaction t : transactions){
                               incomingAmount=incomingAmount.add(target.incomingAmount(t));
                               outgoingAmount=outgoingAmount.add(target.outgoingAmount(t));
                       }
           }
		
		unspent=incomingAmount.subtract(outgoingAmount);
		this.add(new JLabel("Incoming amount: "+Utils.bitcoinValueToFriendlyString(incomingAmount)));
		this.add(new JLabel("Outgoing amount: "+Utils.bitcoinValueToFriendlyString(outgoingAmount)));
		this.add(new JLabel("Est unspent: "+Utils.bitcoinValueToFriendlyString(unspent)));
		
	}

}

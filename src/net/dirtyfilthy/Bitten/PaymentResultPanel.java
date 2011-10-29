package net.dirtyfilthy.Bitten;

import java.util.ArrayList;

import javax.swing.BoxLayout;

import com.google.bitcoin.core.Accountable;
import com.google.bitcoin.core.GraphTransaction;

public class PaymentResultPanel extends ResultSetPanel {
	
	public Accountable target;
	private ControlPanel panel;

	public PaymentResultPanel(ControlPanel p, SearchWalletTask searchWalletTask) {
		super(searchWalletTask);
		this.panel=p;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	@Override
	protected void processResultSet(ArrayList arrayList) {
		// TODO Auto-generated method stub

	}

}

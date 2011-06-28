package net.dirtyfilthy.Bitten;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.BlockChainListener;
import com.google.bitcoin.core.StoredBlock;

public class StatusBar extends JLabel implements BlockChainListener{

	public StatusBar() {
		// TODO Auto-generated constructor stub
	}
	
	public StatusBar(BlockChain c){
		super("Initializing...");
		c.addBlockChainListener(this);
	}

	public StatusBar(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	public StatusBar(Icon image) {
		super(image);
		// TODO Auto-generated constructor stub
	}

	public StatusBar(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public StatusBar(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public StatusBar(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void newBlockAdded(final StoredBlock b) {
		final StatusBar us=this;
		SwingUtilities.invokeLater(new Runnable() {	
			public void run(){
				us.setText(" Block "+b.getHeight());
			}		
		});
		
		
	}

}

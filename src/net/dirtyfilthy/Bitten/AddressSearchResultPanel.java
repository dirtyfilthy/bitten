package net.dirtyfilthy.Bitten;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JPanel;

import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.GraphAddress;
import com.google.bitcoin.core.GraphBlockStore;


public class AddressSearchResultPanel extends JPanel implements Closeable {

	private TransactionResultPanel results;
	
	AddressSearchResultPanel(ControlPanel p, GraphBlockStore store,String address){
		try {
			GraphAddress a=store.findOrCreateAddress(address);
			SearchAddressTask task=new SearchAddressTask(a);
			results=new TransactionResultPanel(p, task);
			results.target=a;
			results.setVisible(true);
			this.add(results);
			results.execute();
		} catch (AddressFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void close() {
		results.close();
		
	}
	
}


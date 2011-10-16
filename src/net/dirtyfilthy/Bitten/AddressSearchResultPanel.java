package net.dirtyfilthy.Bitten;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JPanel;

import com.google.bitcoin.core.SqlAddress;
import com.google.bitcoin.core.SqlBlockStore;

public class AddressSearchResultPanel extends JPanel implements Closeable {

	protected final static String SQL="SELECT DISTINCT transactions.*,blocks.time AS created_at FROM transaction_inputs JOIN transactions ON transaction_id=transactions.id JOIN blocks ON blocks.id=block_id WHERE from_address_id=? UNION SELECT DISTINCT transactions.*,blocks.time AS created_at FROM transaction_outputs JOIN transactions ON transaction_id=transactions.id JOIN blocks ON blocks.id=block_id WHERE to_address_id=?";
	private TransactionResultPanel results;
	
	AddressSearchResultPanel(ControlPanel p, SqlBlockStore store,String address){
		try {
			SqlAddress a=store.findAddressByHash(address.trim());
			PreparedStatement query=store.prepareStatement(SQL);
			query.setLong(1, a.id);
			query.setLong(2, a.id);
			
			results=new TransactionResultPanel(p, store, query);
			results.targetAddress=a;
			results.setVisible(true);
			this.add(results);
			results.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void close() {
		results.close();
		
	}
	
}


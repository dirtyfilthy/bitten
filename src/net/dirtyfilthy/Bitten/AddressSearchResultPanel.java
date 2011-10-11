package net.dirtyfilthy.Bitten;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JPanel;

import com.google.bitcoin.core.SqlAddress;
import com.google.bitcoin.core.SqlBlockStore;

public class AddressSearchResultPanel extends JPanel {

	protected final static String SQL="SELECT DISTINCT transactions.*,blocks.time AS created_at FROM addresses JOIN transaction_inputs ON from_address_id=addresses.id JOIN transactions ON transaction_id=transactions.id JOIN blocks ON blocks.id=block_id WHERE addresses.base58hash = ? UNION SELECT DISTINCT transactions.*,blocks.time AS created_at FROM addresses JOIN transaction_outputs ON to_address_id=addresses.id JOIN transactions ON transaction_id=transactions.id JOIN blocks ON blocks.id=block_id WHERE addresses.base58hash = ?";
	private TransactionResultPanel results;
	
	AddressSearchResultPanel(ControlPanel p, SqlBlockStore store,String address){
		try {
			PreparedStatement query=store.prepareStatement(SQL);
			query.setString(1, address.trim());
			query.setString(2, address.trim());
			SqlAddress a=store.findAddressByHash(address.trim());
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
	
}


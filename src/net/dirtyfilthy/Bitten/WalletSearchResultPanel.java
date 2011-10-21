package net.dirtyfilthy.Bitten;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JPanel;

import com.google.bitcoin.core.SqlAddress;
import com.google.bitcoin.core.SqlBlockStore;
import com.google.bitcoin.core.SqlTransaction;
import com.google.bitcoin.core.SqlWallet;

public class WalletSearchResultPanel extends JPanel implements Closeable {

	protected final static String SQL="SELECT transactions.* FROM addresses JOIN transaction_inputs ON from_address_id=addresses.id JOIN transactions ON transaction_id=transactions.id JOIN blocks ON blocks.id=block_id WHERE wallet_id=? UNION SELECT transactions.* FROM addresses JOIN transaction_outputs ON to_address_id=addresses.id JOIN transactions ON transaction_id=transactions.id WHERE wallet_id=?";
	private TransactionResultPanel results;
	private SqlWallet wallet;
	
	WalletSearchResultPanel(ControlPanel p, SqlBlockStore store,long wallet_id){
		try {
			PreparedStatement query=store.prepareStatement(SQL);
			query.setLong(1, wallet_id);
			query.setLong(2, wallet_id);
			wallet=p.getWalletStore().findById(wallet_id);
			results=new TransactionResultPanel(p, store, query);
			results.target=wallet;
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

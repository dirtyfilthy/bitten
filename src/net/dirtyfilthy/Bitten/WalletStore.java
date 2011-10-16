package net.dirtyfilthy.Bitten;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import prefuse.action.assignment.ShapeAction;

import com.google.bitcoin.core.SqlAddress;
import com.google.bitcoin.core.SqlBlockStore;
import com.google.bitcoin.core.SqlTransaction;
import com.google.bitcoin.core.SqlTransactionInput;
import com.google.bitcoin.core.SqlTransactionOutput;
import com.google.bitcoin.core.SqlWallet;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;

public class WalletStore {
	
	private HashMap<Long,SqlWallet> walletMap;
	private SqlBlockStore store;
	
	public WalletStore(SqlBlockStore store){
		this.store=store;
		walletMap=new HashMap<Long,SqlWallet>();
	}
	
	
	public String toString(){
		String s="";
		for(SqlWallet w : walletMap.values()){
			s=s+w+"\n";
		}
		return s;
		
	}
	
	public synchronized void addFromTransactions(ArrayList<SqlTransaction> transactions){
		ArrayList<SqlAddress> missing=new ArrayList<SqlAddress>();
		SqlAddress a;
		for(SqlTransaction t : transactions){
			for(TransactionInput i : t.inputs){
				SqlTransactionInput i2=(SqlTransactionInput) i;
				if(i2.isCoinBase()){
					continue;
				}
				a=i2.getAddress();
				if(missing.contains(a)){
					continue;
				}
				if(!walletMap.containsValue(new Long(a.getWalletId()))){
					missing.add(a);
				}
			
			}
			for(TransactionOutput i : t.outputs){
				SqlTransactionOutput i2=(SqlTransactionOutput) i;
				a=i2.getAddress();
				if(missing.contains(a)){
					continue;
				}
				if(!walletMap.containsValue(a.getWalletId())){
					missing.add(a);
				}
			
			}
		}
		System.out.println("missing size "+missing.size());
		ArrayList<SqlWallet> wallets=store.loadWalletsFromAddresses(missing);
		System.out.println("load size "+wallets.size());
		for(SqlWallet w : wallets){
			walletMap.put(w.getWalletId(), w);
		}
	}
	
	public SqlWallet findById(long id){
		SqlWallet w=walletMap.get(new Long(id));
		if(w!=null){
			return w;
		}
		try {
			w=store.findWalletById(id);
			if(w!=null){
				walletMap.put(id, w);
				return w;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

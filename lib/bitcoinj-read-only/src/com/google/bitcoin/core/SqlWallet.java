package com.google.bitcoin.core;

import java.util.ArrayList;
import java.util.Collections;

public class SqlWallet implements WalletIdable, Accountable {
	
	public ArrayList<SqlAddress> addresses;
	public Long id;
	protected String label;
	
	SqlWallet(){
		addresses=new ArrayList<SqlAddress>();
	}

	@Override
	public long getWalletId() {
		if(addresses.size()==0){
			return 0;
		}
		return addresses.get(0).getWalletId();
	}
	
	public long incomingAmount(SqlTransaction t) {
		// TODO Auto-generated method stub
		return t.incomingAmountForWalletId(this.id);
	}

	@Override
	public long outgoingAmount(SqlTransaction t) {
		// TODO Auto-generated method stub
		return t.outgoingAmountForWalletId(this.id);
	}
	
	public String label(){
		if(label!=null){
			return label;
		}
		Collections.sort(addresses);
		for(SqlAddress a : addresses) {
			if(a.label==null){
				continue;
			}
			if(!a.label.trim().equals("")){
				label=a.label.trim();
			}
			
		}
		if(label==null){
			label=new Long(getWalletId()).toString();
		}
		return label;
	}

}

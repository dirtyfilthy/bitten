package com.google.bitcoin.core;

public class SqlAddress extends Address implements Accountable, WalletIdable, Comparable {

	public SqlAddress(NetworkParameters params, byte[] hash160) {
		super(params, hash160);
		// TODO Auto-generated constructor stub
	}

	public SqlAddress(NetworkParameters params, String address)
			throws AddressFormatException {
		super(params, address);
		// TODO Auto-generated constructor stub
	}
	
	public long id;
	public String label;
	public long walletId=0;
	public long getWalletId() {
		return (walletId==0 ? id : walletId);
	}

	@Override
	public long incomingAmount(SqlTransaction t) {
		// TODO Auto-generated method stub
		return t.incomingAmountForAddress(this);
	}

	@Override
	public long outgoingAmount(SqlTransaction t) {
		// TODO Auto-generated method stub
		return t.outgoingAmountForAddress(this);
	}
	
	public boolean isLabelled(){
		return label!=null && !label.equals("");
	}
		

	@Override
	public int compareTo(Object arg0) {
		return ((Long) id).compareTo((Long) ((SqlAddress) arg0).id);
	}

}

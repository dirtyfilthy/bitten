package com.google.bitcoin.core;

public class SqlAddress extends Address {

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

}

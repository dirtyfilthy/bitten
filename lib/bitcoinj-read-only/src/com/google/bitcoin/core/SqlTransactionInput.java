package com.google.bitcoin.core;

public class SqlTransactionInput extends TransactionInput implements AddressListable {
	
	public long id;
	public long transactionId;
	public long previousOutputId;
	public long fromAddressId;
	public long value;
	public long index;
	public SqlAddress address;

	public SqlTransactionInput(NetworkParameters params,
			Transaction parentTransaction, byte[] scriptBytes) {
		super(params, parentTransaction, scriptBytes);
		// TODO Auto-generated constructor stub
	}

	public SqlTransactionInput(NetworkParameters params,
			Transaction parentTransaction, TransactionOutput output) {
		super(params, parentTransaction, output);
	
	}

	public SqlTransactionInput(NetworkParameters params,
			Transaction parentTransaction, byte[] payload, int offset)
			throws ProtocolException {
		super(params, parentTransaction, payload, offset);
	
	}
	
	public SqlAddress getAddress(){
		return address;
	}

	@Override
	public long getAddressId() {
		
		return fromAddressId;
	}

	@Override
	public long getBtcValue() {
		return value;
	}

}

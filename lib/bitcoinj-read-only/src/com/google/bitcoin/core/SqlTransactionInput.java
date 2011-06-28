package com.google.bitcoin.core;

public class SqlTransactionInput extends TransactionInput {
	
	public long id;
	public long transactionId;
	public long previousOutputId;
	public long index;

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

}

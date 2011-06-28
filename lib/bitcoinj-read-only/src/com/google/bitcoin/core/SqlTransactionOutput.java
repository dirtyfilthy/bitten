package com.google.bitcoin.core;

import java.math.BigInteger;



public class SqlTransactionOutput extends TransactionOutput {
	
	public long id;
	public long transactionId;
	public long index;


	public SqlTransactionOutput(NetworkParameters params, Transaction parent,
			byte[] payload, int offset) throws ProtocolException {
		super(params, parent, payload, offset);
	}

	public SqlTransactionOutput(NetworkParameters params, Transaction parent,
			BigInteger value, Address to) {
		super(params, parent, value, to);
	}

	public SqlTransactionOutput(NetworkParameters params, byte[] scriptBytes) {
		super(params, scriptBytes);
	}

}

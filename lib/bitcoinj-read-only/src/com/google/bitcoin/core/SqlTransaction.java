package com.google.bitcoin.core;

import java.util.Date;

public class SqlTransaction extends Transaction {
	
	public long id;
	public long block_id;
	public long index;
	public long createdAt;
	

	public SqlTransaction(NetworkParameters params) {
		super(params);
	}

	public SqlTransaction(NetworkParameters params, byte[] payloadBytes)
			throws ProtocolException {
		super(params, payloadBytes);
		
	}

	public SqlTransaction(NetworkParameters params, byte[] payload, int offset)
			throws ProtocolException {
		super(params, payload, offset);
		
	}

}

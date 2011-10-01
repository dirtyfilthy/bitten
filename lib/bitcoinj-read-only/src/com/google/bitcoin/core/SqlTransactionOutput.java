package com.google.bitcoin.core;

import java.math.BigInteger;

import net.dirtyfilthy.Bitten.TransactionEdge;



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
	
	public boolean equals(Object rhs){
		if(rhs==this){
			return true; 
		}
		
		if(!(rhs instanceof SqlTransactionOutput)){
			return false;
		}
		
		return  ((SqlTransactionOutput) rhs).id==this.id;
	}
	
	public int hashCode(){
		return (int) id;
		
	}
	
	

}

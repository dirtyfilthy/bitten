package com.google.bitcoin.core;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SqlTransaction extends Transaction {
	
	public long id;
	public long block_id;
	public long index;
	public long createdAt;
	

	public SqlTransaction(NetworkParameters params) {
		super(params);
	}
	
	public long incomingAmountForAddress(SqlAddress a){
		long amount=0;
		for(TransactionOutput o : outputs){
			SqlTransactionOutput o2=(SqlTransactionOutput) o;
			if(o2.getAddress().equals(a)){
				amount=amount+o2.getBtcValue();
			}
		}
		return amount;
	}
	
	public long outgoingAmountForAddress(SqlAddress a){
		long amount=0;
		for(TransactionInput i : inputs){
			SqlTransactionInput i2=(SqlTransactionInput) i;
			if(i2.isCoinBase()){
				continue;
			}
			if(i2.getAddress().equals(a)){
				amount=amount+i2.getBtcValue();
			}
		}
		return amount;
	}
	
	public long incomingAmountForWalletId(long walletId){
		long amount=0;
		for(TransactionOutput o : outputs){
			SqlTransactionOutput o2=(SqlTransactionOutput) o;
			if(o2.getAddress().getWalletId()==walletId){
				amount=amount+o2.getBtcValue();
			}
		}
		return amount;
	}
	
	public long outgoingAmountForWalletId(long walletId){
		long amount=0;
		for(TransactionInput i : inputs){
			SqlTransactionInput i2=(SqlTransactionInput) i;
			if(i2.getAddress().getWalletId()==walletId){
				amount=amount+i2.getBtcValue();
			}
		}
		return amount;
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

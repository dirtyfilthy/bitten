package com.google.bitcoin.core;

public interface Accountable {
	
	long incomingAmount(SqlTransaction t);
	
	long outgoingAmount(SqlTransaction t);
	

}

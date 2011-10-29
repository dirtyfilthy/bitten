package com.google.bitcoin.core;

import java.math.BigInteger;

public interface Accountable {
	
	BigInteger incomingAmount(GraphTransaction t);
	
	BigInteger outgoingAmount(GraphTransaction t);
	
	BigInteger cachedTotalIncoming();
	
	BigInteger cachedTotalOutgoing();

}

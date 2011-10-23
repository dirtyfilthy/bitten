package com.google.bitcoin.core;

import java.math.BigInteger;

public interface AddressListable {
	
	public GraphAddress address();
	
	public BigInteger value();

}

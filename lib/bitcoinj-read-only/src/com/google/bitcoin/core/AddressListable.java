package com.google.bitcoin.core;

public interface AddressListable {
	
	long getAddressId();
	
	long getBtcValue();
	
	SqlAddress getAddress();

}

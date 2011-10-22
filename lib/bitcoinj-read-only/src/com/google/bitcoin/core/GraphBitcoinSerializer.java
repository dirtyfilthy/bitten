package com.google.bitcoin.core;

public class GraphBitcoinSerializer extends BitcoinSerializer {

	public GraphBitcoinSerializer(NetworkParameters params,
			boolean usesChecksumming) {
		super(params, usesChecksumming);
		// TODO Auto-generated constructor stub
	}
	
	static {
        names.put(VersionMessage.class, "version");
        names.put(InventoryMessage.class, "inv");
        names.put(GraphBlockHeader.class, "block");
        names.put(GetDataMessage.class, "getdata");
        names.put(Transaction.class, "tx");
        names.put(AddressMessage.class, "addr");
        names.put(Ping.class, "ping");
        names.put(VersionAck.class, "verack");
        names.put(GetBlocksMessage.class, "getblocks");
    }


}

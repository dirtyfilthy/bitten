package com.google.bitcoin.core;

import java.util.ArrayList;
import java.util.List;

public class GraphBlockHeader extends Block {

	List<GraphTransaction> transactions;
	
	public GraphBlockHeader(NetworkParameters params) {
		super(params);
		// TODO Auto-generated constructor stub
	}

	public GraphBlockHeader(NetworkParameters params, byte[] payloadBytes)
			throws ProtocolException {
		super(params, payloadBytes);
		// TODO Auto-generated constructor stub
	}
	
	
	 /** Returns a copy of the block, but without any transactions. */
    public GraphBlockHeader cloneAsHeader() {
        try {
            GraphBlockHeader block = new GraphBlockHeader(params, bitcoinSerialize());
            block.transactions = null;
            return block;
        } catch (ProtocolException e) {
            // Should not be able to happen unless our state is internally inconsistent.
            throw new RuntimeException(e);
        }
    }
    
    /** Returns a copy of the block */
    public GraphBlockHeader clone() {
        try {
            GraphBlockHeader block = new GraphBlockHeader(params, bitcoinSerialize());
            return block;
        } catch (ProtocolException e) {
            // Should not be able to happen unless our state is internally inconsistent.
            throw new RuntimeException(e);
        }
    }
	
	 void parse() throws ProtocolException {
	        version = readUint32();
	        prevBlockHash = readHash();
	        merkleRoot = readHash();
	        time = readUint32();
	        difficultyTarget = readUint32();
	        nonce = readUint32();
	        
	        hash = Utils.reverseBytes(Utils.doubleDigest(bytes, 0, cursor));

	        if (cursor == bytes.length) {
	            // This message is just a header, it has no transactions.
	            return;
	        }

	        int numTransactions = (int) readVarInt();
	        transactions = new ArrayList<GraphTransaction>(numTransactions);
	        for (int i = 0; i < numTransactions; i++) {
	            GraphTransaction tx = new GraphTransaction(params, bytes, cursor);
	            tx.index=i;
	            transactions.add(tx);
	            cursor += tx.getMessageSize();
	        }
	    }

}

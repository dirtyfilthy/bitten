package com.google.bitcoin.core;

import java.math.BigInteger;

public class FullStoredBlock extends StoredBlock {

	public FullStoredBlock(Block block, BigInteger chainWork, int height) {
		super((Block) block, chainWork, height);
		// TODO Auto-generated constructor stub
	}
	
	/**
     * Creates a new StoredBlock, calculating the additional fields by adding to the values in this block.
     */
    public FullStoredBlock build(Block block) throws VerificationException {
        // Stored blocks track total work done in this chain, because the canonical chain is the one that represents
        // the largest amount of work done not the tallest.
        BigInteger chainWork = this.chainWork.add(block.getWork());
        int height = this.height + 1;
        return new FullStoredBlock(block, chainWork, height);
    }
    
    /**
     * The block header this object wraps. The referenced block object must not have any transactions in it.
     */
    public Block getHeader() {
        return header.cloneAsHeader();
    }
    
    /**
     * The block header this object wraps. The referenced block object must not have any transactions in it.
     */
    public Block getBlock() {
        return header;
    }
    


}

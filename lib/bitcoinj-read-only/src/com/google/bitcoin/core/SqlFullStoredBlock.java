package com.google.bitcoin.core;

import java.math.BigInteger;

public class SqlFullStoredBlock extends FullStoredBlock {
	public long id;
	
	public SqlFullStoredBlock(Block block, BigInteger chainWork, int height) {
		super(block, chainWork, height);
	}
	
	/**
     * Creates a new StoredBlock, calculating the additional fields by adding to the values in this block.
     */
    public SqlFullStoredBlock build(Block block) throws VerificationException {
        // Stored blocks track total work done in this chain, because the canonical chain is the one that represents
        // the largest amount of work done not the tallest.
        BigInteger chainWork = this.chainWork.add(block.getWork());
        int height = this.height + 1;
        return new SqlFullStoredBlock(block, chainWork, height);
    }
	
}

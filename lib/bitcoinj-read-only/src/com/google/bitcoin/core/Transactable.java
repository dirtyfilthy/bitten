package com.google.bitcoin.core;

import java.util.ArrayList;


public interface Transactable extends Timeable {
	
	public GraphTransaction transaction();
	
	public GraphWallet fromWallet();
	
	public boolean isCoinBase();

	public ArrayList<GraphWallet> toWallets();
	
	

}

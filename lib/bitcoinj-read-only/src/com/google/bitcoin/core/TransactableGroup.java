package com.google.bitcoin.core;

import java.util.ArrayList;
import java.util.Date;

public class TransactableGroup implements Transactable, Timeable {

	
	public ArrayList<Transactable> list;
	
	public TransactableGroup(){
		list=new ArrayList<Transactable>();
	}
	
	public void add(Transactable t){
		list.add(t);
	}
	
	public GraphTransaction transaction() {
		// TODO Auto-generated method stub
		return list.get(0).transaction();
	}

	@Override
	public GraphWallet fromWallet() {
		return list.get(0).fromWallet();
	}

	@Override
	public boolean isCoinBase() {
		// TODO Auto-generated method stub
		return list.get(0).isCoinBase();
	}
	
	public Date time(){
		return list.get(0).time();
	}

	@Override
	public ArrayList<GraphWallet> toWallets() {
		ArrayList<GraphWallet> w=new ArrayList<GraphWallet>();
		for(Transactable t : list){
			w.addAll(t.toWallets());
		}
		return w;
		
	}

}

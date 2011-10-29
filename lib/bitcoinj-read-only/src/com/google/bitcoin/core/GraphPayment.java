package com.google.bitcoin.core;

import java.util.ArrayList;
import java.util.Date;


import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;


public class GraphPayment implements Timeable, Transactable {
	
	private Relationship relationship;
	private long time;
	private long value;
	private long transaction_id;
	private GraphTransaction transaction;
	
	
	public GraphPayment(Relationship r){
		relationship=r;
		time=(Long) r.getProperty("time");
		value=(Long) r.getProperty("value");
		transaction_id=(Long) r.getProperty("transaction_d");
	}
	
	
	
	public boolean isSelfPayment(){
		return relationship.getStartNode().equals(relationship.getEndNode());
	}

	@Override
	public Date time() {
		return new Date((((long) time) &  0xFFFFFFFFL)*1000L);
		
	}
	
	public long value() {
		return value;
	}
	
	public GraphTransaction transaction() {
		if(transaction==null){
			Node n=relationship.getGraphDatabase().getNodeById(transaction_id);
			transaction=new GraphTransaction(n);
		}
		return transaction;
		
		
	}



	@Override
	public GraphWallet fromWallet() {
		return new GraphWallet(relationship.getStartNode());
	}



	@Override
	public ArrayList<GraphWallet> toWallets() {
		// TODO Auto-generated method stub
		ArrayList<GraphWallet> t=new ArrayList<GraphWallet>();
		t.add(new GraphWallet(relationship.getEndNode()));
		return t;
	}



	@Override
	public boolean isCoinBase() {
		return fromWallet().equals(GraphWallet.coinbaseWallet(relationship.getGraphDatabase()));
	}
	
	
	

}

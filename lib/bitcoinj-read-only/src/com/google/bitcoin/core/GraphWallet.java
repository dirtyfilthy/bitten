package com.google.bitcoin.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

public class GraphWallet implements Noteable, Nodeable, Accountable {

	public final static Comparator<GraphWallet> ADDRESS_COUNT_ORDER = new Comparator<GraphWallet>() {
        public int compare(GraphWallet t1, GraphWallet t2) {
            return -t1.addressCount().compareTo(t2.addressCount());
        }
	};
	
	
	
	public BigInteger cachedTotalIncoming;
	
	public BigInteger cachedTotalOutgoing;
	
	private Node node;
	public String notes="";
	public String label="";
	private static GraphWallet coinbaseWallet;
	
	public GraphWallet(){
		notes="";
		label="";
	}
	
	public static GraphWallet coinbaseWallet(GraphDatabaseService graph){
		if(coinbaseWallet==null){
			Index<Node> namedNodes=graph.index().forNodes("namedNodes");
			IndexHits<Node> hits= namedNodes.get("name", "coinbaseWallet");
			Node node=hits.getSingle();
			if(node!=null){
				coinbaseWallet=new GraphWallet(node);
			}
			else{
				org.neo4j.graphdb.Transaction tx = graph.beginTx();
				try{
					coinbaseWallet=new GraphWallet();
					coinbaseWallet.label="COINBASE";
					coinbaseWallet.save(graph);
					namedNodes.add(coinbaseWallet.node(),"name", "coinbaseWallet");
					tx.success();
					
				}
				finally {
					tx.finish();
				}
			}
		}
		return coinbaseWallet;
		
	}
	
	public boolean isLabelled(){
		return !label.equals("");
	}
	
	public GraphWallet(Node n) {
		node=n;
		notes="";
		label="";
		if(n.hasProperty("label"))
			label=(String) n.getProperty("label");
		if(n.hasProperty("notes"))
			notes=(String) n.getProperty("notes");

	}

	@Override
	public Node node() {
		// TODO Auto-generated method stub
		return node;
	}

	@Override
	public void save(GraphDatabaseService graph) {
		org.neo4j.graphdb.Transaction tx = graph.beginTx();
		try {
		IndexManager index=graph.index();
		Index<Node> walletIndex=index.forNodes("wallets");
		if(node==null){
			node=graph.createNode();
			
		}
		if(node.hasProperty("label")){
			node.removeProperty("label");
			walletIndex.remove(node,"label");
		}
		if(node.hasProperty("notes")){
			node.removeProperty("notes");
		}
		if(!label.equals("")){
			node.setProperty("label",label);
			walletIndex.add(node, "label", label);
		}
		if(!notes.equals("")){
			node.setProperty("notes",notes);
		}
		tx.success();
		}
		finally{
			tx.finish();
		}

	}
	
	public boolean equals(Object rhs){
		return rhs instanceof GraphWallet && ((GraphWallet) rhs).node().equals(node);
	}
	
	public Integer addressCount(){
		if(node.hasProperty("addressCount")){
			return (Integer) node.getProperty("addressCount");
		}
		return 1;
	}
	
	public ArrayList<GraphAddress> addresses(){
		ArrayList<GraphAddress> list=new ArrayList<GraphAddress>();
		Collection<Node> nodeList= node.traverse( Order.BREADTH_FIRST,
			     StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE,
			     GraphRelationships.HAS_ADDRESS, Direction.OUTGOING ).getAllNodes();
		
		for(Node n : nodeList){
			try {
				list.add(new GraphAddress(n));
			} catch (AddressFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Collections.sort(list,Nodeable.NODE_ORDER);
		return list;
	}
	
	public static void processTransaction(GraphTransaction t){
		int time=t.createdAt;
		ArrayList<GraphWallet> wallets=new ArrayList<GraphWallet>();
		for(GraphTransactionInput i : t.inputs){
			if(i.isCoinBase() || i.address()==null){
				continue;
			}
			GraphWallet w=i.address().wallet();
			if(!wallets.contains(w)){
				wallets.add(w);
			}
		}
		
		GraphWallet main;
		if(wallets.size()!=0){
			Collections.sort(wallets,ADDRESS_COUNT_ORDER);
			main=wallets.get(0);
			System.out.println("main wallet merge count="+main.addressCount());
			for(int i=1;i<wallets.size();i++){
				System.out.println("merging wallet "+wallets.get(i));
				main.mergeWallet(wallets.get(i));
			}
		} else {
			main=GraphWallet.coinbaseWallet(t.node().getGraphDatabase());
		}
		
		for(GraphTransactionOutput o : t.outputs){
			
			
			GraphAddress addr=o.address();
			if(addr==null){
				continue;
			}
			GraphWallet w=addr.wallet();
			Relationship r=main.node().createRelationshipTo(w.node(), GraphRelationships.PAYMENT);
			r.setProperty("time", time);
			r.setProperty("value",o.node().getProperty("value"));
			r.setProperty("transaction_id", t.node().getId());
			
			
		}
		
		
		
	}
	
	public void mergeWallet(GraphWallet rhs){
		if(this.equals(rhs)){
			return;
		}
		GraphDatabaseService graph = node.getGraphDatabase();
		IndexManager index=graph.index();
		Index<Node> walletIndex=index.forNodes("wallets");
		Iterator<Relationship> i=rhs.node().getRelationships(Direction.OUTGOING,GraphRelationships.HAS_ADDRESS).iterator();
		ArrayList<Relationship> toDelete=new ArrayList<Relationship>();
		boolean needsSave=false;
		if(!rhs.label.equals("")){
			if(label.equals("")){
				label=rhs.label;
			}
			else{
				label=label+", "+rhs.label;
			}
			needsSave=true;
		}
		if(!rhs.notes.equals("")){
			if(notes.equals("")){
				notes=rhs.notes;
			}
			else{
				notes=notes+", "+rhs.notes;
			}
			needsSave=true;
		}
		if(needsSave){
			save(node.getGraphDatabase());
		}
		while(i.hasNext()){
			Relationship r=i.next();
			Node addr=r.getEndNode();
			toDelete.add(r);
			node.createRelationshipTo(addr, GraphRelationships.HAS_ADDRESS );
		}
		i=rhs.node().getRelationships(Direction.BOTH,GraphRelationships.PAYMENT).iterator();
		while(i.hasNext()){
			Relationship r=i.next();
			Relationship r2;
			Node e=r.getEndNode();
			Node s=r.getStartNode();
			if(s.equals(rhs.node()) && e.equals(rhs.node())){
				r2=node.createRelationshipTo(node, GraphRelationships.PAYMENT);
			}
			else if(s.equals(rhs.node())){
				r2=node.createRelationshipTo(e, GraphRelationships.PAYMENT);
			}
			else {
				r2=s.createRelationshipTo(node, GraphRelationships.PAYMENT);
			}
			r2.setProperty("time", r.getProperty("time"));
			r2.setProperty("value", r.getProperty("value"));
			r2.setProperty("transaction_id", r.getProperty("transaction_id"));
			toDelete.add(r);
		}
		for(Relationship r : toDelete){
			r.delete();
		}
		walletIndex.remove(rhs.node());
		node.setProperty("addressCount",this.addressCount()+rhs.addressCount());
		rhs.node().delete();
		System.out.println("deleting "+rhs.node().getId());
		
	}

	@Override 
	public BigInteger incomingAmount(GraphTransaction t){
		return t.incomingAmountForWallet(this);
	}

	@Override
	public BigInteger outgoingAmount(GraphTransaction t) {
		// TODO Auto-generated method stub
		return t.outgoingAmountForWallet(this);
	}
	
	public ArrayList<GraphTransaction> transactions(){
		return transactions(false);
	}
	
	public ArrayList<GraphTransaction> transactions(boolean precache) {
		ArrayList<GraphTransaction> list=new ArrayList<GraphTransaction>();
		HashMap<Node,Boolean> contains=new HashMap<Node,Boolean>();
		HashMap<Node,Boolean> outWalletMap=new HashMap<Node,Boolean>();
		HashMap<Node,Boolean> inWalletMap=new HashMap<Node,Boolean>();
		long incoming=0;
		long outgoing=0;
		for(Relationship r : node.getRelationships(Direction.BOTH, GraphRelationships.PAYMENT)){
			Node n=node().getGraphDatabase().getNodeById((Long) r.getProperty("transaction_id"));
			inWalletMap.put(r.getStartNode(), true);
			outWalletMap.put(r.getEndNode(), true);
			GraphTransaction t=new GraphTransaction(n,precache);
			t.cachedIsIncoming=true;
			if(!r.getStartNode().equals(r.getEndNode())){
				long v=(Long) r.getProperty("value");
				if(r.getStartNode().equals(node)){
					outgoing+=v;
					t.cachedIsIncoming=false;
				}
				else{
					incoming+=v;
				}
			}
			
			if(!contains.containsKey(t.node())){
				list.add(t);
				contains.put(t.node(), true);
			}
		}
		cachedTotalIncoming=BigInteger.valueOf(incoming);
		cachedTotalOutgoing=BigInteger.valueOf(outgoing);
		Collections.sort(list, Timeable.TIME_ORDER);
		GraphTransaction first=list.get(0);
		if(first!=null){
			first.incomingWallets=inWalletMap.size()-1;
			first.outgoingWallets=outWalletMap.size()-1;
		}
		return list;
		
	}

	public String label() {
		if(!label.equals("")){
			return label;
		}
		return Long.valueOf(node.getId()).toString();
	}

	@Override
	public void setLabel(String label) {
		this.label=label;
		
	}

	@Override
	public void setNotes(String notes) {
		this.notes=notes;
		
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return label;
	}

	@Override
	public String getNotes() {
		// TODO Auto-generated method stub
		return notes;
	}
	
	public void save(){
		save(node().getGraphDatabase());
	}

	@Override
	public BigInteger cachedTotalIncoming() {
		
		return cachedTotalIncoming;
	}

	@Override
	public BigInteger cachedTotalOutgoing() {
		// TODO Auto-generated method stub
		return cachedTotalOutgoing;
	}
		

}

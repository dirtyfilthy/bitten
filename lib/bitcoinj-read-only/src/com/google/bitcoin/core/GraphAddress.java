package com.google.bitcoin.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

public class GraphAddress extends Address implements Noteable, Nodeable, Accountable {

	private Node node;
	public String label="";
	public String notes="";
	private static MapCache<String,GraphAddress> cache=new MapCache<String,GraphAddress>(2000);
	
	
	public GraphAddress(NetworkParameters params, byte[] hash160) throws AddressFormatException {
		super(params, hash160);
		// TODO Auto-generated constructor stub
	}
	
	public GraphAddress(NetworkParameters params, String address)
			throws AddressFormatException {
		super(params, address);
		// TODO Auto-generated constructor stub
	}
	

	public GraphAddress(Node a) throws AddressFormatException {
		super(NetworkParameters.prodNet(),(byte[]) a.getProperty("hash"));
		node=a;
		label="";
		notes="";
		if(node.hasProperty("label")){
			label=(String) a.getProperty("label");
		}
		if(node.hasProperty("notes")){
			notes=(String) a.getProperty("notes");
		}
		
			
		
	}

	@Override
	public Node node() {
		// TODO Auto-generated method stub
		return node;
	}

	public static GraphAddress findOrCreateAddress(GraphDatabaseService graph, NetworkParameters p, String base58hash) throws AddressFormatException{
		if(cache.containsKey(base58hash)){
			return cache.get(base58hash);
		}
		IndexManager index=graph.index();
		Index<Node> addressIndex=index.forNodes("addresses");
		Node a=addressIndex.get("hash", base58hash).getSingle();
		GraphAddress address;
		if(a!=null){
			address=new GraphAddress(a);
		}
		else{
			org.neo4j.graphdb.Transaction tx = graph.beginTx();
			try{
				address=new GraphAddress(p,base58hash);
				address.save(graph);
				tx.success();
			}
			finally{
				tx.finish();
				
			}
		}
		cache.put(base58hash, address);
		return address;
	}
	
	public ArrayList<GraphTransaction> transactions(){
		ArrayList<GraphTransaction> trans=new ArrayList<GraphTransaction>();
		for(Relationship r : node.getRelationships(Direction.INCOMING, GraphRelationships.FROM_ADDRESS)){
			Node n=r.getStartNode();
			Relationship r2=n.getSingleRelationship(GraphRelationships.TRANSACTION_INPUT, Direction.INCOMING);
			n=r2.getStartNode();
			GraphTransaction t=new GraphTransaction(n);
			if(!trans.contains(t)){
				trans.add(t);
			}
		}
		for(Relationship r : node.getRelationships(Direction.INCOMING, GraphRelationships.TO_ADDRESS)){
			Node n=r.getStartNode();
			Relationship r2=n.getSingleRelationship(GraphRelationships.TRANSACTION_OUTPUT, Direction.INCOMING);
			n=r2.getStartNode();
			GraphTransaction t=new GraphTransaction(n);
			if(!trans.contains(t)){
				trans.add(t);
			}
		}
		Collections.sort(trans, Timeable.TIME_ORDER);
		return trans;
		
	}
	
	public GraphWallet wallet(){
		Relationship r=node.getSingleRelationship(GraphRelationships.HAS_ADDRESS, Direction.INCOMING);
		if(r==null){
			return null;
		}
		return new GraphWallet(r.getStartNode());
	}
	
	@Override
	public void save(GraphDatabaseService graph) {
		IndexManager index=graph.index();
		Index<Node> addressIndex=index.forNodes("addresses");
		org.neo4j.graphdb.Transaction tx = graph.beginTx();
		try{
		if(node==null){
			node=graph.createNode();
			node.setProperty("hash",this.getHash160());
			if(!label.equals("")){
				node.setProperty("label",label);
				addressIndex.add(node, "label", label);
			}
			if(!notes.equals("")){
				node.setProperty("notes",notes);
			}
			
			addressIndex.add(node,"hash",this.toString());
			GraphWallet w=new GraphWallet();
			if(!label.equals("")){
				w.label=label;
				w.notes=notes;
			}
			w.save(graph);
			w.node().createRelationshipTo(node, GraphRelationships.HAS_ADDRESS);
		}
		else{
			if(node.hasProperty("label")){
				node.removeProperty("label");
				addressIndex.remove(node,"label");
			}
			if(node.hasProperty("notes")){
				node.removeProperty("notes");
			}
			if(!label.equals("")){
				node.setProperty("label",label);
				System.out.println("setting label "+label);
				addressIndex.add(node, "label", label);
			}
			if(!notes.equals("")){
				System.out.println("setting notes "+notes);
				node.setProperty("notes",notes);
			}
		
			
			GraphWallet w=wallet();
			if(!label.equals("") && w.label.equals("")){
				w.label=label;
				w.notes=notes;
				w.save(graph);
			}
		}
		tx.success();
		} finally {
			tx.finish();
		}
		
		
	}

	@Override
	public BigInteger incomingAmount(GraphTransaction t) {
		// TODO Auto-generated method stub
		return t.incomingAmountForAddress(this);
	}

	@Override
	public BigInteger outgoingAmount(GraphTransaction t) {
		// TODO Auto-generated method stub
		return t.outgoingAmountForAddress(this);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger cachedTotalOutgoing() {
		// TODO Auto-generated method stub
		return null;
	}

}

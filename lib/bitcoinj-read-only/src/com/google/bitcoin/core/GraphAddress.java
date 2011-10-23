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

public class GraphAddress extends Address implements GraphSaveable, Nodeable, Accountable {

	private Node node;
	public String label="";
	public String notes="";
	private static MapCache<String,GraphAddress> cache=new MapCache<String,GraphAddress>(2000);
	
	
	public GraphAddress(NetworkParameters params, byte[] hash160) {
		super(params, hash160);
		// TODO Auto-generated constructor stub
	}
	
	public GraphAddress(NetworkParameters params, String address)
			throws AddressFormatException {
		super(params, address);
		// TODO Auto-generated constructor stub
	}
	

	public GraphAddress(Node a) {
		super(NetworkParameters.prodNet(),(byte[]) a.getProperty("hash"));
		node=a;
		label=(String) a.getProperty("label");
		notes=(String) a.getProperty("notes");
	}

	@Override
	public Node node() {
		// TODO Auto-generated method stub
		return node;
	}

	public static GraphAddress findOrCreateAddress(GraphDatabaseService graph, NetworkParameters p, String base58hash) throws AddressFormatException{
		//if(cache.containsKey(base58hash)){
		//	return cache.get(base58hash);
		//}
		IndexManager index=graph.index();
		Index<Node> addressIndex=index.forNodes("addresses");
		Node a=addressIndex.get("hash", base58hash).getSingle();
		GraphAddress address;
		if(a!=null){
			address=new GraphAddress(a);
		}
		else{
			address=new GraphAddress(p,base58hash);
			address.save(graph);
		}
		// cache.put(base58hash, address);
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
		if(node==null){
			node=graph.createNode();
			node.setProperty("hash",this.getHash160());
			node.setProperty("label",label);
			node.setProperty("notes",notes);
			addressIndex.add(node,"hash",this.toString());
			addressIndex.add(node,"label",label);
			GraphWallet w=new GraphWallet();
			w.save(graph);
			w.node().createRelationshipTo(node, GraphRelationships.HAS_ADDRESS);
		}
		else{
			node.removeProperty("label");
			node.removeProperty("notes");
			addressIndex.remove(node,"hash");
			addressIndex.remove(node,"label");
			addressIndex.add(node,"hash",this.toString());
			addressIndex.add(node,"label",label);
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

}

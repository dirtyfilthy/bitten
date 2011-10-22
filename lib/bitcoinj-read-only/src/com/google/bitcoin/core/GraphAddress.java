package com.google.bitcoin.core;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

public class GraphAddress extends Address implements GraphSaveable, Nodeable {

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
	

	public GraphAddress(NetworkParameters p, Node a) {
		super(p,(byte[]) a.getProperty("hash"));
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
		if(cache.containsKey(base58hash)){
			return cache.get(base58hash);
		}
		IndexManager index=graph.index();
		Index<Node> addressIndex=index.forNodes("addresses");
		Node a=addressIndex.get("hash", base58hash).getSingle();
		GraphAddress address;
		if(a!=null){
			address=new GraphAddress(p, a);
		}
		else{
			address=new GraphAddress(p,base58hash);
			address.save(graph);
		}
		cache.put(base58hash, address);
		return address;
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

}

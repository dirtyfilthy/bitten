package com.google.bitcoin.core;

import java.math.BigInteger;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class GraphTransactionOutput extends TransactionOutput implements
		Nodeable, GraphSaveable, Indexable, AddressListable {
	
	private GraphTransaction transaction;
	private Node node;
	public long index;

	GraphTransactionOutput(NetworkParameters params, byte[] scriptBytes) {
		super(params, scriptBytes);
		// TODO Auto-generated constructor stub
	}

	GraphTransactionOutput(NetworkParameters params, Transaction parent, BigInteger value, Address to) {
        super(params,parent,value,to);
       
    }
	
	 public byte[] getScriptBytes() {
		 if(scriptBytes==null){
			 scriptBytes=(byte[]) node.getProperty("scriptBytes");
		 }
	     return scriptBytes;
	 }
	
	public GraphTransactionOutput(NetworkParameters params, Node n) {
		super(params);
		node=n;
		transaction=null;
		value=BigInteger.valueOf((Long) node.getProperty("value"));
		// lazy load script bytes in getScriptBytes() 
		index=(Long) node.getProperty("index");
	}
	
	
	public GraphTransactionOutput(NetworkParameters params, Transaction parent, byte[] payload,
            int offset) throws ProtocolException {
		super(params, parent, payload, offset);
	}
	
	public Node transactionNode(){
		Relationship t=node.getSingleRelationship(GraphRelationships.TRANSACTION_OUTPUT, Direction.INCOMING);
		if(t==null){
			return null;
		}
		return t.getStartNode();
		
	}
	
	public GraphTransaction transaction(){
		if(transaction==null){
			Node n=transactionNode();
			if(n==null){
				throw new RuntimeException("Can't find parent transaction");
			}
			transaction=new GraphTransaction(params,n);
		}
		return transaction;
		
	}
	
	public GraphAddress address(){
		Relationship r=node.getSingleRelationship(GraphRelationships.TO_ADDRESS, Direction.OUTGOING);
		if(r==null){
			return null;
		}
		return new GraphAddress(r.getEndNode());
}
	

	@Override
	public void save(GraphDatabaseService graph) {
		if(node==null){
			node=graph.createNode();
		}
		node.setProperty("value",value.longValue());
		// node.setProperty("scriptBytes",scriptBytes);
		node.setProperty("index",index);
		try {
			GraphAddress a=GraphAddress.findOrCreateAddress(graph, params, getToAddress().toString());
			node.createRelationshipTo(a.node(), GraphRelationships.TO_ADDRESS);
		} catch (AddressFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public Node node() {
		// TODO Auto-generated method stub
		return node;
	}

	@Override
	public Long index() {
		// TODO Auto-generated method stub
		return index;
	}

	@Override
	public BigInteger value() {
		// TODO Auto-generated method stub
		return value;
	}
	
	public void save(){
		save(node().getGraphDatabase());
	}

}

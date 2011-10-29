package com.google.bitcoin.core;

import java.math.BigInteger;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

public class GraphTransactionInput extends TransactionInput implements
		GraphSaveable, Nodeable, Indexable, AddressListable {
	
	private Node node;
	public BigInteger value;
	public BigInteger coinbaseValue=BigInteger.ZERO;
	public int index;
	private GraphTransaction transaction;

	GraphTransactionInput(NetworkParameters params,
			Transaction parentTransaction, byte[] scriptBytes) {
		super(params, parentTransaction, scriptBytes);
		
		// TODO Auto-generated constructor stub
	}
	
	public GraphTransactionInput(NetworkParameters params, Transaction parentTransaction,
            byte[] payload, int offset) throws ProtocolException {
		super(params, parentTransaction, payload, offset);
	}
	
	public GraphTransactionInput(NetworkParameters params, Transaction parentTransaction, TransactionOutput output) {
		super(params,parentTransaction,output);
	}
	
	public TransactionOutPoint outpoint(){
		if(outpoint!=null){
			return outpoint;
		}
		Relationship prevOutputRelationship=node.getSingleRelationship(GraphRelationships.PREV_OUTPUT,Direction.OUTGOING);
		if(prevOutputRelationship!=null){
			Node o=prevOutputRelationship.getEndNode();
			Relationship t=o.getSingleRelationship(GraphRelationships.TRANSACTION_OUTPUT, Direction.INCOMING);
			if(t==null){
				throw new RuntimeException("Unable to load previous output node transaction");
			}
			Node trans=t.getStartNode();
			try {
				outpoint=new TransactionOutPoint(params, (byte[]) trans.getProperty("hash"),(Long) t.getProperty(("index")));
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException("Should never happen!!!");
			}
		}
		return outpoint;
	}
	
	public byte[] getScriptBytes() {
		 if(scriptBytes==null){
			 scriptBytes=(byte[]) node.getProperty("scriptBytes");
		 }
	     return scriptBytes;
	 }
	
	public GraphTransactionInput(NetworkParameters params, Node n){
		super(params);
		node=n;
		
		// outpoint is lazy loaded by the outpoint() method, most of the time we don't need it 
		
		value=BigInteger.valueOf((Long) node.getProperty("value"));
		// sequence=(Long) node.getProperty("sequence"); #never use this
		coinbase=(Boolean) node.hasProperty("coinbase");
		// scriptBytes=(byte[]) node.getProperty("scriptBytes"); lazy load script bytes instead
		if(node.hasProperty("index")){
			index=(Integer) node.getProperty("index");
		}
		else{
			index=0;
		}
		
	}
	
	public GraphAddress address(){
			Relationship r=node.getSingleRelationship(GraphRelationships.FROM_ADDRESS, Direction.OUTGOING);
			if(r==null){
				return null;
			}
			try {
				GraphAddress a=new GraphAddress(r.getEndNode());
				return a;
			} catch (AddressFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	}
	
	
	public Node transactionNode(){
		Relationship t=node.getSingleRelationship(GraphRelationships.TRANSACTION_INPUT, Direction.INCOMING);
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

	@Override
	public Node node() {
		// TODO Auto-generated method stub
		return node;
	}

	@Override
	public void save(GraphDatabaseService graph) {
		IndexManager index=graph.index();
		Index<Node> transactionIndex=index.forNodes("transactions");
		if(node==null){
			node=graph.createNode();
		}
		// node.setProperty("sequence", sequence);
		if(isCoinBase()){
			node.setProperty("coinbase", isCoinBase());
		}
		// node.setProperty("scriptBytes", scriptBytes);
		
		if(!isCoinBase()){
			Node transactionNode=GraphTransaction.findTransactionNode(graph, Utils.bytesToHexString(outpoint.hash));
			if(transactionNode==null){
				throw new RuntimeException("Can't find transaction when looking for outpoint");
			}
			Iterable<Relationship> i=transactionNode.getRelationships(Direction.OUTGOING, GraphRelationships.TRANSACTION_OUTPUT);
			Node previousOutpoint = null;
			int index_i=((int) (outpoint.index));
			for(Relationship r : i){
				Node e=r.getEndNode();
				boolean hasIndex=e.hasProperty("index");
				if((index_i==0 && !hasIndex) || (index_i!=0 && hasIndex && e.getProperty("index").equals(index_i))){
					previousOutpoint=e;
					break;
				}
			}
			if(previousOutpoint==null){
				throw new RuntimeException("Can't find output index  when looking for outpoint - "+Utils.bytesToHexString(outpoint.hash)+" : "+outpoint.index);
			}
			node.createRelationshipTo(previousOutpoint, GraphRelationships.PREV_OUTPUT);
			node.setProperty("value",(Long) previousOutpoint.getProperty("value"));
			Relationship r=previousOutpoint.getSingleRelationship(GraphRelationships.TO_ADDRESS, Direction.OUTGOING);
			if(r!=null){
				Node addr=r.getEndNode();
				node.createRelationshipTo(addr, GraphRelationships.FROM_ADDRESS);
			}
		}
		else{
			node.setProperty("value",coinbaseValue.longValue());
		}
		if(this.index!=0){
			node.setProperty("index",this.index);
		}
	}

	@Override
	public Integer index() {
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

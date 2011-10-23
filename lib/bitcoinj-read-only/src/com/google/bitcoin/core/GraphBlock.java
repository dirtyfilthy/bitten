package com.google.bitcoin.core;

import java.math.BigInteger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;


public class GraphBlock extends StoredBlock implements Nodeable, GraphSaveable {
	private Node node;
	private NetworkParameters params;
	protected GraphBlockHeader header;
	
	
	
	public GraphBlock(GraphBlockHeader header, BigInteger chainWork, int height) {
		super(header, chainWork, height);
		this.header=header;
	}
	
	public void save(GraphDatabaseService graph){
		IndexManager index=graph.index();
		Index<Node> blockIndex=index.forNodes("blocks");
		if(node==null){
			node=graph.createNode();
		}
		node.setProperty("height", height);
		node.setProperty("chainWork", chainWork.toByteArray());
		node.setProperty("hash",header.getHash());
		node.setProperty("version", header.getVersion());
		node.setProperty("prevBlockHash", header.getPrevBlockHash());
		node.setProperty("merkleRoot", header.getMerkleRoot());
		node.setProperty("time", header.getTime());
		node.setProperty("difficultyTarget",header.getDifficultyTarget());
		node.setProperty("nonce",header.getNonce());
		blockIndex.add(node, "hash", header.getHashAsString());
		blockIndex.add(node, "height", height);
		for(GraphTransaction t : header.transactions){
			t.createdAt=header.getTime();
			t.coinbaseValue=getSubsidy();
			t.save(graph);
			Relationship r=node.createRelationshipTo(t.node(), GraphRelationships.HAS_TRANSACTION);
			r.setProperty("index",t.index);
			GraphWallet.processTransaction(t);
		}
	} 
	
	 public boolean equals(Object other) {
	        if (!(other instanceof GraphBlock)) return false;
	        GraphBlock o = (GraphBlock) other;
	        return o.header.equals(header) && o.chainWork.equals(chainWork) && o.height == height;
	    }
	
	
	public GraphBlockHeader getHeader(){
		return header;
	}
	

	public GraphBlock(NetworkParameters n, Node node) {
		super();
		params=n;
		this.header=new GraphBlockHeader(params);
		this.node=node;
		this.chainWork=new BigInteger((byte[]) node.getProperty("chainWork"));
		this.height=(Integer) node.getProperty("height");
		header.setVersion( (Long) node.getProperty("version"));
		header.setPrevBlockHash((byte[]) node.getProperty("prevBlockHash"));
		header.setMerkleRoot((byte[]) node.getProperty("merkleRoot"));
		header.setTime( (Long) node.getProperty("time"));
		header.setDifficultyTarget((Long) node.getProperty("difficultyTarget"));
		header.setNonce((Long) node.getProperty("nonce"));
	}
	
	public BigInteger getSubsidy(){
		return Utils.FIDDY_COIN.shiftLeft(height/210000);
	}
	
	public GraphBlock build(Block block) throws VerificationException {
        // Stored blocks track total work done in this chain, because the canonical chain is the one that represents
        // the largest amount of work done not the tallest.
        BigInteger chainWork = this.chainWork.add(block.getWork());
        int height = this.height + 1;
        return new GraphBlock((GraphBlockHeader) block, chainWork, height);
    }

	@Override
	public Node node() {
		// TODO Auto-generated method stub
		return node;
	}

}

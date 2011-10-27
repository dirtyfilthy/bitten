package com.google.bitcoin.core;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

public class GraphBlockChainNodeAdapter implements Nodeable {

	
	private Node node;
	
	
	private IndexManager index;
	private GraphDatabaseService graph;
	private NetworkParameters params;
	
	
	
	GraphBlockChainNodeAdapter(NetworkParameters n, GraphDatabaseService graph){
		this.graph=graph;
		this.index=graph.index();
		this.params=n;
		Index<Node> namedNodes=index.forNodes("namedNodes");
		IndexHits<Node> hits= namedNodes.get("name", "blockChain");
		node=hits.getSingle();
		if(node==null){
			node=graph.createNode();
			namedNodes.add(node, "name", "blockChain");
		}
		Node genesis= namedNodes.get("name", "genesis").getSingle();
		if(genesis==null){
			Block genesisHeader = params.genesisBlock;
			GraphBlock storedGenesis;
			try {
				storedGenesis = new GraphBlock(
						new GraphBlockHeader(params, genesisHeader.bitcoinSerialize()), genesisHeader.getWork(), 0);
				System.out.println("genesis "+storedGenesis.getHeader().getHashAsString());
				storedGenesis.save(graph);
				namedNodes.add(storedGenesis.node(), "name", "genesis");
				setChainHead(storedGenesis);
				
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (VerificationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		
	}
	
	public Node node() {
		// TODO Auto-generated method stub
		return node;
	}
	
	private Relationship getChainHeadRelationship(){
		Relationship current=node.getSingleRelationship(GraphRelationships.CHAIN_HEAD, Direction.OUTGOING );
		return current;
	}
	
	public void add(GraphBlock graphBlock){
		graphBlock.save(graph);
		Relationship hasBlock=node.createRelationshipTo(graphBlock.node(),GraphRelationships.HAS_BLOCK);
		hasBlock.setProperty("time",graphBlock.getHeader().getTime());
		hasBlock.setProperty("height",graphBlock.getHeight());
	}
	
	public GraphBlock get(byte[] hash){
		Index<Node> blockIndex=index.forNodes("blocks");
		Node n=blockIndex.get("hash", Utils.bytesToHexString(hash)).getSingle();
		if(n==null){
			return null;
		}
		return new GraphBlock(params,n);
	}
	
	public GraphBlock getChainHead(){
		Relationship current=getChainHeadRelationship();
		if(current==null){
			return null;
		}
		return new GraphBlock(params,current.getEndNode());
	}
	
	public void setChainHead(GraphBlock g){
		Relationship current=getChainHeadRelationship();
		if(current!=null){
			current.delete();
		}
		node.createRelationshipTo(g.node(), GraphRelationships.CHAIN_HEAD );
	}
	
	
	

}

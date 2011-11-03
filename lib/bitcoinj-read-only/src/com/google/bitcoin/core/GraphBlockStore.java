package com.google.bitcoin.core;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.ibex.nestedvm.util.Seekable.InputStream;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager; 
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.index.IndexStore;
import org.neo4j.kernel.impl.traversal.TraversalDescriptionImpl;

public class GraphBlockStore implements BlockStore {
	
	private GraphDatabaseService graph;
	private GraphBlockChainNodeAdapter graphBlockChain;
	private IndexManager indexer;
	private Index<Node> namedNodes;
	private NetworkParameters params;
	
	
	public GraphBlockStore(NetworkParameters n, String path){ 
		         Properties props = new Properties();
		         try
		         {
		             java.io.InputStream stream = this.getClass().getResourceAsStream("/neo4j.properties");
		             try
		             {
		                 props.load( stream );
		             }
		             finally
		             {
		                 stream.close();
		             }
		         }
		         catch ( Exception e )
		         {
		             throw new IllegalArgumentException( "Unable to load");
		         }
		         Set<Entry<Object,Object>> entries = props.entrySet();
		         Map<String,String> stringProps = new HashMap<String,String>();
		         for ( Entry<Object,Object> entry : entries )
		         {
		             String key = (String) entry.getKey();
		             String value = (String) entry.getValue();
		             stringProps.put( key, value );
		         }
		
		graph = new EmbeddedGraphDatabase( path,stringProps );
		indexer = graph.index();
		params = n;
		
		org.neo4j.graphdb.Transaction tx = graph.beginTx();
		try
		{
			graphBlockChain = new GraphBlockChainNodeAdapter(params, graph);
			
			
		    tx.success();
		}
		finally
		{
		    tx.finish();
		}
		Block genesisHeader = params.genesisBlock;
		GraphBlockHeader h;
		try {
			h = new GraphBlockHeader(params, genesisHeader.bitcoinSerialize());
			GraphBlock h2=get(h.hash);
			if(h2==null){
				throw new  RuntimeException("can't find genesis block");
			}
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BlockStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		registerShutdownHook( graph );
	}

	@Override
	public void put(GraphBlock block) throws BlockStoreException {
		org.neo4j.graphdb.Transaction tx = graph.beginTx();
		try
		{
			GraphBlock graphBlock=(GraphBlock) block;
			graphBlockChain.add(graphBlock);
			
			
		    tx.success();
		}
		finally
		{
		    tx.finish();
		}


	}
	
	

	@Override
	public GraphBlock get(byte[] hash) throws BlockStoreException {
		// TODO Auto-generated method stub
		return graphBlockChain.get(hash);
	}

	@Override
	public GraphBlock getChainHead() throws BlockStoreException {
		// TODO Auto-generated method stub
		return graphBlockChain.getChainHead();
	}

	@Override
	public void setChainHead(GraphBlock chainHead) throws BlockStoreException {
		org.neo4j.graphdb.Transaction tx = graph.beginTx();
		try
		{
			GraphBlock graphBlock=(GraphBlock) chainHead;
			graphBlockChain.setChainHead(graphBlock);
			
			
		    tx.success();
		}
		finally
		{
		    tx.finish();
		}


	}
	
	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running example before it's completed)
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	        }
	    } );
	}
	
	public GraphDatabaseService graph(){
		return graph;
	}
	
	public GraphAddress findOrCreateAddress(String base58hash) throws AddressFormatException{
		return GraphAddress.findOrCreateAddress(graph, params, base58hash);
	}

	public StoredBlock getByHeight(int i) {
		Index<Node> blockIndex=indexer.forNodes("blocks");
		Node n=blockIndex.get("height", i).getSingle();
		if(n==null){
			return null;
		}
		return new GraphBlock(params,n); 
	}

}

package com.google.bitcoin.core;

import static com.google.bitcoin.core.Utils.doubleDigest;
import static com.google.bitcoin.core.Utils.reverseBytes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

public class GraphTransaction extends Transaction implements GraphSaveable,
		Nodeable, Timeable {
	
	public final static Comparator<Relationship> R_INDEX_ORDER = new Comparator<Relationship>() {
        public int compare(Relationship r1, Relationship r2) {
            Integer i1=(!r1.hasProperty("index") ? 0 : (Integer) r1.getProperty("index"));
            Integer i2=(!r2.hasProperty("index") ? 0 : (Integer) r2.getProperty("index"));
        	return i1.compareTo(i2);
        }
	};
	
	

	private static MapCache<String, Node> cache=new MapCache<String,Node>(2000);
	private Node node;
	public int createdAt;
	public int index;
	public int outgoingWallets=0;
	public int incomingWallets=0;
	public BigInteger coinbaseValue;
	public ArrayList<GraphTransactionInput> inputs;
	public ArrayList<GraphTransactionOutput> outputs;
	public Boolean cachedIsIncoming=null;
	public GraphTransaction(NetworkParameters params) {
		super(params);
		// TODO Auto-generated constructor stub
	}

	public GraphTransaction(NetworkParameters params, byte[] payloadBytes)
			throws ProtocolException {
		super(params, payloadBytes);
		// TODO Auto-generated constructor stub
	}

	public GraphTransaction(NetworkParameters params, byte[] payload, int offset)
			throws ProtocolException {
		super(params, payload, offset);
		// TODO Auto-generated constructor stub
	}
	
	public GraphTransaction(NetworkParameters params, Node n){
		super(params);
		this.node=n;
		/// this.version=(Long) node.getProperty("version");
		/// this.lockTime=(Long) node.getProperty("lockTime");
		this.createdAt=(Integer) node.getProperty("createdAt");
		
	}
	
	public ArrayList<GraphWallet> getIncomingWallets(){
		ArrayList<GraphWallet> w=new ArrayList<GraphWallet>();
		GraphWallet w2;
		for(GraphTransactionInput t : inputs){
			if(t.address()!=null){
				w2=t.address().wallet();
				if(!w.contains(w2)){
					w.add(w2);
				}
			}
		}
		return w;
	}
	

	public ArrayList<GraphWallet> getOutgoingWallets(){
		ArrayList<GraphWallet> w=new ArrayList<GraphWallet>();
		GraphWallet w2;
		for(GraphTransactionOutput t : outputs){
			if(t.address()!=null){
				w2=t.address().wallet();
				if(!w.contains(w2)){
					w.add(w2);
				}
			}
		}
		return w;
	}
	
	
	public static Node findTransactionNode(GraphDatabaseService graph, String hash){
		if(cache.containsKey(hash)){
			return cache.get(hash);
		}
		IndexManager index=graph.index();
		Index<Node> transactionIndex=index.forNodes("transactions");
		Node n=transactionIndex.get("hash", hash).getSingle();
		cache.put(hash, n);
		return n;
		
	}
	
	
	

	public BigInteger incomingAmountForAddress(GraphAddress a){
		BigInteger amount=BigInteger.ZERO;
		for(TransactionOutput o : outputs){
			GraphTransactionOutput o2=(GraphTransactionOutput) o;
			if(o2.address().equals(a)){
				amount=amount.add(o2.value);
			}
		}
		return amount;
	}
	
	public BigInteger outgoingAmountForAddress(GraphAddress a){
		BigInteger amount=BigInteger.ZERO;
		for(TransactionInput i : inputs){
			GraphTransactionInput i2=(GraphTransactionInput) i;
			if(i2.isCoinBase()){
				continue;
			}
			if(i2.address()!=null && i2.address().equals(a)){
				amount=amount.add(i2.value);
			}
		}
		return amount;
	}
	
	public Relationship paymentFor(GraphTransactionOutput o){
		GraphWallet src;
		GraphWallet dst=o.address().wallet();
		for(Relationship r : dst.node().getRelationships(GraphRelationships.PAYMENT, Direction.INCOMING)){
			if(((Long) r.getProperty("transaction_id")).equals(node().getId())){
				return r;
			}
		}
		return null;
		
		
	}
	
	
	
	public BigInteger incomingAmountForWallet(GraphWallet w){
		BigInteger amount=BigInteger.ZERO;
		for(GraphTransactionOutput o : outputs){
			if(o.address().wallet().equals(w)){
				amount=amount.add(o.value);
			}
		}
		return amount;
	}
	
	public BigInteger outgoingAmountForWallet(GraphWallet w){
		BigInteger amount=BigInteger.ZERO;
		if(this.isCoinBase() || !inputs.get(0).address().wallet().equals(w)){
			return amount;
		}
		for(GraphTransactionInput o : inputs){
			if(o.address().wallet().equals(w)){
				amount=amount.add(o.value);
			}
		}
		return amount;
	}


	
	private void load(Node n, boolean precache){
		this.node=n;
		ArrayList<Relationship> r_inputs=new ArrayList<Relationship>();
		ArrayList<Relationship> r_outputs=new ArrayList<Relationship>();
		/*
		we never use these
		this.version=(Long) node.getProperty("version");
		this.lockTime=(Long) node.getProperty("lockTime");
		
		*/
		
		this.createdAt=(Integer) node.getProperty("time");
		
		
		inputs=new ArrayList<GraphTransactionInput>();
		outputs=new ArrayList<GraphTransactionOutput>();
		for(Relationship r : node.getRelationships(Direction.OUTGOING, GraphRelationships.TRANSACTION_INPUT)){
			r_inputs.add(r);
		}
		for(Relationship r : node.getRelationships(Direction.OUTGOING, GraphRelationships.TRANSACTION_OUTPUT)){
			r_outputs.add(r);
		}
		Collections.sort(r_inputs,R_INDEX_ORDER);
		Collections.sort(r_outputs,R_INDEX_ORDER);
		for(Relationship r : r_inputs){
			Node n2=r.getEndNode();
			GraphTransactionInput in=new GraphTransactionInput(NetworkParameters.prodNet(),n2);
			if(precache){
				if(in.address()!=null){
					in.address().wallet();
				}
			}
			inputs.add(in);
		}
		for(Relationship r : r_outputs){
			Node n2=r.getEndNode();
			GraphTransactionOutput out=new GraphTransactionOutput(NetworkParameters.prodNet(),n2);
			if(precache){
				if(out.address()!=null){
					out.address().wallet();
				}
			}
			outputs.add(out);
		}
		
	}
	
	public GraphTransaction(Node n, boolean precache){
		super(NetworkParameters.prodNet());
		load(n,precache);
	}
	
	public GraphTransaction(Node n){
		super(NetworkParameters.prodNet());
		load(n,false);
	}
	
	public boolean equals(Object o){
		return o instanceof GraphTransaction && ((GraphTransaction) o).node().equals(node);
	}


	@Override
	public Node node() {
		// TODO Auto-generated method stub
		return node;
	}

	@Override
	public void save(GraphDatabaseService graph) {
		if(node==null){
			node=graph.createNode();
		}
		// node.setProperty("version", this.version);
		// node.setProperty("lockTime", this.lockTime);
		node.setProperty("time", this.createdAt);
		node.setProperty("hash", this.getHash().hash);
		if(this.index!=0){
			node.setProperty("index", this.index);
		}
		IndexManager index=graph.index();
		Index<Node> transactionsIndex=index.forNodes("transactions");
		transactionsIndex.remove(node);
		transactionsIndex.add(node, "hash", this.getHash().toString());
		int i=0;
		for(GraphTransactionInput in : inputs){
			in.index=i;
			in.save(graph);
			if(in.node()==null){
				continue;
			}
			Relationship r=node.createRelationshipTo(in.node(), GraphRelationships.TRANSACTION_INPUT);
			if(i!=0){
				r.setProperty("index", in.index);
			}
			i++;
		}
		i=0;
		for(GraphTransactionOutput out : outputs){
			out.save(graph);
			Relationship r=node.createRelationshipTo(out.node(), GraphRelationships.TRANSACTION_OUTPUT);
			if(i!=0){
				r.setProperty("index", i);
			}
			i++;
		}
	}
	
	 /**
     * A coinbase transaction is one that creates a new coin. They are the first transaction in each block and their
     * value is determined by a formula that all implementations of BitCoin share. In 2011 the value of a coinbase
     * transaction is 50 coins, but in future it will be less. A coinbase transaction is defined not only by its
     * position in a block but by the data in the inputs.
     */
    public boolean isCoinBase() {
        return inputs.get(0).isCoinBase();
    }
	

    void parse() throws ProtocolException {
        version = readUint32();
        // First come the inputs.
        long numInputs = readVarInt();
        inputs = new ArrayList<GraphTransactionInput>((int)numInputs);
        for (int i = 0; i < numInputs; i++) {
            GraphTransactionInput input = new GraphTransactionInput(params, this, bytes, cursor);
            input.index=i;
            inputs.add(input);
            cursor += input.getMessageSize();
        }
        // Now the outputs
        long numOutputs = readVarInt();
        outputs = new ArrayList<GraphTransactionOutput>((int)numOutputs);
        for (int i = 0; i < numOutputs; i++) {
            GraphTransactionOutput output = new GraphTransactionOutput(params, this, bytes, cursor);
            output.index=i;
            outputs.add(output);
            cursor += output.getMessageSize();
        }
        lockTime = readUint32();
        
        // Store a hash, it may come in useful later (want to avoid reserialization costs).
        hash = new Sha256Hash(reverseBytes(doubleDigest(bytes, offset, cursor - offset)));
    }
    
    public int hashCode() {
    	return node().hashCode();
    }

	@Override
	public Date time() {
		
		return new Date((((long) createdAt) &  0xFFFFFFFFL)*1000L);
	}
	
	public void save(){
		save(node().getGraphDatabase());
	}

}

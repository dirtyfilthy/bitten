package com.google.bitcoin.core;

import static com.google.bitcoin.core.Utils.doubleDigest;
import static com.google.bitcoin.core.Utils.reverseBytes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

public class GraphTransaction extends Transaction implements GraphSaveable,
		Nodeable, Timeable {

	
	private Node node;
	public long createdAt;
	public long index;
	public BigInteger coinbaseValue;
	public ArrayList<GraphTransactionInput> inputs;
	public ArrayList<GraphTransactionOutput> outputs;
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
		this.version=(Long) node.getProperty("version");
		this.lockTime=(Long) node.getProperty("lockTime");
		this.createdAt=(Long) node.getProperty("createdAt");
		
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
			if(i2.address().equals(a)){
				amount=amount.add(i2.value);
			}
		}
		return amount;
	}
	
	public BigInteger incomingAmountForWallet(GraphWallet w){
		BigInteger amount=BigInteger.ZERO;
		for(TransactionOutput o : outputs){
			GraphTransactionOutput o2=(GraphTransactionOutput) o;
			if(o2.address().wallet().equals(w)){
				amount=amount.add(o2.value);
			}
		}
		return amount;
	}
	
	public BigInteger outgoingAmountForWallet(GraphWallet w){
		BigInteger amount=BigInteger.ZERO;
		for(TransactionInput o : inputs){
			GraphTransactionInput o2=(GraphTransactionInput) o;
			if(o2.address().wallet().equals(w)){
				amount=amount.add(o2.value);
			}
		}
		return amount;
	}


	
	public GraphTransaction(Node n){
		super(NetworkParameters.prodNet());
		this.node=n;
		this.version=(Long) node.getProperty("version");
		this.lockTime=(Long) node.getProperty("lockTime");
		this.createdAt=(Long) node.getProperty("createdAt");
		inputs=new ArrayList<GraphTransactionInput>();
		outputs=new ArrayList<GraphTransactionOutput>();
		for(Relationship r : node.getRelationships(Direction.OUTGOING, GraphRelationships.TRANSACTION_INPUT)){
			Node n2=r.getEndNode();
			inputs.add(new GraphTransactionInput(NetworkParameters.prodNet(),n2));
		}
		for(Relationship r : node.getRelationships(Direction.OUTGOING, GraphRelationships.TRANSACTION_OUTPUT)){
			Node n2=r.getEndNode();
			outputs.add(new GraphTransactionOutput(NetworkParameters.prodNet(),n2));
		}
		Collections.sort(inputs, Indexable.INDEX_ORDER);
		Collections.sort(outputs, Indexable.INDEX_ORDER);
		
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
		node.setProperty("version", this.version);
		node.setProperty("lockTime", this.lockTime);
		node.setProperty("createdAt", this.createdAt);
		node.setProperty("hash", this.getHash().hash);
		node.setProperty("index", this.index);
		IndexManager index=graph.index();
		Index<Node> transactionsIndex=index.forNodes("transactions");
		transactionsIndex.remove(node);
		transactionsIndex.add(node, "hash", this.getHash().toString());
		int i=0;
		for(GraphTransactionInput in : inputs){
			in.coinbaseValue=coinbaseValue;
			in.index=Long.valueOf(i);
			in.save(graph);
			Relationship r=node.createRelationshipTo(in.node(), GraphRelationships.TRANSACTION_INPUT);
			r.setProperty("index", in.index);
			i++;
		}
		i=0;
		for(GraphTransactionOutput out : outputs){
			out.index=i;
			out.save(graph);
			Relationship r=node.createRelationshipTo(out.node(), GraphRelationships.TRANSACTION_OUTPUT);
			r.setProperty("index", out.index);
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
        for (long i = 0; i < numInputs; i++) {
            GraphTransactionInput input = new GraphTransactionInput(params, this, bytes, cursor);
            input.index=i;
            inputs.add(input);
            cursor += input.getMessageSize();
        }
        // Now the outputs
        long numOutputs = readVarInt();
        outputs = new ArrayList<GraphTransactionOutput>((int)numOutputs);
        for (long i = 0; i < numOutputs; i++) {
            GraphTransactionOutput output = new GraphTransactionOutput(params, this, bytes, cursor);
            output.index=i;
            outputs.add(output);
            cursor += output.getMessageSize();
        }
        lockTime = readUint32();
        
        // Store a hash, it may come in useful later (want to avoid reserialization costs).
        hash = new Sha256Hash(reverseBytes(doubleDigest(bytes, offset, cursor - offset)));
    }

	@Override
	public Date time() {
		
		return new Date(createdAt*1000);
	}

}

package com.google.bitcoin.core;

import static com.google.bitcoin.core.Utils.doubleDigest;
import static com.google.bitcoin.core.Utils.reverseBytes;

import java.math.BigInteger;
import java.util.ArrayList;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

public class GraphTransaction extends Transaction implements GraphSaveable,
		Nodeable {

	
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
			Relationship r=node.createRelationshipTo(out.node(), GraphRelationships.TRANSACTION_INPUT);
			r.setProperty("index", out.index);
			i++;
		}
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

}

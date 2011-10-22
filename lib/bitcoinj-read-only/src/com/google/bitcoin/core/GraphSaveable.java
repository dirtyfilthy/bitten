package com.google.bitcoin.core;

import org.neo4j.graphdb.GraphDatabaseService;

public interface GraphSaveable {
	
	public void save(GraphDatabaseService graph);

}

package com.google.bitcoin.core;

import java.util.Comparator;

import org.neo4j.graphdb.Node;

public interface Nodeable {
	
	public final static Comparator<Nodeable> NODE_ORDER = new Comparator<Nodeable>() {
        public int compare(Nodeable t1, Nodeable t2) {
            return Long.valueOf(t1.node().getId()).compareTo(Long.valueOf(t2.node().getId()));
        }
	};
	
	public Node node();

}

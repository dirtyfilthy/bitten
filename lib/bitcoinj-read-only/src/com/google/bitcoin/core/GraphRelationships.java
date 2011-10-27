package com.google.bitcoin.core;

import org.neo4j.graphdb.RelationshipType;

public enum GraphRelationships implements RelationshipType {
	CHAIN_HEAD,
	HAS_BLOCK,
	TRANSACTION_INPUT,
	TRANSACTION_OUTPUT,
	PREV_OUTPUT,
	HAS_TRANSACTION,
	FROM_ADDRESS,
	TO_ADDRESS,
	HAS_ADDRESS,
	PAYMENT,
}

package com.google.bitcoin.core;

import java.util.Comparator;

public interface Indexable {
	
	public final static Comparator<Indexable> INDEX_ORDER = new Comparator<Indexable>() {
        public int compare(Indexable t1, Indexable t2) {
            return t1.index().compareTo(t2.index());
        }
	};
	
	public Integer index();
	
	
	
}

package com.google.bitcoin.core;

import java.util.Comparator;

public interface Timeable {
	public final static Comparator<Timeable> TIME_ORDER = new Comparator<Timeable>() {
        public int compare(Timeable t1, Timeable t2) {
            return t1.time().compareTo(t2.time());
        }
	};
	
	public java.util.Date time();

}

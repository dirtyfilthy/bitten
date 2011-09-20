package com.google.bitcoin.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapCache<K, V> extends LinkedHashMap<K, V> {
	private int maxCapacity=100;
	
	public MapCache() {
		// TODO Auto-generated constructor stub
	}

	public MapCache(int initialCapacity) {	
		super(initialCapacity);    
		maxCapacity=initialCapacity;
		
		// TODO Auto-generated constructor stub
	}

	public MapCache(Map<? extends K, ? extends V> m) {
		super(m);
		// TODO Auto-generated constructor stub
	}

	public MapCache(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		maxCapacity=initialCapacity;   
		// TODO Auto-generated constructor stub
	}

	public MapCache(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor, accessOrder);
		maxCapacity=initialCapacity;   
		// TODO Auto-generated constructor stub
	}
	@Override
	protected boolean removeEldestEntry(Map.Entry eldest) {
		return size() > maxCapacity;
	}
}
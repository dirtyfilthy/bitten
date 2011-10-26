package com.google.bitcoin.core;

public interface Noteable extends GraphSaveable {
	
	public void setLabel(String label);
	
	public void setNotes(String notes);
	
	public String getLabel();
	
	public String getNotes();

}

package net.dirtyfilthy.Bitten;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.tree.TreeNode;

import org.jdesktop.swingx.treetable.TreeTableNode;

import com.google.bitcoin.core.GraphTransaction;
import com.google.bitcoin.core.Transactable;

public class RootTransactableTreeNode implements TreeTableNode {

	public  ArrayList<Transactable> list;
	private ArrayList<TreeTableNode> children=new ArrayList<TreeTableNode>();
	private HashMap<Transactable,TransactableTreeNode> transactableMap;
	RootTransactableTreeNode(ControlPanel p, ArrayList<Transactable> list){
		this.list=list;
		transactableMap=new HashMap<Transactable,TransactableTreeNode>();
		for(Transactable t : list){
			TransactableTreeNode n=new TransactableTreeNode(this,p,t);
			transactableMap.put(t, n);
			children.add(n);
		}
	}
	
	public TransactableTreeNode findNodeByTransactabt(Transactable t){
		return transactableMap.get(t);
	}
	
	@Override
	public boolean getAllowsChildren() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getChildCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public int getIndex(TreeNode arg0) {
		// TODO Auto-generated method stub
		return children.indexOf(arg0);
	}

	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Enumeration<? extends TreeTableNode> children() {
		// TODO Auto-generated method stub
		return java.util.Collections.enumeration(children);
	}

	@Override
	public TreeTableNode getChildAt(int arg0) {
		// TODO Auto-generated method stub
		return children.get(arg0);
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 7;
	}

	@Override
	public TreeTableNode getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getUserObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValueAt(int arg0) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public boolean isEditable(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setUserObject(Object arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValueAt(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

}

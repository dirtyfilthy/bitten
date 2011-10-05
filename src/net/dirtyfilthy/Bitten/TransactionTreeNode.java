package net.dirtyfilthy.Bitten;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import org.jdesktop.swingx.treetable.TreeTableNode;

import com.google.bitcoin.core.SqlTransaction;
import com.google.bitcoin.core.SqlTransactionInput;
import com.google.bitcoin.core.SqlTransactionOutput;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;

public class TransactionTreeNode implements TreeTableNode {
	private SqlTransaction transaction;
	private boolean visible=false;
	ArrayList<TreeTableNode> children=new ArrayList<TreeTableNode>();
	TreeTableNode parent;
	TransactionTreeNode(TreeTableNode parent, SqlTransaction t){
		transaction=t;
		this.parent=parent;
		ArrayList<TransactionInput> inputs=transaction.inputs;
		ArrayList<TransactionOutput> outputs=transaction.outputs;
		int size=inputs.size() > outputs.size() ? inputs.size() : outputs.size();
		int c=0;
		
		SqlTransactionInput i;
		SqlTransactionOutput o;
		while(c<size){
			TransactionRowTreeNode n=new TransactionRowTreeNode(this);
			i=null;
			o=null;
			if(c<inputs.size()){
				i=(SqlTransactionInput) inputs.get(c);
			}
			if(c<outputs.size()){
				o=(SqlTransactionOutput) outputs.get(c);
			}
			n.input=i;
			n.output=o;
			children.add(n);
			c++;
		}
		
	}
	
	@Override
	public boolean getAllowsChildren() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getChildCount() {
		return children.size();
	}

	@Override
	public int getIndex(TreeNode node) {
		// TODO Auto-generated method stub
		return children.indexOf(node);
	}

	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Enumeration<? extends TreeTableNode> children() {
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
		return parent;
	}

	@Override
	public Object getUserObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValueAt(int arg0) {
		// TODO Auto-generated method stub
		switch(arg0){
		case 0:
			return transaction.getHashAsString();
		case 1:
			return new Date(transaction.createdAt*1000);
		case 6:
			return visible;
		default:
			return "";
		}
	}

	@Override
	public boolean isEditable(int arg0) {
		// TODO Auto-generated method stub
		if(arg0==6){
			return true;
		}
		return false;
	}

	@Override
	public void setUserObject(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setValueAt(Object arg0, int arg1) {
		// TODO Auto-generated method stub
				if(arg1==6){
					visible=(Boolean) arg0;
				}

	}

}

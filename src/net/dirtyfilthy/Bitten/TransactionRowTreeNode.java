package net.dirtyfilthy.Bitten;

import java.math.BigInteger;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import org.jdesktop.swingx.treetable.TreeTableNode;

import com.google.bitcoin.core.GraphTransactionInput;
import com.google.bitcoin.core.GraphTransactionOutput;

import com.google.bitcoin.core.Utils;

public class TransactionRowTreeNode implements TreeTableNode {

	private TreeTableNode parent;
	public GraphTransactionInput input;
	public GraphTransactionOutput output;

	
	public TransactionRowTreeNode(TreeTableNode parent){
		this.parent=parent;
	}
	
	public boolean getAllowsChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getChildCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getIndex(TreeNode node) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Enumeration<? extends TreeTableNode> children() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeTableNode getChildAt(int arg0) {
		// TODO Auto-generated method stub
		return null;
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
		switch(arg0){
		case 0:
			return "";
		case 1:
			return "";
		case 2:
			if(input==null){
				return "";
			}
			else{
				if(input.address()==null){
					return "COINBASE";
				}
				else {
					return input.address().toString();
				}
			}
		case 3:
			if(input==null){
				return "";
			}
			return Utils.bitcoinValueToFriendlyString(input.value());
		case 4:
			if(output==null){
				return "";
			}
			else{
				if(output.address()==null){
					return "COINBASE";
				}
				else {
					return output.address().toString();
				}
			}
		case 5:
			if(output==null){
				return "";
			}
			return Utils.bitcoinValueToFriendlyString(output.value());
		default:
			return null;
				
		}
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

package net.dirtyfilthy.Bitten;

import java.awt.List;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import org.jdesktop.swingx.treetable.TreeTableNode;

import com.google.bitcoin.core.GraphAddress;
import com.google.bitcoin.core.GraphTransaction;
import com.google.bitcoin.core.GraphTransactionInput;
import com.google.bitcoin.core.GraphTransactionOutput;
import com.google.bitcoin.core.GraphWallet;

import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;

public class TransactionTreeNode implements TreeTableNode {
	public GraphTransaction transaction;
	public boolean visible=false;
	private ControlPanel panel;
	ArrayList<TreeTableNode> children=new ArrayList<TreeTableNode>();
	TreeTableNode parent;
	TransactionTreeNode(TreeTableNode parent, ControlPanel p, GraphTransaction t){
		transaction=t;
		panel=p;
		this.parent=parent;
		ArrayList<GraphTransactionInput> inputs=transaction.inputs;
		ArrayList<GraphTransactionOutput> outputs=transaction.outputs;
		int size=inputs.size() > outputs.size() ? inputs.size() : outputs.size();
		int c=0;
		
		GraphTransactionInput i;
		GraphTransactionOutput o;
		while(c<size){
			TransactionRowTreeNode n=new TransactionRowTreeNode(this);
			i=null;
			o=null;
			if(c<inputs.size()){
				i= inputs.get(c);
			}
			if(c<outputs.size()){
				o=outputs.get(c);
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
		GraphAddress i;
		// TODO Auto-generated method stub
		switch(arg0){
		case 0:
			return "";
		case 1:
			return new Date(transaction.createdAt*1000);
		case 2:
			String label;
			if(transaction.isCoinBase()){
				label="COINBASE";
				return label;
			}
			else{
				i=transaction.inputs.get(0).address();
				return i.wallet().label();
			}
		case 3:
			BigInteger amt=BigInteger.ZERO;
			for(GraphTransactionInput inp : transaction.inputs){
				amt=amt.add(inp.value);
			}
			return Utils.bitcoinValueToFriendlyString(amt);
		case 4:
			ArrayList<GraphWallet> wallets = new ArrayList<GraphWallet>();
			GraphWallet iw;
			String l2="";
			if(transaction.isCoinBase()){
				i=null;
				iw=null;
				l2="COINBASE";
			}
			else{
				i=transaction.inputs.get(0).address();
				iw=i.wallet();
				l2=i.toString();
			}
			
			for(GraphTransactionOutput out : transaction.outputs){
				GraphWallet w=out.address().wallet();
				if(w.equals(iw) || wallets.contains(w)){
					continue;
				}
				wallets.add(w);
			}
			if(wallets.size()==0){
				return "*SELF*";
			}
			boolean first=true;
			for(GraphWallet w : wallets){
				if(!first){
					l2=l2+w.label()+" ";
				}
				else{
					l2=w.label();
					first=false;
				}
			}
			
			return l2.toString();
		case 5:
			//if(transaction.isCoinBase()){
			//	i=null;
			//}
			//else{
			//	i=((SqlTransactionInput) transaction.inputs.get(0)).getAddress();
			//}
			BigInteger amt2=BigInteger.ZERO;
			for(GraphTransactionOutput out : transaction.outputs){
				
				amt2=amt2.add(out.value());
			}
			return Utils.bitcoinValueToFriendlyString(amt2);
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
					if(visible!=((Boolean) arg0)){
						visible=(Boolean) arg0;
						panel.notifyVisibilityChange(this);
					}
				}
				

	}

	public void setValueAtNoTrigger(Object arg0, int arg1) {
		if(arg1==6){
			if(visible!=((Boolean) arg0)){
				visible=(Boolean) arg0;
			}
		}
		
	}

}

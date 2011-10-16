package net.dirtyfilthy.Bitten;

import java.awt.List;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import org.jdesktop.swingx.treetable.TreeTableNode;

import com.google.bitcoin.core.SqlAddress;
import com.google.bitcoin.core.SqlTransaction;
import com.google.bitcoin.core.SqlTransactionInput;
import com.google.bitcoin.core.SqlTransactionOutput;
import com.google.bitcoin.core.SqlWallet;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;

public class TransactionTreeNode implements TreeTableNode {
	public SqlTransaction transaction;
	public boolean visible=false;
	private ControlPanel panel;
	ArrayList<TreeTableNode> children=new ArrayList<TreeTableNode>();
	TreeTableNode parent;
	TransactionTreeNode(TreeTableNode parent, ControlPanel p, SqlTransaction t){
		transaction=t;
		panel=p;
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
		SqlAddress i;
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
				i=((SqlTransactionInput) transaction.inputs.get(0)).getAddress();
				SqlWallet w=panel.getWalletStore().findById(i.walletId);
				if(w!=null){
					return w.label();
				}
				return i.toString();
			}
		case 3:
			long amt=0;
			for(TransactionInput inp : transaction.inputs){
				SqlTransactionInput inp2=(SqlTransactionInput) inp;
				amt=amt+inp2.value;
			}
			return Utils.bitcoinValueToFriendlyString(BigInteger.valueOf(amt));
		case 4:
			ArrayList<SqlAddress> addresses = new ArrayList<SqlAddress>();
			long iw;
			if(transaction.isCoinBase()){
				i=null;
				iw=-1;
			}
			else{
				i=((SqlTransactionInput) transaction.inputs.get(0)).getAddress();
				iw=i.getWalletId();
			}
			for(TransactionOutput out : transaction.outputs){
				SqlTransactionOutput out2=(SqlTransactionOutput) out;
				SqlAddress a=out2.getAddress();
				if(addresses.contains(a) || a==i || (a.getWalletId()!=0 && a.getWalletId()==iw)){
					continue;
				}
				addresses.add(a);
			}
			String l2="";
			if(addresses.size()==0){
				return "*SELF*";
			}
			for(SqlAddress s : addresses){
				SqlWallet w=panel.getWalletStore().findById(s.walletId);
				if(w!=null){
					l2=l2+w.label()+" ";
				}
				else{
					l2=l2+s.toString();
				}
			}
			return l2;
		case 5:
			if(transaction.isCoinBase()){
				i=null;
			}
			else{
				i=((SqlTransactionInput) transaction.inputs.get(0)).getAddress();
			}
			long amt2=0;
			for(TransactionOutput out : transaction.outputs){
				SqlTransactionOutput out2=(SqlTransactionOutput) out;
				SqlAddress a=out2.getAddress();
				if(i!=null && (a==i || (a.getWalletId()!=0 && a.getWalletId()==i.getWalletId()))){
					continue;
				}
				amt2=amt2+out2.getBtcValue();
			}
			return Utils.bitcoinValueToFriendlyString(BigInteger.valueOf(amt2));
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

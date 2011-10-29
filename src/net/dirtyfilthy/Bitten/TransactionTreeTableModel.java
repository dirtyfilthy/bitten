package net.dirtyfilthy.Bitten;

import java.util.List;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.google.bitcoin.core.Accountable;

public class TransactionTreeTableModel extends DefaultTreeTableModel {

	private ControlPanel panel;
	
	public TransactionTreeTableModel() {
		// TODO Auto-generated constructor stub
	}

	public TransactionTreeTableModel(TreeTableNode root) {
		super(root);
	
		// TODO Auto-generated constructor stub
	}

	public TransactionTreeTableModel(TreeTableNode root, List<?> columnNames) {
		super(root, columnNames);
	
		// TODO Auto-generated constructor stub
	}
	
	 public Class<?> getColumnClass(int column) {
	        switch(column){
	        case 6:
	        	return Boolean.class;
	        default:
	        	return Object.class;
	        }
	 }
	 
	 @Override
		public boolean isCellEditable(Object arg0, int arg1) {
			if(arg1==6){
				return true;
			}
			return false;
		}

	public void setIncomingVisible(Accountable target, boolean b) {
		int count=this.getRoot().getChildCount();
		int i=0;
		for(i=0;i<count;i++){
			TransactionTreeNode t=(TransactionTreeNode) this.getRoot().getChildAt(i);
			if(target.incomingAmount(t.transaction).compareTo(target.outgoingAmount(t.transaction))>=0){
				System.out.println("setting value to "+b);
				t.setValueAt(b,6);
			}
		}
		
	}
	
	public void setOutgoingVisible(Accountable target, boolean b) {
		int count=this.getRoot().getChildCount();
		int i=0;
		for(i=0;i<count;i++){
			TransactionTreeNode t=(TransactionTreeNode) this.getRoot().getChildAt(i);
			if(target.incomingAmount(t.transaction).compareTo(target.outgoingAmount(t.transaction))<0){
				System.out.println("setting value to "+b);
				t.setValueAt(b,6);
			}
		}
		
	}


}

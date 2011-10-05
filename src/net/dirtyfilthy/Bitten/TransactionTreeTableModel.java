package net.dirtyfilthy.Bitten;

import java.util.List;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.google.bitcoin.core.SqlTransaction;

public class TransactionTreeTableModel extends DefaultTreeTableModel {

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


}

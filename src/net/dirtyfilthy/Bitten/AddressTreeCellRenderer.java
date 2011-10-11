package net.dirtyfilthy.Bitten;

import java.awt.Component;
import java.net.URL;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;


import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.google.bitcoin.core.SqlAddress;

public class AddressTreeCellRenderer extends DefaultXTreeCellRenderer {

	protected SqlAddress targetAddress;
	
	public AddressTreeCellRenderer(SqlAddress a) {
		super();
		targetAddress=a;
		 
	}
	
	 public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		 		if((value instanceof TransactionTreeNode) && (value != null) && (targetAddress!=null)) {
		 			TransactionTreeNode t=(TransactionTreeNode) value;
		 			URL img;
		 			if(t.transaction.incomingAmountForAddress(targetAddress)>t.transaction.outgoingAmountForAddress(targetAddress)){
		 				img=this.getClass().getResource("/icons/incoming.png");
		 			}
		 			else{
		 				img=this.getClass().getResource("/icons/outgoing.png");
		 			}
		 			setIcon(new javax.swing.ImageIcon(img));	
		 			
		 		}
		 		else {
		 			setIcon(null);
		 		}
		 		
		 		//we can not call super.getTreeCellRendererComponent method, since it overrides our setIcon call and cause rendering of labels to '...' when node expansion is done

		 		//so, we copy (and modify logic little bit) from super class method:

		 		 String stringValue = ((TreeTableNode) value).getValueAt(0).toString();

		 		 this.hasFocus = hasFocus;
		 		 setText(stringValue);
		 		 if(sel)
		 		   setForeground(getTextSelectionColor());
		 		 else
		 		   setForeground(getTextNonSelectionColor());

		 		 if (!tree.isEnabled()) {
		 		   setEnabled(false);
		 		 }
		 		 else {
		 		   setEnabled(true);
		 		 }
		 		  setComponentOrientation(tree.getComponentOrientation());
		 		 selected = sel;
		 		 return this;

		 		}
		 		
	 }


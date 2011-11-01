package net.dirtyfilthy.Bitten;

import java.awt.Component;
import java.net.URL;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;


import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.google.bitcoin.core.Accountable;

public class AddressTreeCellRenderer extends DefaultXTreeCellRenderer {

	protected Accountable target;
	
	public AddressTreeCellRenderer(Accountable target) {
		super();
		this.target=target;
		 
	} 
	
	

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if((value instanceof TransactionTreeNode) && (value != null)) {
			TransactionTreeNode t=(TransactionTreeNode) value;
			if(t.icon==null){
				URL img;
				if(t.transaction.isCoinBase()){
					img=this.getClass().getResource("/icons/coinbase.png");
				}
				else{
					if(((target!=null && t.transaction.cachedIsIncoming==null && target.incomingAmount(t.transaction).compareTo(target.outgoingAmount(t.transaction))>0)) || (t.transaction.cachedIsIncoming!=null && t.transaction.cachedIsIncoming) ){
						img=this.getClass().getResource("/icons/incoming.png");
					}
					else{
						img=this.getClass().getResource("/icons/outgoing.png");
					}
				}
				t.icon=new javax.swing.ImageIcon(img);
			}
			setIcon(t.icon);	

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


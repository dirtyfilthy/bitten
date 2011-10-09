package net.dirtyfilthy.Bitten;

import java.awt.Point;

import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.rollover.RolloverController;
import org.jdesktop.swingx.rollover.TableRolloverController;

import com.google.bitcoin.core.SqlTransactionInput;

public class HighlightRolloverController extends TableRolloverController {
	
	protected WalletView view;
	
	HighlightRolloverController(WalletView view){
		this.view=view;
	}
	
	protected void rollover(Point oldLocation, Point newLocation) {
		super.rollover(oldLocation, newLocation);
    if ((newLocation == null) || (newLocation.x < 0)
            || !component.hasFocus()) return;
    Object newRow=((JXTreeTable) component).getPathForLocation(newLocation.x, newLocation.y).getLastPathComponent();
    // Object oldRow=((JXTreeTable) component).getPathForLocation(oldLocation.x, oldLocation.y).getLastPathComponent();
    int column=((JXTreeTable) component).columnAtPoint(newLocation);
    if(newRow instanceof TransactionRowTreeNode){
    	TransactionRowTreeNode t=(TransactionRowTreeNode) newRow;
    	SqlTransactionInput input=(SqlTransactionInput) ((TransactionTreeNode) t.getParent()).transaction.inputs.get(0);
    	if(input.isCoinBase()){
    		return;
    	}
    	long source=input.getAddress().walletId;
    	long target=t.output.getAddress().walletId;
    	System.out.println("highlighting nodes "+source+" "+target);
    	view.highlightNodes(source,target);
    	
    }
    	
	}
    
    public void install(JXTable table) {
    	super.install(table);
    	table.setSurrendersFocusOnKeystroke(true);
}
    
    
    
}



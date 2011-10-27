package net.dirtyfilthy.Bitten;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.treetable.TreeTableModel;

import com.google.bitcoin.core.Accountable;
import com.google.bitcoin.core.GraphTransactionInput;
import com.google.bitcoin.core.GraphTransactionOutput;
import com.google.bitcoin.core.GraphWallet;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;

public class TransactionTreeTable extends JXTreeTable {

	ControlPanel panel;
	public TransactionTreeTable() {
		// TODO Auto-generated constructor stub
	}

	public TransactionTreeTable(ControlPanel p, TreeTableModel treeModel) {
		super(treeModel);
		panel=p;
		// TODO Auto-generated constructor stub
		//setDefaultRenderer(SqlTransaction.class,
		//         new DefaultTableRenderer(new ToggleViewCheckBoxProvider(StringValues.EMPTY)));
		this.setDefaultEditor(Boolean.class, new  MyBooleanEditor());
		this.setDefaultRenderer(Boolean.class, new NullTableRenderer(new  ToggleViewCheckBoxProvider()));
		final TransactionTreeTable t=this;
		this.addMouseListener(new MouseAdapter()
		{
		
		    public void mouseClicked(MouseEvent e)
		    {
		        if (e.getComponent().isEnabled() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
		        {
		            Point p = e.getPoint();
		            int row = t.rowAtPoint(p); 
		            int column = t.columnAtPoint(p);
		            TreePath path=t.getPathForLocation(p.x, p.y);
		            Object c=path.getLastPathComponent();
		            if(c instanceof TransactionRowTreeNode){
		            	TransactionRowTreeNode node=(TransactionRowTreeNode) c;
		            	if(column==4){
		            		
		            		String address=node.output.address().toString();
		            		System.out.println("address "+address);
		            		panel.searchAddress(address);
		            	}
		            	else if(column==2 && !node.input.isCoinBase()){
		            		
		            		String address=node.input.address().toString();
		            		System.out.println("address "+address);
		            		panel.searchAddress(address);
		            	}
		            	
		            }
		            else if(c instanceof TransactionTreeNode){
		            	TransactionTreeNode node=(TransactionTreeNode) c;
		            	if(column==4){
		            		GraphTransactionOutput o=node.transaction.outputs.get(0);
		     
		            		panel.showWallet(o.address().wallet());
		           
		            	}
		            	if(column==2){
		            		GraphTransactionInput o=node.transaction.inputs.get(0);
		            		if(!node.transaction.isCoinBase()){
		            			panel.showWallet(o.address().wallet());
		            		}
		           
		            	}
		            	
		            }
		        }
		        
		       
		    }
		    
		});
		this.addTreeSelectionListener(new TreeSelectionListener(){
			
			public void valueChanged(TreeSelectionEvent e){
				System.out.println("selection event "+e);
				Object obj=e.getPath().getLastPathComponent();
				if(obj instanceof TransactionRowTreeNode){
			    	TransactionRowTreeNode n=(TransactionRowTreeNode) obj;
			    	GraphTransactionInput input=((TransactionTreeNode) n.getParent()).transaction.inputs.get(0);
			    	if(input.isCoinBase() || n.output==null){
			    		return;
			    	}
			    	GraphWallet source=input.address().wallet();
			    	GraphWallet target=n.output.address().wallet();
			    	System.out.println("highlighting nodes "+source+" "+target);
			    	panel.getView().clearHighlights();
			    	//panel.getView().highlightNodes(source,target);
			    }
				else if(obj instanceof TransactionTreeNode){
			    	TransactionTreeNode n=(TransactionTreeNode) obj;
			    	
			    	
			    	GraphTransactionInput in= n.transaction.inputs.get(0);
			    	if(in.isCoinBase()){
			    		return;
			    	}
			    	panel.getView().clearHighlights();
			    	GraphWallet source=in.address().wallet();
			    	for(GraphTransactionOutput o : n.transaction.outputs){
			    		GraphWallet target=o.address().wallet();
			    		panel.getView().highlightNodes(source,target);
			    	}
			    	
			    	
			    }
				
			}
			
			
		});
		
	}

	
	
	

}

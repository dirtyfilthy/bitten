package net.dirtyfilthy.Bitten;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.treetable.TreeTableModel;

import com.google.bitcoin.core.SqlTransaction;

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
		            	String address=(String) node.getValueAt(4);
		            	System.out.println("address "+address);
		            	panel.searchAddress(address);
		            	
		            }      
		        }
		       
		    }
		});
	}
	
	

}

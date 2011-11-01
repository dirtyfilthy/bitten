package net.dirtyfilthy.Bitten;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.rollover.RolloverController;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;

import com.google.bitcoin.core.Accountable;
import com.google.bitcoin.core.GraphTransaction;

import com.google.bitcoin.core.TransactionOutput;

public class TransactionResultPanel extends ResultSetPanel implements Closeable {
	protected ArrayList<GraphTransaction> transactions;
	protected TransactionTreeTable table;
	protected ControlPanel panel;
	protected TransactionInfoPanel info;
	protected TransactionOptionsPanel options;
	public Accountable target;
	
	TransactionResultPanel(ControlPanel p,SwingWorker<ArrayList<GraphTransaction>, Object> task){
		super(task);
		this.panel=p;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	

	
	@Override
	protected void processResultSet(ArrayList<GraphTransaction> transactions) {
		System.out.println("process transactions");
	
			
			String columns[]={"id","time","from","btc","to","btc","V"};
			
			TransactionTreeTableModel  treeTableModel = new TransactionTreeTableModel( new RootTransactionTreeNode(panel, transactions), Arrays.asList(columns));
			System.out.println("transactions "+transactions.size() );
			if(target!=null){
				info=new TransactionInfoPanel(target, transactions);
			
				this.add(info);
			}
			options=new TransactionOptionsPanel(panel,target, treeTableModel);
			this.add(options);
			table=new TransactionTreeTable(panel, treeTableModel);
			table.setTreeCellRenderer(new AddressTreeCellRenderer(target));
			
			// RolloverController controller = new HighlightRolloverController(panel.getView());
			//table=new JTable(m);
			table.setVisible(true);
			//controller.install(table);
			this.add(new JScrollPane(table));
			table.getModel().addTableModelListener(panel);
			panel.registerTransactionTreeTable(table);
			this.repaint();
			options.processViz();
			
		
		
		

	}



	public void close() {
		if(table!=null){
			panel.unregisterTransactionTreeTable(table);
		}
		
	}
	
	

}

package net.dirtyfilthy.Bitten;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.rollover.RolloverController;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;

import com.google.bitcoin.core.SqlAddress;
import com.google.bitcoin.core.SqlBlockStore;
import com.google.bitcoin.core.SqlTransaction;
import com.google.bitcoin.core.SqlTransactionOutput;
import com.google.bitcoin.core.TransactionOutput;

public class TransactionResultPanel extends ResultSetPanel {
	protected ArrayList<SqlTransaction> transactions;
	protected SqlBlockStore store;
	protected JXTreeTable table;
	protected ControlPanel panel;
	protected TransactionInfoPanel info;
	public SqlAddress targetAddress;
	
	TransactionResultPanel(ControlPanel p,SqlBlockStore s,PreparedStatement query){
		super();
		this.query=query;
		this.store=s;
		this.panel=p;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		transactions=new ArrayList<SqlTransaction>();
	}
	

	
	@Override
	protected void processResultSet(ResultSet rs) {
		System.out.println("process transactions");
		try {
			while(rs.next()){
				transactions.add(store.loadTransactionFromResultSet(rs));
				System.out.println("add transaction");
			};
			rs.close();
			String columns[]={"id","time","from","btc","to","btc","V"};
			
			TreeTableModel  treeTableModel = new TransactionTreeTableModel( new RootTransactionTreeNode(panel, transactions), Arrays.asList(columns));
			System.out.println("transactions "+transactions.size() );
			info=new TransactionInfoPanel(targetAddress, transactions);
			this.add(info);
			
			table=new TransactionTreeTable(panel, treeTableModel);
			table.setTreeCellRenderer(new AddressTreeCellRenderer(targetAddress));
			
			RolloverController controller = new HighlightRolloverController(panel.getView());
			//table=new JTable(m);
			table.setVisible(true);
			//controller.install(table);
			this.add(new JScrollPane(table));
			table.getModel().addTableModelListener(panel);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

	}
	
	

}

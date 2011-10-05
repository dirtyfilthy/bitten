package net.dirtyfilthy.Bitten;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.treetable.TreeTableModel;

import com.google.bitcoin.core.SqlTransaction;

public class TransactionTreeTable extends JXTreeTable {

	public TransactionTreeTable() {
		// TODO Auto-generated constructor stub
	}

	public TransactionTreeTable(TreeTableModel treeModel) {
		super(treeModel);
		// TODO Auto-generated constructor stub
		//setDefaultRenderer(SqlTransaction.class,
		//         new DefaultTableRenderer(new ToggleViewCheckBoxProvider(StringValues.EMPTY)));
		this.setDefaultEditor(Boolean.class, new  MyBooleanEditor());
		this.setDefaultRenderer(Boolean.class, new NullTableRenderer(new  ToggleViewCheckBoxProvider()));
	}

}

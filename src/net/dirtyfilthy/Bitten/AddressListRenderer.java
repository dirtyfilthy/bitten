package net.dirtyfilthy.Bitten;

import java.awt.Component;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.google.bitcoin.core.AddressListable;
import com.google.bitcoin.core.GraphAddress;
import com.google.bitcoin.core.Utils;

public class AddressListRenderer implements TableCellRenderer {

	public AddressListRenderer() {
		// TODO Auto-generated constructor stub
	}


	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {
		Collection<AddressListable> list=(Collection<AddressListable>) value;
		Object[][] data=new Object[list.size()][];
		Object[] columns={"Address","Value"};
		int i=0;
		for(AddressListable l : list){
			String add="COINBASE";
			
			GraphAddress  a=l.address();
			BigInteger v=l.value();
			
			if(a!=null){
				add=a.toString();
			}
			data[i]=new Object[] {add,Utils.bitcoinValueToFriendlyString(v)};
			i++;
		}
		JTable t=new JTable(new DefaultTableModel(data,columns));
		
			table.setRowHeight(64);
		
		return t;
	}

}

package net.dirtyfilthy.Bitten;

import java.awt.Component;

import javax.swing.JTable;

import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.TableCellContext;

public class NullTableRenderer extends DefaultTableRenderer {
	
	private TableCellContext cellContext;

	public NullTableRenderer() {
		// TODO Auto-generated constructor stub
		this.cellContext = new TableCellContext();
	}

	public NullTableRenderer(ComponentProvider<?> componentProvider) {
		super(componentProvider);
		// TODO Auto-generated constructor stub
		this.cellContext = new TableCellContext();
	}

	public NullTableRenderer(StringValue converter) {
		super(converter);
		// TODO Auto-generated constructor stub
		this.cellContext = new TableCellContext();
	}

	public NullTableRenderer(StringValue converter, int alignment) {
		super(converter, alignment);
		// TODO Auto-generated constructor stub
		this.cellContext = new TableCellContext();
	}

	public NullTableRenderer(StringValue stringValue, IconValue iconValue) {
		super(stringValue, iconValue);
		// TODO Auto-generated constructor stub
		this.cellContext = new TableCellContext();
	}

	public NullTableRenderer(StringValue stringValue, IconValue iconValue,
			int alignment) {
		super(stringValue, iconValue, alignment);
		// TODO Auto-generated constructor stub
		this.cellContext = new TableCellContext();
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        cellContext.installContext(table, value, row, column, isSelected, hasFocus,
                true, true);
        if(value==null){
        	return new DefaultTableRenderer().getTableCellRendererComponent(table,"",isSelected,hasFocus,row,column);
        }
        Component comp = componentController.getRendererComponent(cellContext);
        // fix issue #1040-swingx: memory leak if value not released
        cellContext.replaceValue(null);
        return comp;
    }

}

package net.dirtyfilthy.Bitten;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.rollover.RolloverRenderer;

public class ToggleViewCheckBoxProvider extends CheckBoxProvider {

	public ToggleViewCheckBoxProvider() {
		// TODO Auto-generated constructor stub
	}

	public ToggleViewCheckBoxProvider(StringValue stringValue) {
		super(stringValue);
		// TODO Auto-generated constructor stub
	}

	public ToggleViewCheckBoxProvider(StringValue stringValue, int alignment) {
		super(stringValue, alignment);
		// TODO Auto-generated constructor stub
	}
	
	protected void format(CellContext context) {
		
	    


		    rendererComponent.setSelected(getValueAsBoolean(context));
		    rendererComponent.setText(getValueAsString(context));
		
    }
	
	
	
	


}

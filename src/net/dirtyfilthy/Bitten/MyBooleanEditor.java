package net.dirtyfilthy.Bitten;

import javax.swing.JCheckBox;

import org.jdesktop.swingx.JXTable.BooleanEditor;

public class MyBooleanEditor extends BooleanEditor {

	public MyBooleanEditor() {
		super();
		 System.out.println("editor");
	}
	
	 public void setValue(Object value)
	     {
		 System.out.println("in set value");
	       JCheckBox c = (JCheckBox) editorComponent;
	       
	       if (value == null){
	        c.setSelected(false);
	       c.setVisible(false);
	       System.out.println("Value null");
	       }
	      else
	        c.setSelected( ((Boolean) value).booleanValue());
	    }


}

package net.dirtyfilthy.Bitten;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.google.bitcoin.core.Noteable;

public class NoteEditorPanel extends JPanel implements ActionListener {
	
	private Noteable target;
	private JTextField label;
	private JTextArea notes;
	private JButton save;
	
	
	NoteEditorPanel(Noteable target){
		this.target=target;
		label=new JTextField(20);
		notes=new JTextArea(6,20);
		label.setText(target.getLabel());
		notes.setText(target.getNotes());
		notes.setBorder(BorderFactory.createLineBorder(new Color(71,102,102)));
		JLabel label1,label2;
		label1 = new JLabel();
		label2 = new JLabel();
		label1.setText("Label:");
		label2.setText("Notes:");
		JLabel d=new JLabel();
		d.setText(" ");
		save=new JButton("Save");
		save.addActionListener(this);
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets=new Insets(5,5,5,5);
		c.anchor=GridBagConstraints.NORTHWEST;
		this.add(label1,c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		this.add(label,c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		this.add(label2,c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		this.add(notes,c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		this.add(d,c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		this.add(save,c);
		
		
		
		// c.gridwidth = GridBagConstraints.REMAINDER;
		
		
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==save){
			target.setLabel(label.getText());
			target.setNotes(notes.getText());
			target.save();
		}
		
	}
	

}

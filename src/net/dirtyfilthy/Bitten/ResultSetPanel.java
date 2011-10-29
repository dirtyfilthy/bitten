package net.dirtyfilthy.Bitten;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import com.google.bitcoin.core.GraphTransaction;


public abstract class ResultSetPanel extends JPanel implements PropertyChangeListener{
	
	protected PreparedStatement query;
	protected JProgressBar progress;
	protected SwingWorker task;
	
	ResultSetPanel(){
		
	}
	
	ResultSetPanel(SwingWorker task){
		this.task=task;	
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if(arg0.getPropertyName().equals("resultset")){
			processResultSet((ArrayList<GraphTransaction>) arg0.getNewValue());
			progress.setVisible(false);
		}
		
		
	}
	
	protected void createStatusBar(){
		progress=new JProgressBar(0,100);
		progress.setVisible(true);
		progress.setIndeterminate(true);
		progress.setString("Working...");
		progress.setStringPainted(true);
		this.add(progress);
		this.repaint();
	}
	
	public void execute(){
		createStatusBar();
		task.addPropertyChangeListener(this);
		task.execute();
	}
	
	protected abstract void processResultSet(ArrayList arrayList);

}

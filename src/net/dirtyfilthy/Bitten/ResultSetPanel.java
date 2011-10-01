package net.dirtyfilthy.Bitten;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

public abstract class ResultSetPanel extends JPanel implements PropertyChangeListener{
	
	protected SearchBlockStoreTask task;
	protected PreparedStatement query;
	protected JProgressBar progress;
	
	ResultSetPanel(){
		
	}
	
	ResultSetPanel(PreparedStatement query){
		this.query=query;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if(arg0.getPropertyName().equals("resultset")){
			processResultSet((ResultSet) arg0.getNewValue());
		}
		progress.setVisible(false);
		
	}
	
	protected void createStatusBar(){
		progress=new JProgressBar();
		progress.setIndeterminate(true);
		this.add(progress);
	}
	
	public void execute(){
		createStatusBar();
		task=new SearchBlockStoreTask(query);
		task.addPropertyChangeListener(this);
		task.execute();
	}
	
	protected abstract void processResultSet(ResultSet r);

}

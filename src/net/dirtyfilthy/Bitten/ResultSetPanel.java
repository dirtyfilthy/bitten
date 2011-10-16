package net.dirtyfilthy.Bitten;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.google.bitcoin.core.SqlBlockStore;
import com.google.bitcoin.core.SqlTransaction;

public abstract class ResultSetPanel extends JPanel implements PropertyChangeListener{
	
	protected SearchBlockStoreTask task;
	protected PreparedStatement query;
	protected JProgressBar progress;
	protected SqlBlockStore store;
	
	ResultSetPanel(){
		
	}
	
	ResultSetPanel(SqlBlockStore store, PreparedStatement query){
		this.query=query;
		this.store=store;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if(arg0.getPropertyName().equals("resultset")){
			processResultSet((ArrayList<SqlTransaction>) arg0.getNewValue());
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
		task=new SearchBlockStoreTask(store, query,"resultset");
		task.addPropertyChangeListener(this);
		task.execute();
	}
	
	protected abstract void processResultSet(ArrayList<SqlTransaction> r);

}

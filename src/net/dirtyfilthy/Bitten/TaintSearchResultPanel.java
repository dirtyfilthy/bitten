package net.dirtyfilthy.Bitten;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JPanel;

import com.google.bitcoin.core.GraphTransactionOutput;
import com.google.bitcoin.core.GraphWallet;


public class TaintSearchResultPanel extends JPanel implements Closeable {

	private TransactionResultPanel results;
	private NoteEditorPanel editor;
	
	TaintSearchResultPanel(ControlPanel p, GraphTransactionOutput o, int depth){
		results=new TransactionResultPanel(p, new SearchTaintedTask(o, depth));
		results.target=null;
		results.setVisible(true);
		this.add(results);
		results.execute();
	}

	@Override
	public void close() {
		results.close();
		
	}
	
	

}

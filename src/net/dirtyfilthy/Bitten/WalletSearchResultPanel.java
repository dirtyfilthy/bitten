package net.dirtyfilthy.Bitten;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JPanel;

import com.google.bitcoin.core.GraphWallet;


public class WalletSearchResultPanel extends JPanel implements Closeable {

	private TransactionResultPanel results;
	private NoteEditorPanel editor;
	
	WalletSearchResultPanel(ControlPanel p, GraphWallet w){
		editor=new NoteEditorPanel(w);
		this.add(editor);
		results=new TransactionResultPanel(p, new SearchWalletTask(w));
		results.target=w;
		results.setVisible(true);
		this.add(results);
		results.execute();
	}

	@Override
	public void close() {
		results.close();
		
	}
	
	

}

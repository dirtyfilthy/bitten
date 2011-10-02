package net.dirtyfilthy.Bitten;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;
import java.io.File;
import java.sql.SQLException;

import javax.swing.JLabel;

import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.BlockStore;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.SqlBlockStore;
import com.google.bitcoin.core.Wallet;

public class Bitten {

	private JLabel lblStatusBar;
	private JFrame frame;
	private BlockChain blockChain;
	private BlockChainDownloader downloader;
	public static final NetworkParameters networkParameters=NetworkParameters.prodNet();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Bitten window = new Bitten();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public BlockStore getBlockStore(){
		File f=new File("/home/alhazred/bitten.sqlite");
		return new SqlBlockStore(Bitten.networkParameters,f);
	}
	
	/**
	 * Create the application.
	 * @throws SQLException 
	 */
	public Bitten() throws SQLException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws SQLException 
	 */
	private void initialize() throws SQLException {
		
		frame = new JFrame();
		frame.setBounds(100, 100, 546, 381);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		
		// we don't care about the wallet
		
		Wallet wallet = new Wallet(Bitten.networkParameters);
		blockChain=new BlockChain(Bitten.networkParameters,wallet,getBlockStore());
		SqlBlockStore store= (SqlBlockStore) getBlockStore();
		 lblStatusBar = new StatusBar(blockChain);
		final AddressView t;
		frame.getContentPane().add(lblStatusBar, BorderLayout.SOUTH);
		ControlPanel c=new ControlPanel(store);
			t=new AddressView(store.getConnection());
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    t, c);
			splitPane.setVisible(true);
			splitPane.setOneTouchExpandable(true);
			splitPane.setDividerLocation(150);
			Dimension minimumSize = new Dimension(100, 50);
			c.setMinimumSize(minimumSize);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);;
		 frame.pack();           // layout components in window
	     frame.setVisible(true); // show the window
	     
	     Runnable r=new Runnable()
	     {
	     public void run()
	     {
	    	 int min=200000;
	    	 int max=200010;
	    	 int i=min;
	      while(i<max){
	    	
	    	  
			//	t.loadTransaction(new Long[] {Long.valueOf(i)});
			
	    	  i++;
	    	  try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      }
	     }
	     };
	 //   new Thread(r).start();
		// frame.getContentPane().add(t, BorderLayout.CENTER);
		downloader=new BlockChainDownloader(blockChain);
	   // downloader.start();
	}

}

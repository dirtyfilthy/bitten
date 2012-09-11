package net.dirtyfilthy.Bitten;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.io.File;
import java.sql.SQLException;

import javax.swing.JLabel;

import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.BlockStore;
import com.google.bitcoin.core.GraphBlockStore;
import com.google.bitcoin.core.NetworkParameters;
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
		Toolkit.getDefaultToolkit().setDynamicLayout(true);
		System.setProperty("sun.awt.noerasebackground", "true");
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		try {
		    UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
		} catch(Exception ex) {
		    ex.printStackTrace();
		}
		String v;
		if(args.length==0){
			v = System.getProperty("user.home")+"/bitten.graph";
		}else{
			v=args[0];
		}
		final String path=v;
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					Bitten window = new Bitten(path);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	 private String getJarFolder() {
		 try{
		    // get name and path
		    String name = getClass().getName().replace('.', '/');
		    name = getClass().getResource("/" + name + ".class").toString();
		    // remove junk
		    name = name.substring(0, name.indexOf(".jar"));
		    name = name.substring(name.lastIndexOf(':')-1, name.lastIndexOf('/')+1).replace('%', ' ');
		    // remove escape characters
		    String s = "";
		    for (int k=2; k<name.length(); k++) {
		      s += name.charAt(k);
		      if (name.charAt(k) == ' ') k += 2;
		    }
		    // replace '/' with system separator char
		    return s.replace('/', File.separatorChar);
		  }
		 catch(java.lang.StringIndexOutOfBoundsException e){
			 try{
			 return new File(getClass().getResource(".").getPath()).getParentFile().getParent();
			 }
			 catch(Exception e2){
				 e2.printStackTrace();
			 }
	   }
	return null;
	 }
	 

	
	public GraphBlockStore getBlockStore(String storePath){
		return new GraphBlockStore(Bitten.networkParameters,storePath);
	}
	
	/**
	 * Create the application.
	 * @throws SQLException 
	 */
	public Bitten(String storePath) throws SQLException {
		initialize(storePath);
		
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws SQLException 
	 */
	private void initialize(String storePath) throws SQLException {
		
		frame = new JFrame();
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		
		// we don't care about the wallet
		
		Wallet wallet = new Wallet(Bitten.networkParameters);
		GraphBlockStore store = getBlockStore(storePath);
		blockChain=new BlockChain(Bitten.networkParameters,wallet, store);
		 lblStatusBar = new StatusBar(blockChain);
		final WalletView view=new WalletView();
		frame.getContentPane().add(lblStatusBar, BorderLayout.SOUTH);
		ControlPanel panel=new ControlPanel(store,view);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    view, panel);
			splitPane.setVisible(true);
			splitPane.setOneTouchExpandable(true);
			splitPane.setResizeWeight(1.0);
			Dimension minimumSize = new Dimension(550, 500);
			panel.setMinimumSize(minimumSize);
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
	    downloader.start();
	}

}

package net.dirtyfilthy.Bitten;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

import javax.swing.event.EventListenerList;

import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.BlockChainListener;
import com.google.bitcoin.core.DnsDiscovery;
import com.google.bitcoin.core.IrcDiscovery;
import com.google.bitcoin.core.NetworkConnection;
import com.google.bitcoin.core.Peer;
import com.google.bitcoin.core.PeerDiscovery;
import com.google.bitcoin.core.PeerDiscoveryException;
import com.google.bitcoin.core.ProtocolException;
import com.google.bitcoin.core.SeedPeers;
import com.google.bitcoin.core.StoredBlock;


public class BlockChainDownloader extends Thread {
	CountDownLatch latch;
	private BlockChain blockChain;
	public BlockChainDownloader(BlockChain b) {
		blockChain=b;
	}

	public void run(){
	
		PeerDiscovery peerDiscovery=new SeedPeers(Bitten.networkParameters);
		try {
			while(true){
			InetSocketAddress addresses[]=peerDiscovery.getPeers();
			//InetSocketAddress addresses[]={new InetSocketAddress("localhost",8333)};
			System.out.println("peers "+addresses.length);
			NetworkConnection conn;
			Peer peer;
			System.out.println("connecting to peers");
			
				for(InetSocketAddress a : addresses){
					try {
						System.out.println("Trying "+a);
						conn=new NetworkConnection(a.getAddress(),Bitten.networkParameters,blockChain.getChainHead().getHeight(),3000);
						System.out.println("after network connection");
					peer=new Peer(Bitten.networkParameters,conn,blockChain);
					System.out.println("starting chain download");
					peer.start();
					peer.startBlockChainDownload();
					while(true){
						if(!peer.running()){
							System.out.println("peer stopped!");
							break;
						}
						sleep(1000);
					}
					} catch (IOException e) {
						System.out.println(e);
						continue;
					} catch (ProtocolException e) {
						continue;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					} 
				}
				
			}
		
		} catch (PeerDiscoveryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
		
		
	}



}

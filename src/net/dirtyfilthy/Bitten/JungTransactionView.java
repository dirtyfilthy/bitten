package net.dirtyfilthy.Bitten;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.bitcoin.core.SqlBlockStore;
import com.google.bitcoin.core.SqlTransactionOutput;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;

public class JungTransactionView{ 
	private HashMap<Long,TransactionNode> outMap;
	private Graph<TransactionNode,TransactionEdge> graph;
	private SqlBlockStore blockStore;
	private Layout<TransactionNode, TransactionEdge>  layout;
	public   VisualizationViewer<TransactionNode, TransactionEdge> view;
	public JungTransactionView(SqlBlockStore blockStore){
		graph=new  ObservableGraph<TransactionNode,TransactionEdge>(Graphs.<TransactionNode,TransactionEdge>synchronizedDirectedGraph(new DirectedSparseMultigraph<TransactionNode,TransactionEdge> ()));
		this.blockStore=blockStore;
		outMap=new HashMap<Long,TransactionNode>();
		 layout = new FRLayout<TransactionNode, TransactionEdge>(graph);
		  layout.setSize(new Dimension(2000,2000)); // sets the initial size of the space
		  
		  Relaxer relaxer = new VisRunner((IterativeContext) layout);
		  relaxer.stop();
		 relaxer.prerelax();
		 StaticLayout staticLayout =new StaticLayout<TransactionNode, TransactionEdge>(graph, layout);
		   view = new  VisualizationViewer<TransactionNode,TransactionEdge>(staticLayout);
		   view.setPreferredSize(new Dimension(1024,768)); //Sets the viewing area siz
		   DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
	        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
	        view.setGraphMouse(gm); 
	}
	
	public void loadTransaction(long id) throws SQLException{
		String sql="SELECT DISTINCT transaction_outputs.* FROM transaction_outputs WHERE transaction_id="+id+" UNION SELECT DISTINCT transaction_outputs.* FROM transaction_inputs JOIN transaction_outputs ON previous_output_id=transaction_outputs.id WHERE transaction_inputs.transaction_id="+id;
		ArrayList<SqlTransactionOutput> list=blockStore.loadTransactionOutputsFromSql(sql);
		TransactionNode tn=new TransactionNode(id);
		addNode(tn);
		for(SqlTransactionOutput out : list){
			addNode(new TransactionNode(out));
		}
		sql="SELECT DISTINCT transaction_inputs.previous_output_id as id,0 as type FROM transaction_inputs WHERE transaction_id="+id;
		sql=sql+" UNION SELECT DISTINCT transaction_outputs.id as id, 1 as type FROM transaction_outputs WHERE transaction_id="+id;
		ResultSet rs=blockStore.rawSqlQuery(sql);
		while(rs.next()){
			TransactionEdge e;
			long id2=rs.getLong(1);
			int type=rs.getInt(2);
			if(type==0){
				if(id2<1){
					continue;
				}
				TransactionNode from=outMap.get(id2);
				e=new TransactionEdge(id2,from,tn);
				graph.addEdge(e,from,tn);
			}
			else{
				TransactionNode to=outMap.get(id2);
				e=new TransactionEdge(id2,tn,to);
				graph.addEdge(e,tn,to);
			}
		}
		layout.initialize();
		
		         		Relaxer relaxer = new VisRunner((IterativeContext)layout);
		         		relaxer.stop();
		         		relaxer.prerelax();
		         		StaticLayout staticLayout = new StaticLayout<TransactionNode,TransactionEdge>(graph, layout);
						LayoutTransition<TransactionNode,TransactionEdge> lt =
		 					new LayoutTransition<TransactionNode,TransactionEdge>(view, view.getGraphLayout(),
		 							staticLayout);
						Animator animator = new Animator(lt);
		 				animator.start();
		 //				vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
		 				view.repaint();
	}
	 
	private void addNode(TransactionNode node){
		if(node.type==TransactionNode.OUTPUT){
			outMap.put(node.id,node);
		}
		System.out.println("Adding node "+node.id+" type "+node.type);
		graph.addVertex(node);
	}
	
	
	
	

}

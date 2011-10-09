package net.dirtyfilthy.Bitten;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.text.html.HTMLDocument.Iterator;

import com.google.bitcoin.core.SqlTransaction;
import com.google.bitcoin.core.SqlTransactionInput;
import com.google.bitcoin.core.SqlTransactionOutput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.DragForce;
import prefuse.visual.VisualItem;
public class WalletView extends Display {
	private String label="label";
	private Table walletNodeTable;
	private Table walletEdgeTable;
	private Graph graph;
	private Visualization viz;
	
	WalletView(){
		int[] palette = new int[] {
			    ColorLib.rgb(255,180,180), ColorLib.rgb(190,190,255), ColorLib.rgb(190,190,0)
			};
		
		walletNodeTable=new Table();
		walletEdgeTable=new Table();
		walletNodeTable.addColumn("id",long.class);
		walletEdgeTable.addColumn("btc",long.class);
		walletEdgeTable.addColumn("source",long.class);
		walletEdgeTable.addColumn("target",long.class);
		graph=new Graph(walletNodeTable, walletEdgeTable, true, "id", "source","target");
		Node n1 = graph.addNode();
		Node n2 = graph.addNode();
		n1.setLong(0, 1);
		n2.setLong(0, 2);
		graph.addEdge(n1, n2);
		viz=new Visualization();
		viz.add("graph", graph);
		// draw the "name" label for NodeItems
		LabelRenderer r = new LabelRenderer("id");
		r.setRoundedCorner(8, 8); // round the corners
		EdgeRenderer e = new EdgeRenderer();
		e.setArrowType(Constants.EDGE_ARROW_FORWARD);
		e.setArrowHeadSize(8, 8);
		
		viz.setRendererFactory(new DefaultRendererFactory(r,e));
		ActionList layout = new ActionList(Activity.INFINITY);
		ForceDirectedLayout f=new ForceDirectedLayout("graph");
		// f.getForceSimulator().setIntegrator(new EulerIntegrator());
		f.getForceSimulator().addForce(new DragForce((float) 0.01));
		layout.add(f);
		//layout.add(new NodeLinkTreeLayout("graph"));
		layout.add(new RepaintAction());
		
		ColorAction fill = new ColorAction("graph.nodes", VisualItem.FILLCOLOR, ColorLib.gray(100));
		ColorAction text = new ColorAction("graph.nodes",
                VisualItem.TEXTCOLOR, ColorLib.gray(0));
        // use light grey for edges
        ColorAction edges = new ColorAction("graph.edges",
                VisualItem.STROKECOLOR, ColorLib.gray(200));
        ColorAction fill2 = new ColorAction("graph.edges",
                VisualItem.FILLCOLOR, ColorLib.gray(200));
        DataSizeAction edgeWidth = new DataSizeAction("graph.edges", "btc");
        
        edgeWidth.setScale(Constants.LOG_SCALE);
        edgeWidth.setMaximumSize(20);
        ActionList color = new ActionList();
        color.add(text);
        color.add(fill);
        color.add(fill2);
        color.add(edges);
        color.add(edgeWidth);
		viz.putAction("layout", layout);
        viz.putAction("color", color);
		setSize(720, 500); // set display size
		setVisualization(viz);
        
		addControlListener(new DragControl()); // drag items around
		addControlListener(new PanControl());  // pan with background left-drag
		addControlListener(new ZoomControl()); // zoom with vertical right-drag
		viz.run("color");
        // start up the animated layout
        viz.run("layout");
		
	}
	
	public Node findOrCreateWalletNode(long id, String label){
		System.out.println("adding node "+id);
		Node n=graph.getNodeFromKey(id);
		if(n==null){
			n=graph.addNode();
		}
		n.setLong(0, id);
		return n;
	}
	
	public void removeOutputEdge(long source, long target, long amount){
		System.out.println("Removing edge src "+source+" dst "+target+" btc "+amount);
		Node sourceNode=graph.getNodeFromKey(source);
		System.out.println("source node "+sourceNode);
		Node targetNode=graph.getNodeFromKey(target);
		Edge edge=graph.getEdge(sourceNode, targetNode);
		long amt=edge.getLong(0);
		amt=amt-amount;
		if(amt>0){
			edge.setLong(0, amt);
		}else{
			System.out.println("removing edge");
			graph.removeEdge(edge);
		}
		if(!sourceNode.edges().hasNext()){
			graph.removeNode(sourceNode);
		}
		if(!targetNode.edges().hasNext()){
			graph.removeNode(targetNode);
		}
		java.util.Iterator<Edge> i=graph.edges();
		while(i.hasNext()){
			System.out.println("edge: "+i.next());
		}
		
	}
	
	public Edge addOutputEdge(long source, long target, long amount){
		System.out.println("Adding edge src "+source+" dst "+target+" btc "+amount);
		Node sourceNode=graph.getNodeFromKey(source);
		System.out.println("source node "+sourceNode);
		Node targetNode=graph.getNodeFromKey(target);
		Edge edge=graph.getEdge(sourceNode, targetNode);
		if(edge==null){
			edge=graph.addEdge(sourceNode, targetNode);
			edge.setLong(0, amount);
		}else{
			edge.setLong(0, edge.getLong(0)+amount);
		}
		java.util.Iterator<Edge> i=graph.edges();
		while(i.hasNext()){
			System.out.println("edge: "+i.next());
		}
		System.out.println("getkey "+graph.getNodeFromKey(source));
		return edge;
	}
	
	public void removeTransaction(SqlTransaction transaction){
		synchronized(viz){
			if(transaction.inputs.get(0).isCoinBase()){
				return;
			}
		System.out.println("Removing transaction");
		long sourceKey=((SqlTransactionInput) transaction.inputs.get(0)).getAddress().getWalletId();
		ArrayList<Long> targetKeys=new ArrayList<Long>();
		
		for(TransactionOutput o: transaction.outputs){
			SqlTransactionOutput out=(SqlTransactionOutput) o;
			long target=out.getAddress().getWalletId();
			removeOutputEdge(sourceKey,target, o.getValue().longValue());
		}
		
		}
		viz.run("color");
		viz.run("layout");
		
	}
	
	public void addTransaction(SqlTransaction transaction){
		Node srcNode;
		synchronized(viz){
			if(transaction.inputs.get(0).isCoinBase()){
				return;
			}
		System.out.println("Adding transaction");
		long sourceKey=((SqlTransactionInput) transaction.inputs.get(0)).getAddress().getWalletId();
		ArrayList<Long> targetKeys=new ArrayList<Long>();
		
		srcNode=findOrCreateWalletNode(sourceKey,"");
		for(TransactionOutput o: transaction.outputs){
			SqlTransactionOutput out=(SqlTransactionOutput) o;
			long target=out.getAddress().getWalletId();
			findOrCreateWalletNode(target,"");
			addOutputEdge(sourceKey,target, o.getValue().longValue());
		}
		
		}
		viz.run("color");
		viz.run("layout");
		VisualItem vi=viz.getVisualItem("graph", srcNode);
		System.out.println("position= "+vi.getStartX()+" "+ vi.getStartY());
		this.animatePanToAbs(new Point2D.Double(vi.getStartX(), vi.getStartY()),500);
		
	}
	
}

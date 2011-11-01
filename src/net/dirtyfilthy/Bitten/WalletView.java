package net.dirtyfilthy.Bitten;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.awt.event.MouseEvent;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument.Iterator;

import com.google.bitcoin.core.GraphAddress;
import com.google.bitcoin.core.GraphTransaction;
import com.google.bitcoin.core.GraphTransactionOutput;
import com.google.bitcoin.core.GraphWallet;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.WalletIdable;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.force.DragForce;
import prefuse.util.ui.JForcePanel;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class WalletView extends Display {
	private String label = "label";
	private Table walletNodeTable;
	private Table walletEdgeTable;
	private Graph graph;
	private Visualization viz;
	private ArrayList<Tuple> highlightedTuples;
	private ForceDirectedLayout forceDirected;
	private boolean showValues=false;
	public ControlPanel panel; // tightly coupled but fuck it
	
	  private static final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema(); 
	    static { 
	    	DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false); 
	    	DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.rgb(0,255,0)); 
	    	DECORATOR_SCHEMA.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma",8));
	    	DECORATOR_SCHEMA.setDefault(VisualItem.FILLCOLOR, ColorLib.rgb(0,0,0));
	    }
	
	    class LabelLayout2 extends Layout {
	        public LabelLayout2(String group) {
	            super(group);
	        }
	        public void run(double frac) {
	            java.util.Iterator iter = viz.items(m_group);
	            while ( iter.hasNext() ) {
	                DecoratorItem decorator = (DecoratorItem)iter.next();
	                VisualItem decoratedItem = decorator.getDecoratedItem();
	                Rectangle2D bounds = decoratedItem.getBounds();

	                double x = bounds.getCenterX();
	                double y = bounds.getCenterY();

	               /*
	                double x2 = 0, y2 = 0, x3 = 0, y3 = 0;
	                double m;
	                if (decoratedItem instanceof EdgeItem){
	                    VisualItem dest = ((EdgeItem)decoratedItem).getTargetItem();
	                    VisualItem src = ((EdgeItem)decoratedItem).getSourceItem();
	                    x2 = dest.getX();
	                    y2 = dest.getY();
	                    y3 = -(x2-x);
	                    x3 = (y2-y);
	                    m=Math.sqrt(x3*x3+y3*y3);
	                    x3=x3/m;
	                    y3=y3/m;
	                    x=x+(x3*10);
	                    y=y+(y3*10);
	                }
	                */
					boolean isSelf=((EdgeItem)decoratedItem).getTargetItem().equals(((EdgeItem)decoratedItem).getSourceItem());
	                if(isSelf){
	                	decoratedItem.setVisible(false);
	                }
	                if(showValues && !isSelf){
	                	decorator.setVisible(true);
	                }
	                else{
	                	decorator.setVisible(false);
	                }
	                
	                setX(decorator, null, x);
	                setY(decorator, null, y);
	            }
	        }
	    } // end of inner class LabelLayout    
	   
	WalletView() {
		this.setBackground(new Color(0, 0, 0));
		int[] palette = new int[] { ColorLib.rgb(255, 180, 180),
				ColorLib.rgb(190, 190, 255), ColorLib.rgb(190, 190, 0) };
		int coinbaseColor=ColorLib.rgb(0, 0, 255);
		highlightedTuples = new ArrayList<Tuple>();
		walletNodeTable = new Table();
		walletEdgeTable = new Table();
		walletNodeTable.addColumn("id", long.class);
		walletNodeTable.addColumn("label", String.class);
		walletNodeTable.addColumn("coinbase", boolean.class);
		walletEdgeTable.addColumn("btc", long.class);
	
		
		walletEdgeTable.addColumn("source", long.class);
		walletEdgeTable.addColumn("target", long.class);
		walletEdgeTable.addColumn("btc_d", double.class);
		graph = new Graph(walletNodeTable, walletEdgeTable, true, "id",
				"source", "target");
		 Node n1 = graph.addNode();
		 Node n2 = graph.addNode();
		 n1.setLong(0, 1);
		 n2.setLong(0, 2);
		 Edge e2=graph.addEdge(n1, n2);
		
		viz = new Visualization();
		viz.add("graph", graph);
		viz.getVisualItem("graph", n1).setVisible(false);
		 viz.getVisualItem("graph", n2).setVisible(false);
		 viz.getVisualItem("graph", e2).setVisible(false);
		// draw the "name" label for NodeItems
		// viz.getVisualItem("graph", e2).setVisible(false);
		LabelRenderer r = new LabelRenderer("label");
		r.setRoundedCorner(8, 8); // round the corners
		r.setHorizontalPadding(5);
		EdgeRenderer e = new EdgeRenderer(Constants.EDGE_TYPE_CURVE);
		
		e.setArrowType(Constants.EDGE_ARROW_FORWARD);
		e.setArrowHeadSize(10, 10);
		DefaultRendererFactory df = new DefaultRendererFactory(r, e);
		viz.setRendererFactory(df);
		LabelRenderer r2=new LabelRenderer("btc_d");
		r2.setRoundedCorner(5, 5);
		r2.setRenderType(LabelRenderer.RENDER_TYPE_DRAW_AND_FILL);
		df.add("INGROUP('edgeDeco')", r2);
		viz.addDecorators("edgeDeco", "graph.edges",DECORATOR_SCHEMA);
		ActionList layout = new ActionList(Activity.INFINITY);
		forceDirected = new ForceDirectedLayout("graph");
		// f.getForceSimulator().setIntegrator(new EulerIntegrator());
		forceDirected.getForceSimulator().addForce(new DragForce((float) 0.01));
		layout.add(forceDirected);
		
		// layout.add(new NodeLinkTreeLayout("graph"));
		layout.add(new RepaintAction());
		layout.add(new LabelLayout2("edgeDeco"));
		ColorAction fill = new ColorAction("graph.nodes", VisualItem.FILLCOLOR,
				ColorLib.rgb(40,40,40));
		ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR,
			ColorLib.gray(0));
		// use light grey for edges
		ColorAction stroke = new ColorAction("graph.nodes",
				VisualItem.STROKECOLOR, ColorLib.rgb(40, 230, 40));
		// stroke.add("coinbase", coinbaseColor);
		stroke.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
		ColorAction edges = new ColorAction("graph.edges",
				VisualItem.STROKECOLOR, ColorLib.gray(200));
		edges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
		ColorAction fill2 = new ColorAction("graph.edges",
				VisualItem.FILLCOLOR, ColorLib.gray(200));
		// fill2.add("coinbase", coinbaseColor);
		fill2.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
		DataSizeAction edgeWidth = new DataSizeAction("graph.edges", "btc_d");

		edgeWidth.setScale(Constants.LOG_SCALE);
		edgeWidth.setMaximumSize(50);
		ActionList color = new ActionList();
		color.add(text);
		color.add(new ColorAction("graph.edges",VisualItem.TEXTCOLOR,ColorLib.rgb(255,255,255)));
		color.add(new ColorAction("graph.nodes",VisualItem.TEXTCOLOR,ColorLib.rgb(255,255,255)));
		color.add(fill);
		color.add(fill2);
		color.add(stroke);
		color.add(edges);
		color.add(edgeWidth);
		viz.putAction("layout", layout);
		viz.putAction("color", color);
		setVisualization(viz);
		
		class NodeControl extends ControlAdapter {
			
		    public void itemClicked(VisualItem item, MouseEvent e) {
		    	 if (!SwingUtilities.isLeftMouseButton(e)) return;
		         if ( e.getClickCount() == 2 ) {//DoubleClick

		        	 NodeItem ni = (NodeItem) item;
		        	 GraphWallet w=new GraphWallet(panel.getBlockStore().graph().getNodeById(ni.getLong("id")));
		        	 panel.showWallet(w);		            
		         }
		    }
		}
		
		addControlListener(new DragControl()); // drag items around
		addControlListener(new PanControl()); // pan with background left-drag
		addControlListener(new ZoomControl()); // zoom with vertical right-drag
		addControlListener(new ZoomToFitControl()); // zoom with vertical right-drag
		addControlListener(new NodeControl()); // zoom with vertical right-drag
		registerKeyboardAction(new ActionListener  () {
			        public void actionPerformed(ActionEvent   e) {
			        	JForcePanel.showForcePanel(forceDirected.getForceSimulator());
			                repaint();
			           }
			        }, "show force panel", KeyStroke.getKeyStroke("alt F"),
			        WHEN_IN_FOCUSED_WINDOW );
		registerKeyboardAction(new ActionListener  () {
	        public void actionPerformed(ActionEvent   e) {
	        	if(showValues){
	        		showValues=false;
	        	}
	        	else{
	        		showValues=true;
	        	}
	        	repaint();
	           }
	        }, "show edges", KeyStroke.getKeyStroke("alt S"),
	        WHEN_IN_FOCUSED_WINDOW );
			
		this.setHighQuality(true);
		viz.run("color");
		// start up the animated layout
		viz.run("layout");

	}

	public Node findOrCreateWalletNode(GraphWallet w) {
		System.out.println("adding node " + w.node().getId());
		Node n = graph.getNodeFromKey(w.node().getId());
		if (n == null) {
			n = graph.addNode();
		}
		n.setLong(0, w.node().getId());
		n.setString(1, w.label());
		if(w.equals(GraphWallet.coinbaseWallet(w.node().getGraphDatabase()))){
			n.setBoolean(2,true);
		}
		else{
			n.setBoolean(2,false);
		}
		return n;
	}
	
	public void removeOutputEdge(GraphWallet source, GraphWallet target, long amount) {
		
		Node sourceNode = graph.getNodeFromKey(source.node().getId());
		System.out.println("source node " + sourceNode);
		Node targetNode = graph.getNodeFromKey(target.node().getId());
		if(sourceNode==null || targetNode == null){
			return;
		}
		Edge edge = graph.getEdge(sourceNode, targetNode);
		long amt = edge.getLong(0);
		amt = amt - amount;
		if (amt > 0) {
			edge.setLong(0, amt);
			edge.setDouble(3, Utils.btcToDouble(amt));
			
		} else {
			System.out.println("removing edge");
			graph.removeEdge(edge);
		}
		if (!sourceNode.edges().hasNext()) {
			graph.removeNode(sourceNode);
		}
		targetNode = graph.getNodeFromKey(target.node().getId());
		if (targetNode!=null && !targetNode.edges().hasNext()) {
			graph.removeNode(targetNode);
		}

	}

	public Edge addOutputEdge(GraphWallet source, GraphWallet target, long amount) {
		Node sourceNode = graph.getNodeFromKey(source.node().getId());
		System.out.println("source node " + sourceNode);
		Node targetNode = graph.getNodeFromKey(target.node().getId());
		Edge edge = graph.getEdge(sourceNode, targetNode);
		if (edge == null) {
			edge = graph.addEdge(sourceNode, targetNode);
			edge.setLong(0, amount);
			edge.setDouble(3, Utils.btcToDouble(amount));
		} else {
			edge.setDouble(3, Utils.btcToDouble(edge.getLong(0) + amount));
			edge.setLong(0, edge.getLong(0) + amount);
		}
		return edge;
	}

	public void removeTransaction(GraphTransaction transaction) {
		synchronized (viz) {
			GraphWallet source;
			if (transaction.inputs.get(0).isCoinBase()) {
				return; 
				//source=GraphWallet.coinbaseWallet(transaction.node().getGraphDatabase());
			}
			else{
				source = transaction.inputs.get(0).address().wallet();
			}
			ArrayList<Long> targetKeys = new ArrayList<Long>();

			for (GraphTransactionOutput o : transaction.outputs) {
	
				GraphWallet target = o.address().wallet();
				removeOutputEdge(source, target, o.getValue().longValue());
			}

		}
		viz.run("color");
		viz.run("layout");

	}

	public void addTransaction(GraphTransaction transaction) {
		Node srcNode;
		synchronized (viz) {
			GraphWallet source;
			if (transaction.inputs.get(0).isCoinBase()) {
				// source = GraphWallet.coinbaseWallet(transaction.node().getGraphDatabase());
				return;
			}
			else{
				source = transaction.inputs.get(0).address().wallet();
			}
			ArrayList<Long> targetKeys = new ArrayList<Long>();
		
			srcNode = findOrCreateWalletNode(source);
			for (GraphTransactionOutput o : transaction.outputs) {
				GraphWallet target = o.address().wallet();
				findOrCreateWalletNode(target);
				addOutputEdge(source, target, o.getValue().longValue());
				highlightNodes(source,target);
			}

		}
		viz.run("layout");
		viz.run("color");
	}
	
	public void panToNodeId(long id){
		Node n=graph.getNodeFromKey(id);
		panToNode(n);
	}
	
	public void panToNode(Node n){
	
		VisualItem vi = viz.getVisualItem("graph", n);
		Rectangle2D bounds=vi.getBounds();
		this.animatePanToAbs(
				new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()), 500);
	
	}
	
	public void panToAddress(GraphAddress a){
		panToNodeId(a.wallet().node().getId());
	}

	public void clearHighlights() {
		for (Tuple t : highlightedTuples) {
			try {
				VisualItem vi = viz.getVisualItem("graph", t);
				vi.setHighlighted(false);
			} catch (NullPointerException e) {
				continue;
			}
		}
		highlightedTuples.clear();
	}

	public void highlightTuple(Tuple t) {
		highlightedTuples.add(t);
		viz.getVisualItem("graph", t).setHighlighted(true);
	}

	public void highlightNodes(GraphWallet source, GraphWallet target) {
		Node sourceNode = graph.getNodeFromKey(source.node().getId());
		Node targetNode = graph.getNodeFromKey(target.node().getId());
		if (targetNode == null || sourceNode == null) {
			viz.run("color");
			return;
			
		}
		System.out.println("setting highlight");
		highlightTuple(sourceNode);
		highlightTuple(targetNode);
		Edge e = graph.getEdge(sourceNode, targetNode);
		if (e != null) {
			highlightTuple(e);
		}
		viz.run("color");
	}

	public void panToWallet(WalletIdable target) {
		panToNodeId(target.getWalletId());
		
	}

}

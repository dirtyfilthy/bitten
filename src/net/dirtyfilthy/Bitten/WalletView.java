package net.dirtyfilthy.Bitten;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.KeyStroke;
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
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.DragForce;
import prefuse.util.ui.JForcePanel;
import prefuse.visual.VisualItem;

public class WalletView extends Display {
	private String label = "label";
	private Table walletNodeTable;
	private Table walletEdgeTable;
	private Graph graph;
	private Visualization viz;
	private ArrayList<Tuple> highlightedTuples;
	private ForceDirectedLayout forceDirected;

	WalletView() {
		this.setBackground(new Color(0, 0, 0));
		int[] palette = new int[] { ColorLib.rgb(255, 180, 180),
				ColorLib.rgb(190, 190, 255), ColorLib.rgb(190, 190, 0) };
		highlightedTuples = new ArrayList<Tuple>();
		walletNodeTable = new Table();
		walletEdgeTable = new Table();
		walletNodeTable.addColumn("id", long.class);
		walletNodeTable.addColumn("label", String.class);
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
		// draw the "name" label for NodeItems
		viz.getVisualItem("graph", e2).setVisible(false);
		LabelRenderer r = new LabelRenderer("label");
		r.setRoundedCorner(8, 8); // round the corners
		EdgeRenderer e = new EdgeRenderer(Constants.EDGE_TYPE_CURVE);
		e.setArrowType(Constants.EDGE_ARROW_FORWARD);
		e.setArrowHeadSize(10, 10);

		viz.setRendererFactory(new DefaultRendererFactory(r, e));
		ActionList layout = new ActionList(Activity.INFINITY);
		forceDirected = new ForceDirectedLayout("graph");
		// f.getForceSimulator().setIntegrator(new EulerIntegrator());
		forceDirected.getForceSimulator().addForce(new DragForce((float) 0.01));
		layout.add(forceDirected);
		
		// layout.add(new NodeLinkTreeLayout("graph"));
		layout.add(new RepaintAction());

		ColorAction fill = new ColorAction("graph.nodes", VisualItem.FILLCOLOR,
				ColorLib.gray(100));
		ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR,
			ColorLib.gray(0));
		// use light grey for edges
		ColorAction stroke = new ColorAction("graph.nodes",
				VisualItem.STROKECOLOR, ColorLib.rgb(0, 0, 0));
		stroke.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
		ColorAction edges = new ColorAction("graph.edges",
				VisualItem.STROKECOLOR, ColorLib.gray(200));
		edges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
		ColorAction fill2 = new ColorAction("graph.edges",
				VisualItem.FILLCOLOR, ColorLib.gray(200));
		fill2.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
		DataSizeAction edgeWidth = new DataSizeAction("graph.edges", "btc_d");

		edgeWidth.setScale(Constants.LOG_SCALE);
		edgeWidth.setMaximumSize(50);
		ActionList color = new ActionList();
		color.add(text);
		color.add(fill);
		color.add(fill2);
		color.add(stroke);
		color.add(edges);
		color.add(edgeWidth);
		viz.putAction("layout", layout);
		viz.putAction("color", color);
		setVisualization(viz);

		addControlListener(new DragControl()); // drag items around
		addControlListener(new PanControl()); // pan with background left-drag
		addControlListener(new ZoomControl()); // zoom with vertical right-drag
		addControlListener(new ZoomToFitControl()); // zoom with vertical right-drag
		
		registerKeyboardAction(new ActionListener  () {
			        public void actionPerformed(ActionEvent   e) {
			        	JForcePanel.showForcePanel(forceDirected.getForceSimulator());
			                repaint();
			           }
			        }, "show force panel", KeyStroke.getKeyStroke("alt F"),
			               WHEN_FOCUSED);

			
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
		return n;
	}
	
	public void removeOutputEdge(GraphWallet source, GraphWallet target, long amount) {
		System.out.println("Removing edge src " + source + " dst " + target
				+ " btc " + amount);
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
		System.out.println("Adding edge src " + source + " dst " + target
				+ " btc " + amount);
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
		java.util.Iterator<Edge> i = graph.edges();
		while (i.hasNext()) {
			System.out.println("edge: " + i.next());
		}
		return edge;
	}

	public void removeTransaction(GraphTransaction transaction) {
		synchronized (viz) {
			if (transaction.inputs.get(0).isCoinBase()) {
				return;
			}
			System.out.println("Removing transaction");
			GraphWallet source = transaction.inputs.get(0).address().wallet();
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
			if (transaction.inputs.get(0).isCoinBase()) {
				return;
			}
			System.out.println("Adding transaction");
			GraphWallet source = transaction.inputs.get(0).address().wallet();
			
			ArrayList<Long> targetKeys = new ArrayList<Long>();
		
			srcNode = findOrCreateWalletNode(source);
			for (GraphTransactionOutput o : transaction.outputs) {
				GraphWallet target = o.address().wallet();
				findOrCreateWalletNode(target);
				addOutputEdge(source, target, o.getValue().longValue());
				highlightNodes(source,target);
			}

		}
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

package net.dirtyfilthy.Bitten;

import java.sql.Connection;
import java.sql.SQLException;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.io.DataIOException;
import prefuse.data.io.sql.ConnectionFactory;
import prefuse.data.io.sql.DatabaseDataSource;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

public class TransactionView extends Display {
	private DatabaseDataSource dbSource;
	private String label="label";
	private Table transactionNodeTable=new Table();
	private Table transactionEdgeTable=new Table();
	private Graph graph;
	private Visualization viz;
	
	public TransactionView(Connection c) throws SQLException{
		int[] palette = new int[] {
			    ColorLib.rgb(255,180,180), ColorLib.rgb(190,190,255), ColorLib.rgb(190,190,0)
			};
		dbSource=ConnectionFactory.getDatabaseConnection(c);
		transactionNodeTable.addColumn("id",int.class);
		transactionNodeTable.addColumn("color",int.class);
		transactionEdgeTable.addColumn("id", int.class);
		transactionEdgeTable.addColumn("source", int.class);
		transactionEdgeTable.addColumn("target", int.class);
		
		graph=new Graph(transactionNodeTable, transactionEdgeTable, true, "id", "source","target");
		
		viz=new Visualization();
		viz.add("graph", graph);
		// draw the "name" label for NodeItems
		LabelRenderer r = new LabelRenderer("id");
		r.setRoundedCorner(8, 8); // round the corners
		EdgeRenderer e = new EdgeRenderer();
		e.setArrowType(Constants.EDGE_ARROW_FORWARD);
		e.setArrowHeadSize(8, 8);
		

		// create a new default renderer factory
		// return our name label renderer as the default for all non-EdgeItems
		// includes straight line edges for EdgeItems by default
		viz.setRendererFactory(new DefaultRendererFactory(r,e));
		ActionList layout = new ActionList(Activity.INFINITY);
		layout.add(new ForceDirectedLayout("graph"));
		layout.add(new RepaintAction());
		DataColorAction fill = new DataColorAction("graph.nodes", "color",
			    Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		ColorAction text = new ColorAction("graph.nodes",
                VisualItem.TEXTCOLOR, ColorLib.gray(0));
        // use light grey for edges
        ColorAction edges = new ColorAction("graph.edges",
                VisualItem.STROKECOLOR, ColorLib.gray(200));
        ColorAction fill2 = new ColorAction("graph.edges",
                VisualItem.FILLCOLOR, ColorLib.gray(200));
        ActionList color = new ActionList();
        color.add(text);
        color.add(edges);
        color.add(fill);
        color.add(fill2);
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
	
	public void loadTransaction(long id){
		String sql="";
		synchronized(viz){
		try {
			sql="SELECT (id+99999) AS id, is_coinbase AS color FROM transactions  WHERE id="+id;
			dbSource.getData(transactionNodeTable,sql,"id");
		
		sql="SELECT previous_output_id AS id,2 AS color FROM transaction_inputs WHERE previous_output_id!=0 AND transaction_id="+id;
		dbSource.getData(transactionNodeTable,sql,"id");
		sql="SELECT id AS id,2 AS color FROM transaction_outputs WHERE transaction_id="+id;
		dbSource.getData(transactionNodeTable,sql,"id");
		sql="SELECT id as id, (transaction_id+99999) as target, previous_output_id as source FROM transaction_inputs WHERE previous_output_id!=0 AND transaction_id="+id;
		dbSource.getData(transactionEdgeTable,sql,"id");
		sql="SELECT (id+99999) as id, (transaction_id+99999) as source, id as target FROM transaction_outputs WHERE transaction_id="+id;
		dbSource.getData(transactionEdgeTable,sql,"id");
		viz.run("color");
		
		} catch (DataIOException e) {
			throw new RuntimeException(e);
		}
		}
		
	}

}

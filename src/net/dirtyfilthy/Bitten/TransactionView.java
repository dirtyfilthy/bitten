package net.dirtyfilthy.Bitten;

import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
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
import prefuse.util.force.DragForce;
import prefuse.util.force.EulerIntegrator;
import prefuse.visual.VisualItem;

public class TransactionView extends Display {
	private DatabaseDataSource dbSource;
	private String label="label";
	private Table transactionNodeTable=new Table();
	private Table transactionEdgeTable=new Table();
	private Graph graph;
	private Visualization viz;
	private Connection db;
	
	
	public TransactionView(Connection c) throws SQLException{
		db=c;
		int[] palette = new int[] {
			    ColorLib.rgb(255,180,180), ColorLib.rgb(190,190,255), ColorLib.rgb(190,190,0)
			};
		dbSource=ConnectionFactory.getDatabaseConnection(c);
		transactionNodeTable.addColumn("id",int.class);
		transactionNodeTable.addColumn("type",int.class);
		transactionNodeTable.addColumn("transaction_id",int.class);
		transactionNodeTable.addColumn("btc",double.class);
		transactionEdgeTable.addColumn("id", int.class);
		transactionEdgeTable.addColumn("source", int.class);
		transactionEdgeTable.addColumn("target", int.class);
		transactionEdgeTable.addColumn("btc",double.class);
		
		graph=new Graph(transactionNodeTable, transactionEdgeTable, true, "id", "source","target");
		
		viz=new Visualization();
		viz.add("graph", graph);
		// draw the "name" label for NodeItems
		LabelRenderer r = new LabelRenderer("id");
		r.setRoundedCorner(8, 8); // round the corners
		EdgeRenderer e = new EdgeRenderer();
		e.setArrowType(Constants.EDGE_ARROW_FORWARD);
		e.setArrowHeadSize(8, 8);
		FocusControl focusControl=new FocusControl(2){
			public void itemClicked(VisualItem i, java.awt.event.MouseEvent e){
				int id=i.getInt("id");
				int type=i.getInt("type");
				int transaction_id=i.getInt("transaction_id");
				if(type==2){
					expandOutput(id,false);
				}
				else if(type==0 || type==1){
					expandTransaction(transaction_id);
				}
				
			}
		};
		
		ControlAdapter keyboardAdapter=new ControlAdapter(){
			public void keyTyped(KeyEvent e){
				processKeys(e);
			}
			
			
			
			private void processKeys(KeyEvent e){
				switch(e.getKeyChar()){
					case 's':
						showSearchDialog();
						break;
				}
				
			}
			
		};
		

		// create a new default renderer factory
		// return our name label renderer as the default for all non-EdgeItems
		// includes straight line edges for EdgeItems by default
		viz.setRendererFactory(new DefaultRendererFactory(r,e));
		ActionList layout = new ActionList(Activity.INFINITY);
		ForceDirectedLayout f=new ForceDirectedLayout("graph");
		// f.getForceSimulator().setIntegrator(new EulerIntegrator());
		f.getForceSimulator().addForce(new DragForce((float) 0.01));
		layout.add(f);
		//layout.add(new NodeLinkTreeLayout("graph"));
		layout.add(new RepaintAction());
		DataColorAction fill = new DataColorAction("graph.nodes", "type",
			    Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
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
        color.add(edges);
        color.add(fill);
        color.add(fill2);
        color.add(edgeWidth);
		viz.putAction("layout", layout);
        viz.putAction("color", color);
		setSize(720, 500); // set display size
		setVisualization(viz);
		addControlListener(new DragControl()); // drag items around
		addControlListener(new PanControl());  // pan with background left-drag
		addControlListener(new ZoomControl()); // zoom with vertical right-drag
		addControlListener(focusControl);
		viz.run("color");
        // start up the animated layout
        viz.run("layout");
	}
	
	
	public void showSearchDialog(){
		String s = (String)JOptionPane.showInputDialog(this, "Search:","Search", JOptionPane.QUESTION_MESSAGE);
		expandStringAddress(s, true);
	}

	public void expandTransaction(final long transactionId){
		System.out.println("expanding tran "+transactionId);
		Runnable r=new Runnable() {
			public void run(){
		try {
			
			ArrayList<Long> a=new ArrayList<Long>();
			PreparedStatement s=db.prepareStatement("SELECT id FROM transaction_outputs WHERE transaction_id=? UNION SELECT previous_output_id as id FROM transaction_inputs WHERE transaction_id=? and previous_output_id!=0");
			s.setLong(1, transactionId);
			s.setLong(2, transactionId);
			ResultSet rs=s.executeQuery();
			while(rs.next()){
				a.add(rs.getLong(1));
			}
			rs.close();
			
				expandOutputMulti((Long[]) a.toArray(new Long[a.size()]),true);
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			}
			};
			new Thread(r).start();
	
	}
	
	public void expandOutputMulti(final Long[] idz, boolean sync){
		System.out.println("expanding out "+idz);
			Runnable r=new Runnable() {
			public void run(){
				try {
					StringBuilder builder = new StringBuilder();
					
					 for (int i = 0; i < idz.length;) {
					        builder.append(idz[i]);
					        if (++i < idz.length) {
					            builder.append(",");
					        }
					    }
			ArrayList<Long> a=new ArrayList<Long>();
			PreparedStatement s=db.prepareStatement("SELECT DISTINCT (transaction_id) as id from transaction_outputs where id IN ("+builder+") UNION SELECT DISTINCT (transaction_id) as id from transaction_inputs where previous_output_id IN ("+builder+")");
			System.out.println("execute query");
			ResultSet rs=s.executeQuery();
			while(rs.next()){
				a.add(rs.getLong(1));
			}
			rs.close();
			
				loadTransaction((Long[]) a.toArray(new Long[a.size()]));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			}
		};
		if(sync){
			r.run();
		}
		else{
			new Thread(r).start();
		}
	
	}
	
	public void expandStringAddress(final String address, boolean sync){
		Runnable r=new Runnable() {
			public void run(){
				try {
			ArrayList<Long> a=new ArrayList<Long>();
			PreparedStatement s=db.prepareStatement("SELECT DISTINCT (transactions.id) from transactions left join transaction_inputs ON transactions.id=transaction_inputs.transaction_id LEFT JOIN transaction_outputs AS prev_outputs ON transaction_inputs.previous_output_id=prev_outputs.id LEFT JOIN transaction_outputs ON transactions.id=transaction_outputs.transaction_id JOIN addresses ON transaction_outputs.address_id=addresses.id OR prev_outputs.address_id=addresses.id WHERE addresses.base58hash=?");
			s.setString(1, address.trim());
			System.out.println("execute query");
			ResultSet rs=s.executeQuery();
			while(rs.next()){
				a.add(rs.getLong(1));
			}
			rs.close();
			System.out.println("load transaction");
			
			loadTransaction((Long[]) a.toArray(new Long[a.size()]));
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			}
		};
		if(sync){
			r.run();
		}
		else{
			new Thread(r).start();
		}
	
	}
		
	
	
	public void expandOutput(final long outputId, boolean sync){
		System.out.println("expanding out "+outputId);
			Runnable r=new Runnable() {
			public void run(){
				try {
			ArrayList<Long> a=new ArrayList<Long>();
			PreparedStatement s=db.prepareStatement("SELECT DISTINCT (transactions.id) from transactions left join transaction_inputs ON transactions.id=transaction_inputs.transaction_id LEFT JOIN transaction_outputs ON transactions.id=transaction_outputs.transaction_id WHERE transaction_outputs.id=? OR transaction_inputs.previous_output_id=?");
			s.setLong(1, outputId);
			s.setLong(2, outputId);
			System.out.println("execute query");
			ResultSet rs=s.executeQuery();
			while(rs.next()){
				a.add(rs.getLong(1));
			}
			rs.close();
			System.out.println("load transaction");
			
			loadTransaction((Long[]) a.toArray(new Long[a.size()]));
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			}
		};
		if(sync){
			r.run();
		}
		else{
			new Thread(r).start();
		}
	
	}
	
	public void loadTransaction(Long[] idz){
		String sql="";
		StringBuilder builder = new StringBuilder();
		
		 for (int i = 0; i < idz.length;) {
		        builder.append(idz[i]);
		        if (++i < idz.length) {
		            builder.append(",");
		        }
		    }
		synchronized(viz){
		try {
			sql="SELECT (id+9999999) AS id, is_coinbase AS type, id as transaction_id, 0.0 AS btc FROM transactions  WHERE id IN("+builder+")";
			dbSource.getData(transactionNodeTable,sql,"id");
		
		sql="SELECT previous_output_id AS id,2 AS type, transaction_inputs.transaction_id, value/100000000.0 AS btc FROM transaction_inputs  LEFT JOIN transaction_outputs ON transaction_outputs.id=previous_output_id WHERE previous_output_id!=0 AND transaction_inputs.transaction_id IN("+builder+")";
		dbSource.getData(transactionNodeTable,sql,"id");
		sql="SELECT id AS id,2 AS type, transaction_id,value/100000000.0 AS btc FROM transaction_outputs WHERE transaction_id IN("+builder+")";
		dbSource.getData(transactionNodeTable,sql,"id");
		sql="SELECT transaction_inputs.id as id, (transaction_inputs.transaction_id+9999999) as target, previous_output_id as source, value/100000000.0 AS btc FROM transaction_inputs  LEFT JOIN transaction_outputs ON transaction_outputs.id=previous_output_id WHERE previous_output_id!=0 AND transaction_inputs.transaction_id IN("+builder+")";
		dbSource.getData(transactionEdgeTable,sql,"id");
		sql="SELECT (id+9999999) as id, (transaction_id+9999999) as source, id as target, value/100000000.0 AS btc FROM transaction_outputs WHERE transaction_id IN("+builder+")";
		dbSource.getData(transactionEdgeTable,sql,"id");
		viz.run("color");
		
		} catch (DataIOException e) {
			throw new RuntimeException(e);
		}
		}
		
	}

}

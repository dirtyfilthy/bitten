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

public class AddressView extends Display {
	private DatabaseDataSource dbSource;
	private String label="label";
	private Table transactionNodeTable=new Table();
	private Table transactionEdgeTable=new Table();
	private Graph graph;
	private Visualization viz;
	private Connection db;
	
	
	public AddressView(Connection c) throws SQLException{
		db=c;
		int[] palette = new int[] {
			    ColorLib.rgb(255,180,180), ColorLib.rgb(190,190,255), ColorLib.rgb(190,190,0)
			};
		dbSource=ConnectionFactory.getDatabaseConnection(c);
		transactionNodeTable.addColumn("id",int.class);
		transactionNodeTable.addColumn("type",int.class);
		transactionNodeTable.addColumn("btc",double.class);
		transactionEdgeTable.addColumn("id", int.class);
		transactionEdgeTable.addColumn("source", int.class);
		transactionEdgeTable.addColumn("target", int.class);
		transactionEdgeTable.addColumn("btc",double.class);
		transactionEdgeTable.addColumn("type",int.class);
		
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
				
				expandAddress(id);
				
				
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
        DataColorAction edgeColor = new DataColorAction("graph.edges", "type", Constants.NUMERICAL, VisualItem.STROKECOLOR,new int[] {ColorLib.rgb(255,0,0),ColorLib.rgb(0,255,0)});
        edgeColor.setBinCount(2);
        
        edgeWidth.setScale(Constants.LOG_SCALE);
        edgeWidth.setMaximumSize(20);
        ActionList color = new ActionList();
        color.add(text);
        color.add(edgeColor);
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
		addControlListener(keyboardAdapter);
		viz.run("color");
        // start up the animated layout
        viz.run("layout");
	}
	
	
	public void showSearchDialog(){
		String s = (String)JOptionPane.showInputDialog(this, "Search:","Search", JOptionPane.QUESTION_MESSAGE);
		expandStringAddress(s, true);
	}

	public void expandAddress(final long addressId){
		System.out.println("expanding tran "+addressId);
		Runnable r=new Runnable() {
			public void run(){
		try {
			
			ArrayList<Long> a=new ArrayList<Long>();
			PreparedStatement s=db.prepareStatement("SELECT from_address_id AS id FROM transaction_outputs LEFT JOIN transaction_inputs ON transaction_inputs.transaction_id=transaction_outputs.transaction_id  WHERE to_address_id=? UNION SELECT to_address_id as id FROM transaction_inputs LEFT JOIN transaction_outputs ON transaction_inputs.transaction_id=transaction_outputs.transaction_id  WHERE from_address_id=? ");
			s.setLong(1, addressId);
			s.setLong(2, addressId);
			ResultSet rs=s.executeQuery();
			while(rs.next()){
				a.add(rs.getLong(1));
			}
			rs.close();
			
		      loadAddress((Long[]) a.toArray(new Long[a.size()]));
			
			
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
			
		//		loadTransaction((Long[]) a.toArray(new Long[a.size()]));
			
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
			PreparedStatement s=db.prepareStatement("SELECT DISTINCT id from addresses where addresses.base58hash=? ");
			s.setString(1, address.trim());
			System.out.println("execute query find address "+address);
			ResultSet rs=s.executeQuery();
			System.out.println("after exec");
			while(rs.next()){
				a.add(rs.getLong(1));
			}
			rs.close();
			System.out.println("load transaction");
			
			loadAddress((Long[]) a.toArray(new Long[a.size()]));
			
			
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
			
		//	loadTransaction((Long[]) a.toArray(new Long[a.size()]));
			
			
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
	
	public void loadAddress(Long[] idz){
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
			sql="SELECT id AS id, 0 as type, 0.0 AS btc FROM addresses  WHERE id IN("+builder+")";
			dbSource.getData(transactionNodeTable,sql,"id");
		
		sql="SELECT DISTINCT from_address_id AS id, 0 AS type, 0 AS btc FROM transaction_outputs  LEFT JOIN transaction_inputs ON transaction_outputs.transaction_id=transaction_inputs.transaction_id WHERE transaction_outputs.to_address_id IN("+builder+") AND from_address_id IS NOT NULL";
		dbSource.getData(transactionNodeTable,sql,"id");
		sql="SELECT DISTINCT to_address_id AS id, 0 AS type, 0 AS btc FROM transaction_inputs  LEFT JOIN transaction_outputs ON transaction_outputs.transaction_id=transaction_inputs.transaction_id  WHERE transaction_inputs.from_address_id IN("+builder+") AND to_address_id IS NOT NULL";
		dbSource.getData(transactionNodeTable,sql,"id");
		sql="SELECT transaction_outputs.id+(99*transaction_inputs.id) as id, to_address_id  as target, from_address_id as source, value/100000000.0 AS btc, 0 AS type FROM transaction_inputs  LEFT JOIN transaction_outputs ON transaction_outputs.transaction_id=transaction_inputs.transaction_id WHERE from_address_id IN ("+builder+") AND to_address_id NOT NULL";
		dbSource.getData(transactionEdgeTable,sql,"id");
		sql="SELECT transaction_outputs.id+(99*transaction_inputs.id) as id, to_address_id  as target, from_address_id as source, value/100000000.0 AS btc, 0 AS type FROM transaction_outputs  LEFT JOIN transaction_inputs ON transaction_outputs.transaction_id=transaction_inputs.transaction_id WHERE to_address_id IN ("+builder+") AND from_address_id NOT NULL";
		dbSource.getData(transactionEdgeTable,sql,"id");
		viz.run("color");
		
		} catch (DataIOException e) {
			throw new RuntimeException(e);
		}
		}
		
	}

}

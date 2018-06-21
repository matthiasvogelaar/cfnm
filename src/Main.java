import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.JFrame;

public class Main {
	
	/*
	 * KEYBOARD CONTROLS:
	 * S - Safe the current polygon / startpos to a file named polygon.cfn
	 * L - Try to load a polygon / startpos from a file named polygon.cfn
	 * Q - Reset the startpos for the navigator
	 * R - Completely reset the state
	 * O - Restart the navigation, reset the navigator to its start pos
	 */
	
	DataContainer data;
	Interspace space;
	ProgramState state;
	GeometryDerivative gem;
	Thread alg;

	public Main() {
		// create the data container
		// test();
		gem = new GeometryDerivative(new EuclGeometry());
		data = new DataContainer();
		space = new Interspace(this, data, gem);
		state = ProgramState.BUILDING_POLYGON;
		
		// create the interspace
		JFrame frame = new JFrame("CFN");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add( space );
		frame.pack();
		frame.setVisible(true);
		
		updateState();
	}
	
	
	public void processKeyStroke(String c) {
		System.out.println("Pressed: " + c);
		switch( c.charAt(0) ) {
		case 'S':
			saveToFile();
			break;
		case 'L':
			if( state != ProgramState.FINDING_PATH) {
				loadFromFile();
				updateState();
			}
			break;
		case 'R':
			reset();
		break;
		case 'Q':
			resetStartPos();
		break;
		case 'O':
			resetAlgorithmRun();
		break;
		}
	}
	
	public void processClickEvent(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int button = e.getButton();
		System.out.println("(" + x + "," + y + ") " + button);
			
		ProgramState prevState = state;
		
		switch( state ) {
		case BUILDING_POLYGON:
			if( button == MouseEvent.BUTTON1 )
				addVertex(x,y);
			else if( button == MouseEvent.BUTTON2 )
				removeLastVertex();
			else if( button == MouseEvent.BUTTON3 )
				state = ProgramState.SET_START_POS;
			break;
		case FIRE_AT_WILL:
			state = ProgramState.FINDING_PATH;
			break;
		case FINDING_PATH:
			break;
		case DONE:
			// reset the nav related stuff in the data container
			data.resetNavigator();
			repaint();			
			// redo the algorithm
			state = ProgramState.FIRE_AT_WILL;
			break;
		case SET_LOS:
			
			Point navPos = data.getNavigatorPos();
			Point o = gem.extendLine(navPos, new Point(x,y));
			Intersection i = gem.linePolygonIntersect( navPos, o, data.getPolygon() );
			if( i != null ) {
				data.setLOS( i.intersection, i.l1 );
				state = ProgramState.FIRE_AT_WILL;
			}
			break;
		case SET_START_POS:
			data.setNavigatorPos( (float) x, (float) y);
			data.addPathVertex( new Point(x,y));
			state = ProgramState.SET_LOS;
			break;
		default:
			break;
		}
		
		if( state != prevState )
			updateState();
	}
	
	private void updateState() {
		switch( state ) {
		case BUILDING_POLYGON:
			space.setHeadline("Left-click to add polygon vertices. Right-click to continue");
			break;
		case FIRE_AT_WILL:
			space.setHeadline("Click to start the algorithm");
			break;
		case FINDING_PATH:
			space.setHeadline("Algorithm is running...");
			Algorithm a = new Algorithm(this, data, gem );
			alg = new Thread(a);
			alg.start();
			break;
		case SET_LOS:
			space.setHeadline("Click on a wall to set the initial line of sight");
			break;
		case SET_START_POS:
			space.setHeadline("Click within the polygon to set the start point");
			break;
		case DONE:
			space.setHeadline("Algorithm done! Click to redo.");
		default:
			break;
		
		}
	}
	
	private void saveToFile() {
		try {
			String filename = "polygon.cfn";
			Writer output = new OutputStreamWriter(new FileOutputStream(filename));
			
			// write the points that make up the polygon
			ArrayList<Point> poly = data.getPolygon();
			for(int i = 0, is = poly.size(); i < is; i++) {
				Point p = poly.get(i);
				output.write("P:" + p.x() + " " + p.y() + "\n");
			}
			// write the start location
			Point startPos = data.getStartPos();
			if( startPos != null )
				output.write("S:" + startPos.x() + " " + startPos.y() + "\n");
			// write the start LOS
			Point startLos = data.getStartLOSPoint();
			if( startLos != null) {
				Line startLosLine = data.getStartLOSLine();
				output.write("LP:" + startLos.x() + " " + startLos.y() + "\n");
				output.write("L1:" + startLosLine.p1.x() + " " + startLosLine.p1.y() + "\n");
				output.write("L2:" + startLosLine.p2.x() + " " + startLosLine.p2.y() + "\n" );
			}
			output.close();
		} catch( Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadFromFile() {
		try {
			data.reset();
			BufferedReader input = new BufferedReader( new InputStreamReader( new FileInputStream( "polygon.cfn" )));

			int polyCount = 0;
			
			Point LOS = null;
			Point L1 = null;
			Point L2 = null;
			while(input.ready()) {
				String line = input.readLine();
				if( line == null || line.length() <= 0)
					break;
				int indexOf = line.indexOf(':');
				if( indexOf < 0 || line.length() == indexOf + 1)
					continue;
				String prefix = line.substring(0, indexOf);
				String suffix1 = line.substring(indexOf + 1);
				indexOf = suffix1.indexOf(" ");
				if( indexOf < 0 || suffix1.length() == indexOf + 1)
					continue;
				String suffix2 = suffix1.substring(indexOf + 1);
				suffix1 = suffix1.substring(0, indexOf);
				double coordX = Double.parseDouble( suffix1 );
				double coordY = Double.parseDouble( suffix2 );
				
				switch( prefix ) {
				case "P":
					data.addVertexToPolygon((int) coordX, (int) coordY);
					polyCount++;
				break;
				case "L1":
					L1 = new Point( coordX, coordY);
					break;
				case "L2":
					L2 = new Point( coordX, coordY);
					break;
				case "S":
					data.setNavigatorPos(coordX, coordY);
					break;
				case "LP":
					LOS = new Point( coordX, coordY );
					break;
				}
			}
			input.close();
			// check if all the LOS variables have been set and a polygon has been defined
			if( polyCount < 3) {
				data.reset();
				state = ProgramState.BUILDING_POLYGON;
				updateState();
				repaint();
				return;
			}
			if( LOS == null || L1 == null || L2 == null) {
				state = ProgramState.SET_START_POS;
				updateState();
				repaint();
				return;
			}
			ArrayList<Point> poly = data.getPolygon();
			// search for the L1 and L2 points
			boolean foundL1 = false;
			boolean foundL2 = false;
			for(int i = 0, is = poly.size(); i < is; i++) {
				if( foundL1 && foundL2 ) break;
				Point p = poly.get(i);
				if( p.equals(L1) ) {
					L1 = p;
					foundL1 = true;
					continue;
				}
				if( p.equals(L2) ) {
					L2 = p;
					foundL2 = true;
					continue;
				}
			}
			
			if( !foundL1 || !foundL2 ) {
				state = ProgramState.SET_LOS;
				updateState();
				repaint();
				return;
			}
			data.addPathVertex(data.getNavigatorPos());
			data.setLOS(LOS, new Line(L1,L2) );
			state = ProgramState.FIRE_AT_WILL;
			updateState();
			repaint();
		} catch( Exception e) {
			e.printStackTrace();
		}
	}
	
	public void reset() {
		// Stop the algorithm from running if it is currently running
		if( alg != null ) {
			alg.interrupt();
			alg = null;
		}
		data.reset();
		state = ProgramState.BUILDING_POLYGON;
		updateState();
		repaint();
	}
	
	public void resetAlgorithmRun() {
		if( alg != null ) {
			alg.interrupt();
			alg = null;
			data.resetNavigator();
			data.resetPointsToDraw();
			data.resetLinesToDraw();
			state = ProgramState.FIRE_AT_WILL;
			updateState();
			repaint();
		}
	}
	
	public void resetStartPos() {
		// Stop the algorithm from running if it is currently running
		if( state == ProgramState.BUILDING_POLYGON)
			return;
		if( alg != null ) {
			alg.interrupt();
			alg = null;
		}
		data.resetStartPos();
		state = ProgramState.SET_START_POS;
		updateState();
		repaint();
	}
	
	public void repaint() {
		space.repaint();
	}
	
	private void addVertex(int x, int y) {
		data.addVertexToPolygon(x, y);
		space.repaint();
	}
	
	private void removeLastVertex() {
		data.removeLastVertexOfPolygon();
		space.repaint();
	}
	
	public ProgramState getState() {
		return state;
	}
	
	public void algorithmDone() {
		state = ProgramState.DONE;
		updateState();
	}
	
	public static void main( String args[] ) {
		new Main();
	}
}

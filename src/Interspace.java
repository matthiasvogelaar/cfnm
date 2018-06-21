import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;

public class Interspace extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
	
	private static final long serialVersionUID = 1L;
	
	public static final int width = 640;
	public static final int height = 480;
	// own data
	int mousePos[];
	String headline;
	// external references
	DataContainer data;
	Main controller;
	GeometryDerivative gem;
	
	Point list[];
	int listIndex;
	
	public Interspace(Main m, DataContainer d, GeometryDerivative gem) {
		// set some custom data
		mousePos = new int[2];
		headline = "";
		list = new Point[6];
		listIndex = 0;
		
		// supply it a reference to our data container
		data = d;
		controller = m;
		this.gem = gem;
		
		// add an on-click event
		addMouseListener( this );
		addMouseMotionListener( this );
		addKeyListener( this );
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(Interspace.width,Interspace.height);
	}
	
	public void setHeadline(String s) {
		headline = s;
		repaint();
	}
	
	public boolean isFocusable() {
		return true;
	}
	
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// paintTesting(g);
		paintProgram( g );
	}
	
	private void paintProgram( Graphics g ) {
		g.setColor(Color.BLACK);
		g.drawString(headline, 10, 10);
		
		// draw the polygon
		g.setColor(Color.BLACK);
		ArrayList<Point> polygon = data.getPolygon();
		for(int i = 0, is = polygon.size(); i < is; i++) {
			Point current = polygon.get(i);
			Point next = polygon.get((i+1) % is);
			// draw the current point
			gem.drawPoint(g, current );
			// draw the line between the two points
			gem.drawLine(g, current, next);
			g.drawString(current.getId() + "", current.xI()-5, current.yI()+14);
		}
		// draw a line for setting the LOS if we are in that state
		
		// draw the LOS
		g.setColor(Color.BLUE);
		Point navPos = data.getNavigatorPos();
		Point los = data.getLOSPoint();
		if( los != null ) {
			gem.drawPoint(g, los );
			gem.drawLine(g, navPos, los );
		}
		
		// draw the path that has been traversed
		ArrayList<Point> path = data.getPath();
		Point prevPoint = null;
		for(int i = 0, is = path.size(); i < is; i++) {
			Point p = path.get(i);
			gem.drawPoint(g, p);
			if( prevPoint != null )
				gem.drawLine(g, prevPoint, p );
			prevPoint = p;
		}
		// draw a line from the last node to the navigator if the navigator currently isn't on the last navpoint
		if( prevPoint != navPos)
			gem.drawLine(g, prevPoint, navPos );
		
		// draw points and lines of interest
		ArrayList<Point> points = data.getPointsToDraw();
		ArrayList<Line> lines = data.getLinesToDraw();
		g.setColor(Color.red);
		for( int i = 0, is = lines.size(); i < is; i++) {
			Line l = lines.get(i);
			if( l == null )
				continue;
			gem.drawLine(g, l.p1, l.p2 );
		}
		
		for( int i = 0, is = points.size(); i < is; i++) {
			Point p = points.get(i);
			if( p == null )
				continue;
			g.setColor(Color.black);
			switch(p.type()) {
			case VERTEX:
				g.setColor(Color.red);
			break;
			case INTER_STEP:
				g.setColor(Color.orange);
				break;
			default:
				break;		
			}
			gem.drawPoint(g, p);
		}
		
		// draw the current player pos
		g.setColor(Color.BLUE);
		if( controller.getState() == ProgramState.SET_LOS )
			los = new Point(mousePos[0], mousePos[1], true);
		if( controller.getState() == ProgramState.SET_START_POS ) {
			navPos = new Point( mousePos[0], mousePos[1], true);
		}
		if( navPos != null ) {
			double dir = 90;
			if( los != null )
				dir = pointDirection(navPos.x(), navPos.y(), los.x(), los.y());
			int x[] = {(int) (navPos.xI() + lengthdirX(10,dir)),
					   (int) (navPos.xI() + lengthdirX(5,dir+135)),
					   (int) (navPos.xI() + lengthdirX(5,dir-135)) };
			int y[] = {(int) (navPos.yI() + lengthdirY(10,dir)),
					   (int) (navPos.yI() + lengthdirY(5,dir+135)),
					   (int) (navPos.yI() + lengthdirY(5,dir-135)) };
			g.fillPolygon(x, y, 3);
			if( controller.getState() == ProgramState.SET_LOS ) {
				Point o = gem.extendLine(navPos, new Point(mousePos[0],mousePos[1], true));
				Intersection in = gem.linePolygonIntersect( navPos, o , data.getPolygon() );
				Point i = null;
				if( in != null) i = in.intersection;
				if( i != null ) {
					g.fillOval( i.xI()-2, i.yI()-2, 5, 5);
					gem.drawLine(g, navPos, i);
				} else {
					g.setColor(Color.RED);
					gem.drawLine(g, navPos, new Point(mousePos[0], mousePos[1], true) );
				}
			}
		}
	}
	
	// DRAWING CALC STUFF
	// takes a direction in degrees and a length an returns the x component of the vector
	public double lengthdirY(double length, double direction) {
		return -Math.sin(Math.toRadians(direction))*length;
	}
	
	// takes a direction in degrees and a length an returns the y component of the vector
	public double lengthdirX(double length, double direction) {
		return Math.cos(Math.toRadians(direction))*length;
	}
	
	public double pointDirection(double x1, double y1, double x2, double y2) {
		x2 = x2 - x1;
		y2 = -(y2 - y1);
		//if( y2 < 0)
		double ang = Math.toDegrees(Math.atan( ((double) (y2 / x2) ) ));
		if( x2 < 0 ) 
			if( y2 >= 0)
				return 180 + ang;
			else
				return ang - 180;
		return ang;
		//else
		//	return Math.toDegrees(Math.atan( ((double) y2 / x2 ) ));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		/*
		if( listIndex < 4 ) {
			int x = e.getX();
			int y = e.getY();
			list[ listIndex ] = new Point( x, y, PointType.VERTEX );
			listIndex++;
			if( listIndex == 3) {
				System.out.println( Geometry.pointIsRightOfLine(list[2], list[0], list[1]));
			}
			if( listIndex >= 4 ) {
				list[5] = Geometry.lineSegmentIntersect( list[0], list[1], list[2], list[3]);
				list[4] = Geometry.lineIntersect(list[0], list[1], list[2], list[3]);
			}
		} else {
			list = new Point[6];
			listIndex = 0;
		}
		repaint();
		*/
		// Testing code
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		requestFocusInWindow();
		controller.processClickEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		mousePos[0] = e.getX();
		mousePos[1] = e.getY();
		switch(controller.getState()) {
		case SET_START_POS:
		case SET_LOS:
			repaint();
		break;
		default:
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		controller.processKeyStroke(KeyEvent.getKeyText(arg0.getKeyCode()));
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}

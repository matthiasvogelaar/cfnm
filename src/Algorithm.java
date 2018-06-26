import java.util.ArrayList;

public class Algorithm implements Runnable {

	private DataContainer data;
	private Main controller;
	private GeometryDerivative g;
	
	private Point pos;
	private Point los;
	private ArrayList<Point> poly;
	
	public Algorithm(Main c, DataContainer d, GeometryDerivative g) {
		controller = c;
		data = d;
		this.g = g;
	}
	
	@Override
	public void run() {
		algorithm();		
	}
	
	public void algorithm() {
		// get all relevant data
		poly = data.getPolygon();
		int ps = poly.size();
		int iterations = 0;		// if iterations == ps, then we've seen every wall in the maze
		// the current position
		pos = data.getNavigatorPos();
		// the current los
		los = data.getLOSPoint();
		// save the start LOS
		Point a = data.getLOSLine().p1;
		Point b = data.getLOSLine().p2;
		Point prev = null;
		
		// pick a direction
		int direction = 1;
		if( Math.random() > 0.5 ) {
			direction = -1;
			a = b;
			b = data.getLOSLine().p1;
		}
		// currently, A is the 'previous' point, B is in the direction of the next one
		
		// get the index of point B in the polygon
		int index = -1;
		for(int i = 0; i < ps; i++) {
			Point t = poly.get(i);
			if( t.equals( b ) ) {
				index = i;
				break;
			}
		}
		try {
			while( iterations < ps && !Thread.currentThread().isInterrupted() ) {
				if( iterations != 0 )
					sleep( 1000 );
				iterations++;
				data.resetLinesToDraw();
				data.resetPointsToDraw();
				controller.repaint();
				index = (index + direction) % ps;
				if( index < 0) index += ps;
				prev = a;	// we need this for the concave case
				a = b;
				b = poly.get(index);
				// check if we have a clear line of sight on the ENTIRE next wall:
				if( hasLosOnEntireWall(a,b) ) {
					// move towards the wall we are CURRENTLY facing
					Point m = g.getPointOnLine(pos, los, data.getPolygon());
					updatePos(m);
				}
				// check whether we are in the convex or concave case
				boolean convex = g.pointsOnSameSideOfLine(pos, b, prev, a);
	
				if( convex ) {
					getLosOnWall(a,b);
					sleep( 500 );
					data.resetLinesToDraw();
					data.resetPointsToDraw();
					controller.repaint();			
				} else {
					System.out.println("CONVEX!");
					// extend the prev/a line and get a point on that line
					Point target = g.extendPolygonLine(prev, a, poly);
					Point p = g.getPointOnLine(a, target, data.getPolygon());
					moveToPoint(p, target);
					
					ArrayList<Point> inTriangle = g.pointsInTriangle(poly, pos, a, b);
					if( inTriangle == null) {
						updateLOS( g.getPointOnLine(a, b, data.getPolygon()));
						redrawAndSleep(300);
					} else {
						ArrayList<Point> intersects = new ArrayList<Point>();
						for(int i = 0, is = inTriangle.size(); i < is; i++)
							intersects.add( g.lineIntersect(pos, inTriangle.get(i), a, b));
						g.orderPoints(intersects, a, b);
						updateLOS( g.getPointOnLine(a, intersects.get(0), data.getPolygon()));
					}
					//getLosOnWall(a,b);
				}
				sleep( 500 );
			}
			controller.algorithmDone();
		} catch( InterruptedException e) {
			// apparently we were interrupted :P so stop
		}
	}
	
	private boolean hasLosOnEntireWall(Point a, Point b) {
		ArrayList<Point> inTriangle = g.pointsInTriangle(poly, pos, a, b);
		if( inTriangle != null) return false;
		return !g.losPolygonIntersect(pos, a, poly) && !g.losPolygonIntersect(pos, b, poly);
	}
	
	private void moveToPoint(Point p, Point tlos) throws InterruptedException {
		// create a triangle of POS, LOS, and P
		data.addPointToDraw(p);
		redrawAndSleep(300);
		ArrayList<Point> inTriangle = g.pointsInTriangle(poly, pos, los, p);
		if( inTriangle == null ) {
			// we already have an LOS on the target, secure it.
			updatePos(p);
			updateLOS(tlos);
			return;
		}
		ArrayList<Point> intersects = new ArrayList<Point>();
		for(int i = 0, is = inTriangle.size(); i < is; i++)
			intersects.add( g.lineIntersect(pos, los, p, inTriangle.get(i)));
		g.orderPoints(intersects, pos, los);
		Point goal = g.getPointOnLine(intersects.get(intersects.size()-1), los, data.getPolygon());
		animateMoveAlongPoints(intersects, goal, p);
		updateLOS(p);
		updatePos(p);
		updateLOS(tlos);
	}
	
	private void getLosOnWall(Point a, Point b) throws InterruptedException {
		System.out.println("Starting LOS ON WALL PROCEDURE");
		// construct a triangle using currentPos, LOS and A, and get all the points from the polygon within that triangle
		ArrayList<Point> inTriangle = g.pointsInTriangle(poly, pos, los, a);
		Point target = a;
		data.addLineToDraw(new Line(a, b) );
		// if it is empty... then we already have an los on the line
		if( inTriangle == null) {
			inTriangle = g.pointsInTriangle(poly, pos, los, b);
			target = b;
		}
		if( inTriangle == null) {
			// get an los on this line
			updateLOS(g.getPointOnLine( a, b, data.getPolygon()));
			return;
		}
		// get the intersections of these points with the line pos/los
		ArrayList<Point> intersects = new ArrayList<Point>();
		for(int i = 0, is = inTriangle.size(); i < is; i++)
			intersects.add( g.lineIntersect(pos, los, target, inTriangle.get(i)) );
		g.orderPoints(intersects, pos, los);
		Point goal = g.getPointOnLine(los, intersects.get( intersects.size() - 1), data.getPolygon() );
		animateMoveAlongPoints( intersects, goal, target);
		
		data.addLineToDraw(new Line(a, b) );
		controller.repaint();
		
		// rotate towards this wall
		inTriangle = g.pointsInTriangle(poly, pos, a, b);
		if( inTriangle == null ) {
			updateLOS(g.getPointOnLine(a, b, data.getPolygon()));
			return;
		}
		intersects = new ArrayList<Point>();
		for(int i = 0, is = inTriangle.size(); i < is; i++)
			intersects.add( g.lineIntersect(pos, g.extendLine(pos, inTriangle.get(i)), a, b));
		g.orderPoints(intersects, b, a);
		goal = g.getPointOnLine(intersects.get(intersects.size()-1), a, data.getPolygon());
		animateRotateUsingPoints(intersects, goal);
		
	}
	
	private void animateMoveAlongPoints(ArrayList<Point> list, Point goal, Point reference) throws InterruptedException {
		for(int i = 0, is = list.size(); i < is; i++) {
			Point p = list.get(i);
			p.setLabel("" + i);
			p.setType(PointType.INTER_STEP);
			data.addPointToDraw( p );
			data.addLineToDraw( new Line( reference, p ) );
			redrawAndSleep( 350 );
		}
		data.addPointToDraw( goal );
		redrawAndSleep( 350 );
		for(int i = 0, is = list.size(); i < is; i++) {
			data.removePointToDraw(0);
			data.removeLineToDraw(1);
			updatePos( list.get(i), false );
		}
		data.resetLinesToDraw();
		data.resetPointsToDraw();
		updatePos( goal );
	}
	
	private void animateRotateUsingPoints(ArrayList<Point> list, Point goal) throws InterruptedException {
		for(int i = 0, is = list.size(); i < is; i++) {
			Point p = list.get(i);
			p.setLabel("" + i);
			p.setType(PointType.INTER_STEP);
			data.addPointToDraw( p );
			data.addLineToDraw( new Line( pos, p ) );
			redrawAndSleep( 350 );
		}
		data.addPointToDraw( goal );
		data.addLineToDraw( new Line( pos, goal ));
		redrawAndSleep( 350 );
		
		for(int i = 0, is = list.size(); i < is; i++ ) {
			data.removePointToDraw(0);
			data.removeLineToDraw(1);
			updateLOS(list.get(i));
		}
		data.resetLinesToDraw();
		data.resetPointsToDraw();
		updateLOS( goal );
	}
	
	private void updatePos(Point p, boolean isPartOfPath) throws InterruptedException {
		if( isPartOfPath )
			data.addPathVertex(p);
		pos = p;
		data.setNavigatorPos(pos.x(), pos.y());
		redrawAndSleep(300);
	}
	
	private void updatePos(Point p) throws InterruptedException {
		updatePos(p, true);
	}
	
	private void updateLOS(Point p) throws InterruptedException {
		data.setLOS(p, new Line(pos,p));
		los = p;
		redrawAndSleep(300);
	}
	
	public void redrawAndSleep(long milliseconds) throws InterruptedException {
		controller.repaint();
		sleep(milliseconds);
	}
	
	public void sleep(long milliseconds) throws InterruptedException {
		Thread.sleep(milliseconds);
	}
}

import java.awt.Graphics;

public class EuclGeometry implements Geometry {
	
	private final static double EPSILON = 0.01;
	
	// AXIOMS AND THEIR DIRECT DERIVATIVES
	// Get the point where two infinite lines intersect
	public Point lineIntersect(Point p1, Point p2, Point p3, Point p4) {
		double x1 = p1.x(); double y1 = p1.y();
		double x2 = p2.x(); double y2 = p2.y();
		double x3 = p3.x(); double y3 = p3.y();
		double x4 = p4.x(); double y4 = p4.y();
		
		double a1 = y2 - y1;
		double b1 = x1 - x2;
		double c1 = a1*x1 + b1*y1;
		
		double a2 = y4 - y3;
		double b2 = x3 - x4;
		double c2 = a2*x3 + b2*y3;
		
		double d = a1*b2 - a2* b1;
		
		if( d == 0 )
			return null;
		double x = (b2*c1 - b1*c2)/d;
		double y = (a1*c2 - a2*c1)/d;
		
		return new Point(x,y,true);
	}

	// Get the point where to line segments intersect
	public Point lineSegmentIntersect(Point a1, Point a2, Point b1, Point b2) {
		double x1 = a1.x(); double y1 = a1.y();
		double x2 = a2.x(); double y2 = a2.y();
		double x3 = b1.x(); double y3 = b1.y();
		double x4 = b2.x(); double y4 = b2.y();
		
		if( x1 > x2 ) { x1 = a2.x(); x2 = a1.x(); }
		if( x3 > x4 ) { x3 = b2.x(); x4 = b1.x(); }
		if( y1 > y2 ) { y1 = a2.y(); y2 = a1.y(); }
		if( y3 > y4 ) { y3 = b2.y(); y4 = b1.y(); }
		
		Point intersection = lineIntersect(a1,a2,b1,b2);
		if( intersection == null )
			return null;
		double x = intersection.x();
		double y = intersection.y();
		
		if( x > x2 || x < x1 || x > x4 || x < x3 )
		    return null;
		if( y > y2 || y < y1 || y > y4 || y < y3 )
		    return null;
		    
		return new Point(x,y,true);
	}
	
	// Return a new point on a given line
	public Point getPointOnLine(Point a, Point b) {
		// for now, return the exact center of the line
		return new Point( ( a.x() + b.x() ) / 2, ( a.y() + b.y() ) / 2, true ); 
	}
	
	// check whether a point is on a line or not
	public boolean pointIsOnLine(Point p, Point l1, Point l2) {
		double deltaX = l2.x() - l1.x();
		double deltaY = l2.y() - l1.y();
//		System.out.println("Point is on line:");
//		System.out.println(p);
//		System.out.println("dx: " + deltaX + " dy: " + deltaY);
		// edge cases where we have a line parallel to one of the axis
		if( deltaX == 0 )
			return eq(p.x(), l1.x());
		if( deltaY == 0)
			return eq(p.y(), l1.y());
		deltaY = deltaY / deltaX;
		double y = l1.y() + ((p.x() - l1.x()) * deltaY);
//		System.out.println("y:" + y);
		return eq(y, p.y());
	}
	
	public boolean pointIsOnLineSegment(Point p, Point l1, Point l2) {
		boolean isOnLine = pointIsOnLine(p, l1, l2);
		return isOnLine && pointIsBetween(p, l1, l2);
	}
	
	private boolean eq(double a, double b) {
		return (Math.abs(a-b) < EPSILON);
	}
	
	public void drawPoint(Graphics g, Point p) {
		switch(p.type()) {
		case INTER_STEP:
			g.fillOval(p.xI()-5, p.yI()-5, 10, 10);
			break;
		case STEP:
			g.fillOval(p.xI()-1, p.yI()-1, 3, 3);
			break;
		case VERTEX:
			g.fillRect(p.xI()-3, p.yI()-3, 6, 6);
			break;
		case LOS:
		case POSITION:
		case INTERSECT:
		case GENERAL:
		default:
			g.fillOval(p.xI()-4, p.yI()-4, 8, 8);
			break;
		
		}
		g.fillOval(p.xI()-3, p.yI()-3, 6, 6);
	}
	
	public void drawLine(Graphics g, Point a, Point b) {
		g.drawLine(a.xI(), a.yI(), b.xI(), b.yI());
	}
	
	
	
	// checking whether two points are on the same side of a line (NOT A LINE SEGMENT!)
	public boolean pointsOnSameSideOfLine(Point a, Point b, Point l1, Point l2) {
		Point intersect = lineIntersect(a,b,l1,l2);
		if( intersect == null) return true;
		return !intersectPointIsOnLine( intersect, a, b);
	}
	
	// check if point C is in between point A and B
	public boolean pointIsBetween(Point b, Point a, Point c) {
		if(!pointIsOnLine(b, a, c)) return false;		// point has to be on the same line
		if( b.equals(a) || b.equals(c) ) return false;	// points need to be distinct
		double x1 = Math.min(c.x(), a.x());
		double x2 = Math.max(c.x(), a.x());
		double y1 = Math.min(c.y(), a.y());
		double y2 = Math.max(c.y(), a.y());
		return b.x() >= x1 && b.x() <= x2 && b.y() >= y1 && b.y() <= y2;
	}

	public Point extendLine(Point from, Point to) {
		double fx = from.x();
		double fy = from.y();
		double tx = to.x();
		double ty = to.y();
		
		double deltaY = ty - fy;
		if( deltaY == 0 ) {
			if( fx > tx )
				return new Point(0,fy, true);
			else
				return new Point(Interspace.width,fy, true);
		}
		double deltaX = tx - fx;
		if( deltaX == 0) {
			if( fy > ty )
				return new Point(fx, 0, true);
			else
				return new Point(fx, Interspace.height,true);
		}
		
		double slopeYX = deltaY / deltaX;
		double slopeXY = deltaX / deltaY;

		double x = 0;
		if( deltaX > 0) x = Interspace.width;
		double y = fy + (x - fx)*slopeYX;
		
		if(y < 0 || y > Interspace.height) {
			double pos = 0;
			if( y > Interspace.height)
				pos = Interspace.height;
			x = x + (pos - y)*slopeXY;
			y = pos;
		}
		return new Point( x,y,true );
	}
	
	public boolean intersectPointIsOnLine(Point p, Point l1, Point l2) {
		double x1 = l1.x(); double x2 = l2.x();
		if( x1 > x2 ) { x2 = x1; x1 = l2.x(); }
		double y1 = l1.y(); double y2 = l2.y();
		if( y1 > y2 ) { y2 = y1; y1 = l2.y(); }
		double px = p.x();
		double py = p.y();
		return x1 <= px && x2 >= px && y1 <= py && y2 >= py;
	}	
	
}

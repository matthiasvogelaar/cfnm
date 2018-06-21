import java.awt.Graphics;
import java.util.ArrayList;

public class GeometryDerivative implements Geometry  {
	
	public final Geometry g;
	
	public GeometryDerivative(Geometry g) {
		this.g = g;
	}
	
	public Intersection linePolygonIntersect(Point from, Point to, ArrayList<Point> poly) {
		Point intersect = null;
		Point l1 = null;
		Point l2 = null;
		for(int i = 0, is = poly.size(); i < is; i++) {
			Point a = poly.get(i);
			Point b = poly.get((i+1)%is);
			//System.out.println(a + " -> " + b);
			Point c = g.lineIntersect(from, to, a, b);
			if( c == null )
				continue;
			if( !g.intersectPointIsOnLine( c, a, b) || !g.intersectPointIsOnLine( c, from, to ) )
				continue;
			if( intersect == null || g.pointIsBetween(c, from, intersect) ) {
				intersect = c;
				l1 = a;
				l2 = b;
			}
		}
		if( intersect == null) 
			return null;
		return new Intersection(l1,l2,intersect,from,to);
	}
	
	public boolean losPolygonIntersect(Point from, Point to, ArrayList<Point> poly) {
		for(int i = 0, is = poly.size(); i < is; i++) {
			Point a = poly.get(i);
			Point b = poly.get((i+1)%is);
			// skip all the lines that have point to in them, since they will always intersect at point to
			if( a.equals(to) || b.equals(to) )
				continue;
			Point c = g.lineIntersect(from, to, a, b);
			if( c == null ) continue;
			if( g.intersectPointIsOnLine(c, a, b) && g.intersectPointIsOnLine(c, from, to) )
				return true;
		}
		return false;
	}
	
	public Point extendPolygonLine(Point from, Point to, ArrayList<Point> poly) {
		Point intersect = null;
		Point endPoint = g.extendLine(from, to);
		for( int i = 0, is = poly.size(); i < is; i++) {
			Point a = poly.get(i);
			Point b = poly.get((i+1)%is);
			if( a.equals(from) || a.equals(to) || b.equals(from) || b.equals(to) )
				continue;
			Point c = g.lineIntersect(from, endPoint, a, b);
			if( c == null )
				continue;
			if( !g.intersectPointIsOnLine(c, a, b ) || !g.intersectPointIsOnLine(c, from, endPoint ))
				continue;
			if( intersect == null || g.pointIsBetween(c, from, intersect) )
				intersect = c;
		}
		if( intersect == null)
			intersect = extendLine(from, to);
		return intersect;
	}
	
	public ArrayList<Point> pointsInTriangle( ArrayList<Point> points, Point a, Point b, Point c) {
		ArrayList<Point> res = new ArrayList<Point>();
		for(int i = 0, is = points.size(); i < is; i++) {
			Point p = points.get(i);
			if( g.pointsOnSameSideOfLine(p, a, b, c) &&
					g.pointsOnSameSideOfLine(p, b, a, c) &&
					g.pointsOnSameSideOfLine(p, c, a, b) &&
				!g.pointIsOnLine(p,a,b) && !g.pointIsOnLine(p,b,c) && !g.pointIsOnLine(p,c,a))
					res.add(p);
		}
		if( res.size() > 0)
			return res;
		return null;
	}
	
	public void orderPoints(ArrayList<Point> p, Point from, Point to) {
		Point a = from;
		Point b = to;
		int ps = p.size();
		ArrayList<Point> scratchpad = new ArrayList<Point>(p);
		// find the 'lowest' and 'highest' points
		for(int i = 0; i < ps; i++) {
			Point t = scratchpad.get(i);
			if( !g.pointIsOnLine(t, from, to)) {
				System.out.println("ERROR");
				System.out.println(t + " <" + from + "," + to + ">");
			}
			if( g.pointIsBetween(a, t, b ) )
				a = t;
			else if( g.pointIsBetween(b, a, t) )
				b = t;
		}
		// find the 'furthest' point
		//result.add( a );	// add the start point
		for(int i = 0; i < ps; i++ ) {
			// find the next closest point to A
			Point n = null;
			for( int j = 0; j < ps; j++ ) {
				Point c = scratchpad.get(j);
				if( n == null ) {
					if( g.pointIsBetween(c, a, b) )
						n = c;
				}
				else if( g.pointIsBetween( c, a, n ) )
					n = c;
			}
			p.set(i, n); 
			a = n;
		}
		//result.add( b );	// add the end point
	}

	@Override
	public Point lineIntersect(Point a1, Point a2, Point b1, Point b2) {
		return g.lineIntersect(a1, a2, b1, b2);
	}

	@Override
	public Point lineSegmentIntersect(Point a1, Point a2, Point b1, Point b2) {
		// TODO Auto-generated method stub
		return g.lineSegmentIntersect(a1, a2, b1, b2);
	}

	@Override
	public Point getPointOnLine(Point a, Point b) {
		return g.getPointOnLine(a, b);
	}

	@Override
	public boolean pointIsOnLine(Point p, Point a, Point b) {
		return g.pointIsOnLine(p, a, b);
	}

	@Override
	public boolean pointIsOnLineSegment(Point p, Point a, Point b) {
		return g.pointIsOnLineSegment(p, a, b);
	}

	@Override
	public boolean pointsOnSameSideOfLine(Point a, Point b, Point l1, Point l2) {
		return g.pointsOnSameSideOfLine(a, b, l1, l2);
	}

	@Override
	public boolean pointIsBetween(Point p, Point a, Point b) {
		return g.pointIsBetween(p, a, b);
	}

	@Override
	public Point extendLine(Point from, Point to) {
		return g.extendLine(from, to);
	}

	@Override
	public void drawLine(Graphics gr, Point a, Point b) {
		// TODO Auto-generated method stub
		g.drawLine(gr, a, b);
	}

	@Override
	public void drawPoint(Graphics gr, Point p) {
		// TODO Auto-generated method stub
		g.drawPoint(gr, p);
	}

	@Override
	public boolean intersectPointIsOnLine(Point p, Point l1, Point l2) {
		return g.intersectPointIsOnLine(p, l1, l2);
	}

}

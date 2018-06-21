import java.awt.Graphics;

public interface Geometry
{
	public Point lineIntersect(Point a1, Point a2, Point b1, Point b2);
	
	public Point lineSegmentIntersect(Point a1, Point a2, Point b1, Point b2);
	
	public Point getPointOnLine(Point a, Point b);
	
	public boolean pointIsOnLine(Point p, Point a, Point b);
	
	public boolean pointIsOnLineSegment(Point p, Point a, Point b);
	
	public boolean pointsOnSameSideOfLine(Point a, Point b, Point l1, Point l2);
	
	public boolean pointIsBetween(Point p, Point a, Point b);
	
	public Point extendLine(Point from, Point to);
	
	public void drawLine(Graphics g, Point a, Point b);
	
	public void drawPoint(Graphics g, Point p);
	
	public boolean intersectPointIsOnLine(Point p, Point l1, Point l2);
}
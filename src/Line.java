
public class Line {
	public final Point p1;
	public final Point p2;
	
	public Line(Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public Line(float x1, float y1, float x2, float y2) {
		this.p1 = new Point(x1,y1);
		this.p2 = new Point(x2,y2);
	}
	
	public String toString() {
		return p1.toString() + " -> " + p2.toString();
	}
}

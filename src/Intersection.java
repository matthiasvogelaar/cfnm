
public class Intersection {
	public final Line l1;
	public final Point intersection;
	public final Line l2;
	
	public Intersection(Line l1, Point inter, Line l2) {
		this.l1 = l1;
		this.intersection = inter;
		this.l2 = l2;
	}
	
	public Intersection(Point l1p1, Point l1p2, Point inter, Point l2p1, Point l2p2) {
		this.l1 = new Line( l1p1, l1p2);
		this.intersection = inter;
		this.l2 = new Line( l2p1, l2p2);
	}
}

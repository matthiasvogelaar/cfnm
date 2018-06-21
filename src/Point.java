
public class Point implements Comparable<Point>{
	
	private static int idCounter = 0;
	private int id;
	private double x;
	private double y;
	private PointType type;
	private String label;
	
	public Point(double x, double y) {
		id = Point.idCounter;
		Point.idCounter++;
		this.x = x;
		this.y = y;
		this.type = PointType.GENERAL;
		label = null;
	}
	
	public Point(double x, double y, boolean vol) {
		id = -1;
		this.x = x;
		this.y = y;
		this.type = PointType.VOLATILE;
		label = null;
	}
	
	public Point(double x, double y, PointType t) {
		id = Point.idCounter;
		Point.idCounter++;
		this.x = x;
		this.y = y;
		this.type = t;
		label = null;
	}
	
	public Point(double x, double y, PointType t, String l) {
		id = Point.idCounter;
		Point.idCounter++;
		this.x = x;
		this.y = y;
		this.type = t;
		label = l;
	}
	
	public int getId() {
		return id;
	}
	
	public double x() {
		return x;
	}
	
	public int xI() {
		return (int) Math.floor(x);
	}
	
	public double y() {
		return y;
	}
	
	public int yI() {
		return (int) Math.floor(y);
	}
	
	public PointType type() {
		return type;
	}
	
	public Point setType(PointType pt) {
		type = pt;
		return this;
	}
	
	public String getLabel() {
		return label;
	}
	
	public Point setLabel(String s) {
		label = s;
		return this;
	}

	@Override
	public int compareTo(Point o) {
		// TODO Auto-generated method stub
		if( o.x() != x )
			return (int) Math.floor(x - o.x());
		return (int) Math.floor(y - o.y());
	}
	
	public String toString() {
		return id + "(" + x + "," + y + ")";
	}
	
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o instanceof Point) {
			Point t = (Point) o;
			if( t.id == this.id && t.id >= 0 ) return true;
			if( this.x == t.x && this.y == t.y ) return true;
		}
		return false;
	}
}

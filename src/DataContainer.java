import java.util.ArrayList;

public class DataContainer {
	
	private ArrayList<Point> polygon;
	private Point navigatorPos;
	private Point LOS;
	private Point startPos;
	private Point startLOS;
	private Line startLOSLine;
	private Line LOSLine;
	private ArrayList<Point> path;
	private ArrayList<Point> pointsToDraw;
	private ArrayList<Line> linesToDraw;

	public DataContainer() {
		// init the structures
		polygon = new ArrayList<Point>();
		path = new ArrayList<Point>();
		navigatorPos = null;
		LOS = null;
		LOSLine = null;
		pointsToDraw = new ArrayList<Point>();
		linesToDraw = new ArrayList<Line>();
	}
	
	public void reset() {
		polygon = new ArrayList<Point>();
		path = new ArrayList<Point>();
		navigatorPos = null;
		LOS = null;
		LOSLine = null;
		pointsToDraw = new ArrayList<Point>();
		linesToDraw = new ArrayList<Line>();
	}
	
	public void addVertexToPolygon(int x, int y) {
		polygon.add( new Point(x,y,PointType.VERTEX) );
	}
	
	public void removeLastVertexOfPolygon() {
		if( polygon.size() > 0 )
			polygon.remove( polygon.size() - 1);
	}
	
	public ArrayList<Point> getPolygon() {
		return new ArrayList<Point>(polygon);
	}
	
	public void setNavigatorPos(double x, double y) {
		if( navigatorPos == null )
			startPos = new Point(x,y, PointType.POSITION);
		navigatorPos = new Point(x,y, PointType.POSITION);
	}
	
	public Point getNavigatorPos() {
		return navigatorPos;
	}
	
	public Point getStartPos() {
		return startPos;
	}
	
	public Point getStartLOSPoint() {
		return startLOS;
	}
	
	public Line getStartLOSLine() {
		return startLOSLine;
	}
	
	public void setLOS(Point p, Line l) {
		if( LOS == null ) {
			startLOS = p;
			startLOSLine = l;
		}
		LOS = p;
		LOSLine = l;
	}
	
	public Point getLOSPoint() {
		return LOS;
	}
	
	public Line getLOSLine() {
		return LOSLine;
	}
	
	public DataContainer addLineToDraw(Line l) {
		linesToDraw.add(l);
		return this;
	}
	
	public DataContainer resetLinesToDraw() {
		linesToDraw = new ArrayList<Line>();
		return this;
	}
	
	public ArrayList<Line> getLinesToDraw() {
		return new ArrayList<Line>( linesToDraw );
	}
	
	public DataContainer addPointToDraw(Point p) {
		pointsToDraw.add(p);
		return this;
	}
	
	public DataContainer removePointToDraw(int index) {
		for(int i = 0, is = pointsToDraw.size(); i < is; i++) {
			Point p = pointsToDraw.get(i);
			if( p == null) {
				index++;
				continue;
			}
			if( i == index) {
				pointsToDraw.remove(i);
				return this;
			}
		}
		return this;
	}
	
	public DataContainer removeLineToDraw(int index) {
		for(int i = 0, is = linesToDraw.size(); i < is; i++) {
			Line p = linesToDraw.get(i);
			if( p == null) {
				index++;
				continue;
			}
			if( i == index) {
				linesToDraw.remove(i);
				return this;
			}
		}
		return this;
	}
	
	public DataContainer resetPointsToDraw() {
		pointsToDraw = new ArrayList<Point>();
		return this;
	}
	
	public ArrayList<Point> getPointsToDraw() {
		return new ArrayList<Point>( pointsToDraw );
	}
	
	public void resetNavigator() {
		navigatorPos = startPos;
		LOS = startLOS;
		LOSLine = startLOSLine;
		path = new ArrayList<Point>();
		path.add(startPos);
		resetPointsToDraw();
		resetLinesToDraw();
	}
	
	public void resetStartPos() {
		resetPointsToDraw();
		resetLinesToDraw();
		navigatorPos = null;
		LOS = null;
		LOSLine = null;
		path = new ArrayList<Point>();
	}
	
	public void addPathVertex(Point p) {
		Point x = new Point( p.x(), p.y(), PointType.STEP );
		path.add(x);
	}
	
	public ArrayList<Point> getPath() {
		return path;
	}
}

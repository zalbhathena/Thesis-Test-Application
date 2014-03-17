import java.awt.Point;
public class Edge {
	private int x1,y1,x2,y2;
	public Edge(int x1, int y1, int x2, int y2){
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	public Edge(Point p1, Point p2) {
		x1 = p1.x;
		y1 = p1.y;
		x2 = p2.x;
		y2 = p2.y;
	}
	
	public Point getFromPoint(){return new Point(x1,y1);}
	public Point getToPoint(){return new Point(x2,y2);}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x1;
		result = prime * result + x2;
		result = prime * result + y1;
		result = prime * result + y2;
		return result;
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		
		if(x1 == other.x1 && y1 == other.y1 && x2 == other.x2 && y2 == other.y2)
			return true;
		
		if(x1 == other.x2 && y1 == other.y2 && x2 == other.x1 && y2 == other.y1)
			return true;
		
		return false;
	}
}
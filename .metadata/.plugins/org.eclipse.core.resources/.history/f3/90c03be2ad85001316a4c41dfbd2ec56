import java.awt.Point;
import java.util.ArrayList;
public class SearchSpaceNode implements Comparable<SearchSpaceNode> {
	Point[] point_list;
	int f_value = 0;
	private ArrayList<SearchSpaceNode> neighbors;
	public SearchSpaceNode(Point... p) {
		point_list = new Point[p.length];
		for(int x = 0; x < point_list.length; x++) {
			point_list[x] = new Point(p[x].x, p[x].y);
		}
		
	}
	
	public ArrayList<SearchSpaceNode> getNeighbors() {
		return neighbors;
	}
	
	public void setNeighbors(ArrayList<SearchSpaceNode> neighbors) {
		this.neighbors = new ArrayList<SearchSpaceNode>();
		for(int x = 0; x < neighbors.size(); x++) {
			this.neighbors.add(neighbors.get(x));
		}
	}
	
	public int compareTo(SearchSpaceNode o) {
		return Double.compare(f_value, o.f_value);
	}
}
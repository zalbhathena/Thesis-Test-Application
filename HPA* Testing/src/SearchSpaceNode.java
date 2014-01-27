import java.awt.Point;
import java.util.Set;
import java.util.HashSet;
public class SearchSpaceNode {
	Point[] point_list;
	private Set<SearchSpaceNode> neighbors = new HashSet<SearchSpaceNode>();
	public SearchSpaceNode(Point... p) {
		point_list = new Point[p.length];
		for(int x = 0; x < point_list.length; x++) {
			point_list[x] = new Point(p[x].x, p[x].y);
		}
	}
	
	public SearchSpaceNode(SearchSpaceNode node) {
		int length = node.point_list.length;
		point_list = new Point[length];
		for(int i = 0; i < length; i++)
			point_list[i] = new Point(node.point_list[i]);
	}
	
	public Set<SearchSpaceNode> getNeighbors() {
		return neighbors;
	}
	
	public void setNeighbors(Set<SearchSpaceNode> neighbors) {
		this.neighbors = new HashSet<SearchSpaceNode>();
		for(SearchSpaceNode node: neighbors) {
			this.neighbors.add(node);
		}
	}
	
	public void addNeighbor(SearchSpaceNode neighbor) {
		neighbors.add(neighbor);
	}
	
	
}
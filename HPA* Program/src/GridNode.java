import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
public class GridNode implements Node {
	
	//hashcode and equals only generated with point_list
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(point_list);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GridNode other = (GridNode) obj;
		if (!Arrays.equals(point_list, other.point_list))
			return false;
		return true;
	}

	Point[] point_list;
	private Set<Node> neighbors = new HashSet<Node>();
	public GridNode(Point... p) {
		point_list = new Point[p.length];
		for(int x = 0; x < point_list.length; x++) {
			point_list[x] = new Point(p[x].x, p[x].y);
		}
	}
	
	public GridNode(int x, int y) {
		point_list = new Point[4];
		point_list[0] = new Point(x,y);
		point_list[1] = new Point(x+1,y);
		point_list[2] = new Point(x+1,y+1);
		point_list[3] = new Point(x,y+1);
	}
	
	public GridNode(Node node) {
		int length = node.getPoints().length;
		point_list = new Point[length];
		for(int i = 0; i < length; i++)
			point_list[i] = new Point(node.getPoints()[i]);
	}
	
	public Set<Node> getNeighbors() {
		return neighbors;
	}
	
	public void setNeighbors(Set<Node> neighbors) {
		this.neighbors = new HashSet<Node>();
		for(Node node: neighbors) {
			this.neighbors.add(node);
		}
	}
	
	public void addNeighbor(Node neighbor) {
		neighbors.add(neighbor);
	}
	
	public void removeNeighbor(Node neighbor) {
		neighbors.remove(neighbor);
	}
	
	public void removeSelfFromGraph() {
		for(Node neighbor:neighbors)
			neighbor.removeNeighbor(this);
		neighbors.clear();
	}
	
	public Point[] getPoints(){return point_list;}
	
}
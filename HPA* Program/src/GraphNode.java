import java.awt.Point;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
public class GraphNode implements Node {
	
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
		GraphNode other = (GraphNode) obj;
		if (!Arrays.equals(point_list, other.point_list))
			return false;
		return true;
	}

	Point[] point_list;
	private Set<Node> neighbors = new HashSet<Node>();
	public GraphNode(Point p) {
		point_list = new Point[1];
		point_list[0] = new Point(p.x,p.y);
	}
	
	public GraphNode(int x, int y) {
		point_list = new Point[1];
		point_list[0] = new Point(x,y);
	}
	
	public GraphNode(Node node) {
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
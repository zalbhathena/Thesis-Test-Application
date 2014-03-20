import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TriangulationNode implements Node {
	Point[] point_list = new Point[3];
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
		TriangulationNode other = (TriangulationNode) obj;
		if (!Arrays.equals(point_list, other.point_list))
			return false;
		return true;
	}

	private Set<Node> neighbors = new HashSet<Node>();
	
	public TriangulationNode(Point... p) {
		point_list = new Point[p.length];
		for(int x = 0; x < point_list.length; x++) {
			point_list[x] = new Point(p[x]);
		}
	}
	
	public TriangulationNode(Point p1, Point p2, Point p3) {
		point_list[0] = new Point(p1);
		point_list[1] = new Point(p2);
		point_list[2] = new Point(p3);
	}
	
	public TriangulationNode(Node node) {
		int length = node.getPoints().length;
		if(length != 3)
			point_list = null;
		point_list = new Point[3];
		for(int i = 0; i < length; i++)
			point_list[i] = new Point(node.getPoints()[i]);
	}
	
	@Override
	public Point[] getPoints() {
		return point_list;
	}

	@Override
	public Set<Node> getNeighbors() {
		return neighbors;
	}

	@Override
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
	
	public String toString() {
		return point_list[0].x + "," + point_list[0].y +" "+point_list[1].x + "," + point_list[1].y +" "+point_list[2].x + "," + point_list[2].y;
	}
	
}
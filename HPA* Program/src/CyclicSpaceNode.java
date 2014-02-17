import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;



public class CyclicSpaceNode implements Node {
	int x; int y;
	TreeSet<CyclicNodeNeighbor> cyclic_neighbors = new TreeSet<CyclicNodeNeighbor>();
	
	public CyclicSpaceNode(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public CyclicSpaceNode(Node node) {
		Point p = node.getPoints()[0];
		this.x = p.x;
		this.y = p.y;
	}
	
	public Set<Node> getNeighbors() {
		Set<Node> cyclic_neighbor_set = new HashSet<Node>();
		for(CyclicNodeNeighbor node: cyclic_neighbors) {
			cyclic_neighbor_set.add(node.node);
		}
		return cyclic_neighbor_set;
	}
	
	public void setNeighbors(Node[] neighbors) {
		cyclic_neighbors.clear();
		for(Node node: neighbors) {
			this.cyclic_neighbors.add(new CyclicNodeNeighbor(node));
		}
	}
	
	public Point[] getPoints() {
		return new Point[]{new Point(x,y)};
	}


	public void addNeighbor(Node neighbor) {
		cyclic_neighbors.add(new CyclicNodeNeighbor(neighbor));
	}

	public void removeNeighbor(Node neighbor) {
		/*ArrayList<CyclicNodeNeighbor>remove_list = new ArrayList<CyclicNodeNeighbor>();
		for(CyclicNodeNeighbor node:cyclic_neighbors)
			if(node.node == neighbor)
				remove_list.add(node);
		for(CyclicNodeNeighbor node: remove_list)		
			cyclic_neighbors.remove(node);*/
		
		CyclicNodeNeighbor to_remove;
		CyclicNodeNeighbor neighbor_node = new CyclicNodeNeighbor(neighbor);
		//do {
			to_remove = cyclic_neighbors.ceiling(neighbor_node);
			if(to_remove != null)
				cyclic_neighbors.remove(to_remove);
		//}
		//while(to_remove!=null);
	}
	
	public void removeNeighborsBetween(Node start_neighbor, Node end_neighbor) {
		Point start_point = start_neighbor.getPoints()[0];
		Point end_point = end_neighbor.getPoints()[0];
		double start_angle = Math.atan2(start_point.y - y, start_point.x - x);
		double end_angle = Math.atan2(end_point.y - y, end_point.x - x);
		
		
		boolean is_reverse = Double.compare(start_angle, end_angle) > 0; 
		/*if( )
		{
			double temp_angle = start_angle;
			start_angle = end_angle;
			end_angle = temp_angle;
		}*/
		
		boolean run = is_reverse;
		ArrayList<CyclicNodeNeighbor> remove_list = new ArrayList<CyclicNodeNeighbor>();
		
		for (Iterator<CyclicNodeNeighbor> iterator = cyclic_neighbors.iterator(); iterator.hasNext();) {
			CyclicNodeNeighbor node = iterator.next();
			int compare1 = Double.compare(start_angle, node.angle);
			int compare2 = Double.compare(end_angle, node.angle);
			if(compare1 == 0 || compare2 == 0)
				continue;
			if(is_reverse) {
				if(compare1 < 0 || compare2 > 0) 
					remove_list.add(node);
			}
			else {	
				if( compare1 < 0 &&  compare2 > 0) 
					remove_list.add(node);
			}
				
		}
		int count = remove_list.size();
		for(CyclicNodeNeighbor node:remove_list) {
			cyclic_neighbors.remove(node);
			node.node.removeNeighbor(this);
		}
	}
	
	private class CyclicNodeNeighbor implements Comparable<CyclicNodeNeighbor>{
		private double angle;
		Node node;
		
		public CyclicNodeNeighbor(Node node) {
			this.node = node;
			Point p = node.getPoints()[0];
			double x_diff = p.x - x;
			double y_diff = p.y - y;
			angle = Math.atan2(y_diff, x_diff);
		}
		
		public int compareTo(CyclicNodeNeighbor o) {
			// TODO Auto-generated method stub
			return Double.compare(this.angle, o.angle);
		}
		
	}
}
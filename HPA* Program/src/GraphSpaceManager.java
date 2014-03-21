import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.awt.Point;

import partition.main.PMetis;

public class GraphSpaceManager implements SearchSpaceManager {
	Set<Node> search_space = new HashSet<Node>();
	ArrayList<Node> search_space_list = new ArrayList<Node>();
	Map<Node, Map<Node, Double>> cost_function;
	Map<Node, Integer> node_to_cluster;
	Map<Integer, Set<Node>> cluster_to_node;
	
	public GraphSpaceManager(Set<Node> search_space, 
			Map<Node, Map<Node, Double>> cost_function,
			Map<Node, Integer> node_to_cluster,
			Map<Integer, Set<Node>> cluster_to_node){
		this.search_space_list = new ArrayList<Node>(search_space);
		this.search_space = search_space;
		this.cost_function = cost_function;
		this.node_to_cluster = node_to_cluster;
		this.cluster_to_node = cluster_to_node;
	}
	
	public Set<Node> getNeighborsForNode(Node node,
			boolean cluster) {
		return search_space_list.get(search_space_list.indexOf(node)).getNeighbors();
	}

	public Set<Node> getEntranceNodes() {
		return null;
	}

	public Set<Node> getSearchSpace() {
		return search_space;
	}

	
	public Set<Polygon> getClusterBoundaries() {
		return null;
	}

	
	public int getClusterID(Node node) {
		return node_to_cluster.get(node);
	}
	
	public double getCost(Node from, Node to) {
		if(!from.getNeighbors().contains(to))
			throw new IllegalArgumentException("Not neighboring states.");
		return cost_function.get(from).get(to);
	}
	
	public ArrayList<Point>getPath(Point start_point, Point goal_point, Node start, Node goal,
			Map<Node, Map<Node, Double>>cost_function) {
		search_space_list.add(start);
		search_space_list.add(goal);
		
		for(Node node1:cost_function.keySet()) {
			if(!this.cost_function.containsKey(node1))
				this.cost_function.put(node1, new HashMap<Node,Double>());
			Map<Node,Double>node1_map = cost_function.get(node1);
			for(Node node2:node1_map.keySet()) {
				this.cost_function.get(node1).put(node2, node1_map.get(node2));
			}
		}
		
		for(Node node: goal.getNeighbors()) {
			node.getNeighbors().add(goal);
		}
		
		
		ArrayList<Point> path = 
				SearchAlgorithms.AStar(this, this.cost_function, start_point, goal_point, start, goal, false);
		
		for(Node node1:cost_function.keySet()) {
			Map<Node, Double> this_map = this.cost_function.get(node1);
			Map<Node,Double>node1_map = cost_function.get(node1);
			for(Node node2:node1_map.keySet()) {
				this.cost_function.get(node1).remove(node2);
			}
			if(this_map.size() == 0)
				this.cost_function.remove(node1);
		}
		
		for(Node node: goal.getNeighbors()) {
			if(cost_function.get(node) == null)
				System.out.println("OOPS");
			node.getNeighbors().remove(goal);
			cost_function.get(node).remove(goal);
		}
		search_space_list.remove(start);
		search_space_list.remove(goal);
		
		return path;
	}
	
	public ArrayList<Point>getPath(Node start, Node goal) {
		
		ArrayList<Point> path = 
				SearchAlgorithms.AStar(this, this.cost_function, start.getPoints()[0], goal.getPoints()[0], start, goal, false);
		
		return path;
	}
	
	public PathUpdater getPath(Point start_point, Point goal_point) {
		return null;
	}
	
	public ArrayList<Point> getSubpath(Point start_point, Point goal_point) {
		return null;
	}
}

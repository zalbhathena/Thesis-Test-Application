import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.awt.Point;

public class GraphSpaceManager implements SearchSpaceManager {
	
	Set<SearchSpaceNode> search_space = new HashSet<SearchSpaceNode>();
	ArrayList<SearchSpaceNode> search_space_list = new ArrayList<SearchSpaceNode>();
	Map<SearchSpaceNode, Map<SearchSpaceNode, Double>> cost_function;
	Map<SearchSpaceNode, Integer> node_to_cluster;
	Map<Integer, Set<SearchSpaceNode>> cluster_to_node;
	
	public GraphSpaceManager(Set<SearchSpaceNode> search_space, 
			Map<SearchSpaceNode, Map<SearchSpaceNode, Double>> cost_function,
			Map<SearchSpaceNode, Integer> node_to_cluster,
			Map<Integer, Set<SearchSpaceNode>> cluster_to_node){
		this.search_space_list = new ArrayList<SearchSpaceNode>(search_space);
		this.search_space = search_space;
		this.cost_function = cost_function;
		this.node_to_cluster = node_to_cluster;
		this.cluster_to_node = cluster_to_node;
	}
	
	public Set<SearchSpaceNode> getNeighborsForNode(SearchSpaceNode node,
			boolean cluster) {
		return search_space_list.get(search_space_list.indexOf(node)).getNeighbors();
	}

	public Set<SearchSpaceNode> getEntranceNodes() {
		return null;
	}

	public Set<SearchSpaceNode> getSearchSpace() {
		return search_space;
	}

	
	public Set<Polygon> getClusterBoundaries() {
		return null;
	}

	
	public int getClusterID(SearchSpaceNode node) {
		return node_to_cluster.get(node);
	}
	
	public double getCost(SearchSpaceNode from, SearchSpaceNode to) {
		if(!from.getNeighbors().contains(to))
			throw new IllegalArgumentException("Not neighboring states.");
		return cost_function.get(from).get(to);
	}
	
	public ArrayList<Point>getPath(Point start_point, Point goal_point, SearchSpaceNode start, SearchSpaceNode goal,
			Map<SearchSpaceNode, Map<SearchSpaceNode, Double>>cost_function) {
		search_space_list.add(start);
		search_space_list.add(goal);
		
		for(SearchSpaceNode node1:cost_function.keySet()) {
			if(!this.cost_function.containsKey(node1))
				this.cost_function.put(node1, new HashMap<SearchSpaceNode,Double>());
			Map<SearchSpaceNode,Double>node1_map = cost_function.get(node1);
			for(SearchSpaceNode node2:node1_map.keySet()) {
				this.cost_function.get(node1).put(node2, node1_map.get(node2));
			}
		}
		
		for(SearchSpaceNode node: goal.getNeighbors()) {
			node.getNeighbors().add(goal);
		}
		
		
		ArrayList<Point> path = 
				SearchAlgorithms.AStar(this, this.cost_function, start_point, goal_point, start, goal, false);
		
		for(SearchSpaceNode node1:cost_function.keySet()) {
			Map<SearchSpaceNode, Double> this_map = this.cost_function.get(node1);
			Map<SearchSpaceNode,Double>node1_map = cost_function.get(node1);
			for(SearchSpaceNode node2:node1_map.keySet()) {
				this.cost_function.get(node1).remove(node2);
			}
			if(this_map.size() == 0)
				this.cost_function.remove(node1);
		}
		
		for(SearchSpaceNode node: goal.getNeighbors()) {
			node.getNeighbors().remove(goal);
			cost_function.get(node).remove(goal);
		}
		search_space_list.remove(start);
		search_space_list.remove(goal);
		
		return path;
	}
	
	public PathUpdater getPath(Point start_point, Point goal_point) {
		return null;
	}
	
	public ArrayList<Point> getSubpath(Point start_point, Point goal_point) {
		return null;
	}
}
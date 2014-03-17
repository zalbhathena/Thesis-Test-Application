import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class THPAAbstractGraphSpaceManager implements SearchSpaceManager{
	
	Set<Node> search_space = new HashSet<Node>();
	ArrayList<Node> search_space_list = new ArrayList<Node>();
	THPAStarPointAgentEdgeCache edge_cache;
	Map<Node, Integer> node_to_cluster;
	Map<Integer, Set<Node>> cluster_to_node;
	
	public THPAAbstractGraphSpaceManager(Set<Node> search_space, 
			THPAStarPointAgentEdgeCache edge_cache, Map<Node, Integer> node_to_cluster,
			Map<Integer, Set<Node>> cluster_to_node){
		this.search_space_list = new ArrayList<Node>(search_space);
		this.search_space = search_space;
		this.edge_cache = edge_cache;
		this.node_to_cluster = node_to_cluster;
		this.cluster_to_node = cluster_to_node;
	}

	@Override
	public Set<Node> getNeighborsForNode(Node node, boolean cluster) {
		// TODO Auto-generated method stub
		return search_space_list.get(search_space_list.indexOf(node)).getNeighbors();
	}

	@Override
	public Set<Node> getEntranceNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Node> getSearchSpace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Polygon> getClusterBoundaries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getClusterID(Node node) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCost(Node from, Node to) {
		// TODO Auto-generated method stub
		return 0;
	}

	public ArrayList<Node>getPath(Point start_point, Point goal_point, Node start, Node goal, int start_id, int goal_id, Map<Node,Node>new_to_old) {
		
		Node new_start = new TriangulationNode(start);
		Node new_goal = new TriangulationNode(goal);
		
		if(cluster_to_node.get(start_id) == null) {
			System.out.println("POOP");
		}
		
		boolean found = false;
		for(Node node:search_space) {
			Point[]points= node.getPoints();
			if(node.equals(start)) {
				new_start = node;
				found = true;
			}
		}
		boolean found2 = false;
		for(Node node:search_space) {
			Point[]points= node.getPoints();
			if(node.equals(goal)) {
				new_goal = node;
				found2 = true;
			}
		}
		if(!found) {
			search_space_list.add(new_start);
			search_space.add(new_start);
			new_to_old.put(new_start,start);
		for(Node node:cluster_to_node.get(start_id)) {
			new_start.addNeighbor(node);
			//for(Node neighbor:node.getNeighbors()) {
				//Point[] shared_points = SearchAlgorithms.sharedPointsForNodes(node, neighbor);
				//if(shared_points.length == 2 && node_to_cluster.get(neighbor) != node_to_cluster.get(node)) {
					Node from = start;
					Node to = new_to_old.get(node);
					Object[] array = SearchAlgorithms.TAStarFValue(edge_cache.manager, null, from, to, true);
					edge_cache.addEdge(new_start, node, null,(Double)array[0],(Edge)array[1]);
				//}
					
			//}
		}
		}
		if(!found2) {
			search_space_list.add(new_goal);
			search_space.add(new_goal);
			new_to_old.put(new_goal,goal);
		for(Node node:cluster_to_node.get(goal_id)) {
			node.addNeighbor(new_goal);
			//for(Node neighbor:node.getNeighbors()) {
				//Point[] shared_points = SearchAlgorithms.sharedPointsForNodes(node, neighbor);
				//if(shared_points.length == 2 && node_to_cluster.get(neighbor) != node_to_cluster.get(node))
				Node from = new_to_old.get(node);
				Node to = goal;
				Object[] array = SearchAlgorithms.TAStarFValue(edge_cache.manager, null, from, to, true);
				edge_cache.addEdge(node, new_goal, null,(Double)array[0],(Edge)array[1]);
			//}
		}
		}
		
		ArrayList<Node> path = SearchAlgorithms.TAStarWithEdgeCache(this, start_point, goal_point, new_start, new_goal, edge_cache, false);
		
		if(!found) {
		for(Node node:cluster_to_node.get(start_id)) {
			new_start.removeNeighbor(node);
			for(Node neighbor:node.getNeighbors()) {
				Point[] shared_points = SearchAlgorithms.sharedPointsForNodes(node, neighbor);
				if(shared_points.length == 2 && node_to_cluster.get(neighbor) != node_to_cluster.get(node))
					edge_cache.removeEdge(new_start, node, new Edge(shared_points[0],shared_points[1]));
			}
		}
		search_space_list.remove(new_start);
		search_space.remove(new_start);
		}
		if(!found2) {
		for(Node node:cluster_to_node.get(goal_id)) {
			node.removeNeighbor(new_goal);
			for(Node neighbor:node.getNeighbors()) {
				Point[] shared_points = SearchAlgorithms.sharedPointsForNodes(node, neighbor);
				if(shared_points.length == 2 && node_to_cluster.get(neighbor) != node_to_cluster.get(node))
					edge_cache.removeEdge(node, new_goal, new Edge(shared_points[0],shared_points[1]));
			}
		}
		search_space.remove(new_goal);
		search_space_list.remove(new_goal);
		}
		
		return path;
	}
	
	public Edge edgeForNodes(Node n1, Node n2) {
		
		return null;
	}
	
	@Override
	public PathUpdater getPath(Point start, Point goal) {
		
		return null;
	}

	@Override
	public ArrayList<Point> getSubpath(Point start_point, Point goal_point) {
		// TODO Auto-generated method stub
		return null;
	}

}

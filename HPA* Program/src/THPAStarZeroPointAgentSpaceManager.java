import galois.objects.graph.GNode;
import galois.objects.graph.IntGraph;
import galois.objects.graph.MorphGraph;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.awt.Polygon;

import partition.main.MetisGraph;
import partition.main.MetisNode;
import partition.main.PMetis;

public class THPAStarZeroPointAgentSpaceManager implements SearchSpaceManager, THPAStarManager{
	int width, height;
	Set<Node> search_space = new HashSet<Node>();
	Map<Node,Map<Node, Double>> cost_function = new HashMap<Node,Map<Node, Double>>();
	Set<Polygon> boundary_set = new HashSet<Polygon>();
	
	Map<Node,Integer>clusterID_map = new HashMap<Node,Integer>();
	Map<Node,Node> new_to_old = new HashMap<Node,Node>();
	
	Set<Node> entrance_nodes = new HashSet<Node>();
	
	THPAAbstractGraphSpaceManager abstract_graph;
	
	public THPAStarZeroPointAgentSpaceManager(ArrayList<Obstacle> obstacle_list, int width, int height) {
		
		this.width = width;
		this.height = height;
		
		Rectangle boundary = new Rectangle(0,0,width,height);
		
		search_space = SearchAlgorithms.getTriangulation(boundary,obstacle_list);
		
		int[]x_list = {0,width,width,0};
		int[]y_list = {0,0,height,height,0};
		Polygon p = new Polygon(x_list,y_list,4);
		boundary_set = new HashSet<Polygon>();
		boundary_set.add(p);
		
		findClustering();
		findDistanceCache();
		findBoundarySet();
		
		
		createCostFunction();
	}
	
	private void findClustering() {
		/*
		MetisGraph metis_graph;
		IntGraph<MetisNode> graph = new MorphGraph.IntGraphBuilder().backedByVector(true).directed(true).create();
		
		ArrayList<GNode<MetisNode>> nodes = new ArrayList<GNode<MetisNode>>();
		
		Map<Node,GNode>node_to_gnode = new HashMap<Node,GNode>();
		
		int i = 0;
		for(Node node:search_space) {
			GNode<MetisNode> n = graph.createNode(new MetisNode(i, 1));
	        nodes.add(n);
	        graph.add(n);
	        
	        node_to_gnode.put(node,n);
	        
	        i++;
		}
		
		int num_edges = 0;
		for(Node node:search_space) {
			GNode<MetisNode> gnode = node_to_gnode.get(node);
			for(Node neighbor:node.getNeighbors()) {
				GNode<MetisNode>gneighbor = node_to_gnode.get(neighbor);
				graph.addEdge(gnode, gneighbor, 1);
				gnode.getData().addEdgeWeight(1);
				gnode.getData().incNumEdges();
				num_edges++;
			}
		}
		
		MetisGraph metisGraph = new MetisGraph();
	    metisGraph.setNumEdges(num_edges / 2);
	    metisGraph.setGraph(graph);
	    
	    try {
	    	PMetis.partition(metisGraph, 2);
	    }
	    catch(ExecutionException e) {
	    	System.out.println("Partition Exception!!");
	    }
	    
	    int in = 0;
	    in++;
	    */
		int search_space_size = search_space.size();
		int cluster_size = search_space_size/2;//(int) Math.ceil(Math.sqrt(search_space_size));
		
		Stack<Node> remaining = new Stack<Node>();
		for(Node n:search_space) {
			remaining.add(n);
		}
		
		int current_cluster_size = 0;
		int current_cluster_id = 0;
		Node current_node = null;
		
		while(remaining.size() > 0) {
			if(current_node == null) {
				current_cluster_size = 0;
				current_cluster_id++;
				current_node = remaining.pop();
			}
			
			if(current_cluster_size >= cluster_size) {
				current_cluster_size = 0;
				current_cluster_id++;
			}
			
			clusterID_map.put(current_node,current_cluster_id);
			remaining.remove(current_node);
			current_cluster_size++;
			
			Node temp_current_node = current_node;
			current_node = null;
			
			for(Node next:temp_current_node.getNeighbors()) {
				if(remaining.contains(next)) {
					current_node = next;
					break;
				}
			}
		}
	}
	
	private void findDistanceCache() {
		Set<Node> entrances = new HashSet<Node>();
		Map<Node,Node>old_to_new = new HashMap<Node,Node>();
		Map<Node,Node>new_to_old = new HashMap<Node,Node>();
		Map<Node,Integer>node_to_cluster = new HashMap<Node,Integer>();
		Map<Integer,Set<Node>>cluster_to_node = new HashMap<Integer,Set<Node>>();
		
		for(Node node:search_space) {
			int id = getClusterID(node);
			
			Node new_node = null;
			if(old_to_new.containsKey(node))
				new_node = old_to_new.get(node);
			
			for(Node neighbor:node.getNeighbors()) {
				int neighbor_id = getClusterID(neighbor);
				if(neighbor_id != id) {

					if(new_node == null) {
						new_node = new TriangulationNode(node);
						entrances.add(new_node);
						old_to_new.put(node, new_node);
						new_to_old.put(new_node, node);
						node_to_cluster.put(new_node, id);
						if(!cluster_to_node.containsKey(id))
							cluster_to_node.put(id,new HashSet<Node>());
						cluster_to_node.get(id).add(new_node);
					}
					
					Node new_neighbor;
					if(old_to_new.containsKey(neighbor))
						new_neighbor = old_to_new.get(neighbor);
					else {
						new_neighbor = new TriangulationNode(neighbor);
						entrances.add(new_neighbor);
						old_to_new.put(neighbor, new_neighbor);
						new_to_old.put(new_neighbor, neighbor);
						node_to_cluster.put(new_neighbor, neighbor_id);
						if(!cluster_to_node.containsKey(neighbor_id))
							cluster_to_node.put(neighbor_id,new HashSet<Node>());
						cluster_to_node.get(neighbor_id).add(new_neighbor);
					}
					
					new_node.addNeighbor(new_neighbor);
				}
			}	
		}
		
		for(Node node:entrances) {
			int id = node_to_cluster.get(node);
			for(Node neighbor:entrances) {
				int neighbor_id = node_to_cluster.get(neighbor);
				if(neighbor_id == id)
					node.addNeighbor(neighbor);
			}
		}
		
		entrance_nodes = entrances;

		THPAStarPointAgentEdgeCache cache = new THPAStarPointAgentEdgeCache(this);
		for(Node node:entrance_nodes) {
			int id = node_to_cluster.get(node);
			for(Node neighbor:node.getNeighbors()) {
				int neighbor_id = node_to_cluster.get(neighbor);
				if(neighbor_id == id) {
					//for(Node neighbor2:node.getNeighbors()) {
						//if(id != node_to_cluster.get(neighbor2)) {
							//Point[] shared_points = SearchAlgorithms.sharedPointsForNodes(node, neighbor2);
							Node from = new_to_old.get(node);
							Node to = new_to_old.get(neighbor);
							Object[] array = SearchAlgorithms.TAStarFValue(this, null, from, to, true);
							if(array==null) {
								int i = 0;
								i++;
							}
							cache.addEdge(node, neighbor, null, (Double)array[0], (Edge)array[1]);
						//}
					//}
				}
			}
		}
		
		this.new_to_old = new_to_old;
		
		abstract_graph = new THPAAbstractGraphSpaceManager(entrance_nodes, cache,node_to_cluster,cluster_to_node);
	}
	
	private void findBoundarySet() {
		Map<Integer, ArrayList<Node>> reverse_map = new HashMap<Integer, ArrayList<Node>>();
		for(Node n:clusterID_map.keySet()) {
			int id = clusterID_map.get(n);
			if(!reverse_map.containsKey(id))
				reverse_map.put(id, new ArrayList<Node>());
			reverse_map.get(id).add(n);
		}
		boundary_set.clear();
		for(int id:reverse_map.keySet()) {
			ArrayList<Edge>edge_list = new ArrayList<Edge>();
			
			for(Node node:reverse_map.get(id)) {
				int node_id = getClusterID(node);
				Set<Node>neighbors = node.getNeighbors();
				Point[]node_point_list = node.getPoints();
				for(Node neighbor:neighbors) {
					
					if(node_id != getClusterID(neighbor)) {
						Point[] point_list = SearchAlgorithms.sharedPointsForNodes(node, neighbor);
						Edge e = new Edge(point_list[0],point_list[1]);
						edge_list.add(e);
					}
				}
				if(neighbors.size() < 3) {
					int[] p_count = new int[3];
					for(Node neighbor:neighbors) {
						Point[] neighbor_point_list = SearchAlgorithms.sharedPointsForNodes(node, neighbor);
						for(Point neighbor_point:neighbor_point_list) {
							for(int i = 0; i < 3; i++)
								if(neighbor_point.equals(node_point_list[i]))
									p_count[i]++;
 						}
					}
					if(p_count[0]<2 && p_count[1]<2) {
						edge_list.add(new Edge(node_point_list[0],node_point_list[1]));
					}
					if(p_count[0]<2 && p_count[2]<2) {
						edge_list.add(new Edge(node_point_list[0],node_point_list[2]));
					}
					if(p_count[1]<2 && p_count[2]<2) {
						edge_list.add(new Edge(node_point_list[1],node_point_list[2]));
					}
				}
			}
			
			for(Edge e: edge_list) {
				Point p1 = e.getToPoint();
				Point p2 = e.getFromPoint();
				int[] x_list = {p1.x,p2.x};
				int[] y_list = {p1.y,p2.y};
				boundary_set.add(new Polygon(x_list,y_list,2));
			}
		}
	}
	
	private void createCostFunction() {
		for(Node node1:search_space) {
			for(Node node2:node1.getNeighbors()) {
				if(node1 == node2)
					continue;
				
				Point p1 = node1.getPoints()[0];
				Point p2 = node2.getPoints()[0];
				double distance = Point.distance(p1.x, p1.y, p2.x, p2.y);
				
				if(!cost_function.containsKey(node1))
					cost_function.put(node1,new HashMap<Node, Double>());
				cost_function.get(node1).put(node2, distance);
					
				if(!cost_function.containsKey(node2))
					cost_function.put(node2,new HashMap<Node, Double>());
				cost_function.get(node2).put(node1, distance);
			}
		}
	}

	public Set<Node> getNeighborsForNode(Node node,
			boolean cluster) {
		Set<Node> neighbors = node.getNeighbors();
		if(!cluster)
			return neighbors;
		int node_id = getClusterID(node);
		Set<Node> new_neighbors = new HashSet<Node>();
		for(Node neighbor: neighbors) {
			if(getClusterID(neighbor) == node_id)
				new_neighbors.add(neighbor);
		}
		return new_neighbors;
	}


	public Set<Node> getEntranceNodes() {
		// TODO Auto-generated method stub
		return new HashSet<Node>();
	}


	public Set<Node> getSearchSpace() {
		// TODO Auto-generated method stub
		return search_space;
	}


	public Set<Polygon> getClusterBoundaries() {
		return boundary_set;
	}

	public int getClusterID(Node node) {
		return clusterID_map.get(node);
	}


	public double getCost(Node from, Node to) {
		return 0;
	}
	
	public PathUpdater getPath(Point start_point, Point goal_point) {
		return null;
	}
	
	public THPAStarPathUpdater getNodePath(Point start_point, Point goal_point) {
		Node start = null;
		Node goal = null;
		
		start_point = new Point(0,0);
		goal_point = new Point(width,height);
		for(Node node:search_space) {
			Point p1 = node.getPoints()[0];
			Point p2 = node.getPoints()[1];
			Point p3 = node.getPoints()[2];
			if(SearchAlgorithms.pointInTriangle(start_point, p1, p2, p3)) {
				start = node;
			}
			if(SearchAlgorithms.pointInTriangle(goal_point, p1, p2, p3)) {
				goal = node;
			}
			if(start!=null && goal!=null)
				break;
		}
		
		int start_id = getClusterID(start);
		int goal_id = getClusterID(goal);
		
		ArrayList<Node>node_path = abstract_graph.getPath(start_point, goal_point, start, goal, start_id, goal_id,new_to_old);
		
		//ArrayList<Point>point_list = new ArrayList<Point>();
		
		if(node_path == null) {
			System.out.println("NODEPATH IS NULL");
			node_path = new ArrayList<Node>();
			node_path.add(start);
		}
		/*System.out.println(node_path.size());
		Node previous = start;
		//System.out.println(list[0].x + " " + list[0].y + " " + list[1].x + " " + list[1].y + " " + list[2].x + " " + list[2].y);
		for(int i = 0; i<node_path.size(); i++) {
			Point[] points = node_path.get(i).getPoints();
			
			double x1 = points[0].x;
			double y1 = points[0].y;
			double x2 = points[1].x;
			double y2 = points[1].y;
			double x3 = points[2].x;
			double y3 = points[2].y;
			
			double px = (x1+x2+x3)/3;
			double py = (y1+y2+y3)/3;
			
			Point p = new Point((int)px,(int)py);
			
			point_list.add(p);
		}
		
		point_list.add(0, start_point);
		point_list.add(goal_point);*/
		
		THPAStarPathUpdater path_updater = new THPAStarPathUpdater(this,node_path);
		
		return path_updater;
	}
	
	public ArrayList<Node> getNodeSubpath(Node start, Node goal) {
		//USING NAIVE METHOD THAT STARTS FROM CENTER OF NODE
		Node new_start = new_to_old.get(start);
		Node new_goal = new_to_old.get(goal);
		

		Point[] points = new_start.getPoints();
		
		double x1 = points[0].x;
		double y1 = points[0].y;
		double x2 = points[1].x;
		double y2 = points[1].y;
		double x3 = points[2].x;
		double y3 = points[2].y;
		
		double px = (x1+x2+x3)/3;
		double py = (y1+y2+y3)/3;
		
		Point start_point = new Point((int)px,(int)py);
		
		points = new_goal.getPoints();
		
		x1 = points[0].x;
		y1 = points[0].y;
		x2 = points[1].x;
		y2 = points[1].y;
		x3 = points[2].x;
		y3 = points[2].y;
		
		px = (x1+x2+x3)/3;
		py = (y1+y2+y3)/3;
		
		Point goal_point = new Point((int)px,(int)py);
		
		ArrayList<Node> node_path = SearchAlgorithms.TAStar(this, start_point, goal_point, new_start, new_goal, true);
		
		return node_path;
	}
	
	public ArrayList<Point> getSubpath(Point start, Point goal) {
		ArrayList<Point> point = new ArrayList<Point>();
		point.add(goal);
		return point;
	}

}

import galois.objects.graph.GNode;
import galois.objects.graph.IntGraph;
import galois.objects.graph.MorphGraph;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import partition.main.MetisGraph;
import partition.main.MetisNode;
import partition.main.PMetis;
public class THPAStarPointAgentVertexSpaceManager implements SearchSpaceManager{

	Set<Node>search_space;
	Map<Integer,Node>point_to_node;
	int key_mult;
	
	int num_obstacles;
	int width,height;
	
	Map<Node,Integer>node_to_id;
	Map<Integer,Set<Node>>id_to_nodes;
	
	Set<Polygon>boundary_set;
	
	Set<Polygon>inter_edges;
	Map<Polygon,Integer>intra_edges;
	
	Set<Node>entrance_graph;
	Map<Node,Node> old_to_new;
	
	Map<Node,Map<Node,Double>>distance_cache;
	
	Point start_point;
	Point goal_point;
	
	boolean graphics = true;
	
	GraphSpaceManager entrance_manager;
	
	public THPAStarPointAgentVertexSpaceManager(ArrayList<Obstacle> obstacle_list, int width, int height) {
		int num_obstacles = obstacle_list.size();
		this.width = width;
		this.height = height;
		this.key_mult = Math.max(width, height);
		
		start_point = new Point(0,0);
		goal_point = new Point(width,height);
		
		Rectangle boundary = new Rectangle(0,0,width,height);
		
		search_space = SearchAlgorithms.getTriangulation1(boundary,obstacle_list);
		
		int[]x_list = {0,width,width,0};
		int[]y_list = {0,0,height,height,0};
		Polygon p = new Polygon(x_list,y_list,4);
		boundary_set = new HashSet<Polygon>();
		boundary_set.add(p);
		
		findClustering();
		
		if(graphics)
			findGraphVisuals();
		
		findEntranceGraph();
	}
	
	@Override
	public Set<Node> getNeighborsForNode(Node node, boolean cluster) {
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

	@Override
	public Set<Node> getEntranceNodes() {
		return new HashSet<Node>();
	}

	@Override
	public Set<Node> getSearchSpace() {
		return search_space;
	}

	@Override
	public Set<Polygon> getClusterBoundaries() {
		return boundary_set;
	}

	@Override
	public int getClusterID(Node node) {
		return node_to_id.get(node);
	}

	@Override
	public double getCost(Node from, Node to) {
		return 0;
	}

	@Override
	public PathUpdater getPath(Point start_point, Point goal_point) {
		start_point = this.start_point;
		goal_point = this.goal_point;
		
		Node old_start = point_to_node.get(start_point.x*key_mult + start_point.y);
		Node old_goal = point_to_node.get(goal_point.x*key_mult + goal_point.y);
		
		Node start = old_to_new.get(old_start);
		Node goal = old_to_new.get(old_goal);
		
		ArrayList<Point> path = entrance_manager.getPath(start, goal);
		path.add(0, start_point);
		PathUpdater path_updater = new PathUpdater(this,path);
		
		return path_updater;
	}

	@Override
	public ArrayList<Point> getSubpath(Point start_point, Point goal_point) {
		Node start = point_to_node.get(start_point.x*key_mult + start_point.y);
		Node goal = point_to_node.get(goal_point.x*key_mult + goal_point.y);
		
		ArrayList<Point> point_list = 
				SearchAlgorithms.AStarEuclideanCost(this, start_point, goal_point,start,goal,false);
		return point_list;
	}
	
	private void findClustering() {
		MetisGraph metis_graph;
		IntGraph<MetisNode> graph = new MorphGraph.IntGraphBuilder().backedByVector(true).directed(true).create();
		
		ArrayList<GNode<MetisNode>> nodes = new ArrayList<GNode<MetisNode>>();
		
		Map<Node,GNode<MetisNode>>node_to_gnode = new HashMap<Node,GNode<MetisNode>>();
		
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
	    
	    i-=num_obstacles*2;
	    System.out.println(i+"");
	    try {
	    	PMetis.partition(metisGraph, (int)Math.ceil(Math.sqrt(i)));
	    }
	    catch(ExecutionException e) {
	    	System.out.println("Partition Exception!!");
	    }

	    node_to_id = new HashMap<Node,Integer>();
	    id_to_nodes = new HashMap<Integer,Set<Node>>();
	    point_to_node = new HashMap<Integer,Node>();
	    
	    for(Node n:node_to_gnode.keySet()) {
	    	GNode<MetisNode> gnode = node_to_gnode.get(n);
	    	
	    	int cluster_id = gnode.getData().getPartition();
	    	
	    	node_to_id.put(n,cluster_id);
	    	
	    	if(!id_to_nodes.containsKey(cluster_id)) {
	    		id_to_nodes.put(cluster_id, new HashSet<Node>());
	    	}
	    	id_to_nodes.get(cluster_id).add(n);
	    	
	    	Point p = n.getPoints()[0];
	    	int key = p.x*key_mult + p.y;
	    	point_to_node.put(key, n);
	    }
	}
	
	private void findGraphVisuals() {
		inter_edges = new HashSet<Polygon>();
		intra_edges = new HashMap<Polygon,Integer>();
		for(Node node:search_space) {
			int node_id = getClusterID(node);
			for(Node neighbor:node.getNeighbors()) {
				int neighbor_id = getClusterID(neighbor);
				Point p1 = node.getPoints()[0];
				Point p2 = neighbor.getPoints()[0];
				int[] x_list = {p1.x,p2.x};
				int[] y_list = {p1.y,p2.y};
				Polygon p = new Polygon(x_list,y_list,2);
				if(node_id != neighbor_id) {
					inter_edges.add(p);
				}
				else {
					intra_edges.put(p,node_id);
				}
			}
		}
	}
	
	private void findEntranceGraph() {
		entrance_graph = new HashSet<Node>();
		old_to_new = new HashMap<Node,Node>();
		distance_cache = new HashMap<Node,Map<Node,Double>>();
		
		Map<Integer,Set<Node>>entrance_id_to_nodes = new HashMap<Integer,Set<Node>>();
		Map<Node,Integer>entrance_node_to_id = new HashMap<Node,Integer>();
		
		for(Node node:search_space) {
			boolean stop = true;
			
			int node_id = getClusterID(node);
			
			if(node.getPoints()[0].equals(start_point) || node.getPoints()[0].equals(goal_point)) {
				stop = false;
				
				if(!old_to_new.containsKey(node)) {
					Node new_node = new GraphNode(node);
					old_to_new.put(node, new_node);
					entrance_graph.add(new_node);
					
					if(!entrance_id_to_nodes.containsKey(node_id))
						entrance_id_to_nodes.put(node_id, new HashSet<Node>());
					entrance_id_to_nodes.get(node_id).add(new_node);
					
					entrance_node_to_id.put(new_node, node_id);
				}
			}
			
			for(Node neighbor:node.getNeighbors()) {
				int neighbor_id = getClusterID(neighbor);
				
				if(neighbor_id != node_id) {
					stop = false;
					
					Node new_node;
					if(!old_to_new.containsKey(node)) {
						new_node = new GraphNode(node);
						old_to_new.put(node, new_node);
						entrance_graph.add(new_node);
						
						if(!entrance_id_to_nodes.containsKey(node_id))
							entrance_id_to_nodes.put(node_id, new HashSet<Node>());
						entrance_id_to_nodes.get(node_id).add(new_node);
						
						entrance_node_to_id.put(new_node, node_id);
					}
					new_node = old_to_new.get(node);
					
					Node new_neighbor;
					if(!old_to_new.containsKey(neighbor)) {
						new_neighbor = new GraphNode(neighbor);
						old_to_new.put(neighbor, new_neighbor);
						entrance_graph.add(new_neighbor);
						
						if(!entrance_id_to_nodes.containsKey(neighbor_id))
							entrance_id_to_nodes.put(neighbor_id, new HashSet<Node>());
						entrance_id_to_nodes.get(neighbor_id).add(new_neighbor);
						
						entrance_node_to_id.put(new_neighbor, neighbor_id);
					}
					new_neighbor = old_to_new.get(neighbor);
					
					new_node.addNeighbor(new_neighbor);
					
					double distance = new_node.getPoints()[0].distance(new_neighbor.getPoints()[0]);
					if(!distance_cache.containsKey(new_node))
						distance_cache.put(new_node,new HashMap<Node,Double>());
					distance_cache.get(new_node).put(new_neighbor,distance);
				}
			}
			if(stop)
				continue;
			Node new_node = old_to_new.get(node);
			for(Node neighbor:id_to_nodes.get(node_id)) {
				int neighbor_id = getClusterID(neighbor);
				
				if(node_id == neighbor_id) {
					Node new_neighbor;
					if(!old_to_new.containsKey(neighbor)) {
						new_neighbor = new GraphNode(neighbor);
						old_to_new.put(neighbor, new_neighbor);
						entrance_graph.add(new_neighbor);
						
						if(!entrance_id_to_nodes.containsKey(neighbor_id))
							entrance_id_to_nodes.put(neighbor_id, new HashSet<Node>());
						entrance_id_to_nodes.get(neighbor_id).add(new_neighbor);
						
						entrance_node_to_id.put(new_neighbor, neighbor_id);
					}
					new_neighbor = old_to_new.get(neighbor);
					
					new_node.addNeighbor(new_neighbor);
					
					ArrayList<Point>point_list = SearchAlgorithms.AStarEuclideanCost(this, node.getPoints()[0], neighbor.getPoints()[0], node, neighbor, true);
					double distance = 0;
					
					if(point_list == null)
						distance = Double.MAX_VALUE;
					else {
						Point prev_point = point_list.remove(0);
						for(Point p:point_list) {
							distance+= prev_point.distance(p);
							prev_point = p;
						}
					}
					
					if(!distance_cache.containsKey(new_node))
						distance_cache.put(new_node,new HashMap<Node,Double>());
					distance_cache.get(new_node).put(new_neighbor,distance);
				}
			}
		}
		
		entrance_manager = new GraphSpaceManager(entrance_graph,distance_cache,entrance_node_to_id,entrance_id_to_nodes);
		
		
	}
	
}
import java.util.*;
import java.awt.Point;
import java.awt.Polygon;
public class HPAStarSpaceManager implements SearchSpaceManager{
	public int width, height;
	public int cluster_width, cluster_height;
	private Node[][] grid;
	private Set<Node> search_space = new HashSet<Node>();
	private Set<Polygon> cluster_boundaries = new HashSet<Polygon>();
	private GraphSpaceManager entrance_graph;
	
	public final int MINIMUM_ENTRACE_WIDTH = 6;
	
	Node[][] entrance_nodes_list;
	Set<Node> entrance_nodes = new HashSet<Node>();
	
	public HPAStarSpaceManager(ArrayList<Obstacle> obstacle_list, 
			int width, int height, int cluster_width, int cluster_height) {
		grid = new Node[width][height];
		this.width = width;
		this.height = height;
		this.cluster_height = height;
		this.cluster_width =  width;
		
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				grid[i][j] = new GridNode(i,j);
			}
		}
		for(int i = 0; i < obstacle_list.size(); i++) {
			Obstacle o = obstacle_list.get(i);
			for(int x = o.x; x < o.x+ o.width; x++)
				for(int y = o.y; y < o.y + o.height; y++) {
					if(x < width && y < height)
						grid[x][y] = null;
				}
		}
		for(int i = 0; i < width; i ++) {
			for(int j = 0; j < height; j++) {
				if(grid[i][j] == null)
					continue;
				Set<Node> neighbors = new HashSet<Node>();
				if(i!= width -1 && grid[i+1][j] != null) neighbors.add(grid[i+1][j]);
				if(i!=0 && grid[i-1][j] != null) neighbors.add(grid[i-1][j]);
				if(j!=height-1 && grid[i][j+1] != null) neighbors.add(grid[i][j+1]);
				if(j!=0 && grid[i][j-1] != null) neighbors.add(grid[i][j-1]);
				grid[i][j].setNeighbors(neighbors);
				search_space.add(grid[i][j]);
			}
		}
		
		this.cluster_width = cluster_width;
		this.cluster_height = cluster_height;
		findEntranceNodes();
		findDistanceCache();
		
		int clusters_per_row = clustersPerRow();
		int clusters_per_column = clustersPerColumn();
		for(int i = 0; i < clusters_per_row; i++) {
    		for(int j = 0; j < clusters_per_column; j++) {
    			int x = i*cluster_width;
    			int y = j*cluster_height;
    			int poly_width = (Math.min(width - x, cluster_width));
    			int poly_height = (Math.min(height - y, height));
    			int[] x_list = {x,x+poly_width,x+poly_width,x};
    			int[] y_list = {y,y,y+poly_height,y+poly_height};
    			Polygon p = new Polygon(x_list,y_list,4);
    			cluster_boundaries.add(p);
    		}
    	}
	}
	
	public int getClusterID(Node node) {
		Point[] node_point_list = node.getPoints();
		int x = node_point_list[0].x;
		int y = node_point_list[0].y;
		int cluster_id = x/cluster_width + (clustersPerRow())*(y/cluster_height);
		return cluster_id;
	}
	
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
	
	public Node getNode(int x, int y) {
		//if(grid[x][y] == null)
		//	width++;
		return grid[x][y];
	}
	
	private void findEntranceNodes() {
		entrance_nodes.clear();
		
		int clusters_per_row = clustersPerRow();
		int clusters_per_column = clustersPerColumn();
		entrance_nodes_list = new Node[clusters_per_row][clusters_per_column];
		for(int i = 0; i<clusters_per_row; i++) {
			for(int j = 0; j < clusters_per_column; j++) {
				if(i != 0) {
					int x = i*cluster_width;
					int y = j*cluster_height;
					int limit = Math.min(j*cluster_height + cluster_height, height);
					int open_count = 0;
					
					
					for(;y < limit; y++) {
						if(grid[x][y] != null && grid[x-1][y] != null)
							open_count++;
						else {
							if(open_count == 0)
								continue;
							else if(open_count < MINIMUM_ENTRACE_WIDTH) {
								entrance_nodes.add(grid[x][y - 1 - open_count/2]);
								entrance_nodes.add(grid[x - 1][y - 1 - open_count/2]);
							}
							else {
								entrance_nodes.add(grid[x][y - 1]);
								entrance_nodes.add(grid[x][y - open_count]);
								
								entrance_nodes.add(grid[x-1][y - 1]);
								entrance_nodes.add(grid[x-1][y - open_count]);
							}
							open_count = 0;
						}
					}
					if(open_count > 0) {
						if(open_count < MINIMUM_ENTRACE_WIDTH) {
							entrance_nodes.add(grid[x][y - 1 - open_count/2]);
							entrance_nodes.add(grid[x - 1][y - 1 - open_count/2]);
						}
						else {
							entrance_nodes.add(grid[x][y - 1]);
							entrance_nodes.add(grid[x][y - open_count]);
							
							entrance_nodes.add(grid[x-1][y - 1]);
							entrance_nodes.add(grid[x-1][y - open_count]);
						}
					}
				}
				
				if(j != 0) {
					int x = i*cluster_width;
					int y = j*cluster_height;
					int limit = Math.min(i*cluster_width + cluster_width, width);
					int open_count = 0;
					
					
					for(;x < limit; x++) {
						if(grid[x][y] != null && grid[x][y-1] != null)
							open_count++;
						else {
							if(open_count == 0)
								continue;
							else if(open_count < MINIMUM_ENTRACE_WIDTH) {
								entrance_nodes.add(grid[x - 1 - open_count/2][y]);
								entrance_nodes.add(grid[x - 1 - open_count/2][y-1]);
							}
							else {
								entrance_nodes.add(grid[x - 1][y]);
								entrance_nodes.add(grid[x - open_count][y]);
								
								entrance_nodes.add(grid[x - 1][y - 1]);
								entrance_nodes.add(grid[x - open_count][y - 1]);
							}
							open_count = 0;
						}
					}
					if(open_count > 0) {
						if(open_count < MINIMUM_ENTRACE_WIDTH) {
							entrance_nodes.add(grid[x - 1 - open_count/2][y]);
							entrance_nodes.add(grid[x - 1 - open_count/2][y-1]);
						}
						else {
							entrance_nodes.add(grid[x - 1][y]);
							entrance_nodes.add(grid[x - open_count][y]);
							
							entrance_nodes.add(grid[x - 1][y - 1]);
							entrance_nodes.add(grid[x - open_count][y - 1]);
						}
					}
				}
				
				if(i != clusters_per_row - 1) {
					int x = i*cluster_width + cluster_width - 1;
					int y = j*cluster_height;
					int limit = Math.min(j*cluster_height + cluster_height, height);
					int open_count = 0;
					
					
					for(;y < limit; y++) {
						if(grid[x][y] != null && grid[x+1][y] != null)
							open_count++;
						else {
							if(open_count == 0)
								continue;
							else if(open_count < MINIMUM_ENTRACE_WIDTH) {
								entrance_nodes.add(grid[x][y - 1 - open_count/2]);
								entrance_nodes.add(grid[x + 1][y - 1 - open_count/2]);
							}
							else {
								entrance_nodes.add(grid[x][y - 1]);
								entrance_nodes.add(grid[x][y - open_count]);
								
								entrance_nodes.add(grid[x+1][y - 1]);
								entrance_nodes.add(grid[x+1][y - open_count]);
							}
							open_count = 0;
						}
					}
					if(open_count > 0) {
						if(open_count < MINIMUM_ENTRACE_WIDTH) {
							entrance_nodes.add(grid[x][y - 1 - open_count/2]);
							entrance_nodes.add(grid[x + 1][y - 1 - open_count/2]);
						}
						else {
							entrance_nodes.add(grid[x][y - 1]);
							entrance_nodes.add(grid[x][y - open_count]);
							
							entrance_nodes.add(grid[x+1][y - 1]);
							entrance_nodes.add(grid[x+1][y - open_count]);
						}
					}
					
				}
				
				if(j != clusters_per_column - 1) {
					int x = i*cluster_width;
					int y = j*cluster_height + cluster_height - 1;
					int limit = Math.min(i*cluster_width + cluster_width, width);
					int open_count = 0;
					
					
					for(;x < limit; x++) {
						if(grid[x][y] != null && grid[x][y+1] != null)
							open_count++;
						else {
							if(open_count == 0)
								continue;
							else if(open_count < MINIMUM_ENTRACE_WIDTH) {
								entrance_nodes.add(grid[x - 1 - open_count/2][y]);
								entrance_nodes.add(grid[x - 1 - open_count/2][y+1]);
							}
							else {
								entrance_nodes.add(grid[x - 1][y]);
								entrance_nodes.add(grid[x - open_count][y]);
								
								entrance_nodes.add(grid[x - 1][y + 1]);
								entrance_nodes.add(grid[x - open_count][y + 1]);
							}
							open_count = 0;
						}
					}
					if(open_count > 0) {
						if(open_count < MINIMUM_ENTRACE_WIDTH) {
							entrance_nodes.add(grid[x - 1 - open_count/2][y]);
							entrance_nodes.add(grid[x - 1 - open_count/2][y+1]);
						}
						else {
							entrance_nodes.add(grid[x - 1][y]);
							entrance_nodes.add(grid[x - open_count][y]);
							
							entrance_nodes.add(grid[x - 1][y + 1]);
							entrance_nodes.add(grid[x - open_count][y + 1]);
						}
					}
				}
				
			}
		}
		for(Node node:entrance_nodes) {
			int cluster_id = getClusterID(node);
			entrance_nodes_list[cluster_id/clusters_per_column][cluster_id/clusters_per_row] = node;
		}
	}
	
	private void findDistanceCache() {
		Map<Node, Map<Node, Double>> cost_function = 
				new HashMap<Node, Map<Node, Double>>();
		
		Map<Node, Integer>cluster_function = new HashMap<Node,Integer>();
		
		Map<Integer,Set<Node>>nodes_in_cluster = new HashMap<Integer,Set<Node>>();
		
		Set<Node> temp_entrance_nodes = new HashSet<Node>();
		for(Node node: entrance_nodes) {
			temp_entrance_nodes.add(new GridNode(node));
		}
		entrance_nodes = temp_entrance_nodes;
		
		for(Node node:entrance_nodes) {
			
			int cluster_id = getClusterID(node);
			cluster_function.put(node, cluster_id);
			
			if(nodes_in_cluster.get(cluster_id) == null)
				nodes_in_cluster.put(cluster_id, new HashSet<Node>());
			
			nodes_in_cluster.get(cluster_id).add(node);
		}
		for(int i:nodes_in_cluster.keySet()) {
			for(Node node1:nodes_in_cluster.get(i)) {
				for(Node node2:nodes_in_cluster.get(i)) {
					if(node1 == node2)
						continue;
					Point node1_point = node1.getPoints()[0];
					Point node2_point = node2.getPoints()[0];
					Node start = grid[node1_point.x][node1_point.y];
					Node goal = grid[node2_point.x][node2_point.y];;
					ArrayList<Point> point_list = 
							SearchAlgorithms.AStar(this,null,node1_point, node2_point,start,goal,true);
					
					if(point_list == null || point_list.size() < 1) {
						continue;
					}
					
					node1.addNeighbor(node2);
					node2.addNeighbor(node1);
					
					double distance = point_list.size();
					if(!cost_function.containsKey(node1))
						cost_function.put(node1,new HashMap<Node, Double>());
					cost_function.get(node1).put(node2, distance);
						
					if(!cost_function.containsKey(node2))
						cost_function.put(node2,new HashMap<Node, Double>());
					cost_function.get(node2).put(node1, distance);
				}
			}
		}
		
		for(Node node:entrance_nodes) {
			for(Node neighbor:entrance_nodes) {
				double distance = node.getPoints()[0].distance(neighbor.getPoints()[0]);
				
				if(cluster_function.get(node) == cluster_function.get(neighbor))
					continue;
				if(distance != 1.0) 
					continue;
				
				node.addNeighbor(neighbor);
				neighbor.addNeighbor(node);
				
				if(!cost_function.containsKey(node))
					cost_function.put(node,new HashMap<Node, Double>());
				cost_function.get(node).put(neighbor, distance);
					
				if(!cost_function.containsKey(neighbor))
					cost_function.put(neighbor,new HashMap<Node, Double>());
				cost_function.get(neighbor).put(node, distance);
			}
		}
		/*
		for(Node node2:entrance_nodes) {
			int neighbor = (int)node1.point_list[0].distance(node2.point_list[0]);
			
			if(node1 == node2)
				continue;
			if(node1_cluster_id != getClusterID(node2) && neighbor > 1.0)
				continue;
			if(cost_function.containsKey(node2) && cost_function.get(node2).containsKey(node1)) {
				continue;
			}
			
			node1.addNeighbor(node2);
			node2.addNeighbor(node1);
			
			ArrayList<Point> point_list = 
					SearchAlgorithms.AStar(this,null, node1.point_list[0], node2.point_list[0],node1,node2,false);
			 double distance = point_list.size();
			if(!cost_function.containsKey(node1))
				cost_function.put(node1,new HashMap<Node, Double>());
			cost_function.get(node1).put(node2, distance);
				
			if(!cost_function.containsKey(node2))
				cost_function.put(node2,new HashMap<Node, Double>());
			cost_function.get(node2).put(node1, distance);
			
		}
		*/
		entrance_graph = new GraphSpaceManager(entrance_nodes, cost_function, cluster_function, nodes_in_cluster);
 	}
	
	public PathUpdater getPath(Point start_point, Point goal_point) {
		Node start = new GridNode(start_point);
		Node goal = new GridNode(goal_point);
		
		
		int start_cluster_id = getClusterID(start);
		int goal_cluster_id = getClusterID(goal);
		
		Map<Node, Map<Node,Double>> extra_cost_function = 
				new HashMap<Node, Map<Node,Double>>();
		for(Node node: entrance_graph.cluster_to_node.get(start_cluster_id)) 
			start.addNeighbor(node);
		for(Node node: entrance_graph.cluster_to_node.get(goal_cluster_id))
			goal.addNeighbor(node);
		
		for(Node node: start.getNeighbors()) {			
			ArrayList<Point> point_list = 
					SearchAlgorithms.AStar(this,null, start.getPoints()[0], node.getPoints()[0],start,node,false);
			if(point_list == null || point_list.size() < 1) {
				start.getNeighbors().remove(node);
				continue;
			}
			double distance = 0;
			Point prev_point = point_list.remove(0);
			for(Point p:point_list) {
				distance+= prev_point.distance(p);
				prev_point = p;
			}
			if(extra_cost_function.get(start) == null)
				extra_cost_function.put(start, new HashMap<Node, Double>());
			extra_cost_function.get(start).put(node, distance);
		}
		for(Node node: goal.getNeighbors()) {
			ArrayList<Point> point_list = 
					SearchAlgorithms.AStar(this,null, goal.getPoints()[0], node.getPoints()[0],goal,node,true);
			if(point_list == null || point_list.size() < 1) {
				goal.getNeighbors().remove(node);
				continue;
			}
			double distance = 0;
			Point prev_point = point_list.remove(0);
			for(Point p:point_list) {
				distance+= prev_point.distance(p);
				prev_point = p;
			}
			if(extra_cost_function.get(node) == null)
				extra_cost_function.put(node, new HashMap<Node, Double>());
			extra_cost_function.get(node).put(goal, distance);
			if(extra_cost_function.get(goal) == null)
				extra_cost_function.put(goal, new HashMap<Node, Double>());
			extra_cost_function.get(goal).put(node, distance);
			
		}
		ArrayList<Point> path = entrance_graph.getPath(start_point, goal_point, start, goal,extra_cost_function);
		path.add(0, start_point);
		PathUpdater path_updater = new PathUpdater(this,path);
		/*ArrayList<Point> new_path = new ArrayList<Point>();
		Point _start = start_point;
		Point _goal;
		while(path.size() > 0) {
			_goal = path.remove(0);
			ArrayList<Point>temp_path = getSubpath(_start,_goal);
			for(int i =0; i < temp_path.size(); i++)
				new_path.add(temp_path.get(i));
			_start = _goal;
		}*/
		return path_updater;
	}
	
	public ArrayList<Point> getSubpath(Point start_point, Point goal_point) {
		Node start = grid[start_point.x][start_point.y];
		Node goal = grid[goal_point.x][goal_point.y];
		ArrayList<Point> point_list = 
				SearchAlgorithms.AStar(this,null, start_point, goal_point,start,goal,false);
		return point_list;
	}
	
	
	public int clustersPerRow() {
		return (int)Math.ceil( width / cluster_width);
	}
	public int clustersPerColumn() {
		return (int)Math.ceil( width / cluster_height);
	}
	
	public Set<Node>getEntranceNodes() {return entrance_nodes;}
	
	public Set<Node>getSearchSpace() {return search_space;}
	
	public Set<Polygon> getClusterBoundaries() {
		return cluster_boundaries;
	}
	
	public double getCost(Node from, Node to) {
		if(!from.getNeighbors().contains(to))
			throw new IllegalArgumentException("Not neighboring states.");
		return 1;
	}
	
}
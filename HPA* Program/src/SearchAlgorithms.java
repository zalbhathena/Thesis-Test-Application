import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map;


public class SearchAlgorithms {
	static {
		System.loadLibrary("SearchAlgorithms");
	}
	
	final static double END = -12345.6;
	
	static SearchAlgorithms triangulation_library = new SearchAlgorithms();
	
	private native double[] getTriangulation(double[] f);
	private native double[] getTriangulation2(double[] f);
	
	public static Set<Node> getTriangulation1(Rectangle boundary,ArrayList<Obstacle> obstacle_list) {
		final int FACTOR = 4;
		double obstacle_boundaries[] = new double[obstacle_list.size()*18 + 9 + 1];
		//9 doubles for each obstacle (4 points and END)
		//plus the boundary rectangle and the final END 

		System.out.println(boundary);

		int boundary_width = boundary.width*FACTOR;
		int boundary_height = boundary.height*FACTOR;
		int min_x = boundary.x * FACTOR;
		int max_x = boundary.x * FACTOR + boundary_width;
		int min_y = boundary.y * FACTOR;
		int max_y = boundary.y * FACTOR + boundary_height;

		obstacle_boundaries[0] = min_x;
		obstacle_boundaries[1] = min_y;
		obstacle_boundaries[2] = max_x;
		obstacle_boundaries[3] = min_y;
		obstacle_boundaries[4] = max_x;
		obstacle_boundaries[5] = max_y;
		obstacle_boundaries[6] = min_x;
		obstacle_boundaries[7] = max_y;

		obstacle_boundaries[8] = END;

		int key_mult = Math.max(boundary.width,boundary.height)*FACTOR;

		Map<Integer,Node> triangulations = new HashMap<Integer,Node>();
		int border_top_left_key = key_mult*min_x + min_y;
		int border_top_right_key = key_mult*max_x + min_y;
		int border_bottom_right_key = key_mult*max_x + max_y;
		int border_bottom_left_key = key_mult*min_x + max_y;
		triangulations.put(border_top_left_key,new GraphNode(min_x/FACTOR,min_y/FACTOR));
		triangulations.put(border_top_right_key,new GraphNode(max_x/FACTOR,min_y/FACTOR));
		triangulations.put(border_bottom_right_key,new GraphNode(max_x/FACTOR,max_y/FACTOR));
		triangulations.put(border_bottom_left_key,new GraphNode(min_x/FACTOR,max_y/FACTOR));

		ArrayList<Rectangle> removeList = new ArrayList<Rectangle>();

		for(int i = 0; i < obstacle_list.size(); i++) {
			Obstacle o = obstacle_list.get(i);

			int o_x = o.x * FACTOR;
			int o_y = o.y * FACTOR;
			int o_width = o.width * FACTOR;
			int o_height = o.height * FACTOR;

			int inner_x = o_x + 1;
			int inner_y = o_y + 1;
			int inner_width = o_width - 2;
			int inner_height = o_height - 2;

			obstacle_boundaries[(i) * 18 + 9 + 0] = o_x;
			obstacle_boundaries[(i) * 18 + 9 + 1] = o_y;
			obstacle_boundaries[(i) * 18 + 9 + 2] = o_x + o_width;
			obstacle_boundaries[(i) * 18 + 9 + 3] = o_y;
			obstacle_boundaries[(i) * 18 + 9 + 4] = o_x + o_width;
			obstacle_boundaries[(i) * 18 + 9 + 5] = o_y + o_height;
			obstacle_boundaries[(i) * 18 + 9 + 6] = o_x;
			obstacle_boundaries[(i) * 18 + 9 + 7] = o_y + o_height;

			obstacle_boundaries[(i) * 18 + 9 + 8] = END;

			obstacle_boundaries[(i) * 18 + 9 + 9] = inner_x;
			obstacle_boundaries[(i) * 18 + 9 + 10] = inner_y;
			obstacle_boundaries[(i) * 18 + 9 + 11] = inner_x + inner_width;
			obstacle_boundaries[(i) * 18 + 9 + 12] = inner_y;
			obstacle_boundaries[(i) * 18 + 9 + 13] = inner_x + inner_width;
			obstacle_boundaries[(i) * 18 + 9 + 14] = inner_y + inner_height;
			obstacle_boundaries[(i) * 18 + 9 + 15] = inner_x;
			obstacle_boundaries[(i) * 18 + 9 + 16] = inner_y + inner_height;

			obstacle_boundaries[(i) * 18 + 9 + 17] = END;

			int top_left_key = key_mult*o_x + o_y;
			int top_right_key = key_mult*(o_x+o_width) + o_y;
			int bottom_right_key = key_mult*(o_x+o_width) + o_y + o_height;
			int bottom_left_key = key_mult*o_x + o_y+o_height;

			if(!triangulations.containsKey(top_left_key))
				triangulations.put(top_left_key, new GraphNode(o_x/FACTOR,o_y/FACTOR));
			if(!triangulations.containsKey(top_right_key))
				triangulations.put(top_right_key, new GraphNode((o_x+o_width)/FACTOR,o_y/FACTOR));
			if(!triangulations.containsKey(bottom_right_key))
				triangulations.put(bottom_right_key, new GraphNode((o_x+o_width)/FACTOR,(o_y+o_height)/FACTOR));
			if(!triangulations.containsKey(bottom_left_key))
				triangulations.put(bottom_left_key, new GraphNode(o_x/FACTOR,(o_y+o_height)/FACTOR));
		}

		obstacle_boundaries[obstacle_boundaries.length - 1] = END;

		double[] edge_list = triangulation_library.getTriangulation2(obstacle_boundaries);



		int num_edges = edge_list.length;

		for(int i = 0; i < num_edges; i+=4) {
			int x1 = (int)edge_list[i + 0];
			int y1 = (int)edge_list[i + 1];
			int x2 = (int)edge_list[i + 2];
			int y2 = (int)edge_list[i + 3];
			if(x1<min_x || x1>max_x)
				continue;
			if(x2<min_x || x2>max_x)
				continue;
			if(y1<min_y || y1>max_y)
				continue;
			if(y2<min_y || y2>max_y)
				continue;

			int key1 = key_mult*x1 + y1;
			int key2 = key_mult*x2 + y2;

			if(!triangulations.containsKey(key1) || !triangulations.containsKey(key2))
				continue;

			Node n1 = triangulations.get(key1);
			Node n2 = triangulations.get(key2);
			
			n1.addNeighbor(n2);
			n2.addNeighbor(n1);
		}

		Set<Node>set = new HashSet<Node>();
		for(int key:triangulations.keySet())
			set.add(triangulations.get(key));

		return set;
	}
	
	public static Set<Node> getTriangulation(Rectangle boundary,ArrayList<Obstacle> obstacle_list) {
		double obstacle_boundaries[] = new double[obstacle_list.size()*9 + 9 + 1];
		//9 doubles for each obstacle (4 points and END)
		//plus the boundary rectangle and the final END 
		
		System.out.println(boundary);
		
		Map<Integer,Map<Integer,Set<Node>>>edge_map = new HashMap<Integer,Map<Integer,Set<Node>>>();
		Map<Integer,Set<Integer>>constrained_edges = new HashMap<Integer,Set<Integer>>();
		
		int boundary_width = boundary.width;
		int boundary_height = boundary.height;
		int min_x = boundary.x;
		int max_x = boundary.x + boundary_width;
		int min_y = boundary.y;
		int max_y = boundary.y + boundary_height;
		
		obstacle_boundaries[0] = min_x;
		obstacle_boundaries[1] = min_y;
		obstacle_boundaries[2] = max_x;
		obstacle_boundaries[3] = min_y;
		obstacle_boundaries[4] = max_x;
		obstacle_boundaries[5] = max_y;
		obstacle_boundaries[6] = min_x;
		obstacle_boundaries[7] = max_y;
		
		obstacle_boundaries[8] = END;
		
		int key_mult = Math.max(boundary.width,boundary.height);
		
		for(int i = 0; i < obstacle_list.size(); i++) {
			Obstacle o = obstacle_list.get(i);
			
			int o_x = o.x;
			int o_y = o.y;
			int o_width = o.width;
			int o_height = o.height;
			
			//removeList.add(new Rectangle(inner_x,inner_y,inner_width,inner_height));
			
			obstacle_boundaries[(i) * 9 + 9 + 0] = o_x;
			obstacle_boundaries[(i) * 9 + 9 + 1] = o_y;
			obstacle_boundaries[(i) * 9 + 9 + 2] = o_x + o_width;
			obstacle_boundaries[(i) * 9 + 9 + 3] = o_y;
			obstacle_boundaries[(i) * 9 + 9 + 4] = o_x + o_width;
			obstacle_boundaries[(i) * 9 + 9 + 5] = o_y + o_height;
			obstacle_boundaries[(i) * 9 + 9 + 6] = o_x;
			obstacle_boundaries[(i) * 9 + 9 + 7] = o_y + o_height;
			
			obstacle_boundaries[(i) * 9 + 9 + 8] = END;
		}
		
		obstacle_boundaries[obstacle_boundaries.length - 1] = END;
		
		double[] edge_list = triangulation_library.getTriangulation(obstacle_boundaries);
		
		int num_edges = edge_list.length;
		int start = 0;
		for(int i = 0; i < num_edges; i+=4) {
			if(edge_list[i] == END) {
				start = i + 1;
				break;
			}
			
			int x1 = (int)Math.round(edge_list[i + 0]);
			int y1 = (int)Math.round(edge_list[i + 1]);
			int x2 = (int)Math.round(edge_list[i + 2]);
			int y2 = (int)Math.round(edge_list[i + 3]);
			
			int p1Key = x1*key_mult + y1;
			int p2Key = x2*key_mult + y2;
			
			int minKey = Math.min(p1Key, p2Key);
			int maxKey = Math.max(p1Key, p2Key);
			
			if(!constrained_edges.containsKey(minKey))
				constrained_edges.put(minKey,new HashSet<Integer>());
			constrained_edges.get(minKey).add(maxKey);
		}
		
		Set<Node>search_space = new HashSet<Node>();

		for(int i = start; i < num_edges; i+=6) {
			int x1 = (int)Math.round(edge_list[i + 0]);
			int y1 = (int)Math.round(edge_list[i + 1]);
			int x2 = (int)Math.round(edge_list[i + 2]);
			int y2 = (int)Math.round(edge_list[i + 3]);
			int x3 = (int)Math.round(edge_list[i + 4]);
			int y3 = (int)Math.round(edge_list[i + 5]);
			
			Point p1 = new Point(x1,y1);
			Point p2 = new Point(x2,y2);
			Point p3 = new Point(x3,y3);
			
			if(x1<min_x || x1>max_x || y1<min_y || y1>max_y)
				continue;
			if(x2<min_x || x2>max_x || y2<min_y || y2>max_y)
				continue;
			if(x3<min_x || x3>max_x || y3<min_y || y3>max_y)
				continue;
			
			int p1Key = x1*key_mult + y1;
			int p2Key = x2*key_mult + y2;
			int p3Key = x3*key_mult + y3;
			
			TriangulationNode node = new TriangulationNode(p1,p2,p3);
			search_space.add(node);
			
			int minKey;
			int maxKey;
			
			minKey = Math.min(p1Key, p2Key);
			maxKey = Math.max(p1Key, p2Key);
			if(constrained_edges.containsKey(minKey) && constrained_edges.get(minKey).contains(maxKey));
			else {
				if(!edge_map.containsKey(minKey))
					edge_map.put(minKey, new HashMap<Integer,Set<Node>>());
				if(!edge_map.get(minKey).containsKey(maxKey))
					edge_map.get(minKey).put(maxKey, new HashSet<Node>());
				edge_map.get(minKey).get(maxKey).add(node);
			}
			
			minKey = Math.min(p1Key, p3Key);
			maxKey = Math.max(p1Key, p3Key);
			if(constrained_edges.containsKey(minKey) && constrained_edges.get(minKey).contains(maxKey));
			else {
				if(!edge_map.containsKey(minKey))
					edge_map.put(minKey, new HashMap<Integer,Set<Node>>());
				if(!edge_map.get(minKey).containsKey(maxKey))
					edge_map.get(minKey).put(maxKey, new HashSet<Node>());
				edge_map.get(minKey).get(maxKey).add(node);
			}
			
			minKey = Math.min(p3Key, p2Key);
			maxKey = Math.max(p3Key, p2Key);
			if(constrained_edges.containsKey(minKey) && constrained_edges.get(minKey).contains(maxKey));
			else {
				if(!edge_map.containsKey(minKey))
					edge_map.put(minKey, new HashMap<Integer,Set<Node>>());
				if(!edge_map.get(minKey).containsKey(maxKey))
					edge_map.get(minKey).put(maxKey, new HashSet<Node>());
				edge_map.get(minKey).get(maxKey).add(node);
			}
		}

		for(int key1:edge_map.keySet()) {
			for(int key2:edge_map.get(key1).keySet()) {
				Set<Node> s = edge_map.get(key1).get(key2);

				if(s.size() == 2) {
					Node n1 = null;
					Node n2 = null;
					for(Node n:s) {
						if(n1==null)
							n1=n;
						else if(n2==null)
							n2=n;
					}
					n1.addNeighbor(n2);
					n2.addNeighbor(n1);

				}
			}
		}
		
		return search_space;
	}
	
	public static Set<Node> getTriangulationGraph(Rectangle boundary,ArrayList<Obstacle> obstacle_list) {
		double obstacle_boundaries[] = new double[obstacle_list.size()*9 + 9 + 1];
		//9 doubles for each obstacle (4 points and END)
		//plus the boundary rectangle and the final END 
		
		System.out.println(boundary);
		
		
		Map<Integer,Set<Integer>>constrained_edges = new HashMap<Integer,Set<Integer>>();
		
		int boundary_width = boundary.width;
		int boundary_height = boundary.height;
		int min_x = boundary.x;
		int max_x = boundary.x + boundary_width;
		int min_y = boundary.y;
		int max_y = boundary.y + boundary_height;
		
		obstacle_boundaries[0] = min_x;
		obstacle_boundaries[1] = min_y;
		obstacle_boundaries[2] = max_x;
		obstacle_boundaries[3] = min_y;
		obstacle_boundaries[4] = max_x;
		obstacle_boundaries[5] = max_y;
		obstacle_boundaries[6] = min_x;
		obstacle_boundaries[7] = max_y;
		
		obstacle_boundaries[8] = END;
		
		int key_mult = Math.max(boundary.width,boundary.height);
		
		for(int i = 0; i < obstacle_list.size(); i++) {
			Obstacle o = obstacle_list.get(i);
			
			int o_x = o.x;
			int o_y = o.y;
			int o_width = o.width;
			int o_height = o.height;
			
			//removeList.add(new Rectangle(inner_x,inner_y,inner_width,inner_height));
			
			obstacle_boundaries[(i) * 9 + 9 + 0] = o_x;
			obstacle_boundaries[(i) * 9 + 9 + 1] = o_y;
			obstacle_boundaries[(i) * 9 + 9 + 2] = o_x + o_width;
			obstacle_boundaries[(i) * 9 + 9 + 3] = o_y;
			obstacle_boundaries[(i) * 9 + 9 + 4] = o_x + o_width;
			obstacle_boundaries[(i) * 9 + 9 + 5] = o_y + o_height;
			obstacle_boundaries[(i) * 9 + 9 + 6] = o_x;
			obstacle_boundaries[(i) * 9 + 9 + 7] = o_y + o_height;
			
			obstacle_boundaries[(i) * 9 + 9 + 8] = END;
		}
		
		obstacle_boundaries[obstacle_boundaries.length - 1] = END;
		
		double[] edge_list = triangulation_library.getTriangulation(obstacle_boundaries);
		
		int num_edges = edge_list.length;
		int start = 0;
		for(int i = 0; i < num_edges; i+=4) {
			if(edge_list[i] == END) {
				start = i + 1;
				break;
			}
			
			int x1 = (int)Math.round(edge_list[i + 0]);
			int y1 = (int)Math.round(edge_list[i + 1]);
			int x2 = (int)Math.round(edge_list[i + 2]);
			int y2 = (int)Math.round(edge_list[i + 3]);
			
			int p1Key = x1*key_mult + y1;
			int p2Key = x2*key_mult + y2;
			
			int minKey = Math.min(p1Key, p2Key);
			int maxKey = Math.max(p1Key, p2Key);
			
			if(!constrained_edges.containsKey(minKey))
				constrained_edges.put(minKey,new HashSet<Integer>());
			constrained_edges.get(minKey).add(maxKey);
		}
		
		Set<Node>search_space = new HashSet<Node>();
		Map<Integer,Set<Integer>>search_space_map = new HashMap<Integer,Set<Integer>>();
		Map<Integer,Node>point_to_node = new HashMap<Integer,Node>();
		

		for(int i = start; i < num_edges; i+=6) {
			int x1 = (int)Math.round(edge_list[i + 0]);
			int y1 = (int)Math.round(edge_list[i + 1]);
			int x2 = (int)Math.round(edge_list[i + 2]);
			int y2 = (int)Math.round(edge_list[i + 3]);
			int x3 = (int)Math.round(edge_list[i + 4]);
			int y3 = (int)Math.round(edge_list[i + 5]);
			
			Point[]point_list = new Point[3];
			
			point_list[0] = new Point(x1,y1);
			point_list[1] = new Point(x2,y2);
			point_list[2] = new Point(x3,y3);
			
			if(x1<min_x || x1>max_x || y1<min_y || y1>max_y)
				continue;
			if(x2<min_x || x2>max_x || y2<min_y || y2>max_y)
				continue;
			if(x3<min_x || x3>max_x || y3<min_y || y3>max_y)
				continue;
			
			int[] keys = new int[3];
			keys[0] = x1*key_mult + y1;
			keys[1] = x2*key_mult + y2;
			keys[2] = x3*key_mult + y3;
			
			Node[]nodes = new Node[3];
			
			for(int j = 0; j < point_list.length; j++) {
				Node node;
				if(!search_space_map.containsKey(keys[j])) {
					node = new GraphNode(point_list[j]);
					point_to_node.put(keys[j],node);
					search_space_map.put(keys[j], new HashSet<Integer>());
					search_space.add(node);
				}
				node = point_to_node.get(keys[j]);
				
				nodes[j] = node;
			}
			
			if(!search_space_map.get(keys[0]).contains(keys[1])) {
				nodes[0].addNeighbor(nodes[1]);
				search_space_map.get(keys[0]).add(keys[1]);
			}
			
			if(!search_space_map.get(keys[0]).contains(keys[2])) {
				nodes[0].addNeighbor(nodes[2]);
				search_space_map.get(keys[0]).add(keys[2]);
			}
			
			if(!search_space_map.get(keys[1]).contains(keys[2])) {
				nodes[1].addNeighbor(nodes[2]);
				search_space_map.get(keys[1]).add(keys[2]);
			}
			
			if(!search_space_map.get(keys[1]).contains(keys[0])) {
				nodes[1].addNeighbor(nodes[0]);
				search_space_map.get(keys[1]).add(keys[0]);
			}
			
			if(!search_space_map.get(keys[2]).contains(keys[1])) {
				nodes[2].addNeighbor(nodes[1]);
				search_space_map.get(keys[2]).add(keys[1]);
			}
			
			if(!search_space_map.get(keys[2]).contains(keys[0])) {
				nodes[2].addNeighbor(nodes[0]);
				search_space_map.get(keys[2]).add(keys[0]);
			}
		}
		
		return search_space;
	}
	
	public static ArrayList<Point> AStarEuclideanCost(SearchSpaceManager manager, 
			Point start_point,Point goal_point, Node start, Node goal, boolean cluster) {
		
		SearchNodeQueue open_set = new SearchNodeQueue();
		ArrayList<Node> closed_set = new ArrayList<Node>();
		
		HashMap<Node, Node> came_from = new HashMap<Node, Node>();
		HashMap<Node, Double> g_value = new HashMap<Node, Double>();
		
		double start_f_value = start_point.distance(goal_point);
		
		
		g_value.put(start, 0.0);
		
		open_set.add(start, start_f_value);

		while(open_set.size() > 0) {
			Node current = open_set.poll().node;
			
			
			if(current == goal) {
				ArrayList<Point> subgoal_list = reconstructPath(came_from, current);
				subgoal_list.add(goal_point);
				return subgoal_list;
			}
			
			closed_set.add(current);
			Set<Node> neighbors = manager.getNeighborsForNode(current, cluster);
			for(Node neighbor:neighbors) {
				if(closed_set.contains(neighbor))
					continue;
				
				double cost = neighbor.getPoints()[0].distance(current.getPoints()[0]);
				
				double tentative_g_value = g_value.get(current) + cost;
				
				if(!open_set.contains(neighbor) || !g_value.containsKey(neighbor) 
						|| tentative_g_value < g_value.get(neighbor)) {
					double dist = neighbor.getPoints()[0].distance(goal_point); 
					double tentative_f_value = tentative_g_value + dist; 
							//manhattan_distance(neighbor, goal);
					
					g_value.put(neighbor, tentative_g_value);
					came_from.put(neighbor, current);
					
					if(!open_set.contains(neighbor)) {
						open_set.add(neighbor, tentative_f_value);
					}
				}
					
			}
		}
		
		
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static Object[] AStarForFunnel(SearchSpaceManager manager, Map<Node,Set<Node>>point_to_triangle,
			Point start_point,Point goal_point, Node start, Node goal, boolean cluster) {
		
		SearchNodeQueue open_set = new SearchNodeQueue();
		ArrayList<Node> closed_set = new ArrayList<Node>();
		
		HashMap<Node, Node> came_from = new HashMap<Node, Node>();
		HashMap<Node, Double> g_value = new HashMap<Node, Double>();
		
		double start_f_value = start_point.distance(goal_point);
		
		
		g_value.put(start, 0.0);
		
		open_set.add(start, start_f_value);

		while(open_set.size() > 0) {
			Node current = open_set.poll().node;
			
			
			if(current == goal) {
				Object[] list = reconstructPathForFunnel(came_from, current,point_to_triangle);
				((ArrayList<Point>)list[0]).add(goal_point);
				return list;
			}
			
			closed_set.add(current);
			Set<Node> neighbors = manager.getNeighborsForNode(current, cluster);
			for(Node neighbor:neighbors) {
				if(closed_set.contains(neighbor))
					continue;
				
				double cost = neighbor.getPoints()[0].distance(current.getPoints()[0]);
				
				double tentative_g_value = g_value.get(current) + cost;
				
				if(!open_set.contains(neighbor) || !g_value.containsKey(neighbor) 
						|| tentative_g_value < g_value.get(neighbor)) {
					double dist = neighbor.getPoints()[0].distance(goal_point); 
					double tentative_f_value = tentative_g_value + dist; 
							//manhattan_distance(neighbor, goal);
					
					g_value.put(neighbor, tentative_g_value);
					came_from.put(neighbor, current);
					
					if(!open_set.contains(neighbor)) {
						open_set.add(neighbor, tentative_f_value);
					}
				}
					
			}
		}
		
		
		
		return null;
	}
	
	public static ArrayList<Point> AStar(SearchSpaceManager manager, 
			Map<Node, Map<Node,Double>> cost_function,
			Point start_point,Point goal_point, Node start, Node goal, boolean cluster) {
		
		SearchNodeQueue open_set = new SearchNodeQueue();
		ArrayList<Node> closed_set = new ArrayList<Node>();
		
		HashMap<Node, Node> came_from = new HashMap<Node, Node>();
		HashMap<Node, Double> g_value = new HashMap<Node, Double>();
		
		double start_f_value = start_point.distance(goal_point);
		
		
		g_value.put(start, 0.0);
		
		open_set.add(start, start_f_value);

		while(open_set.size() > 0) {
			Node current = open_set.poll().node;
			
			
			if(current == goal) {
				ArrayList<Point> subgoal_list = reconstructPath(came_from, current);
				subgoal_list.add(goal_point);
				return subgoal_list;
			}
			
			closed_set.add(current);
			Set<Node> neighbors = manager.getNeighborsForNode(current, cluster);
			for(Node neighbor:neighbors) {
				if(closed_set.contains(neighbor))
					continue;
				
				double cost;
				if(cost_function == null)
					cost = neighbor.getPoints()[0].distance(current.getPoints()[0]);
				else
					cost = cost_function.get(current).get(neighbor);
				
				double tentative_g_value = g_value.get(current) + cost;
				
				if(!open_set.contains(neighbor) || !g_value.containsKey(neighbor) 
						|| tentative_g_value < g_value.get(neighbor)) {
					double dist = neighbor.getPoints()[0].distance(goal_point); 
					double tentative_f_value = tentative_g_value + dist; 
							//manhattan_distance(neighbor, goal);
					
					g_value.put(neighbor, tentative_g_value);
					came_from.put(neighbor, current);
					
					if(!open_set.contains(neighbor)) {
						open_set.add(neighbor, tentative_f_value);
					}
				}
					
			}
		}
		
		
		
		return null;
	}

	public static ArrayList<Node> TAStarWithEdgeCache(SearchSpaceManager manager, 
			Point start_point,Point goal_point, Node start, Node goal,
			THPAStarPointAgentEdgeCache cache, boolean cluster) {
		SearchNodeQueue open_set = new SearchNodeQueue();
		ArrayList<Node> closed_set = new ArrayList<Node>();
		
		HashMap<Node, Node> came_from = new HashMap<Node, Node>();
		HashMap<Node, Double> g_value = new HashMap<Node, Double>();
		
		double start_f_value = start_point.distance(goal_point);
		
		g_value.put(start, 0.0);
		
		open_set.add(start, start_f_value);

		while(open_set.size() > 0) {
			AStarNode current_astarnode = open_set.poll();
			Node current = current_astarnode.node;
			double current_f_value = current_astarnode.f_value;
			double current_g_value = g_value.get(current);
			double current_h_value = current_f_value - current_g_value;
			
			//boolean goal_reached = pointInTriangle(goal_point,current.getPoints()[0],current.getPoints()[1],current.getPoints()[2]);
			
			//goal_reached = pointInTriangle(goal_point,current.getPoints()[0],current.getPoints()[1],current.getPoints()[2]);
		
			if(goal == current) {
				ArrayList<Node> subgoal_list = reconstructPathTAStar(came_from, current);
				return subgoal_list;
			}
			
			closed_set.add(current);
			Set<Node> neighbors = manager.getNeighborsForNode(current, cluster);
			
			for(Node neighbor:neighbors) {
				if(closed_set.contains(neighbor))
					continue;
				Node loop = current;
				boolean break_a = false;
				while(loop != null) {
					if(neighbor == loop)
						break_a = true;
					loop = came_from.get(loop);
				}
				if(break_a)
					continue;
				
				Edge entrance = cache.edgeForNodes(current, neighbor);
				
				if(entrance == null) {
					continue;
				}
				
				Double tentative_g_value = cache.getDistance(current, neighbor, null);
				double tentative_h_value = closestPointBetweenPointAndSegment(goal_point,entrance);
				
				if(tentative_g_value.equals(Double.NaN)) {
					Point[]p = sharedPointsForNodes(current,neighbor);
					if(p.length != 2)
						continue;
					
					double dist_bound = closestPointBetweenPointAndSegment(start_point,entrance);
					
					double h_diff_bound = current_h_value - tentative_h_value;
					
					double cost = Math.max(dist_bound, h_diff_bound);
					
					tentative_g_value = current_g_value + cost;
				}
				
				if(!open_set.contains(neighbor) || !g_value.containsKey(neighbor) 
						|| tentative_g_value < g_value.get(neighbor)) { 
					double tentative_f_value = tentative_g_value + tentative_h_value; 
					
					g_value.put(neighbor, tentative_g_value);
					came_from.put(neighbor, current);
					
					if(!open_set.contains(neighbor))
						open_set.add(neighbor, tentative_f_value);
				}
					
			}
		}
		for(Node n: closed_set) {
			Point[] p = n.getPoints();
			System.out.println(p[0].x + ", " + p[0].y + " " + p[1].x + ", " + p[1].y + " " + p[2].x + ", " + p[2].y);
		}
		System.out.println();
		
		return null;
	}
	
	public static ArrayList<Node> TAStar(SearchSpaceManager manager, 
			Point start_point,Point goal_point, Node start, Node goal, boolean cluster) {
		SearchNodeQueue open_set = new SearchNodeQueue();
		ArrayList<Node> closed_set = new ArrayList<Node>();
		
		HashMap<Node, Node> came_from = new HashMap<Node, Node>();
		HashMap<Node, Double> g_value = new HashMap<Node, Double>();
		
		double start_f_value = start_point.distance(goal_point);
		
		g_value.put(start, 0.0);
		
		open_set.add(start, start_f_value);

		while(open_set.size() > 0) {
			AStarNode current_astarnode = open_set.poll();
			Node current = current_astarnode.node;
			double current_f_value = current_astarnode.f_value;
			double current_g_value = g_value.get(current);
			double current_h_value = current_f_value - current_g_value;
			
			//boolean goal_reached = pointInTriangle(goal_point,current.getPoints()[0],current.getPoints()[1],current.getPoints()[2]);
			
			//goal_reached = pointInTriangle(goal_point,current.getPoints()[0],current.getPoints()[1],current.getPoints()[2]);
		
			if(current == goal) {
				ArrayList<Node> subgoal_list = reconstructPathTAStar(came_from, current);
				return subgoal_list;
			}
			
			closed_set.add(current);
			Set<Node> neighbors = manager.getNeighborsForNode(current, cluster);
			for(Node neighbor:neighbors) {
				if(closed_set.contains(neighbor))
					continue;
				Node loop = current;
				boolean break_a = false;
				while(loop != null) {
					if(neighbor == loop)
						break_a = true;
					loop = came_from.get(loop);
				}
				if(break_a)
					continue;
					
				Point[] point_list = sharedPointsForNodes(neighbor, current);
				Edge entrance = new Edge(point_list[0], point_list[1]);

				double dist_bound = closestPointBetweenPointAndSegment(start_point,entrance);
				
				double tentative_h_value = closestPointBetweenPointAndSegment(goal_point,entrance);
				
				double h_diff_bound = current_h_value - tentative_h_value;
				
				double cost = Math.max(dist_bound, h_diff_bound);
				
				double tentative_g_value = current_g_value + cost;
				
				if(!open_set.contains(neighbor) || !g_value.containsKey(neighbor) 
						|| tentative_g_value < g_value.get(neighbor)) { 
					double tentative_f_value = tentative_g_value + tentative_h_value; 
					
					g_value.put(neighbor, tentative_g_value);
					came_from.put(neighbor, current);
					
					if(!open_set.contains(neighbor))
						open_set.add(neighbor, tentative_f_value);
				}
					
			}
		}
		
		return null;
	}
	
	public static Object[] TAStarFValue(SearchSpaceManager manager, 
			Edge entrance_edge, Point start_point, Node start, Node goal, boolean cluster) {
		SearchNodeQueue open_set = new SearchNodeQueue();
		ArrayList<Node> closed_set = new ArrayList<Node>();
		
		HashMap<Node, Node> came_from = new HashMap<Node, Node>();
		HashMap<Node, Double> g_value = new HashMap<Node, Double>();
		
		if(entrance_edge == null && start_point != null)
			entrance_edge = new Edge(start_point,start_point);
		else {
			entrance_edge = new Edge(start.getPoints()[0],start.getPoints()[1]);
		}
		
		//assert entrance_edge != null;
			
		double start_f_value = closestPointBetweenEdgeAndTriangle(entrance_edge,goal.getPoints());
		
		g_value.put(start, 0.0);
		
		open_set.add(start, start_f_value);

		Edge entrance = null;
		while(open_set.size() > 0) {
			AStarNode current_astarnode = open_set.poll();
			Node current = current_astarnode.node;
			double current_f_value = current_astarnode.f_value;
			double current_g_value = g_value.get(current);
			double current_h_value = current_f_value - current_g_value;
			
			
			if(current == goal) {
				Object[] array = new Object[2];
				array[0] = new Double(current_f_value);
				array[1] = entrance;
				return array;
			}
			
			closed_set.add(current);
			Set<Node> neighbors = manager.getNeighborsForNode(current, cluster);
			for(Node neighbor:neighbors) {
				if(closed_set.contains(neighbor))
					continue;
				Node loop = current;
				boolean break_a = false;
				while(loop != null) {
					if(neighbor == loop)
						break_a = true;
					loop = came_from.get(loop);
				}
				if(break_a)
					continue;
					
				Point[] point_list = sharedPointsForNodes(neighbor, current);
				entrance = new Edge(point_list[0], point_list[1]);

				double dist_bound = closestPointBetweenSegmentAndSegment(entrance_edge,entrance);
				
				double tentative_h_value = closestPointBetweenEdgeAndTriangle(entrance,goal.getPoints());
				
				double h_diff_bound = current_h_value - tentative_h_value;
				
				double cost = Math.max(dist_bound, h_diff_bound);
				
				double tentative_g_value = current_g_value + cost;
				
				if(!open_set.contains(neighbor) || !g_value.containsKey(neighbor) 
						|| tentative_g_value < g_value.get(neighbor)) { 
					double tentative_f_value = tentative_g_value + tentative_h_value; 
					
					g_value.put(neighbor, tentative_g_value);
					came_from.put(neighbor, current);
					
					if(!open_set.contains(neighbor))
						open_set.add(neighbor, tentative_f_value);
				}
					
			}
		}
		
		return null;
	}
	
	public static ArrayList<Point> reconstructPath(
			HashMap<Node,Node> came_from,Node current_node) {
		ArrayList<Point> subgoal_list = new ArrayList<Point>();
		
		
		while(came_from.containsKey(current_node)) {
			Node temp = came_from.get(current_node); 
			subgoal_list.add(0, temp.getPoints()[0]);
			came_from.remove(current_node);
			current_node = temp;
		}
		if(subgoal_list.size() > 0)
			subgoal_list.remove(0);
		return subgoal_list;
	}
	
	public static ArrayList<Node> findTrianglePath(Node n1, Node n2, Node n3, Map<Node,Set<Node>>point_to_triangle) {
		ArrayList<Node>triangle_path = new ArrayList<Node>();
		
		Set<Node>shared_triangles = new HashSet<Node>();
		
		Point p1 = n1.getPoints()[0];
		Point p2 = n2.getPoints()[0];
		Point p3 = n3.getPoints()[0];
		
		for(Node node:point_to_triangle.get(n1)) {
			if(triangleContainsPoints(node,p1,p2))
				shared_triangles.add(node);
		}

		for(Node start_node:shared_triangles) {
			triangle_path.clear();
			
			Node node = start_node;
			
			Point other = p1;
			boolean b;
			while(!(b=triangleContainsPoints(node,p2,p3))) {
				boolean done = true;
				
				for(Node neighbor:node.getNeighbors()) {
					if(triangleContainsPoints(neighbor,other,p2)) {
						Point[] points = neighbor.getPoints();
						for(int i = 0; i < 3; i++) {
							if(!points[i].equals(other) && !points[i].equals(p2)) {
								other = points[i];
								break;
							}
						}
						triangle_path.add(neighbor);
						node = neighbor;
						done = false;
						break;
					}
				}
				
				if(done)
					break;
			}
			triangle_path.add(node);
			if(!b)
				return triangle_path;
		}
		
		return null;
	}
	
	private static boolean triangleContainsPoints(Node n, Point p1, Point p2) {
		boolean b1 = false;
		boolean b2 = false;
		
		Point[] points = n.getPoints();
		
		for(int i = 0; i < 3; i++) {
			if(p1.equals(points[0]))
				b1 = true;
			if(p2.equals(points[0]))
				b2 = true;
		}
		
		return b1 && b2;
	}
	
	public static Object[] reconstructPathForFunnel(
			HashMap<Node,Node> came_from,Node current,Map<Node,Set<Node>>point_to_triangle) {
		ArrayList<Point> subgoal_list = new ArrayList<Point>();
		ArrayList<Node> triangles_list = new ArrayList<Node>();
		
		Node edge_first = current;
		Node edge_second = null;
		
		while(came_from.containsKey(current)) {
			Node temp = came_from.get(current); 
			subgoal_list.add(0, temp.getPoints()[0]);
			came_from.remove(current);
			current = temp;
			
			if(edge_second == null)
				edge_second = current;
			else {
				ArrayList<Node> triangles_between_edges = findTrianglePath(edge_first, edge_second, current,point_to_triangle);
				for(int i = 0; i < triangles_between_edges.size(); i++) {
					triangles_list.add(triangles_between_edges.get(i));
				}
			}
		}
		
		if(subgoal_list.size() > 0)
			subgoal_list.remove(0);
		
		
		
		Object[] list = {subgoal_list,triangles_list};
		return list;
	}
	
	public static ArrayList<Node> reconstructPathTAStar(
			HashMap<Node,Node> came_from,Node current_node) {
		ArrayList<Node> subgoal_list = new ArrayList<Node>();
		
		subgoal_list.add(0, current_node);
		while(came_from.containsKey(current_node)) {
			Node temp = came_from.get(current_node); 
			subgoal_list.add(0, temp);
			came_from.remove(current_node);
			current_node = temp;
		}
		return subgoal_list;
	}
	
	public static double manhattan_distance(Node start, Node goal) {
		Point start_point = start.getPoints()[0];
		Point goal_point = goal.getPoints()[0];
		return Math.sqrt(Math.pow(goal_point.x - start_point.x,2) + Math.pow(goal_point.x - start_point.x,2));
	}
	
	public static double closestPointBetweenPointAndSegment(Point p, Edge e) {
		Point edge_1 = e.getFromPoint();
		Point edge_2 = e.getToPoint();
		
		double px = edge_2.x-edge_1.x;
		double py = edge_2.y-edge_1.y;
		
		double something = px*px + py*py;
		double u = ((p.x - edge_1.x) * px + (p.y - edge_1.y) * py) / something;
		
		if(u > 1)
			u = 1;
		else if(u < 0)
			u = 0;
		
		double x = edge_1.x + u * px;
		double y = edge_1.y + u * py;
		
		double dx = x - p.x;
		double dy = y - p.y;
			    
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public static double closestPointBetweenSegmentAndSegment(Edge e1, Edge e2) {
		Point e1_p1 = e1.getFromPoint();
		Point e1_p2 = e1.getToPoint();
		Point e2_p1 = e2.getFromPoint();
		Point e2_p2 = e2.getToPoint();
		
		double e1_p1_val = closestPointBetweenPointAndSegment(e1_p1,e2);
		double e1_p2_val = closestPointBetweenPointAndSegment(e1_p2,e2);
		double e2_p1_val = closestPointBetweenPointAndSegment(e2_p1,e1);
		double e2_p2_val = closestPointBetweenPointAndSegment(e2_p2,e1);
		
		return Math.min(Math.min(e2_p1_val, e2_p2_val), Math.min(e1_p1_val, e1_p2_val));
	}
	
	public static double closestPointBetweenPointAndTriangle(Point p,Point[]triangle) {
		Edge e = new Edge(triangle[0],triangle[1]);
		double val = closestPointBetweenPointAndSegment(p,e);
		
		e = new Edge(triangle[0],triangle[2]);
		val = Math.min(val, closestPointBetweenPointAndSegment(p,e));
		
		e = new Edge(triangle[1],triangle[2]);
		val = Math.min(val, closestPointBetweenPointAndSegment(p,e));
		
		return val;
	}
	
	public static double closestPointBetweenEdgeAndTriangle(Edge e, Point[]triangle) {
		Point e1_p1 = e.getFromPoint();
		Point e1_p2 = e.getToPoint();
		Point e2_p1 = triangle[0];
		Point e2_p2 = triangle[1];
		Point e2_p3 = triangle[2];
		
		double e_p1_val = closestPointBetweenPointAndSegment(e2_p1,e);
		double e_p2_val = closestPointBetweenPointAndSegment(e2_p2,e);
		double e_p3_val = closestPointBetweenPointAndSegment(e2_p3,e);
		double e1_p1_val = closestPointBetweenPointAndTriangle(e1_p1,triangle);
		double e2_p1_val = closestPointBetweenPointAndTriangle(e1_p1,triangle);
		double e3_p1_val = closestPointBetweenPointAndTriangle(e1_p1,triangle);

		
		double min = Math.min(e_p1_val, e_p2_val);
		min = Math.min(min, e_p3_val);
		min = Math.min(min, e1_p1_val);
		min = Math.min(min, e2_p1_val);
		min = Math.min(min, e3_p1_val);
		
		return min;
	}
	
	public static boolean pointInTriangle(Point p, Point p1, Point p2, Point p3) {
		double epsilon = -.0001;
		
		double alpha = (double)((p2.y - p3.y)*(p.x - p3.x) + (p3.x - p2.x)*(p.y - p3.y)) /
		        (double)((p2.y - p3.y)*(p1.x - p3.x) + (p3.x - p2.x)*(p1.y - p3.y));
		double beta = (double)((p3.y - p1.y)*(p.x - p3.x) + (p1.x - p3.x)*(p.y - p3.y)) /
		       (double)((p2.y - p3.y)*(p1.x - p3.x) + (p3.x - p2.x)*(p1.y - p3.y));
		double gamma = 1.0 - alpha - beta;
		
		return alpha >= epsilon && beta >= epsilon && gamma >= epsilon;
	}
	
	public static Point[] sharedPointsForNodes(Node n1,Node n2) {
		ArrayList<Point>sharedPoints = new ArrayList<Point>();
		
		Point[] n1_point_list = n1.getPoints();
		Point[] n2_point_list = n2.getPoints();
		for(int i = 0; i<n1_point_list.length;i++)
			for(int j = 0; j<n2_point_list.length;j++)
				if(n1_point_list[i].x==n2_point_list[j].x && n1_point_list[i].y==n2_point_list[j].y)
					sharedPoints.add(new Point(n1_point_list[i].x,n1_point_list[i].y));
		Point[]array = new Point[sharedPoints.size()];
		array = sharedPoints.toArray(array);
		return array;
	}
}

class SearchNodeQueue{
	ArrayList<Node>list = new ArrayList<Node>();
	PriorityQueue<AStarNode> queue = new PriorityQueue<AStarNode>();
	
	public void add(Node node, double f_value) {
		if(list.contains(node))
			return;
		list.add(node);
		AStarNode astar_node = new AStarNode(node,f_value);
		queue.add(astar_node);
	}
	
	public AStarNode poll() {
		AStarNode node = queue.poll();
		
		list.remove(node.node);
		
		return node;
	}
	public boolean contains(Node node) {
		return list.contains(node);
	}
	public int size() {
		return queue.size();
	}
}

class RectilinearEdge implements Comparable<RectilinearEdge> {
	int x;
	int y;
	boolean isHorizontal;
	int length;
	int keyMult;
	public RectilinearEdge(int x, int y, boolean isHorizontal, int length, int keyMult) {
		this.x = x;
		this.y = y;
		this.length = length;
		this.isHorizontal = isHorizontal;
		this.keyMult = keyMult;
	}
	@Override
	public int compareTo(RectilinearEdge o) {
		// TODO Auto-generated method stub
		if(isHorizontal)
			return (x*keyMult + y) - (o.keyMult*o.x + o.y);
		return (y*keyMult + x) - (o.keyMult*o.y + o.x);
	}

}

class AStarNode implements Comparable<AStarNode>{
	Node node;
	double f_value;
	public AStarNode(Node node, double f_value) {
		this.node = node;
		this.f_value = f_value;
	}
	
	public int compareTo(AStarNode o) {
		return Double.compare(f_value, o.f_value);
	}
}
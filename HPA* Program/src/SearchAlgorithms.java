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
	
	static double Example1[] =
	       { 0, 0, 20, 0, 20, 20, 0, 20, END,
	           1, 1, 7, 3, 3, 8, END,
	         END };
	
	static SearchAlgorithms triangulation_library = new SearchAlgorithms();
	
	private native double[] getTriangulation(double[] f);
	
	public static Set<SearchSpaceNode> getTriangulation(Rectangle boundary,ArrayList<Obstacle> obstacle_list) {
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
		
		Map<Integer,SearchSpaceNode> triangulations = new HashMap<Integer,SearchSpaceNode>();
		int border_top_left_key = key_mult*min_x + min_y;
		int border_top_right_key = key_mult*max_x + min_y;
		int border_bottom_right_key = key_mult*max_x + max_y;
		int border_bottom_left_key = key_mult*min_x + max_y;
		triangulations.put(border_top_left_key,new SearchSpaceNode(min_x/FACTOR,min_y/FACTOR));
		triangulations.put(border_top_right_key,new SearchSpaceNode(max_x/FACTOR,min_y/FACTOR));
		triangulations.put(border_bottom_right_key,new SearchSpaceNode(max_x/FACTOR,max_y/FACTOR));
		triangulations.put(border_bottom_left_key,new SearchSpaceNode(min_x/FACTOR,max_y/FACTOR));
		
		ArrayList<Rectangle> removeList = new ArrayList<Rectangle>();
		
		
		//ArrayList<Rectangle> removeList = new ArrayList<Rectangle>();
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
			
			//removeList.add(new Rectangle(inner_x,inner_y,inner_width,inner_height));
			
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
				triangulations.put(top_left_key, new SearchSpaceNode(o_x/FACTOR,o_y/FACTOR));
			if(!triangulations.containsKey(top_right_key))
				triangulations.put(top_right_key, new SearchSpaceNode((o_x+o_width)/FACTOR,o_y/FACTOR));
			if(!triangulations.containsKey(bottom_right_key))
				triangulations.put(bottom_right_key, new SearchSpaceNode((o_x+o_width)/FACTOR,(o_y+o_height)/FACTOR));
			if(!triangulations.containsKey(bottom_left_key))
				triangulations.put(bottom_left_key, new SearchSpaceNode(o_x/FACTOR,(o_y+o_height)/FACTOR));
		}
		
		obstacle_boundaries[obstacle_boundaries.length - 1] = END;
		
		double[] edge_list = triangulation_library.getTriangulation(obstacle_boundaries);
		
		
		
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
			
			SearchSpaceNode n1 = triangulations.get(key1);
			SearchSpaceNode n2 = triangulations.get(key2);
			/*if(x1 == min_x && x2 == min_x && Math.abs(y2 - y1) != boundary_height) {
				left_edge = false;
				continue;
			}
			if(x1 == max_x && x2 == max_x && Math.abs(y2 - y1) != boundary_height) {
				right_edge = false;
				continue;
			}
			if(y1 == min_y && y2 == min_y && Math.abs(x2 - x1) != boundary_width) {
				top_edge = false;
				continue;
			}
			if(y1 == max_y && y2 == max_y && Math.abs(x2 - x1) != boundary_width) {
				bottom_edge = false;
				continue;
			}*/
			n1.addNeighbor(n2);
			n2.addNeighbor(n1);
		}
		
		/*if(left_edge) {
			int key1 = key_mult * min_x + min_y;
			int key2 = key_mult * min_x + max_y;
			SearchSpaceNode n1 = triangulations.get(key1);
			SearchSpaceNode n2 = triangulations.get(key2);
			n1.addNeighbor(n2);
			n2.addNeighbor(n1);
		}
		if(right_edge) {
			int key1 = key_mult * max_x + min_y;
			int key2 = key_mult * max_x + max_y;
			SearchSpaceNode n1 = triangulations.get(key1);
			SearchSpaceNode n2 = triangulations.get(key2);
			n1.addNeighbor(n2);
			n2.addNeighbor(n1);
		}
		if(top_edge) {
			int key1 = key_mult * min_x + min_y;
			int key2 = key_mult * max_x + min_y;
			SearchSpaceNode n1 = triangulations.get(key1);
			SearchSpaceNode n2 = triangulations.get(key2);
			n1.addNeighbor(n2);
			n2.addNeighbor(n1);
		}
		if(bottom_edge) {
			int key1 = key_mult * min_x + max_y;
			int key2 = key_mult * max_x + max_y;
			SearchSpaceNode n1 = triangulations.get(key1);
			SearchSpaceNode n2 = triangulations.get(key2);
			n1.addNeighbor(n2);
			n2.addNeighbor(n1);
		}*/
			
		
		/*for(Rectangle r:removeList) {
			int top_left_key = key_mult*r.x + r.y;
			int top_right_key = key_mult*(r.x+r.width) + r.y;
			int bottom_right_key = key_mult*(r.x+r.width) + r.y + r.height;
			int bottom_left_key = key_mult*r.x + r.y+r.height;
			
			SearchSpaceNode top_left = triangulations.get(top_left_key);
			SearchSpaceNode top_right = triangulations.get(top_right_key);
			SearchSpaceNode bottom_right = triangulations.get(bottom_right_key);
			SearchSpaceNode bottom_left = triangulations.get(bottom_left_key);
			
			top_left.removeSelfFromGraph();
			top_right.removeSelfFromGraph();
			bottom_right.removeSelfFromGraph();
			bottom_left.removeSelfFromGraph();
			
			triangulations.remove(top_left_key);
			triangulations.remove(top_right_key);
			triangulations.remove(bottom_left_key);
			triangulations.remove(bottom_right_key);
		}*/
		
		Set<SearchSpaceNode>set = new HashSet<SearchSpaceNode>();
		for(int key:triangulations.keySet())
			set.add(triangulations.get(key));
		
		return set;
	}
	
	public static ArrayList<Point> AStar(SearchSpaceManager manager, 
			Map<SearchSpaceNode, Map<SearchSpaceNode,Double>> cost_function,
			Point start_point,Point goal_point, SearchSpaceNode start, SearchSpaceNode goal, boolean cluster) {
		
		SearchNodeQueue open_set = new SearchNodeQueue();
		ArrayList<SearchSpaceNode> closed_set = new ArrayList<SearchSpaceNode>();
		
		HashMap<SearchSpaceNode, SearchSpaceNode> came_from = new HashMap<SearchSpaceNode, SearchSpaceNode>();
		HashMap<SearchSpaceNode, Double> g_value = new HashMap<SearchSpaceNode, Double>();
		
		double start_f_value = start_point.distance(goal_point);
		
		
		g_value.put(start, 0.0);
		
		open_set.add(start, start_f_value);

		while(open_set.size() > 0) {
			SearchSpaceNode current = open_set.poll();
			
			
			if(current == goal) {
				ArrayList<Point> subgoal_list = reconstructPath(came_from, current);
				subgoal_list.add(goal_point);
				return subgoal_list;
			}
			
			closed_set.add(current);
			Set<SearchSpaceNode> neighbors = manager.getNeighborsForNode(current, cluster);
			for(SearchSpaceNode neighbor:neighbors) {
				if(closed_set.contains(neighbor))
					continue;
				
				double cost = neighbor.point_list[0].distance(current.point_list[0]);
				
				double tentative_g_value = g_value.get(current) + cost;
				
				if(!open_set.contains(neighbor) || !g_value.containsKey(neighbor) 
						|| tentative_g_value < g_value.get(neighbor)) {
					double dist = neighbor.point_list[0].distance(goal_point); 
					double tentative_f_value = tentative_g_value + dist; 
							//manhattan_distance(neighbor, goal);
					
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
			HashMap<SearchSpaceNode,SearchSpaceNode> came_from,SearchSpaceNode current_node) {
		ArrayList<Point> subgoal_list = new ArrayList<Point>();
		
		
		while(came_from.containsKey(current_node)) {
			SearchSpaceNode temp = came_from.get(current_node); 
			subgoal_list.add(0, temp.point_list[0]);
			came_from.remove(current_node);
			current_node = temp;
		}
		if(subgoal_list.size() > 0)
			subgoal_list.remove(0);
		return subgoal_list;
	}
	
	public static double manhattan_distance(SearchSpaceNode start, SearchSpaceNode goal) {
		Point start_point = start.point_list[0];
		Point goal_point = goal.point_list[0];
		return Math.sqrt(Math.pow(goal_point.x - start_point.x,2) + Math.pow(goal_point.x - start_point.x,2));
	}
}

class SearchNodeQueue{
	ArrayList<SearchSpaceNode>list = new ArrayList<SearchSpaceNode>();
	PriorityQueue<AStarNode> queue = new PriorityQueue<AStarNode>();
	
	public void add(SearchSpaceNode node, double f_value) {
		if(list.contains(node))
			return;
		list.add(node);
		AStarNode astar_node = new AStarNode(node,f_value);
		queue.add(astar_node);
	}
	
	public SearchSpaceNode poll() {
		SearchSpaceNode node = queue.poll().node;
		list.remove(node);
		return node;
	}
	public boolean contains(SearchSpaceNode node) {
		return list.contains(node);
	}
	public int size() {
		return queue.size();
	}
	private class AStarNode implements Comparable<AStarNode>{
		SearchSpaceNode node;
		double f_value;
		public AStarNode(SearchSpaceNode node, double f_value) {
			this.node = node;
			this.f_value = f_value;
		}
		
		public int compareTo(AStarNode o) {
			return Double.compare(f_value, o.f_value);
		}
	}
	private class RectilinearEdge implements Comparable<RectilinearEdge> {
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
}
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
		double obstacle_boundaries[] = new double[obstacle_list.size()*9 + 9 + 1];
		//9 doubles for each obstacle (4 points and END)
		//plus the boundary rectangle and the final END 
		
		System.out.println(boundary);
		
		int min_x = boundary.x;
		int max_x = boundary.x + boundary.width;
		int min_y = boundary.y;
		int max_y = boundary.y + boundary.height;
		
		obstacle_boundaries[0] = min_x;
		obstacle_boundaries[1] = min_y;
		obstacle_boundaries[2] = max_x;
		obstacle_boundaries[3] = min_y;
		obstacle_boundaries[4] = max_x;
		obstacle_boundaries[5] = max_y;
		obstacle_boundaries[6] = min_x;
		obstacle_boundaries[7] = max_y;
		
		obstacle_boundaries[8] = END;
		
		for(int i = 0; i < obstacle_list.size(); i++) {
			Obstacle o = obstacle_list.get(i);
			obstacle_boundaries[(i+1) * 9 + 0] = o.x;
			obstacle_boundaries[(i+1) * 9 + 1] = o.y;
			obstacle_boundaries[(i+1) * 9 + 2] = o.x + o.width;
			obstacle_boundaries[(i+1) * 9 + 3] = o.y;
			obstacle_boundaries[(i+1) * 9 + 4] = o.x + o.width;
			obstacle_boundaries[(i+1) * 9 + 5] = o.y + o.height;
			obstacle_boundaries[(i+1) * 9 + 6] = o.x;
			obstacle_boundaries[(i+1) * 9 + 7] = o.y + o.height;
			
			obstacle_boundaries[(i+1) * 9 + 8] = END;
		}
		obstacle_boundaries[obstacle_boundaries.length - 1] = END;
		
		double[] edge_list = triangulation_library.getTriangulation(obstacle_boundaries);
		
		Set <SearchSpaceNode> triangulations = new HashSet<SearchSpaceNode>();
		
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
			Point[] point_list = {new Point(x1,y1), new Point(x2,y2)};
			triangulations.add(new SearchSpaceNode(point_list));
		}
		
		return triangulations;
	}
	
	public static ArrayList<Point> AStar(SearchSpaceManager manager, 
			Map<SearchSpaceNode, Map<SearchSpaceNode,Double>> cost_function,
			Point start_point,Point goal_point, SearchSpaceNode start, SearchSpaceNode goal, boolean cluster) {
		
		//Point start_point = start.point_list[0];
		SearchNodeQueue open_set = new SearchNodeQueue();
		ArrayList<SearchSpaceNode> closed_set = new ArrayList<SearchSpaceNode>();
		
		HashMap<SearchSpaceNode, SearchSpaceNode> came_from = new HashMap<SearchSpaceNode, SearchSpaceNode>();
		HashMap<SearchSpaceNode, Double> g_value = new HashMap<SearchSpaceNode, Double>();
		
		double start_f_value = manhattan_distance(start,goal);
		
		
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
				double cost;
				if(cost_function == null)
					cost = 1.0;
				else {
					
					cost = cost_function.get(current).get(neighbor);
				}
				double tentative_g_value = g_value.get(current) + cost;
				
				if(!open_set.contains(neighbor) || !g_value.containsKey(neighbor) 
						|| tentative_g_value < g_value.get(neighbor)) {
					double tentative_f_value = tentative_g_value + manhattan_distance(neighbor, goal);
					
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
}
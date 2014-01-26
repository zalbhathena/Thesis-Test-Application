import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map;


public class SearchAlgorithms {
	public static ArrayList<Point> AStar(SearchSpaceManager manager, 
			Map<SearchSpaceNode, Map<SearchSpaceNode,Double>> cost_function,
			Point start_point,Point goal_point, SearchSpaceNode start, SearchSpaceNode goal, boolean cluster) {
		
		//Point start_point = start.point_list[0];
		PriorityQueue<SearchSpaceNode> open_set = new PriorityQueue<SearchSpaceNode>();
		ArrayList<SearchSpaceNode> closed_set = new ArrayList<SearchSpaceNode>();
		
		HashMap<SearchSpaceNode, SearchSpaceNode> came_from = new HashMap<SearchSpaceNode, SearchSpaceNode>();
		HashMap<SearchSpaceNode, Double> g_score = new HashMap<SearchSpaceNode, Double>();
		HashMap<SearchSpaceNode, Double> f_score = new HashMap<SearchSpaceNode, Double>();
		
		double start_f_value = manhattan_distance(start,goal);
		
		g_score.put(start, 0.0);
		f_score.put(start, start_f_value);
		
		start.f_value = start_f_value;
		open_set.add(start);

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
				double tentative_g_score = g_score.get(current) + cost;
				
				if(!open_set.contains(neighbor) || !g_score.containsKey(neighbor) 
						|| tentative_g_score < g_score.get(neighbor)) {
					double tentative_f_score = tentative_g_score + manhattan_distance(neighbor, goal);
					
					came_from.put(neighbor, current);
					g_score.put(neighbor, tentative_g_score);
					f_score.put(neighbor, tentative_f_score);
					neighbor.f_value = tentative_f_score;
					if(!open_set.contains(neighbor))
						open_set.add(neighbor);
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

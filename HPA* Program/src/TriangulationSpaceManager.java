import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class TriangulationSpaceManager implements SearchSpaceManager{
	int width, height;
	Set<SearchSpaceNode> search_space = new HashSet<SearchSpaceNode>();
	Set<Polygon> boundary_set;
	Map<SearchSpaceNode,Map<SearchSpaceNode, Double>> cost_function = new HashMap<SearchSpaceNode,Map<SearchSpaceNode, Double>>();
	
	public TriangulationSpaceManager(ArrayList<Obstacle> obstacle_list, int width, int height) {
		
		this.width = width;
		this.height = height;
		
		Rectangle boundary = new Rectangle(0,0,width,height);
		
		search_space = SearchAlgorithms.triangulation_library.getTriangulation(boundary,obstacle_list);
		
		int[]x_list = {0,width,width,0};
		int[]y_list = {0,0,height,height,0};
		Polygon p = new Polygon(x_list,y_list,4);
		boundary_set = new HashSet<Polygon>();
		boundary_set.add(p);
		
		createCostFunction();
	}
	
	private void createCostFunction() {
		for(SearchSpaceNode node1:search_space) {
			for(SearchSpaceNode node2:node1.getNeighbors()) {
				if(node1 == node2)
					continue;
				
				Point p1 = node1.point_list[0];
				Point p2 = node2.point_list[0];
				double distance = Point.distance(p1.x, p1.y, p2.x, p2.y);
				
				if(!cost_function.containsKey(node1))
					cost_function.put(node1,new HashMap<SearchSpaceNode, Double>());
				cost_function.get(node1).put(node2, distance);
					
				if(!cost_function.containsKey(node2))
					cost_function.put(node2,new HashMap<SearchSpaceNode, Double>());
				cost_function.get(node2).put(node1, distance);
			}
		}
	}

	public Set<SearchSpaceNode> getNeighborsForNode(SearchSpaceNode node,
			boolean cluster) {
		// TODO Auto-generated method stub
		return node.getNeighbors();
	}


	public Set<SearchSpaceNode> getEntranceNodes() {
		// TODO Auto-generated method stub
		return new HashSet<SearchSpaceNode>();
	}


	public Set<SearchSpaceNode> getSearchSpace() {
		// TODO Auto-generated method stub
		return search_space;
	}


	public Set<Polygon> getClusterBoundaries() {
		return boundary_set;
	}

	public int getClusterID(SearchSpaceNode node) {
		return 0;
	}


	public double getCost(SearchSpaceNode from, SearchSpaceNode to) {
		if(from.getNeighbors().contains(to))
			return 1;
		return Integer.MAX_VALUE;
	}

	public PathUpdater getPath(Point start, Point goal) {
		SearchSpaceNode start_node = null;
		SearchSpaceNode goal_node = null;
		
		start = new Point(0,0);
		goal = new Point(width,height);
		for(SearchSpaceNode node:search_space) {
			int x = node.point_list[0].x;
			int y = node.point_list[0].y;
			if(start.x == x && start.y == y)
				start_node = node;
			if(goal.x == x && goal.y == y)
				goal_node = node;
		}
		
		ArrayList<Point> point_list;
		
		point_list = SearchAlgorithms.AStar(this, cost_function, start, goal, start_node, goal_node, false);
		point_list.add(0, start);
		PathUpdater path_updater = new PathUpdater(this,point_list);
		return path_updater;
	}
	
	public ArrayList<Point> getSubpath(Point start, Point goal) {
		SearchSpaceNode start_node = null;
		SearchSpaceNode goal_node = null;
		
		for(SearchSpaceNode node:search_space) {
			int x = node.point_list[0].x;
			int y = node.point_list[0].y;
			if(start.x == x && start.y == y)
				start_node = node;
			if(goal.x == x && goal.y == y)
				goal_node = node;
		}
		ArrayList<Point> point_list;
		point_list = SearchAlgorithms.AStar(this, cost_function, start, goal, start_node, goal_node, false);
		return point_list;
	}

}

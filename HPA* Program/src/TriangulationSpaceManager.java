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
	Set<Node> search_space = new HashSet<Node>();
	Set<Polygon> boundary_set;
	Map<Node,Map<Node, Double>> cost_function = new HashMap<Node,Map<Node, Double>>();
	
	public TriangulationSpaceManager(ArrayList<Obstacle> obstacle_list, int width, int height) {
		
		this.width = width;
		this.height = height;
		
		Rectangle boundary = new Rectangle(0,0,width,height);
		
		search_space = SearchAlgorithms.getTriangulation(boundary,obstacle_list);
		
		int[]x_list = {0,width,width,0};
		int[]y_list = {0,0,height,height,0};
		Polygon p = new Polygon(x_list,y_list,4);
		boundary_set = new HashSet<Polygon>();
		boundary_set.add(p);
		
		createCostFunction();
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
		// TODO Auto-generated method stub
		return node.getNeighbors();
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
		return 0;
	}


	public double getCost(Node from, Node to) {
		if(from.getNeighbors().contains(to))
			return 1;
		return Integer.MAX_VALUE;
	}

	public PathUpdater getPath(Point start, Point goal) {
		Node start_node = null;
		Node goal_node = null;
		
		start = new Point(0,0);
		goal = new Point(width,height);
		for(Node node:search_space) {
			Point p1 = node.getPoints()[0];
			Point p2 = node.getPoints()[1];
			Point p3 = node.getPoints()[2];
			if(SearchAlgorithms.pointInTriangle(start, p1, p2, p3)) {
				start_node = node;
				break;
			}
		}
		
		ArrayList<Node> search_space_list;
		
		search_space_list = SearchAlgorithms.TAStar(this, start, goal, start_node, goal_node, false);
		
		ArrayList<Point>point_list = new ArrayList<Point>();
		Node previous = start_node;
		Point[] list = start_node.getPoints();
		//System.out.println(list[0].x + " " + list[0].y + " " + list[1].x + " " + list[1].y + " " + list[2].x + " " + list[2].y);
		for(int i = 1; i<search_space_list.size(); i++) {
			list = search_space_list.get(i).getPoints();
			System.out.println(list[0].x + " " + list[0].y + " " + list[1].x + " " + list[1].y + " " + list[2].x + " " + list[2].y);
			Point[] edge = SearchAlgorithms.sharedPointsForNodes(previous, search_space_list.get(i));
			Point p = new Point(edge[0].x + (edge[1].x-edge[0].x)/2, edge[0].y + (edge[1].y-edge[0].y)/2);
			previous = search_space_list.get(i);
			point_list.add(p);
		}
		list = search_space_list.get(search_space_list.size()-1).getPoints();
		if(!SearchAlgorithms.pointInTriangle(goal, list[0], list[1], list[2])) {
			System.out.println("BADBADBAD");
			search_space_list = SearchAlgorithms.TAStar(this, start, goal, start_node, goal_node, false);
		}
		point_list.add(0, start);
		point_list.add(goal);
		
		PathUpdater path_updater = new PathUpdater(this,point_list);
		return path_updater;
	}
	
	public ArrayList<Point> getSubpath(Point start, Point goal) {
		System.out.println(goal.x + " " + goal.y);
		ArrayList<Point> list = new ArrayList<Point>();
		list.add(goal);
		return list;
	}

}

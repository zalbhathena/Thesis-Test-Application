import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TriangulationSpaceManager implements SearchSpaceManager{
	static {
		System.loadLibrary("HPAProgram");
	}
	
	private native double[] getTriangulation(double[] f);
	final double END = -12345.6;
	int width, height;
	Set<SearchSpaceNode> search_space = new HashSet<SearchSpaceNode>();
	Set<Polygon> boundary_set;
	HPAProgram triangulation_library;
	
	public TriangulationSpaceManager(ArrayList<Obstacle> obstacle_list, int width, int height) {
		triangulation_library = new HPAProgram();
		
		this.width = width;
		this.height = height;
		
		double obstacle_boundaries[] = new double[obstacle_list.size()*9 + 1];
		for(int i = 0; i < obstacle_list.size(); i++) {
			Obstacle o = obstacle_list.get(i);
			obstacle_boundaries[i * 5 + 0] = o.x;
			obstacle_boundaries[i * 5 + 0] = o.y;
			obstacle_boundaries[i * 5 + 0] = o.x + o.width;
			obstacle_boundaries[i * 5 + 0] = o.y;
			obstacle_boundaries[i * 5 + 0] = o.x + o.width;
			obstacle_boundaries[i * 5 + 0] = o.y + o.height;
			obstacle_boundaries[i * 5 + 0] = o.x;
			obstacle_boundaries[i * 5 + 0] = o.y + o.height;
			
			obstacle_boundaries[i * 5 + 9] = END;
		}
		obstacle_boundaries[obstacle_boundaries.length - 1] = END;
		
		double[] edges = triangulation_library.getTriangulation(obstacle_boundaries);
		
		int[]x_list = {0,width,width,0};
		int[]y_list = {0,0,height,height,0};
		Polygon p = new Polygon(x_list,y_list,4);
		boundary_set = new HashSet<Polygon>();
		boundary_set.add(p);
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
		// TODO Auto-generated method stub
		/*ArrayList<Point> point_list;
		SearchSpaceNode start_node = grid[start.x][start.y];
		SearchSpaceNode goal_node = grid[goal.x][goal.y];
		point_list = SearchAlgorithms.AStar(this, null, start, goal, start_node, goal_node, false);
		point_list.add(0, start);
		PathUpdater path_updater = new PathUpdater(this,point_list);*/
		return null;
	}
	
	public ArrayList<Point> getSubpath(Point start, Point goal) {
		ArrayList<Point> point_list;
		//SearchSpaceNode start_node = grid[start.x][start.y];
		//SearchSpaceNode goal_node = grid[goal.x][goal.y];
		//point_list = SearchAlgorithms.AStar(this, null, start, goal, start_node, goal_node, false);
		return null;
	}

}

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class GridSpaceManager implements SearchSpaceManager{
	int width, height;
	Node[][] grid;
	Set<Node> search_space = new HashSet<Node>();
	Set<Polygon> boundary_set;

	public GridSpaceManager(ArrayList<Obstacle> obstacle_list, int width, int height) {
		grid = new Node[width][height];
		this.width = width;
		this.height = height;
		
		
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
		int[]x_list = {0,width,width,0};
		int[]y_list = {0,0,height,height,0};
		Polygon p = new Polygon(x_list,y_list,4);
		boundary_set = new HashSet<Polygon>();
		boundary_set.add(p);
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
		// TODO Auto-generated method stub
		ArrayList<Point> point_list;
		Node start_node = grid[start.x][start.y];
		Node goal_node = grid[goal.x][goal.y];
		point_list = SearchAlgorithms.AStar(this, null, start, goal, start_node, goal_node, false);
		point_list.add(0, start);
		PathUpdater path_updater = new PathUpdater(this,point_list);
		return path_updater;
	}
	
	public ArrayList<Point> getSubpath(Point start, Point goal) {
		ArrayList<Point> point_list;
		Node start_node = grid[start.x][start.y];
		Node goal_node = grid[goal.x][goal.y];
		point_list = SearchAlgorithms.AStar(this, null, start, goal, start_node, goal_node, false);
		return point_list;
	}

}

import java.util.*;
import java.awt.Point;
public class GridSpaceManager {
	private int width, height;
	private int cluster_width, cluster_height;
	private GridSpaceNode[][] grid;
	public GridSpaceManager(ArrayList<Obstacle> obstacle_list, int width, int height) {
		grid = new GridSpaceNode[width][height];
		this.width = width;
		this.height = height;
		this.cluster_height = height;
		this.cluster_width =  width;
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				grid[i][j] = new GridSpaceNode(i,j);
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
				ArrayList<SearchSpaceNode> neighbors = new ArrayList<SearchSpaceNode>();
				if(i!= width -1 && grid[i+1][j] != null) neighbors.add(grid[i+1][j]);
				if(i!=0 && grid[i-1][j] != null) neighbors.add(grid[i-1][j]);
				if(j!=height-1 && grid[i][j+1] != null) neighbors.add(grid[i][j+1]);
				if(j!=0 && grid[i][j-1] != null) neighbors.add(grid[i][j-1]);
				grid[i][j].setNeighbors(neighbors);
			}
		}
		
	}
	
	public GridSpaceManager(ArrayList<Obstacle> obstacle_list, 
			int width, int height, int cluster_width, int cluster_height) {
		this(obstacle_list,width,height);
		this.cluster_width = cluster_width;
		this.cluster_height = cluster_height;
	}
	
	public int getClusterID(SearchSpaceNode node) {
		int x = node.point_list[0].x;
		int y = node.point_list[0].y;
		int clusters_per_row = (int)Math.ceil( width / cluster_width);
		return x/cluster_width + (clusters_per_row)*(y/cluster_height);
	}
	
	public ArrayList<SearchSpaceNode> getNeighborsForNode(SearchSpaceNode node, boolean cluster) {
		ArrayList<SearchSpaceNode> neighbors = node.getNeighbors();
		if(!cluster)
			return neighbors;
		int node_id = getClusterID(node);
		for(SearchSpaceNode neighbor: neighbors) {
			if(getClusterID(neighbor) != node_id)
				neighbors.remove(neighbor);
		}
		return neighbors;
	}
	
	public GridSpaceNode getNode(int x, int y) {
		if(grid[x][y] == null)
			width++;
		return grid[x][y];
	}
	
}
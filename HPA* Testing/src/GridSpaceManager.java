import java.util.ArrayList;
import java.awt.Point;
public class GridSpaceManager {
	private int width, height;
	private SearchSpaceNode[][] grid;
	public GridSpaceManager(ArrayList<Obstacle> obstacle_list, int width, int height) {
		grid = new SearchSpaceNode[width][height];
		this.width = width;
		this.height = height;
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				grid[i][j] = new SearchSpaceNode(new Point(i,j));
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
	
	public SearchSpaceNode getNode(int x, int y) {
		if(grid[x][y] == null)
			width++;
		return grid[x][y];
	}
	
}
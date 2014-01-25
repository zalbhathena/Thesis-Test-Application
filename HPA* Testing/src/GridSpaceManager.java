import java.util.*;
import java.awt.Point;
import java.awt.Polygon;
public class GridSpaceManager implements SearchSpaceManager{
	public int width, height;
	public int cluster_width, cluster_height;
	private GridSpaceNode[][] grid;
	private Set<SearchSpaceNode> search_space = new HashSet<SearchSpaceNode>();
	private Set<Polygon> cluster_boundaries = new HashSet<Polygon>();
	private GraphSpaceManager entrance_graph;
	
	public final int MINIMUM_ENTRACE_WIDTH = 6;
	
	GridSpaceNode[][] entrance_nodes_list;
	Set<SearchSpaceNode> entrance_nodes = new HashSet<SearchSpaceNode>();
	
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
				search_space.add(grid[i][j]);
			}
		}
		
	}
	
	public GridSpaceManager(ArrayList<Obstacle> obstacle_list, 
			int width, int height, int cluster_width, int cluster_height) {
		this(obstacle_list,width,height);
		this.cluster_width = cluster_width;
		this.cluster_height = cluster_height;
		findEntranceNodes();
		findDistanceCache();
		
		int clusters_per_row = clustersPerRow();
		int clusters_per_column = clustersPerColumn();
		for(int i = 0; i < clusters_per_row; i++) {
    		for(int j = 0; j < clusters_per_column; j++) {
    			int x = i*cluster_width;
    			int y = j*cluster_height;
    			int poly_width = (Math.min(width - x, cluster_width));
    			int poly_height = (Math.min(height - y, height));
    			int[] x_list = {x,x+poly_width,x+poly_width,x};
    			int[] y_list = {y,y,y+poly_height,y+poly_height};
    			Polygon p = new Polygon(x_list,y_list,4);
    			cluster_boundaries.add(p);
    		}
    	}
	}
	
	public int getClusterID(SearchSpaceNode node) {
		int x = node.point_list[0].x;
		int y = node.point_list[0].y;
		int cluster_id = x/cluster_width + (clustersPerRow())*(y/cluster_height);
		return cluster_id;
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
		//if(grid[x][y] == null)
		//	width++;
		return grid[x][y];
	}
	
	private void findEntranceNodes() {
		entrance_nodes.clear();
		
		int clusters_per_row = clustersPerRow();
		int clusters_per_column = clustersPerColumn();
		entrance_nodes_list = new GridSpaceNode[clusters_per_row][clusters_per_column];
		for(int i = 0; i<clusters_per_row; i++) {
			for(int j = 0; j < clusters_per_column; j++) {
				if(i != 0) {
					int x = i*cluster_width;
					int y = j*cluster_height;
					int limit = j*cluster_height + cluster_height;
					int open_count = 0;
					
					
					for(;y < limit; y++) {
						if(grid[x][y] != null && grid[x-1][y] != null)
							open_count++;
						else {
							if(open_count == 0)
								continue;
							else if(open_count < MINIMUM_ENTRACE_WIDTH) {
								entrance_nodes.add(grid[x][y - 1 - open_count/2]);
								entrance_nodes.add(grid[x - 1][y - 1 - open_count/2]);
							}
							else {
								entrance_nodes.add(grid[x][y - 1]);
								entrance_nodes.add(grid[x][y - open_count]);
								
								entrance_nodes.add(grid[x-1][y - 1]);
								entrance_nodes.add(grid[x-1][y - open_count]);
							}
							open_count = 0;
						}
					}
					if(open_count > 0) {
						if(open_count < MINIMUM_ENTRACE_WIDTH) {
							entrance_nodes.add(grid[x][y - 1 - open_count/2]);
							entrance_nodes.add(grid[x - 1][y - 1 - open_count/2]);
						}
						else {
							entrance_nodes.add(grid[x][y - 1]);
							entrance_nodes.add(grid[x][y - open_count]);
							
							entrance_nodes.add(grid[x-1][y - 1]);
							entrance_nodes.add(grid[x-1][y - open_count]);
						}
					}
				}
				
				if(j != 0) {
					int x = i*cluster_width;
					int y = j*cluster_height;
					int limit = j*cluster_width + cluster_width;
					int open_count = 0;
					
					
					for(;x < limit; x++) {
						if(grid[x][y] != null && grid[x][y-1] != null)
							open_count++;
						else {
							if(open_count == 0)
								continue;
							else if(open_count < MINIMUM_ENTRACE_WIDTH) {
								entrance_nodes.add(grid[x - 1 - open_count/2][y]);
								entrance_nodes.add(grid[x - 1 - open_count/2][y-1]);
							}
							else {
								entrance_nodes.add(grid[x - 1][y]);
								entrance_nodes.add(grid[x - open_count][y]);
								
								entrance_nodes.add(grid[x - 1][y - 1]);
								entrance_nodes.add(grid[x - open_count][y - 1]);
							}
							open_count = 0;
						}
					}
					if(open_count > 0) {
						if(open_count < MINIMUM_ENTRACE_WIDTH) {
							entrance_nodes.add(grid[x - 1 - open_count/2][y]);
							entrance_nodes.add(grid[x - 1 - open_count/2][y-1]);
						}
						else {
							entrance_nodes.add(grid[x - 1][y]);
							entrance_nodes.add(grid[x - open_count][y]);
							
							entrance_nodes.add(grid[x - 1][y - 1]);
							entrance_nodes.add(grid[x - open_count][y - 1]);
						}
					}
				}
				
				if(i != clusters_per_row - 1) {
					int x = i*cluster_width + cluster_width - 1;
					int y = j*cluster_height;
					int limit = j*cluster_height + cluster_height;
					int open_count = 0;
					
					
					for(;y < limit; y++) {
						if(grid[x][y] != null && grid[x+1][y] != null)
							open_count++;
						else {
							if(open_count == 0)
								continue;
							else if(open_count < MINIMUM_ENTRACE_WIDTH) {
								entrance_nodes.add(grid[x][y - 1 - open_count/2]);
								entrance_nodes.add(grid[x + 1][y - 1 - open_count/2]);
							}
							else {
								entrance_nodes.add(grid[x][y - 1]);
								entrance_nodes.add(grid[x][y - open_count]);
								
								entrance_nodes.add(grid[x+1][y - 1]);
								entrance_nodes.add(grid[x+1][y - open_count]);
							}
							open_count = 0;
						}
					}
					if(open_count > 0) {
						if(open_count < MINIMUM_ENTRACE_WIDTH) {
							entrance_nodes.add(grid[x][y - 1 - open_count/2]);
							entrance_nodes.add(grid[x + 1][y - 1 - open_count/2]);
						}
						else {
							entrance_nodes.add(grid[x][y - 1]);
							entrance_nodes.add(grid[x][y - open_count]);
							
							entrance_nodes.add(grid[x+1][y - 1]);
							entrance_nodes.add(grid[x+1][y - open_count]);
						}
					}
					
				}
				
				if(j != clusters_per_column - 1) {
					int x = i*cluster_width;
					int y = j*cluster_height + cluster_height - 1;
					int limit = j*cluster_width + cluster_width;
					int open_count = 0;
					
					
					for(;x < limit; x++) {
						if(grid[x][y] != null && grid[x][y+1] != null)
							open_count++;
						else {
							if(open_count == 0)
								continue;
							else if(open_count < MINIMUM_ENTRACE_WIDTH) {
								entrance_nodes.add(grid[x - 1 - open_count/2][y]);
								entrance_nodes.add(grid[x - 1 - open_count/2][y+1]);
							}
							else {
								entrance_nodes.add(grid[x - 1][y]);
								entrance_nodes.add(grid[x - open_count][y]);
								
								entrance_nodes.add(grid[x - 1][y + 1]);
								entrance_nodes.add(grid[x - open_count][y + 1]);
							}
							open_count = 0;
						}
					}
					if(open_count > 0) {
						if(open_count < MINIMUM_ENTRACE_WIDTH) {
							entrance_nodes.add(grid[x - 1 - open_count/2][y]);
							entrance_nodes.add(grid[x - 1 - open_count/2][y+1]);
						}
						else {
							entrance_nodes.add(grid[x - 1][y]);
							entrance_nodes.add(grid[x - open_count][y]);
							
							entrance_nodes.add(grid[x - 1][y + 1]);
							entrance_nodes.add(grid[x - open_count][y + 1]);
						}
					}
				}
				
			}
		}
		for(SearchSpaceNode node:entrance_nodes) {
			int cluster_id = getClusterID(node);
			entrance_nodes_list[cluster_id/clusters_per_column][cluster_id/clusters_per_row] = (GridSpaceNode)node;
		}
	}
	
	private void findDistanceCache() {
		
		ArrayList<SearchSpaceNode> search_space_list = new ArrayList<SearchSpaceNode>();
		Map<SearchSpaceNode, Map<SearchSpaceNode, Double>> cost_function = 
				new HashMap<SearchSpaceNode, Map<SearchSpaceNode, Double>>();
		
		for(SearchSpaceNode node1:entrance_nodes) {
			for(SearchSpaceNode node2:entrance_nodes) {
				ArrayList<Point> point_list = 
						SearchAlgorithms.AStar(this,node1.point_list[0], node2.point_list[0],node1,node2,false);
				 double distance = point_list.size();
				if(cost_function.containsKey(node1)) {
					cost_function.get(node1).put(node2, distance);
				}
				else {
					cost_function.put(node1,new HashMap<SearchSpaceNode, Double>());
					cost_function.get(node1).put(node2, distance);
				}
			}
		}
		entrance_graph = new GraphSpaceManager(entrance_nodes, cost_function);
 	}
	
	public void getDistanceForCachedNodes() {
		
	}
	
	public int clustersPerRow() {
		return (int)Math.ceil( width / cluster_width);
	}
	public int clustersPerColumn() {
		return (int)Math.ceil( width / cluster_height);
	}
	
	public Set<SearchSpaceNode>getEntranceNodes() {return entrance_nodes;}
	
	public Set<SearchSpaceNode>getSearchSpace() {return search_space;}
	
	public Set<Polygon> getClusterBoundaries() {
		return cluster_boundaries;
	}
	
	public double getCost(SearchSpaceNode from, SearchSpaceNode to) {
		if(!from.getNeighbors().contains(to))
			throw new IllegalArgumentException("Not neighboring states.");
		return 1;
	}
	
}
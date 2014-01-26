import java.util.ArrayList;
import java.awt.Point;
public class PathUpdater {
	GridSpaceManager grid;
	ArrayList<Point> hierarchical_path;
	ArrayList<Point> full_path;
	int index = -1;
	Thread thread;
	int safe_size = index;
	boolean done = true;
	public PathUpdater(GridSpaceManager grid, ArrayList<Point>path) {
		this.grid = grid;
		hierarchical_path = path;
	}
	
	public void startPathFinding() {
		done = false;
		thread = new Thread(new Runnable() {
	        public void run(){
	            findPath();
	        }
	    });
		thread.start();
	}
	
	public void findPath() {
		Point start = hierarchical_path.remove(0);
		Point goal;
		while(hierarchical_path.size() > 0) {
			goal = hierarchical_path.remove(0);
			
			ArrayList<Point> next_path = grid.getThePath(start, goal);
			for(int i = 0; i < next_path.size(); i++)
				full_path.add(next_path.get(i));
			start = goal;
			safe_size = full_path.size();
		}
		done = true;
	}
	
	public ArrayList<Point> getLatestPath() {
		
		ArrayList<Point> path_list = new ArrayList<Point>();
		for(int i = index; i<safe_size; i++) {
			path_list.add(new Point(full_path.get(i)));
		}
		index = full_path.size();
		return path_list;
	}
}
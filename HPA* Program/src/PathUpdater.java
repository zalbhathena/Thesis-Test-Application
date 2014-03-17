import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.awt.Point;
public class PathUpdater{
	private SearchSpaceManager search_space_manager;
	private ArrayList<Point> hierarchical_path;
	private ArrayList<ArrayList<Point>> path_updates = new ArrayList<ArrayList<Point>>();
	private FutureTask<ArrayList<Point>> future_task;
	private boolean done = true;
	
	public PathUpdater(SearchSpaceManager grid, ArrayList<Point>path) {
		this.search_space_manager = grid;
		hierarchical_path = path;
	}
	
	public void startPathfinding() {
		done = false;
		Point start = hierarchical_path.remove(0);
		while(hierarchical_path.size() > 0) {
			Point goal = hierarchical_path.remove(0);
			
			PathCalculator path_calculator = new PathCalculator(start, goal);
			future_task = new FutureTask<ArrayList<Point>>(path_calculator);
			future_task.run();
			try {
				ArrayList<Point> next_path = future_task.get();
				path_updates.add(next_path);
			}
			catch(ExecutionException e) {
				System.out.println("EXCEPTION!!! " + e);
			}
			catch(InterruptedException e) {
				System.out.println("EXCEPTION!!!");
			}
			start = goal;
		}
		done = true;
	}
	public boolean isDone() {
		return done;
	}
	
	public int remainingUpdates() {
		return path_updates.size();
	}
	
	public ArrayList<Point> getNextPath() {
		if(done && path_updates.size() == 0) {
			return null;
		}
		return path_updates.remove(0);
	}
	
	public
	
	class PathCalculator implements Callable<ArrayList<Point>>{
		private Point start;
		private Point goal;
		
		public PathCalculator(Point start, Point goal) {
			this.start = start;
			this.goal = goal;
		}
		
		public ArrayList<Point> call() throws Exception {
			return search_space_manager.getSubpath(start, goal);
		}
	}
}

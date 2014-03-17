
import java.awt.Point;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;



public class THPAStarPathUpdater{
	
	private THPAStarManager search_space_manager;
	private ArrayList<Node> hierarchical_path;
	private ArrayList<ArrayList<Node>> path_updates = new ArrayList<ArrayList<Node>>();
	private FutureTask<ArrayList<Node>> future_task;
	private boolean done = true;
	
	public THPAStarPathUpdater(THPAStarManager manager, ArrayList<Node>path) {
		this.search_space_manager = manager;
		hierarchical_path = path;
		Point[] ps = hierarchical_path.get(hierarchical_path.size()-1).getPoints();
		System.out.println("hpath " + ps[0].x + "," + ps[0].y +","+ ps[1].x + " " + ps[1].y + "," + ps[2].x + " " + ps[2].y);
	}
	
	public void startPathfinding() {
		done = false;
		Node start = hierarchical_path.remove(0);
		while(hierarchical_path.size() > 0) {
			Node goal = hierarchical_path.remove(0);
			
			PathCalculator path_calculator = new PathCalculator(start, goal);
			future_task = new FutureTask<ArrayList<Node>>(path_calculator);
			future_task.run();
			try {
				ArrayList<Node> next_path = future_task.get();
				path_updates.add(next_path);
			}
			catch(ExecutionException e) {
				System.out.println("EXCEPTION!");
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
	
	public ArrayList<Node> getNextPath() {
		if(done && path_updates.size() == 0) {
			return null;
		}
		return path_updates.remove(0);
	}
	

	
	class PathCalculator implements Callable<ArrayList<Node>>{
		private Node start;
		private Node goal;
		
		public PathCalculator(Node start, Node goal) {
			this.start = start;
			this.goal = goal;
		}
		
		public ArrayList<Node> call() throws Exception {
			return search_space_manager.getNodeSubpath(start, goal);
		}
	}
}


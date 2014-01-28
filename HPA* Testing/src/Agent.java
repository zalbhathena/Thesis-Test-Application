import java.util.ArrayList;
import java.awt.Point;

public class Agent {
	double diameter, x, y;
	private ArrayList<Point> subgoal_list = new ArrayList<Point>();
	public Agent(int x, int y, int diameter) {
		this.x = x;
		this.y = y;
		this.diameter = diameter;
	}
	
	public void setSubgoals(ArrayList<Point> subgoals) {
		subgoal_list.clear();
		for(int i = 0; i < subgoals.size(); i++) {
			subgoal_list.add(new Point(subgoals.get(i).x, subgoals.get(i).y));
		}
	}
	
	public Point getNextSubgoal() {
		return subgoal_list.remove(0);
	}
}

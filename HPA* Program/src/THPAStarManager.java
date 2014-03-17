import java.awt.Point;
import java.util.ArrayList;


public interface THPAStarManager {
	public THPAStarPathUpdater getNodePath(Point start_point, Point goal_point);
	public ArrayList<Node> getNodeSubpath(Node start, Node goal);
}

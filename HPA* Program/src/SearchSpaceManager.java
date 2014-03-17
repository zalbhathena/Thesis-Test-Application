
import java.util.ArrayList;
import java.util.Set;
import java.awt.Polygon;
import java.awt.Point;

public interface SearchSpaceManager {
	public Set<Node> getNeighborsForNode(Node node, boolean cluster);
	public Set<Node> getEntranceNodes();
	public Set<Node> getSearchSpace();
	public Set<Polygon> getClusterBoundaries();
	public int getClusterID(Node node);
	public double getCost(Node from, Node to);
	public PathUpdater getPath(Point start, Point goal);
	public ArrayList<Point> getSubpath(Point start_point, Point goal_point);
}

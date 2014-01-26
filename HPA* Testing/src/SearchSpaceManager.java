
import java.util.ArrayList;
import java.util.Set;
import java.awt.Polygon;
import java.awt.Point;


public interface SearchSpaceManager {
	public Set<SearchSpaceNode> getNeighborsForNode(SearchSpaceNode node, boolean cluster);
	public Set<SearchSpaceNode> getEntranceNodes();
	public Set<SearchSpaceNode> getSearchSpace();
	public Set<Polygon> getClusterBoundaries();
	public int getClusterID(SearchSpaceNode node);
	public double getCost(SearchSpaceNode from, SearchSpaceNode to);
	public ArrayList<Point> getPath(Point start, Point goal);
}

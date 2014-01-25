import java.util.ArrayList;
import java.util.Set;
import java.awt.Polygon;


public interface SearchSpaceManager {
	public ArrayList<SearchSpaceNode> getNeighborsForNode(SearchSpaceNode node, boolean cluster);
	public Set<SearchSpaceNode> getEntranceNodes();
	public Set<SearchSpaceNode> getSearchSpace();
	public Set<Polygon> getClusterBoundaries();
	public int getClusterID(SearchSpaceNode node);
	public double getCost(SearchSpaceNode from, SearchSpaceNode to);
}

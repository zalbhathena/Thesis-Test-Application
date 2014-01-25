import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;


public class GraphSpaceManager implements SearchSpaceManager {
	
	Set<SearchSpaceNode> search_space = new HashSet<SearchSpaceNode>();
	ArrayList<SearchSpaceNode> search_space_list = new ArrayList<SearchSpaceNode>();
	Map<SearchSpaceNode, Map<SearchSpaceNode, Double>> cost_function;
	
	public GraphSpaceManager(Set<SearchSpaceNode> search_space, 
			Map<SearchSpaceNode, Map<SearchSpaceNode, Double>> cost_function) {
		this.search_space_list = new ArrayList<SearchSpaceNode>(search_space);
		this.search_space = search_space;
		this.cost_function = cost_function;
	}
	
	public ArrayList<SearchSpaceNode> getNeighborsForNode(SearchSpaceNode node,
			boolean cluster) {
		return search_space_list.get(search_space_list.indexOf(node)).getNeighbors();
	}

	public Set<SearchSpaceNode> getEntranceNodes() {
		return null;
	}

	public Set<SearchSpaceNode> getSearchSpace() {
		return search_space;
	}

	
	public Set<Polygon> getClusterBoundaries() {
		return null;
	}

	
	public int getClusterID(SearchSpaceNode node) {
		return 0;
	}
	
	public double getCost(SearchSpaceNode from, SearchSpaceNode to) {
		if(!from.getNeighbors().contains(to))
			throw new IllegalArgumentException("Not neighboring states.");
		return cost_function.get(from).get(to);
	}

}
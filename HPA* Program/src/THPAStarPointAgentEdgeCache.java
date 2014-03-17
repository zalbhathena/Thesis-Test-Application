import java.awt.Point;
import java.util.Map;
import java.util.HashMap;

public class THPAStarPointAgentEdgeCache {
	
	Map<EdgeCacheKey,Double> edge_cache;
	
	Map<Node,Map<Node,Edge>>edge_map;
	
	SearchSpaceManager manager;
	
	public THPAStarPointAgentEdgeCache (SearchSpaceManager manager) {
		this.manager = manager;
		edge_cache = new HashMap<EdgeCacheKey,Double>();
		edge_map = new HashMap<Node,Map<Node,Edge>>();
	}
	
	public void addEdge(Node from, Node to, Edge entrance, double f_value, Edge edge) {
		
		if(!edge_map.containsKey(from))
			edge_map.put(from, new HashMap<Node,Edge>());
		edge_map.get(from).put(to, edge);
		edge_cache.put(new EdgeCacheKey(from,to,entrance), f_value);
	}
	public void removeEdge(Node from, Node to, Edge entrance) {
	
		edge_cache.remove(new EdgeCacheKey(from,to,entrance));
	}
	
	public double getDistance(Node from, Node to, Edge entrance) {
		EdgeCacheKey key = new EdgeCacheKey(from,to,entrance);
		if(edge_cache.containsKey(key))
			return edge_cache.get(key);
		return Double.NaN;
	}
	
	public Edge edgeForNodes(Node n1, Node n2) {
		if(edge_map.containsKey(n1))
			if(edge_map.get(n1).containsKey(n2))
				return edge_map.get(n1).get(n2);
		Point[] point_list = SearchAlgorithms.sharedPointsForNodes(n1, n2);
		if(point_list.length != 2) {
			return null;
		}
		Edge entrance = new Edge(point_list[0], point_list[1]);
		return entrance;
	}
	
	class EdgeCacheKey {
		Node from;
		Node to;
		Edge entrance;
		public EdgeCacheKey(Node from, Node to, Edge entrance) {
			this.from = from;
			this.to = to;
			this.entrance = entrance;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((entrance == null) ? 0 : entrance.hashCode());
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EdgeCacheKey other = (EdgeCacheKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (entrance == null) {
				if (other.entrance != null)
					return false;
			} else if (!entrance.equals(other.entrance))
				return false;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (to == null) {
				if (other.to != null)
					return false;
			} else if (!to.equals(other.to))
				return false;
			return true;
		}
		private THPAStarPointAgentEdgeCache getOuterType() {
			return THPAStarPointAgentEdgeCache.this;
		}
		
		
	}
}
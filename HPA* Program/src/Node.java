import java.awt.Point;
import java.util.Set;
import java.util.HashSet;
public interface Node {
	
	public Point[] getPoints();
	
	public Set<Node> getNeighbors();
	
	public void setNeighbors(Set<Node> neighbors);
	
	public void addNeighbor(Node neighbor);
	
	public void removeNeighbor(Node neighbor);
	
	public void removeSelfFromGraph();
	
	public boolean equals(Object o); //OVERRIDE THESE TWO METHODS FOR THE CACHE TO WORK PROPERLY
	
	public int hashCode();
}
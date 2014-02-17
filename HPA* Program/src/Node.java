import java.awt.Point;
import java.util.Set;
import java.util.HashSet;
public interface Node {
	
	public Point[] getPoints();
	
	public Set<Node> getNeighbors();
	
	public void setNeighbors(Node[] neighbors);
	
	public void addNeighbor(Node neighbor);
	
	public void removeNeighbor(Node neighbor);
}
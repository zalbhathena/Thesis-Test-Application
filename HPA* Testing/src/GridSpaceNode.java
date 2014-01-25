import java.awt.*;
public class GridSpaceNode extends SearchSpaceNode {
	int x,y;
	public GridSpaceNode (int x, int y) {
		super(new Point[]{new Point(x,y),new Point(x+1,y),new Point(x+1,y+1),new Point(x,y+1)});
		this.x = x;
		this.y = y;
	}
}

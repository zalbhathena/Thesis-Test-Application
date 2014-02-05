
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

public class MapView extends JPanel{
	
	private int size, actual_map_size;
	MapModel map_model;
	Point current_subgoal = null;
	
	int frame_number = 0;
	long start_time;
	Color[] color_wheel = {new Color(0,255,255), new Color(255,0,0), new Color(0,127,255),
			new Color(255,127,0), new Color(0,0,255), new Color(255,255,0),
			new Color(127,0,255), new Color(127,255,0), new Color(255,0,255),
			new Color(0,255,0), new Color(255,0,127), new Color(0,255,127)};
	public MapView(int size)
	{
		this.setBounds(0,200,600,600);
		
		this.size = size;
	}
	boolean starting_print = true;
	
	Map<Rectangle, Color> agent_path_list = new HashMap<Rectangle,Color>();
	int agent_color;
	public void paintComponent(Graphics g) {
			
		int x_offset = 10, y_offset = 10;
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect((600-size)/2 + x_offset - 2, y_offset - 2,size + 5, size + 5);
        g.setColor(Color.WHITE);
        g.fillRect((600-size)/2 + x_offset - 1, y_offset - 1,size+3,size+3);
        
        
        if(this.map_model != null) {
    	    
        	SearchSpaceManager grid = map_model.search_space_manager;
        	
        	g.setColor(Color.GRAY);
		    for(SearchSpaceNode node:grid.getEntranceNodes()) {
		    	Point[] point_list = node.point_list;
				int[] x_list = new int[point_list.length];
				int[] y_list = new int[point_list.length];
				for(int k = 0; k < point_list.length; k++) {
					x_list[k] = scaleX(point_list[k].x) + x_offset;
					y_list[k] = scaleY(point_list[k].y) + y_offset;
				}
				g.fillPolygon(x_list,y_list,x_list.length);
		    }
		    
	    	for(SearchSpaceNode node: grid.getSearchSpace()) {
    			
    			if(node != null) {
    				Point[] point_list = node.point_list;
    				int[] x_list = new int[point_list.length];
    				int[] y_list = new int[point_list.length];
    				for(int k = 0; k < point_list.length; k++) {
    					x_list[k] = scaleX(point_list[k].x) + x_offset;
    					y_list[k] = scaleY(point_list[k].y) + y_offset;
    				}
    				
    				g.setColor(color_wheel[grid.getClusterID(node)%color_wheel.length]);
    				
    				g.drawPolygon(x_list,y_list,x_list.length);
    			}
	    	}
	    	
	    	g.setColor(Color.BLACK);
	    	for(Polygon p: grid.getClusterBoundaries()) {
	    		int length = p.xpoints.length;
	    		int[] px_list = p.xpoints;
	    		int[] py_list = p.ypoints;
	    		int[] x_list = new int[length];
				int[] y_list = new int[length];
				for(int k = 0; k < length; k++) {
					x_list[k] = scaleX(px_list[k]) + x_offset;
					y_list[k] = scaleY(py_list[k]) + y_offset;
				}
				g.drawPolygon(x_list,y_list,x_list.length);
	    	}
		
		    
		    g.setColor(color_wheel[2]);
    	    
    	    Agent agent = map_model.agent;
    	    
    	    
    	    if(agent != null) {
    	    	Rectangle rect = new Rectangle(scaleX(agent.x) + x_offset, scaleY(agent.y) + y_offset,
    	    			Math.max(2,scaleSize(agent.diameter)), Math.max(2,scaleSize(agent.diameter)));
    	    	
    	    	g.fillRect(rect.x,rect.y,rect.width,rect.height);
    	    	agent_path_list.put(rect,color_wheel[2]);
    	    }
    	    
    	    agent_color++;
		    if(agent_color == color_wheel.length)
		    	agent_color = 0;
		    
		    for(Rectangle r: agent_path_list.keySet()) {
		    	g.setColor(agent_path_list.get(r));
		    	g.fillRect(r.x,r.y,r.width,r.height);
		    }
		    
		    ArrayList<Obstacle> obstacle_list = map_model.obstacle_list;
		    for(int i = 0; i < obstacle_list.size(); i++) {
		    	Obstacle o = obstacle_list.get(i);
		    	g.setColor(Color.GREEN);
		    	g.fillRect(scaleX(o.x) + x_offset,scaleY(o.y) + y_offset,
		    			scaleSize(o.width),scaleSize(o.height));
		    	g.setColor(Color.BLACK);
		    	g.drawRect(scaleX(o.x) + x_offset,scaleY(o.y) + y_offset,
		    			scaleSize(o.width),scaleSize(o.height));
		    }
        }

    }
	
	public void clearAgentPathList() {
		agent_path_list.clear();
	}
	
	private int scaleX(double x) {
		double scale = ((double)size) / ((double)actual_map_size);
		return (int)(((double)x)*scale)+(600-size)/2;
	}
	private int scaleY(double val) {
		double scale = ((double)size) / ((double)actual_map_size);
		return (int)(((double)val)*scale);
	}
	private int scaleSize(double val) {
		double scale = ((double)size) / ((double)actual_map_size);
		return (int)(((double)val)*scale);
	}
	public void setMapModel(MapModel map) {
		this.map_model = map;
	}
	
	public void setMapScale(int actual_map_size) {
		this.actual_map_size = actual_map_size;
	}
}

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

public class MapView extends JPanel{
	
	private int size, actual_map_size;
	MapModel map_model;
	Point current_subgoal = null;
	
	int frame_number = 0;
	long start_time;
	public MapView(int size)
	{
		this.setBounds(0,200,600,600);
		
		this.size = size;
	}
	boolean starting_print = true;
	public void paintComponent(Graphics g) {
			
		int x_offset = 10, y_offset = 10;
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect((600-size)/2 + x_offset - 2, y_offset - 2,size + 5, size + 5);
        g.setColor(Color.WHITE);
        g.fillRect((600-size)/2 + x_offset - 1, y_offset - 1,size+3,size+3);
        
        
        
        if(this.map_model != null) {
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
		    
		    g.setColor(Color.RED);
		    if(map_model.current_algorithm.equals("A*")) {
		    	GridSpaceManager grid = map_model.grid_manager;
		    	
		    	for(int i = 0; i < map_model.width; i++) {
		    		for(int j = 0; j< map_model.height; j++) {
		    			SearchSpaceNode node = grid.getNode(i, j);
		    			if(node != null) {
		    				Point[] point_list = node.point_list;
		    				int[] x_list = new int[point_list.length];
		    				int[] y_list = new int[point_list.length];
		    				for(int k = 0; k < point_list.length; k++) {
		    					x_list[k] = scaleX(point_list[k].x) + x_offset;
		    					y_list[k] = scaleY(point_list[k].y) + y_offset;
		    				}
		    				g.drawPolygon(x_list,y_list,x_list.length);
		    			}
		    		}
		    	}
		    }
		    
		    g.setColor(Color.BLUE);
		    Agent agent = map_model.agent;
		    
		    
		    if(agent != null) {
		    	g.fillOval(scaleX(agent.x) + x_offset, scaleY(agent.y) + y_offset,
		    			Math.max(2,scaleSize(agent.diameter)), Math.max(2,scaleSize(agent.diameter)));
		    }
		    
        }

    }
	private int scaleX(int x) {
		double scale = ((double)size) / ((double)actual_map_size);
		return (int)(((double)x)*scale)+(600-size)/2;
	}
	private int scaleY(int val) {
		double scale = ((double)size) / ((double)actual_map_size);
		return (int)(((double)val)*scale);
	}
	private int scaleSize(int val) {
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
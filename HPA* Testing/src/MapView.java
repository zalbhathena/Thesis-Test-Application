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
        
        g.setColor(Color.GREEN);
        
        if(this.map_model != null) {
        	ArrayList<Obstacle> obstacle_list = map_model.obstacle_list;
		    for(int i = 0; i < obstacle_list.size(); i++) {
		    	Obstacle o = obstacle_list.get(i);
		    	g.fillRect(scaleX(o.x) + x_offset,scale(o.y) + y_offset,
		    			scale(o.width),scale(o.height));
		    }
		    
		    g.setColor(Color.RED);
		    if(map_model.current_algorithm.equals("A*")) {
		    	GridSpaceManager grid = map_model.grid_manager;
		    	
		    	for(int i = 0; i < map_model.width; i++) {
		    		for(int j = 0; j< map_model.height; j++) {
		    			SearchSpaceNode node = grid.getNode(i, j);
		    			if(node != null) {
		    				Point p = node.point_list[0];
		    				g.drawRect(scaleX(p.x) + x_offset, scale(p.y) + y_offset, scale(1), scale(1));
		    			}
		    		}
		    	}
		    }
		    
		    g.setColor(Color.BLUE);
		    Agent agent = map_model.agent;
		    
		    
		    if(agent != null) {
		    	g.fillOval(scaleX(agent.x) + x_offset, scale(agent.y) + y_offset,
		    			Math.max(2,scale(agent.diameter)), Math.max(2,scale(agent.diameter)));
		    }
		    
        }

    }
	
	private int scaleX(int x) {
		double scale = ((double)size) / ((double)actual_map_size);
		return (int)(((double)x)*scale)+(600-size)/2;
	}
	private int scale(int val) {
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

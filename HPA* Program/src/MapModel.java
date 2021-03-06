import java.util.ArrayList;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.Point;

public class MapModel implements ActionListener{
	boolean running = false;
	int milliseconds_per_frame = 1;
	Timer timer = new Timer(milliseconds_per_frame, this);
	
	int width, height, start_diameter, speed;
	ArrayList<Obstacle> obstacle_list = new ArrayList<Obstacle>();
	Agent agent;
	
	SearchSpaceManager search_space_manager;
	
	boolean final_update = false;
	
	String current_algorithm = "NONE";
	ArrayList<Point> subgoal_list;
	MapView map_view;
	boolean animate = false;
	PathUpdater path_updater;
	
	Point start_point,goal_point;
	public MapModel(int size, MapView map_view)
	{
		this.width = size;
		this.height = size;
		this.map_view = map_view;
		start_point = new Point(0,0);
		goal_point = new Point (size - 1, size - 1);
	}
	
	public void updateMapModel (){
		map_view.repaint();
	}
	//if obstacles can't fit then method returns
	public void createRandomObstacles(int num_obstacles, int start_diameter)
	{
		this.start_diameter = start_diameter;
		obstacle_list.clear();
		
		ArrayList<Rectangle> open_list = new ArrayList<Rectangle>();
		open_list.add(new Rectangle(0,0,width,height));
		int area = width * height;
		for(int i = 0; i < num_obstacles; i++) {
			int rand_rect = (int)(Math.random()*area);
			Rectangle rect = null;
			int count = 0;
			
			while(rand_rect >= 0) {
				rect = open_list.get(count++);
				rand_rect-= rect.width*rect.height;
			}
			
			int x_buffer  = (int)(rect.width / 4);
			int y_buffer  = (int)(rect.height / 4);
			int obstacle_width = (int)(Math.random()*(rect.width/2) + 1);
			int obstacle_height = (int)(Math.random()*(rect.height/2) + 1);
			
			int x_range = (int)(rect.width  * .5) - obstacle_width;
			int y_range = (int)(rect.height  * .5) - obstacle_height;
			
			int obstacle_x = (int)(Math.random()*x_range) + rect.x + x_buffer;
			int obstacle_y = (int)(Math.random()*y_range) + rect.y + y_buffer;
			Obstacle new_obstacle = new Obstacle(obstacle_x,obstacle_y,obstacle_width,obstacle_height);
			obstacle_list.add(new_obstacle);
			
			open_list.remove(rect);
			area -= rect.width*rect.height;
			
			Rectangle left;
			Rectangle right;
			Rectangle up;
			Rectangle down;
			if((int)(Math.random()*2) == 0) {
				left = new Rectangle(rect.x,rect.y,obstacle_x - rect.x,rect.height);
				right = new Rectangle(
						obstacle_x+obstacle_width, rect.y,rect.width-obstacle_width-(obstacle_x - rect.x),rect.height);
				up = new Rectangle(obstacle_x,rect.y,obstacle_width,obstacle_y-rect.y);
				down = new Rectangle(
						obstacle_x,obstacle_y+obstacle_height,obstacle_width,rect.height-obstacle_height-(obstacle_y-rect.y));
			}
			else {
				left = new Rectangle(rect.x,obstacle_y,obstacle_x - rect.x,obstacle_height);
				right = new Rectangle(
						obstacle_x+obstacle_width, obstacle_y,rect.width-obstacle_width-(obstacle_x - rect.x),obstacle_height);
				up = new Rectangle(rect.x,rect.y,rect.width,obstacle_y-rect.y);
				down = new Rectangle(
						rect.x,obstacle_y+obstacle_height,rect.width,rect.height-obstacle_height-(obstacle_y-rect.y));
			}
			open_list.add(left);
			open_list.add(right);
			open_list.add(up);
			open_list.add(down);
			
			area += left.width * left.height;
			area += right.width * right.height;
			area += up.width * up.height;
			area += down.width * down.height;
			
		}
		
		updateMapModel();
	}
	
	public void moveAgent() {
		if(animate) {

			
			double speed = Math.max(1,this.width/10);
			if(subgoal_list.size() == 0) {

				if(path_updater.remainingUpdates() > 0) {
					ArrayList<Point> path_update = path_updater.getNextPath();
					for(int i = 0; i < path_update.size(); i++)
						subgoal_list.add(path_update.get(i));
					return;
				}
				else if(path_updater.isDone() && path_updater.remainingUpdates() == 0){
					animate = false;
					return;
				}
				
			}
			Point p = subgoal_list.get(0);
			if(p.x == agent.x && p.y == agent.y) {
				subgoal_list.remove(0);
				if(subgoal_list.size() == 0)
					return;
				p = subgoal_list.get(0);
			}
			
			double distance = Point.distance(agent.x, agent.y, p.x, p.y);
			double delta_x = ((double)p.x - agent.x)/distance;delta_x*=speed;
			double delta_y = ((double)p.y - agent.y)/distance;delta_y*=speed;
			double distance2 = Math.sqrt(delta_x*delta_x + delta_y*delta_y);
			if(distance2 >= distance) {
				agent.x = p.x;
				agent.y = p.y;
			}
			else {
				agent.x += delta_x;
				agent.y += delta_y;
			}
			updateMapModel();
		}
		else {
			endPathfinding();
		}
	}
	
	public void startAnimation(int agent_diameter, boolean animate) {
		agent = new Agent(0,0,agent_diameter);
		this.animate = animate;
		map_view.clearAgentPathList();
		timer.start();
		moveAgent();
	}
	
	public void startPathfinding(String algorithm, int agent_diameter, boolean animate) {
		
		map_view.clearAgentPathList();
		if(agent_diameter > start_diameter)
			return;
		
		if(algorithm.equals("PTHPA*")) {
			search_space_manager = new THPAStarPointAgentVertexSpaceManager(obstacle_list, width, height);
			this.current_algorithm = "PTHPA*";
		}
			
		else if(algorithm.equals("TA*")) {
			search_space_manager = new TriangulationSpaceManager(obstacle_list, width, height);
			this.current_algorithm = "TA*";
		}
		else if(algorithm.equals("A*")) {
			search_space_manager = new GridSpaceManager(obstacle_list, width, height);
			this.current_algorithm = "A*";
			
		}
		else if(algorithm.equals("HPA*")) {
			int cluster_width = 20,cluster_height = 20;
			search_space_manager = new HPAStarSpaceManager(obstacle_list, width, height, cluster_width, cluster_height);
			this.current_algorithm = "HPA*";
		}
		else if(subgoal_list == null) {
			this.current_algorithm = "NONE";
		}
		
		//final_update = true;
		updateMapModel();
		agent = new Agent(agent_diameter/2, agent_diameter/2, agent_diameter);
		subgoal_list = new ArrayList<Point>();
	
		path_updater = search_space_manager.getPath(start_point, goal_point);
		path_updater.startPathfinding();
		subgoal_list.add(start_point);
		
		
		startAnimation(agent_diameter, animate);
	}
	
	public void endPathfinding() {
		agent = null;
		subgoal_list = null;
		timer.stop();
		this.current_algorithm = "NONE";
		path_updater = null;
	}

	
	
	
	public String algorithmRunning(){return current_algorithm;}
	
	public void actionPerformed(ActionEvent e) {
		if(animate) {
			moveAgent();
		}
	}
}

import java.util.ArrayList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.PriorityQueue;
import java.awt.Point;
import java.util.HashMap;
public class MapModel implements ActionListener{
	boolean running = false;
	int milliseconds_per_frame = 20;
	Timer timer = new Timer(milliseconds_per_frame, this);
	
	int width, height, start_diameter, speed;
	ArrayList<Obstacle> obstacle_list = new ArrayList<Obstacle>();
	Agent agent;
	GridSpaceManager grid_manager;
	String current_algorithm = "NONE";
	ArrayList<Point> subgoal_list;
	MapView map_view;
	boolean animate = false;
	
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

			
			//double speed = Math.sqrt(width*height/10);
			Point p = subgoal_list.get(0);
			if(p.x == agent.x && p.y == agent.y) {
				if(subgoal_list.size() == 1) {
					animate = false;
					return;
				}
				subgoal_list.remove(0);
				p = subgoal_list.get(0);
			}
			
			double distance = Point.distance(agent.x, agent.y, p.x, p.y);
			double delta_x = (p.x - agent.x)/distance;
			double delta_y = (p.y - agent.y)/distance;
			double distance2 = Math.sqrt(delta_x*delta_x + delta_y+delta_y);
			if(distance2 > distance) {
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
		timer.start();
		moveAgent();
	}
	
	public void startPathfinding(String algorithm, int agent_diameter, boolean animate) {
		
		if(algorithm.equals("A*")) {
			startAStar(agent_diameter);
			this.current_algorithm = "A*";
		}
		if(subgoal_list == null) {
			this.current_algorithm = "NONE";
		}
		startAnimation(agent_diameter, animate);
	}
	
	public void endPathfinding() {
		agent = null;
		subgoal_list = null;
		timer.stop();
		this.current_algorithm = "NONE";
	}
	
	
	
	public void startAStar(int agent_diameter) {
		grid_manager = new GridSpaceManager(obstacle_list, width, height);
		if(agent_diameter > start_diameter) return;
		agent = new Agent(agent_diameter/2, agent_diameter/2, agent_diameter);
		SearchSpaceNode start = grid_manager.getNode(0, 0);
		SearchSpaceNode goal = grid_manager.getNode(width - 1, height - 1);
		subgoal_list = AStar(start_point, goal_point, start, goal, false);
	}
	
	public ArrayList<Point> AStar(Point start_point,Point goal_point, SearchSpaceNode start, SearchSpaceNode goal, boolean cluster) {
		//Point start_point = start.point_list[0];
		PriorityQueue<SearchSpaceNode> open_set = new PriorityQueue<SearchSpaceNode>();
		ArrayList<SearchSpaceNode> closed_set = new ArrayList<SearchSpaceNode>();
		
		HashMap<SearchSpaceNode, SearchSpaceNode> came_from = new HashMap<SearchSpaceNode, SearchSpaceNode>();
		HashMap<SearchSpaceNode, Integer> g_score = new HashMap<SearchSpaceNode, Integer>();
		HashMap<SearchSpaceNode, Integer> f_score = new HashMap<SearchSpaceNode, Integer>();
		
		int start_f_value = (int)manhattan_distance(start,goal);
		
		g_score.put(start, 0);
		f_score.put(start, start_f_value);
		
		start.f_value = start_f_value;
		open_set.add(start);

		while(open_set.size() > 0) {
			SearchSpaceNode current = open_set.poll();
			
			
			if(current == goal) {
				ArrayList<Point> subgoal_list = reconstructPath(came_from, current);
				subgoal_list.add(goal_point);
				return subgoal_list;
			}
			
			closed_set.add(current);
			ArrayList<SearchSpaceNode> neighbors = grid_manager.getNeighborsForNode(current, cluster);
			for(int i = 0; i < neighbors.size(); i++) {
				SearchSpaceNode neighbor = neighbors.get(i);
				if(closed_set.contains(neighbor))
					continue;
				
				int tentative_g_score = g_score.get(current) + 1;
				
				if(!open_set.contains(neighbor) || !g_score.containsKey(neighbor) 
						|| tentative_g_score < g_score.get(neighbor)) {
					int tentative_f_score = tentative_g_score + (int)manhattan_distance(neighbor, goal);
					
					came_from.put(neighbor, current);
					g_score.put(neighbor, tentative_g_score);
					f_score.put(neighbor, tentative_f_score);
					neighbor.f_value = tentative_f_score;
					if(!open_set.contains(neighbor))
						open_set.add(neighbor);
				}
					
			}
		}
		
		
		
		return null;
	}
	
	public ArrayList<Point> reconstructPath(
			HashMap<SearchSpaceNode,SearchSpaceNode> came_from,SearchSpaceNode current_node) {
		ArrayList<Point> subgoal_list = new ArrayList<Point>();
		
		
		while(came_from.containsKey(current_node)) {
			SearchSpaceNode temp = came_from.get(current_node); 
			subgoal_list.add(0, temp.point_list[0]);
			came_from.remove(current_node);
			current_node = temp;
		}
		return subgoal_list;
	}
	
	public double manhattan_distance(SearchSpaceNode start, SearchSpaceNode goal) {
		Point start_point = start.point_list[0];
		Point goal_point = goal.point_list[0];
		return Math.sqrt(Math.pow(goal_point.x - start_point.x,2) + Math.pow(goal_point.x - start_point.x,2));
		//return Math.abs(goal_point.x - start_point.x) + Math.abs(goal_point.y - start_point.y);
	}
	
	public String algorithmRunning(){return current_algorithm;}
	
	public void actionPerformed(ActionEvent e) {
		if(animate) {
			moveAgent();
		}
	}
}

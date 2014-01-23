import java.util.ArrayList;
import java.util.PriorityQueue;
import java.awt.Point;
import java.util.HashMap;
public class MapModel {
	int width, height, start_diameter, speed;
	ArrayList<Obstacle> obstacle_list = new ArrayList<Obstacle>();
	Agent agent;
	GridSpaceManager grid_manager;
	String current_algorithm = "NONE";
	ArrayList<Point> subgoal_list;
	MapView map_view;
	boolean animate = false;
	AnimationManager animation_manager = new AnimationManager();
	
	public MapModel(int size, MapView map_view)
	{
		this.width = size;
		this.height = size;
		this.map_view = map_view;
	}
	
	public void updateMapModel (){
		map_view.repaint();
	}
	//if obstacles can't fit then method returns
	public void createRandomObstacles(int num_obstacles, int start_diameter)
	{
		this.start_diameter = start_diameter;
		obstacle_list.clear();
		ArrayList<Point> point_list = new ArrayList<Point>();
		for(int i = 0; i < num_obstacles; i++) {
			int rand_x = (int)(Math.random()*(width-start_diameter))+start_diameter;
			int rand_y = (int)(Math.random()*(height-start_diameter))+start_diameter;
			point_list.add(new Point(rand_x, rand_y));
			/*
			double min_distance = Double.MAX_VALUE;
			Point p1 = point_list.get(i);
			for(int j = 0; j < point_list.size(); j++) {
				Point p2 = point_list.get(j);
				if(j != i && p1.distance(p2) < min_distance)
					min_distance = p1.distance(p2);
			}
			min_distance = Math.sqrt(min_distance*min_distance/2);
			int min_int = (int)(min_distance/2);
			if(min_int > 1) {
				int width = (int)(Math.random()*(min_int)); 
				width = Math.min(width, Math.max(p1.x, this.width - p1.x));
				int height = (int)(Math.random()*(min_int));
				height = Math.min(height, Math.max(p1.y, this.height - p1.y));
				if(p1.x + width > this.width - start_diameter && p1.y + height > this.height - start_diameter) {
					width = this.width - start_diameter - p1.x; 
					height = this.height - start_diameter - p1.y;
				}
				
				if(!(width < 0 && height < 0))
					obstacle_list.add(new Obstacle(p1.x, p1.y, width, height));
			}
			*/
		}
		
		
		for(int i = 0; i < num_obstacles; i++) {
			
			
			double min_distance = Double.MAX_VALUE;
			Point p1 = point_list.get(i);
			for(int j = 0; j < num_obstacles; j++) {
				Point p2 = point_list.get(j);
				if(j != i && p1.distance(p2) < min_distance)
					min_distance = p1.distance(p2);
			}
			min_distance = Math.sqrt(min_distance*min_distance/2);
			int min_int = (int)(min_distance/2);
			if(min_int > 1) {
				int width = (int)(Math.random()*(min_int)); 
				width = Math.min(width, Math.max(p1.x, this.width - p1.x));
				int height = (int)(Math.random()*(min_int));
				height = Math.min(height, Math.max(p1.y, this.height - p1.y));
				if(p1.x + width > this.width - start_diameter && p1.y + height > this.height - start_diameter) {
					width = this.width - start_diameter - p1.x; 
					height = this.height - start_diameter - p1.y;
				}
				
				if(!(width < 0 && height < 0))
					obstacle_list.add(new Obstacle(p1.x, p1.y, width, height));
			}
		}
		
		updateMapModel();
	}
	
	public void moveAgent() {
		if(animation_manager.running) {
			animation_manager.act();
			
			double speed = Math.sqrt(width*height/10);
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
			long sleep_time = animation_manager.timeTillNextFrame();
			if(sleep_time > 0)
				try {
					Thread.sleep(sleep_time);
				}
				catch(InterruptedException e) {
					
				}
			updateMapModel();
			moveAgent();
		}
		else {
			endPathfinding();
		}
	}
	
	public void startAnimation(int agent_diameter, boolean animate) {
		agent = new Agent(0,0,agent_diameter);
		this.animate = animate;
		
		animation_manager.start();
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
		animation_manager.end();
		this.current_algorithm = "NONE";
	}
	
	public void startAStar(int agent_diameter) {
		grid_manager = new GridSpaceManager(obstacle_list, width, height);
		if(agent_diameter > start_diameter) return;
		agent = new Agent(agent_diameter/2, agent_diameter/2, agent_diameter);
		subgoal_list = AStar(grid_manager.getNode(0, 0), grid_manager.getNode(width - 1, height - 1));
	}
	
	public ArrayList<Point> AStar(SearchSpaceNode start, SearchSpaceNode goal) {
		Point start_point = start.point_list[0];
		Point goal_point = goal.point_list[0];
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
			ArrayList<SearchSpaceNode> neighbors = current.getNeighbors();
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
}

class AnimationManager {
	boolean running = false;
	int frame_number = -1;
	int milliseconds_per_frame = 500;
	long start_time = -1;
	
	
	public void start() {
		running  = true;
		start_time = System.currentTimeMillis();
		frame_number = 0;
	}
	
	public void end() {
		running = false;
		start_time = frame_number = -1;
	}
	
	public void act() {
		frame_number++;
	}
	public long timeTillNextFrame() {
		return  (frame_number + 1)*milliseconds_per_frame - (System.currentTimeMillis() - start_time);
	}
}

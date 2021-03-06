//import java.awt.*;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;

import javax.swing.*;  //notice javax
public class MapWindowController extends JFrame implements ActionListener, ItemListener
{
	private static final int WIDTH = 600, HEIGHT = 800;
	private static final int SETTINGS_WIDTH = 600, SETTINGS_HEIGHT = 200;
	private static final int NUM_MAP_TYPE = 2;
	private static final int NUM_ALGORITHM_TYPE = 2;
	private static final int MAP_IMAGE_SIZE = 400, ACTUAL_MAP_SIZE = 100;
	 
	
	private JButton run;
	private JCheckBox[] map_type;
	private JCheckBox[] algorithm_type;
	JPanel panel = new JPanel();
	JPanel settings_panel = new JPanel();
	MapView map_view = new MapView(MAP_IMAGE_SIZE);
	SettingsButtonsPanel settings = new SettingsButtonsPanel();
	
	MapModel map_model = new MapModel(ACTUAL_MAP_SIZE, map_view);
	
	public MapWindowController()
	{
		
		setLocationRelativeTo(null);
		setTitle("HPA* Test");
		setSize(WIDTH, HEIGHT);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		add(panel);
		panel.setBounds(0, 0, 600, 800);
		panel.setLayout(null);
		/*GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;*/
		instantiateSettingsPanel();
		//panel.add(settings);
		/*c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;*/
		
		panel.add(map_view);
		map_view.setBounds(0,200,600,600);
		//button_panel.setBounds(0,0);
		map_view.repaint();
		
	}
	
	public void instantiateSettingsPanel() {
		settings_panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		//this.setLayout(null);
		
		map_type = new JCheckBox[NUM_MAP_TYPE];
		
		map_type[0] = new JCheckBox("Sparse");
		map_type[0].setSelected(true);
		
		map_type[1] = new JCheckBox("Maze");
		map_type[1].setSelected(false);
		
		algorithm_type = new JCheckBox[NUM_ALGORITHM_TYPE];
		
		algorithm_type[0] = new JCheckBox("A*");
		algorithm_type[0].setSelected(true);
		
		algorithm_type[1] = new JCheckBox("HPA*");
		algorithm_type[1].setSelected(false);

		settings_panel.setMaximumSize(new Dimension(600,200));
		for(int i = 0; i < NUM_MAP_TYPE; i++)
		{
			map_type[i].addItemListener(this);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = i+1;
			settings_panel.add(map_type[i], c);
		}
		
		for(int i = 0; i < NUM_MAP_TYPE; i++)
		{
			algorithm_type[i].addItemListener(this);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			c.gridy = i+1;
			settings_panel.add(algorithm_type[i], c);
		}
		
		run = new JButton("Run");
		run.addActionListener(this);
		settings_panel.add(run);
		panel.add(settings_panel);
		settings_panel.setBounds(0,0,SETTINGS_WIDTH,SETTINGS_HEIGHT);
	}

	public void itemStateChanged(ItemEvent e)
	{
		Object source = e.getItemSelectable();
		//if(source == )
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == run) {
			map_model.createRandomObstacles(100, 1);
			map_model.startPathfinding("PTHPA*", 0, true);
			map_view.setMapScale(ACTUAL_MAP_SIZE);
			map_view.setMapModel(map_model);
		}
	}
}
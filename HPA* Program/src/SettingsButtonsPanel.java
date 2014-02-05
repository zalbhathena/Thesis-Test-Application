import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
public class SettingsButtonsPanel extends JPanel implements ItemListener, ActionListener{
	private static final int WIDTH = 600, HEIGHT = 200;
	private static final int NUM_MAP_TYPE = 2;
	 
	
	private JButton run;
	private JCheckBox[] map_type;
	
	
	public SettingsButtonsPanel(){
		
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		this.setBounds(0,0,WIDTH, HEIGHT);
		//this.setLayout(null);
		
		map_type = new JCheckBox[NUM_MAP_TYPE];
		
		map_type[0] = new JCheckBox("Sparse");
		map_type[0].setSelected(true);
		
		map_type[1] = new JCheckBox("Maze");
		map_type[1].setSelected(false);

		this.setMaximumSize(new Dimension(600,200));
		for(int i = 0; i < NUM_MAP_TYPE; i++)
		{
			map_type[i].addItemListener(this);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = i+1;
			this.add(map_type[i], c);
		}
		
		run = new JButton("Run");
		this.add(run);
	}
	
	public void itemStateChanged(ItemEvent e)
	{
		Object source = e.getItemSelectable();
		//if(source == )
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == run) {
			
		}
	}
}

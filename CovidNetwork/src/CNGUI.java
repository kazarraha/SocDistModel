
	import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
	import java.awt.Graphics2D;
	import java.awt.GridLayout;
	import java.awt.RenderingHints;
	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
	import javax.swing.JFrame;
	import javax.swing.JPanel;
	import javax.swing.JTextField;

	/*
	 
		This project was written exclusively by Matthew J Young for research purposes.
		It is not intended for or designed for general use, but if you want to copy and adapt it
		for your own research, you may do so with proper credit/citation.
	 
		This simulates a population model with game theoretic agents with an SIS disease model
		Agents attempt to maximize their utility which increases with socialization
		but decreases with disease risk (which itself increases with socialization among more infected neighbors)


		The CNGUI class runs the main method and sets things up, and the display window.
		The CNSimulator class runs the actual disease and game theoretic components of the model model,
		remembering the population states and updating them.
		The ActionManager class contains static methods, each of which enacts a specific scenario of events,
		which usually end up generating a figure at the end.

		Almost all of the variables and methods are hard-coded, rather than having input fields when the code is run.
		The set of events I want to occur are written step-by-step into an ActionManager method, then that method is
		stuck into the ActionButton in MainGUI, and the old methods are commented out, so when the program is executed
		pressing the Action Button will do the useful thing.  If you wish to run things yourself, find the relevant section
		(search for "actionpaction"), set the corresponding boolean variable to TRUE and all others to FALSE,
		and then run the program and press the action button.  Specific methods may need to be commented/uncommented

		The paper based on this model is titled "Diversity in valuing social contact and 
		risk tolerance leading to the emergence of homophily in populations facing infectious threats"
		and was authored by Matthew J. Young, Matthew J. Silk, Alex J. Pritchard, and Nina H. Fefferman 
		and published in Physical Review E.
		It can be found at https://doi.org/10.1103/PhysRevE.105.044315
		
		
		A network version of this model can be accessed by running the NetworkGUI method.

	*/
	
	
	
	
	public class CNGUI extends JPanel implements MouseListener {
		//handles the main window and buttons
		
		
		//TODO: change box paint to be based on PseudoButtons to guarantee their line-up instead of
		//hardcoding both separately
		
		//TODO: adjust history storage to store turn 0 better

		
		//socialFriction is handled in Group class
		
		
    	//boolean displayFour = false;
    	boolean displayFour = true;
    	   	
    	
    	//boolean displayHistoryMatrix = true;
    	boolean displayHistoryMatrix = true;
		
		public JFrame simFrame;
		public CNGUI myself;
		public CNSimulator sim;
		
		public JPanel runSimPanel;
		public JButton runSimButton;
		public int runSimTimes;
		public JTextField runSimTimesField;
		
		public JPanel resetPanel;
		public JButton resetButton;
		
		public JPanel actionPanel;
		public JButton actionButton;
		
		public PseudoButton[] pseudoButtons;
		public int psyIndex = -1;
		
		
		
		public boolean networkMode = true;

		
	public static void main(String[] args){  //main
			
		
			CNGUI mg = new CNGUI();
			
			
			 JFrame frame = new JFrame("Main Simulation");
		        frame.add(mg);
		        frame.setSize(1400, 1000);
		        frame.setVisible(true);
		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		        mg.simFrame = frame;
		        
			
		}

		public CNGUI() {
			this(false);
		}

		public CNGUI(boolean nm) {
			networkMode = nm;
			myself = this;
			addMouseListener(this);
			
			
			if(networkMode) sim = new NetworkSimulator(0);
			else sim = new CNSimulator();
			
			makePseudoButtons();

			
			runSimPanel = new JPanel(new GridLayout(2,1));
			runSimButton = new JButton("Run Simulation");
			runSimButton.addActionListener(new ActionListener()
			{
			  public void actionPerformed(ActionEvent e)
			  {
				for(int i = 0; i < runSimTimes; i++){	
					
					sim.simDay();
					
				}
				repaint();
				//refreshReportingPanel();
			  }
			});
				
			runSimTimes = 1;
			runSimTimesField = new JTextField("1");
			runSimTimesField.addActionListener(new ActionListener()
			{
			  public void actionPerformed(ActionEvent e)
			  {
			    runSimTimes = Integer.parseInt(runSimTimesField.getText());
			  }
			});	
			runSimPanel.add(runSimButton);
			runSimPanel.add(runSimTimesField);
			this.add(runSimPanel);
			//end runSim structure
			
			
			
			
			resetPanel = new JPanel(new GridLayout(1,1));
			resetButton = new JButton("Reset");
			resetButton.addActionListener(new ActionListener()
			{
			  public void actionPerformed(ActionEvent e)
			  {
				if(networkMode) sim = new NetworkSimulator();
				else sim = new CNSimulator();
				repaint();
			  }
			});
				
			resetPanel.add(resetButton);
			this.add(resetPanel);

			
			//TODO actionpaction
			actionPanel = new JPanel(new GridLayout(1,1));
			actionButton = new JButton("Action");
			actionButton.addActionListener(new ActionListener()
			{
			  public void actionPerformed(ActionEvent e)
			  {
				//insert actionbutton stuff here			
				//ActionManager.graphPublicGoods();
				//ActionManager.graphHomophily();
				ActionManager.actionPaction();
				  
				repaint();
			  }
			});
				
			actionPanel.add(actionButton);
			this.add(actionPanel);

			
		}
		
		
	    public void mouseClicked(MouseEvent e) {
	        int x = e.getX();
	        int y = e.getY();
	        psyIndex = clickPseudoButton(x,y);
	        repaint();
	        
	     }
	    
	    public int clickPseudoButton(int x, int y) {
	    	int psy = -1;
	    	for(int i = 0; i < pseudoButtons.length; i++) {
	    		if(pseudoButtons[i]==null) break;
	    		if(pseudoButtons[i].inside(x, y)) psy = i;
	    	}
	    	return psy;
	    }
		
		
		
		
		public void makePseudoButtons() {

			pseudoButtons = new PseudoButton[8];
			int popID = 0;
			double avgInf = sim.getAverageInf();
			for(int soc = 0; soc <= 1; soc++) {
				for(int conf = 0; conf <= 1; conf++) {
					int y = 110 + 110 * conf;
					for(int health = 0; health <= 1; health++) {
						int x = 10 + 400 * soc;
						x += 110 * health;
						pseudoButtons[popID] = new PseudoButton(x,y,x+103,y+103);
						popID++;
					}
				}
			}
		}
		
		public void paint(Graphics g) {

			
		    this.getRootPane().setBackground(Color.WHITE);
	        super.paint(g);
	        Graphics2D g2d = (Graphics2D) g;
	        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        
	        boolean statusPaint = true;
	        int xLoc = 0;
	        int yLoc = 100;
	        
        	int l = 900; int h = 350;
	        
	        if(statusPaint && !networkMode) paintStatus(g2d, xLoc, yLoc);
	        
	        if(networkMode);// paintAgentStatus(g2d, xLoc+20, yLoc+250);
	        else if(!displayHistoryMatrix){
	        	if(!displayFour) {
	        		if(psyIndex >=0 && psyIndex < sim.population.length) {
	        			paintHistoryGraph(g2d, xLoc+100, yLoc+250, psyIndex,l,h, displayFour, false);
	        			paintHistoryGraph(g2d, xLoc+160+l/2, yLoc+250, psyIndex,l/2,h, displayFour, true);
	        			
	        		}
	        	}
	        	if(displayFour && sim.population.length > 4) {
	        		for(int i = 0; i < 2; i++) {
	        			for(int j = 0; j < 2; j++) {
	        				int index = i+4*j;
	        				paintHistoryGraph(g2d, xLoc+100, yLoc+250, index,l/2,h, displayFour, false);
	        				paintHistoryGraph(g2d, xLoc+160+l/2, yLoc+250, index,l/2,h, displayFour, true);
	        			}
	        		}
	        	}
	        	if(displayFour && sim.population.length == 4) {
	        		for(int index = 0; index < 4; index++) paintHistoryGraph(g2d, xLoc+100, yLoc+250, index,l/2,h, displayFour, false);
	        		for(int index = 0; index < 4; index++) paintHistoryGraph(g2d, xLoc+160+l/2, yLoc+250, index,l/2,h, displayFour, true);
	        	}
	        	//change to allow 4 side-by-side
	        	else paintMatrix(g2d, xLoc+20, yLoc+250);
	        }
	        else paintHistoryMatrix(g2d, xLoc+60,yLoc+250,900,350);
	
		}
		
		public void paintStatus(Graphics2D g2d, int xLoc, int yLoc) {
			int width = 1600;
			int height = 1000;
			
			g2d.setColor(Color.white);
			g2d.fillRect(xLoc, yLoc, width, height);
			
			int popID = 0;
			double avgInf = sim.getAverageInf();
			for(int soc = 0; soc <= 1; soc++) {
				int maxConf = 1; if(!sim.includeConf) maxConf = 0;
				for(int conf = 0; conf <= maxConf; conf++) {
					int y = yLoc+10 + 110 * conf;
					for(int health = 0; health <= 1; health++) {
						int x = xLoc+10 + 400 * soc;
						x += 110 * health;
						g2d.setColor(Color.black);
						g2d.drawRect(x,y,102,102);
						//labels
						int xl = x+10;  int yl = y+10;
						if(soc == 1) g2d.drawString("s",xl,yl);  xl+= 8;
						if(conf == 1) g2d.drawString("c",xl,yl);  xl+= 8;
						if(health == 1) g2d.drawString("h",xl,yl);  xl+= 8;
						xl+= 20;  g2d.drawString(""+popID, xl, yl);
						
						//infection
						int fill = (int)(100 * sim.population[popID].infRate);
						g2d.setColor(Color.green);
						g2d.fillRect(x+1, y+101-fill, 100, fill);
						
						//desired soc
						//double ds = sim.population[popID].getDesiredSoc(avgInf);
						double ds = sim.getAverageInt(popID);
						//double cap = 2;
						double cap = 5;
						int bFill = (int)(ds * 100 / cap);
						g2d.setColor(Color.blue);
						g2d.fillRect(x+70, y+101-bFill, 10, bFill);
						
						popID++;
					}
				}
			}
			
			g2d.setColor(Color.black);
			g2d.drawString("Time: " + sim.numDays,xLoc+width-100,yLoc+20);
			
			
		}
		

		public void paintMatrix(Graphics2D g2d, int xLoc, int yLoc) {
			int width = 400;
			int height = 400;
			
			DecimalFormat df = new DecimalFormat("#.000");			
			g2d.setColor(Color.black);
			for(int i = 0; i < sim.population.length; i++) {
				int y = yLoc+i*height/sim.population.length;
				g2d.drawLine(xLoc, y, xLoc+width, y);
				for(int j = 0; j < sim.population.length; j++) {
					int x = xLoc+j*width/sim.population.length;
					if(i==0) g2d.drawLine(x, yLoc, x, yLoc+height);
					int yMargin = 25;
					String s = df.format(sim.socialDesires[i][j]);
					if(sim.socialDesires[i][j] == sim.socialDesires[j][i] + sim.population[i].incSoc) s = df.format(sim.socialDesires[j][i]);
					else if(sim.socialDesires[i][j] > sim.socialDesires[j][i]) s = df.format(sim.socialDesires[j][i]) + "+";			
					g2d.drawString(s, x+4, y+yMargin);
					
					
				}
			}
			g2d.drawLine(xLoc+width, yLoc, xLoc+width, yLoc+height);
			g2d.drawLine(xLoc, yLoc+height, xLoc+width, yLoc+height);
			
			
			
		}
		
		
		public void paintAgentStatus(Graphics2D g2d, int xLoc, int yLoc) {
			int width = 1000;
			int height = 1000;
			NetworkSimulator nSim = (NetworkSimulator)sim;
			
			//
			DecimalFormat df = new DecimalFormat("#.000");
			//
			
			g2d.setColor(Color.white);
			g2d.fillRect(xLoc, yLoc, width, height);
			
			int w = 50; int h = 50;  int s = 10;
			int x = xLoc; int y = yLoc;
			for(int i = 0; i < nSim.agents.length; i++) {
				if(nSim.agents[i].infected) {
					g2d.setColor(Color.green);
					g2d.fillRect(x, y, w, h);
				}
				g2d.setColor(Color.black);
				g2d.drawRect(x, y, w, h);
				g2d.drawString(""+i, x, y-5);
				g2d.drawString(""+nSim.agents[i].groupIndex, x+2, y+10);
				
				g2d.drawString(df.format(nSim.agents[i].rememberSoc), x+2, y+40);
				
				x+=w+s;
			}

		}

		
		public void paintHistoryGraph(Graphics2D g2d, int xLoc, int yLoc, int index, int length, int height, boolean displayFour, boolean dispInf) {	
			//System.out.println("paintHistoryGraph");
			
			//Color[] fourColors = {Color.blue, Color.red, Color.black, Color.black, Color.cyan, Color.magenta, Color.black, Color.black}; 
			Color[] fourColors = getFourColors();
			
			System.out.println("pHG popSize: " + sim.population.length);
			
			
			Font smallFont = g2d.getFont();
			Font largeFont = smallFont.deriveFont(smallFont.getSize() * 2F);
			g2d.setFont(largeFont);
			
			if(index < 0 || index >= sim.population.length) return; //change later for network version
			double[] yInfBounds = {0,0.5,1};  double yInfMax = yInfBounds[yInfBounds.length-1]; double yInfMin = yInfBounds[0];
			double[] ySocBounds = {0,3,6}; double ySocMax = ySocBounds[ySocBounds.length-1]; double ySocMin = ySocBounds[0];
			//double[] xBounds = {0,1000,2000}; double xMax = xBounds[xBounds.length-1]; double xMin = xBounds[0];
			double[] xBounds = {0,600,1200}; double xMax = xBounds[xBounds.length-1]; double xMin = xBounds[0];
			int timeMax = (int)xMax;
			int numT = 1000;
			int tInc = (int)((xMax-xMin)/numT);
			int timeSpan = Math.min(timeMax, sim.numDays);
			//axes
			int ticLength = 3;
			DecimalFormat df = new DecimalFormat("#.00");		
			g2d.setColor(Color.black);
			//g2d.drawLine(xLoc, yLoc+height, xLoc+length, yLoc+height);
			ActionManager.drawThickLine(g2d, xLoc, yLoc+height, xLoc+length, yLoc+height);
			for(int i = 0; i < xBounds.length; i++) {
				int xPic = CNGraphHandler.convertValueToPixel(xLoc, length, xMin, xMax, xBounds[i]);
				//g2d.drawLine(xPic, yLoc+height, xPic, yLoc+height-ticLength);
				ActionManager.drawThickLine(g2d, xPic, yLoc+height, xPic, yLoc+height-ticLength);
				g2d.drawString(""+(int)xBounds[i], xPic-30, yLoc+height+30);
			}
			//g2d.drawLine(xLoc, yLoc,  xLoc, yLoc+height);	
			ActionManager.drawThickLine(g2d, xLoc, yLoc,  xLoc, yLoc+height);
			for(int i = 0; i < yInfBounds.length; i++) {
				int yInfPic = CNGraphHandler.convertValueToPixel(yLoc, height, yInfMax, yInfMin, yInfBounds[i]);
				int ySocPic = CNGraphHandler.convertValueToPixel(yLoc, height, ySocMax, ySocMin, ySocBounds[i]);
				g2d.setColor(Color.black);
				//g2d.drawLine(xLoc, yInfPic, xLoc+ticLength, yInfPic);
				ActionManager.drawThickLine(g2d, xLoc, yInfPic, xLoc+ticLength, yInfPic);
				if(!displayFour) {
				g2d.setColor(Color.green);
				g2d.drawString(df.format(yInfBounds[i]), xLoc+length+5, yInfPic-10);
				}
				g2d.setColor(Color.black);
				if(!dispInf)g2d.drawString(""+(int)ySocBounds[i], xLoc-50, ySocPic+10);
				else g2d.drawString(""+df.format(yInfBounds[i]), xLoc-50, ySocPic+10);
			}
			
			//graphs
			for(int t = tInc; t < timeSpan; t+=tInc) {
				int oldX = CNGraphHandler.convertValueToPixel(xLoc, length, xMin, xMax, t-tInc);
				int newX = CNGraphHandler.convertValueToPixel(xLoc, length, xMin, xMax, t);
				int oldInfY = CNGraphHandler.convertValueToPixel(yLoc, height, yInfMax, yInfMin, sim.population[index].infHistory.get(t-tInc));
				int newInfY = CNGraphHandler.convertValueToPixel(yLoc, height, yInfMax, yInfMin, sim.population[index].infHistory.get(t));
				if(dispInf) { //inf
					if(displayFour) g2d.setColor(fourColors[index]);
					else g2d.setColor(Color.green);
					ActionManager.drawThickLine(g2d, oldX, oldInfY, newX, newInfY);
				}
				else { //soc
					int oldSocY = CNGraphHandler.convertValueToPixel(yLoc, height, ySocMax, ySocMin, sim.population[index].socHistory.get(t-tInc));
					int newSocY = CNGraphHandler.convertValueToPixel(yLoc, height, ySocMax, ySocMin, sim.population[index].socHistory.get(t));
					if(displayFour) g2d.setColor(fourColors[index]);
					else g2d.setColor(Color.blue);
					ActionManager.drawThickLine(g2d, oldX, oldSocY, newX, newSocY);
				}
			}
			
			
			g2d.setFont(smallFont);
			
			
			
		}
		
		public void paintHistoryMatrix(Graphics2D g2d, int xLoc, int yLoc, int length, int height) {
			boolean includeInf = false;
			boolean paintHomScores = true;
			int[] indices = {0,1,4,5};
			if(!sim.includeConf) {indices[2] = 2; indices[3]=3;}
			int n = indices.length;
			for(int i = 0; i < n; i++) {
				for(int j = 0; j < n; j++) {
					int yLocal = yLoc+i*(30+height/n);
					int xLocal = xLoc+j*(30+length/n);
					boolean xLab = false; if(i==n-1) xLab = true; //label axes only on the edges of the matrix
					boolean yLab = false; if(j == 0) yLab = true;
					CNGraphHandler.paintFillGraph(g2d, xLocal, yLocal, sim.sjHistory[indices[i]][indices[j]], length/n, height/n, xLab, yLab);
					if(includeInf)CNGraphHandler.paintLineGraph(g2d, xLocal, yLocal, sim.population[i].infHistory, length/n, height/n);
				}
				if(paintHomScores) {
					double homScore = sim.getOneHomophily(i);
					//g2d.drawString(""+homScore,xLoc+200+5*(30+length/n),yLoc+i*(30+height/n));
					g2d.drawString("hom: "+homScore,1200,400+i*(30+height/n));
				}
			}		
			

			
			
		}

		
		
		
		

		@Override
		public void mousePressed(MouseEvent e) {
			
		}



		@Override
		public void mouseEntered(MouseEvent e) {
			
		}



		@Override
		public void mouseExited(MouseEvent e) {
			
		}



		@Override
		public void mouseReleased(MouseEvent e) {
		}
	

		public static Color[] getFourColors() {
			Color[] fourColors = new Color[4];
			fourColors[0] = Color.red;
			fourColors[1] = Color.blue;
			//fourColors[2] = Color.green;
			fourColors[2] = new Color(255,160,0);
			fourColors[3] = new Color(127,0,255);
			
			return fourColors;
		}
	
}

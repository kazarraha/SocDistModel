import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;


/*
This project was written exclusively by Matthew J Young for research purposes.
It is not intended for or designed for general use, but if you want to copy and adapt it
for your own research, you may do so with proper credit/citation.

This simulates a population model with game theoretic agents in a network with an SIS disease model
Agents attempt to maximize their utility which increases with socialization
but decreases with disease risk (which itself increases with socialization among more infected neighbors)


The NetworkGUI class runs the main method and sets things up, and the display window.
The NetworkSimulator class runs the actual disease and game theoretic components of the model model,
remembering the population states and updating them.
The ActionManager class contains static methods, each of which enacts a specific scenario of events,
which usually end up generating a figure at the end.

Classes beginning with CN are from the earlier version of this model which models a
continuous well-mixed population rather than a network structure, and can be used through the CNGUI method.


Almost all of the variables and methods are hard-coded, rather than having input fields when the code is run.
The set of events I want to occur are written step-by-step into an ActionManager method, then that method is
stuck into the ActionButton in MainGUI, and the old methods are commented out, so when the program is executed
pressing the Action Button will do the useful thing.  If you wish to run things yourself, find the relevant section
(search for "actionpaction"), set the corresponding boolean variable to TRUE and all others to FALSE,
and then run the program and press the action button.  Specific methods may need to be commented/uncommented


*/




public class NetworkGUI extends CNGUI {


	public int[][] spots;
	
	//for painting and spots and PseudoButton
    int pxLoc = 50;
    int pyLoc = 100;
	int pLength = 600; 
	int pHeight = 600;
	
	boolean communityMode = true;
	
	
	
	public static void main(String[] args){  //main
		
		
		NetworkGUI ng = new NetworkGUI();
		ng.networkMode = true;
		
		
		 JFrame frame = new JFrame("Main Simulation");
	        frame.add(ng);
	        frame.setSize(1400, 1000);
	        frame.setVisible(true);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        ng.simFrame = frame;
	        
		
	}

	
	
	
	
	
	public NetworkGUI() {
		super(true);
		if(!communityMode) {
		int n = (int)Math.sqrt(((NetworkSimulator)(sim)).agents.length/4);
		//TODO: change this to not require all population sizes to be 4k^2
		generateSpots(pyLoc, pxLoc, pLength,pHeight,n,n,2,2);
		}
		else {
			int n = (int)((NetworkSimulator)(sim)).communities[0].length/2;
			generateCommunitySpots(pyLoc, pxLoc, pLength,pHeight,2,n,1,1);
		}
	}
	
	
	
	
	
	public void generateSpots(int yLoc, int xLoc, int height, int length, int nx, int ny, int gx, int gy) {
		
		spots = new int[((NetworkSimulator)sim).agents.length][4];
		pseudoButtons = new PseudoButton[spots.length];
				
				
		int smallGap = 2; int largeGap = 10;
		//int sumYGap = (ny-1)*smallGap + (gy-1)*largeGap; int sumXGap = (nx-1)*smallGap + (gx-1)*largeGap;
		int largeY = (height+largeGap)/gy; int largeX = (length+largeGap)/gx;
		int smallY = (largeY-largeGap+smallGap)/ny; int smallX = (largeX-largeGap+smallGap)/ny;
		
		
		int index = 0;
		for(int yy = 0; yy < gy; yy++) {
			for(int xx = 0; xx < gx; xx++) {
				for(int y = 0; y < ny; y++) {
					for(int x = 0; x < nx; x++) {
						int yPic = yLoc + largeY*yy + smallY*y;
						int xPic = xLoc + largeX*xx + smallX*x;
						
						pseudoButtons[index] = new PseudoButton(xPic, yPic, xPic+smallX-smallGap, yPic+smallY-smallGap);
						spots[index][0]= xPic; spots[index][1]= yPic;
						spots[index][2] = xPic+smallX-smallGap; spots[index][3] = yPic+smallY-smallGap;
						
						
						index++;
					}
				}
			}
		}
		
	}
	
	public void generateCommunitySpots(int yLoc, int xLoc, int height, int length, int nx, int ny, int gx, int gy) {
		
		int numCommunities = ((NetworkSimulator)sim).communities.length;
		int comSize = ((NetworkSimulator)sim).communities[0].length;
		spots = new int[numCommunities+comSize][4];
		pseudoButtons = new PseudoButton[spots.length];
				
				
		int smallGap = 2; int largeGap = 10;
		//int sumYGap = (ny-1)*smallGap + (gy-1)*largeGap; int sumXGap = (nx-1)*smallGap + (gx-1)*largeGap;
		int largeY = (height/2+largeGap)/gy; int largeX = (length/2+largeGap)/gx;
		int smallY = (largeY-largeGap+smallGap)/ny; int smallX = (largeX-largeGap+smallGap)/ny;
		
		nx = 4;
		ny = 4;
		
		int index = 0;
		double angleChange = 2*Math.PI/numCommunities;
		double rad = height/2;
		//circle
		//communities
			for(int i = 0; i < numCommunities; i++) {
				//int yPic = yLoc + largeY*0 + smallY*y;
				double angle = angleChange*index;
				int yPic = (int) (yLoc+height/2-rad*Math.sin(angle));
				int xPic = (int) (xLoc+length/2+rad*Math.cos(angle));
				//int xPic = xLoc + largeX*0 + smallX*x;
						
				pseudoButtons[index] = new PseudoButton(xPic, yPic, xPic+50, yPic+50);
				spots[index][0]= xPic; spots[index][1]= yPic;
				spots[index][2] = xPic+50; spots[index][3] = yPic+50;						
				index++;
			}
		//zoomed in to one community
		angleChange = 2*Math.PI/comSize;
		rad = height/4;
		for(int i = 0; i < comSize; i++) {
			//int yPic = yLoc + largeY*0 + smallY*y;
			double angle = angleChange*index;
			int yPic = (int) (yLoc+height/2-rad*Math.sin(angle));
			int xPic = (int) (xLoc+length/2+rad*Math.cos(angle));
			//int xPic = xLoc + largeX*0 + smallX*x;
			pseudoButtons[index] = new PseudoButton(xPic, yPic, xPic+50, yPic+50);
			spots[index][0]= xPic; spots[index][1]= yPic;
			spots[index][2] = xPic+50; spots[index][3] = yPic+50;					
			index++;
		}
		
	}
	
	
	public void paint(Graphics g) {
		
	    this.getRootPane().setBackground(Color.WHITE);
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        
    	if(!communityMode)  paintSquareNet(g2d, pyLoc, pxLoc, pLength, pHeight);
    	else paintCommunityGraph(g2d, pyLoc, pxLoc, pLength, pHeight);
        
        
	}
	
	
	public void paintSquareNet(Graphics2D g2d, int xLoc, int yLoc, int length, int height) {
		
		NetworkSimulator nSim = (NetworkSimulator)sim;
		DecimalFormat df = new DecimalFormat("#.000");
		
		
		
		g2d.setColor(Color.white);
		g2d.fillRect(yLoc, xLoc, length, height);
		for(int i = 0; i < spots.length; i++) {
			int[] s = spots[i];
			if(nSim.agents[i].infected) {
				g2d.setColor(Color.green);
				g2d.fillRect(s[0],s[1],s[2]-s[0],s[3]-s[1]);
			}
			g2d.setColor(Color.black);
			g2d.drawRect(s[0],s[1],s[2]-s[0],s[3]-s[1]);
			
			g2d.drawString(df.format(nSim.agents[i].rememberSoc), s[0]+2, (s[1]+s[3])/2);
		}
		
		
	}
	
	public void paintCommunityGraph(Graphics2D g2d, int xLoc, int yLoc, int length, int height) {	
		int selected = 0;
		
		NetworkSimulator nSim = (NetworkSimulator)sim;
		DecimalFormat df = new DecimalFormat("#.000");
		
		
		
		g2d.setColor(Color.white);
		g2d.fillRect(yLoc-10, xLoc-10, length*2, height*2);
		
		g2d.setColor(Color.black);
		for(int i = 0; i < nSim.communities.length; i++) {
			int[] s = spots[i];
			if(i == selected) g2d.setColor(Color.red);
			g2d.drawRect(s[0],s[1],s[2]-s[0],s[3]-s[1]);
			double totHeight = s[3]-s[1]-2;
			int infHeight = (int) (totHeight*nSim.communityInfectRate(i));
			g2d.setColor(Color.green);
			g2d.fillRect(s[2]-6, s[3]-infHeight-1, 5, infHeight);
			
			
			g2d.setColor(Color.black);
			g2d.drawString(""+ i, s[0]+2, (s[1]+s[3])/2);
		}
		for(int i = 0; i < nSim.communities[selected].length; i++) {
		//for(int i = 0; i < 0; i++) {
			int[] s = spots[i+nSim.communities.length];
			if(nSim.communities[selected][i].infected) {
				g2d.setColor(Color.green);
				g2d.fillRect(s[0],s[1],s[2]-s[0],s[3]-s[1]);
			}
			g2d.setColor(Color.black);
			g2d.drawRect(s[0],s[1],s[2]-s[0],s[3]-s[1]);
			
			g2d.drawString(df.format(nSim.communities[selected][i].rememberSoc), s[0]+2, (s[1]+s[3])/2);
		}
		
		//connections
		double avgSoc = nSim.avgSocPair();
		double infThresh = 0.001;
		g2d.setColor(Color.black);
		Color lightBlue = new Color(120,120,255);
		for(int i = 0; i < nSim.communities[selected].length; i++) {
			int indexOne = nSim.communities.length+i;
			int xOne = (spots[indexOne][0]+spots[indexOne][2])/2;
			int yOne = (spots[indexOne][1]+spots[indexOne][3])/2;
			for(int j = i+1; j < nSim.communities[selected].length; j++) {
				int indexTwo = nSim.communities.length+j;
				int xTwo = (spots[indexTwo][0]+spots[indexTwo][2])/2;
				int yTwo = (spots[indexTwo][1]+spots[indexTwo][3])/2;
				if(nSim.connections[i][j] > 0) {
					g2d.setColor(Color.black);
					g2d.drawLine(xOne, yOne, xTwo, yTwo); 
					double s = Math.min(nSim.socialDesires[i][j], nSim.socialDesires[j][i]);
					if(s > avgSoc/5) {
						g2d.setColor(lightBlue);
						if(s > 6*avgSoc/5) g2d.setColor(Color.blue);
						g2d.drawLine(xOne+1, yOne+1, xTwo+1, yTwo+1);
					}
					//if(nSim.rememberInfSendCheck) {
					//	s = Math.max(nSim.rememberInfSend[i][j], nSim.rememberInfSend[j][i]);
					//	if(s > infThresh) {
					//		int g = Math.max(255, (int)(infThresh*50));
					//		g2d.setColor(new Color(255-g,255,255-g));
					//		g2d.drawLine(xOne-1, yOne-1, xTwo-1, yTwo-2);
					//	}
					//}
				}
				//if(nSim.connections[i][j] > 0) ActionManager.drawDottedLine(g2d, xOne, yOne, xTwo, yTwo, 5, 5);
			}
			//find index of agent a in sim.agent[]
			//invoke agentToComCon(int a) to find connections to communities
			g2d.setColor(Color.black);
			int a = java.util.Arrays.asList(nSim.agents).indexOf(nSim.communities[selected][i]);
			int[] comCon = nSim.agentToComCon(a);
			for(int j = 0; j < comCon.length; j++) {
				if(comCon[j] > 0 && j != selected) {
					int indexTwo = j;
					int xTwo = (spots[indexTwo][0]+spots[indexTwo][2])/2;
					int yTwo = (spots[indexTwo][1]+spots[indexTwo][3])/2;
					ActionManager.drawDottedLine(g2d, xOne, yOne,xTwo,yTwo, 5,5);
				}
			}
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class CNGraphHandler extends JPanel {

	
	
	
	public JFrame window;
	public double[] graphList;
	public String label;
	
	public ArrayList<double[]> extraLines;
	//public boolean logScaleX = false;

	//double[][] multipleGraphList;
	//boolean multiple = false;
	
	//stuff to externally size the graph
	//boolean setBoundsBoolean = false;
	//int saveHeight;
	//int saveLength;
	//double saveMaxX;
	//double saveMaxY;
	//Color[] preSetColors;
	
	//int squishValue = 1;
	
	
	
	
	
	
	
	public CNGraphHandler(double[] gL, String lab){

		window = new JFrame("sub window");
        window.add(this);
        window.setSize(1400, 1000);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        graphList = gL;
        label = lab;
        
        extraLines = new ArrayList<double[]>();
        repaint();
	}
	
	public void addExtraLine(double[] line) {
		extraLines.add(line);
	}
	

	
	public void paint(Graphics g) {
		 Graphics2D g2d = (Graphics2D)g;
		 int top = 100;
		 int left = 100;
		 int height = 800;
		 int length = 1001;
		 
		 
		 boolean whitebackground = true;
		 if(whitebackground){ g2d.setColor(Color.white); g2d.fillRect(0,0,2*left+length,2*top+height); }
		 
		 //find bounds
		 double yMax = -10000;
		 for(int i = 0; i < graphList.length; i++) if(graphList[i] > yMax) yMax = graphList[i];
		 
		 double yInc = (double)height / yMax; //number of pixels per point in the y direction
		 //if(zoom) yInc = 400;
		 int xInc;
		 
		 xInc = (int) ((double)length / (graphList.length + 1));
		 

		 
		 g2d.setColor(Color.black);
		 DecimalFormat df = new DecimalFormat("#.000");
		 
		 g2d.drawString(label, left+length/2,top/2);
		 g2d.drawString(df.format(yMax), left-50, top);
		 
		 g2d.drawLine(left,top,left, top+height); //vertical line at left
		 g2d.drawLine(left,top+height, left+length, top+height); //horizontal line at bottom
		 
		 int xTicInterval = 5 * xInc; //change value here to change tick labels
		 int yTicInterval = (int)(yInc);
		 if(yInc > height) yTicInterval = (int)(yInc/100);
		 int ticLength = 2;
		 if(xTicInterval<=0) System.out.println("xTicInterval <= 0 in CNGraphHandler.paint");
		 for(int xTicSpot = left; xTicSpot <= left+length; xTicSpot += xTicInterval){
			 g2d.drawLine(xTicSpot, top + height, xTicSpot, top + height - ticLength);
		 }
		 

		 for(int yTicSpot = top+height; yTicSpot >= top; yTicSpot -= yTicInterval){
			 g2d.drawLine(left, yTicSpot, left + ticLength, yTicSpot);
			 
		 }

		 for(int i = 0; i < graphList.length; i++){
			 int yPosition = (int) (top + height - yInc*graphList[i]);  
			 int xPosition;
			 xPosition = left + xInc*i;;
			 if(i > 0){ //connector line
				 int oldY = (int) (top + height - yInc*graphList[i-1]);  
				 int oldX;
				 oldX = left + (int)(xInc*(i-1));
				 g2d.drawLine(oldX, oldY, xPosition, yPosition);
			 }
		 }
		 
		 g2d.setColor(Color.blue);
		 for(int i = 0; i < extraLines.size(); i++) {
			 double[] line = extraLines.get(i);
			 int yPosition = (int) (top + height - yInc*line[3]);  
			 int xPosition;
			 xPosition = left + (int)(xInc *line[2]);
			 int oldY = (int) (top + height - yInc*line[1]);  
			 int oldX;
			 oldX = left + (int)(xInc * line[0]);
		 	 g2d.drawLine(oldX, oldY, xPosition, yPosition);
			
		 }
		 
		 
		 
		 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void paintFillGraph(Graphics2D g2d, int xLoc, int yLoc, ArrayList<Double> data, int length, int height, boolean xLab, boolean yLab) {
		//TODO, change more parameters to be passed in to make this general
		
		double[] ySocBounds = {0,2,4}; double ySocMax = ySocBounds[ySocBounds.length-1]; double ySocMin = ySocBounds[0];
		//double[] ySocBounds = {0,4,8}; double ySocMax = ySocBounds[ySocBounds.length-1]; double ySocMin = ySocBounds[0];
		double[] xBounds = {0,1250,2500}; double xMax = xBounds[xBounds.length-1]; double xMin = xBounds[0];
		int timeMax = (int)xMax;
		int numT = 1000;
		int tInc = (int)((xMax-xMin)/numT);
		int timeSpan = Math.min(timeMax, data.size());

		
		
		//graphs
		for(int t = tInc; t < timeSpan; t+=tInc) {
			int oldX = convertValueToPixel(xLoc, length, xMin, xMax, t-tInc);
			int newX = convertValueToPixel(xLoc, length, xMin, xMax, t);
			
			int oldSocY = convertValueToPixel(yLoc, height, ySocMax, ySocMin, data.get(t-tInc));
			int newSocY = convertValueToPixel(yLoc, height, ySocMax, ySocMin, data.get(t));
			if(oldSocY < yLoc) oldSocY = yLoc;  if(newSocY < yLoc) newSocY = yLoc; 
			//if(displayFour) g2d.setColor(fourColors[index]);
			int yBot = yLoc+height;
			
			int[] xPoly = {oldX, oldX, newX, newX};
			int[] yPoly = {yBot, oldSocY, newSocY, yBot};
			Polygon p = new Polygon(xPoly, yPoly, 4);
			
			g2d.setColor(Color.blue);
			g2d.fillPolygon(p);
		}
		
		//axes
		int ticLength = 3;
		DecimalFormat df = new DecimalFormat("#.00");		
		g2d.setColor(Color.black);
		g2d.drawLine(xLoc, yLoc+height, xLoc+length, yLoc+height);
		g2d.drawLine(xLoc, yLoc, xLoc+length, yLoc);
		for(int i = 0; i < xBounds.length; i++) {
			int xPic = convertValueToPixel(xLoc, length, xMin, xMax, xBounds[i]);
			g2d.drawLine(xPic, yLoc+height, xPic, yLoc+height-ticLength);
			//if(xLab) g2d.drawString(Integer.toString((int)xBounds[i]), xPic-10*i, yLoc+height+12);
			if(xLab) drawLargeFont(g2d, Integer.toString((int)xBounds[i]), xPic-10*i, yLoc+height+20);
			
			//g2d.drawString(""+xBounds[i], xPic-10, yLoc+height+10);
		}
		g2d.drawLine(xLoc, yLoc,  xLoc, yLoc+height);
		g2d.drawLine(xLoc+length, yLoc,  xLoc+length, yLoc+height);
		for(int i = 0; i < ySocBounds.length; i++) {
			int ySocPic = convertValueToPixel(yLoc, height, ySocMax, ySocMin, ySocBounds[i]);
			g2d.setColor(Color.black);
			g2d.drawLine(xLoc, ySocPic, xLoc+ticLength, ySocPic);
			//g2d.setColor(Color.blue);
			//g2d.drawString(""+ySocBounds[i], xLoc-30, ySocPic+10);
			//if(yLab)  g2d.drawString(Integer.toString((int)xBounds[i]), xLoc-30, ySocPic);
			if(yLab) drawLargeFont(g2d, Integer.toString((int)ySocBounds[i]), xLoc-20, ySocPic+3);
			
		}
		
		

		
		
		
	}
		
		
	public static void paintLineGraph(Graphics2D g2d, int xLoc, int yLoc, ArrayList<Double> data, int length, int height) {
		//TODO, change more parameters to be passed in to make this general
		
		double[] yBounds = {0,0.1,0.2}; double yMax = yBounds[yBounds.length-1]; double yMin = yBounds[0];
		double[] xBounds = {0,1000,2000}; double xMax = xBounds[xBounds.length-1]; double xMin = xBounds[0];
		int timeMax = (int)xMax;
		int numT = 1000;
		int tInc = (int)((xMax-xMin)/numT);
		int timeSpan = Math.min(timeMax, data.size());

		//skip axes for now
		
		//graphs
		for(int t = tInc; t < timeSpan; t+=tInc) {
			int oldX = convertValueToPixel(xLoc, length, xMin, xMax, t-tInc);
			int newX = convertValueToPixel(xLoc, length, xMin, xMax, t);
			
			int oldY = convertValueToPixel(yLoc, height, yMax, yMin, data.get(t-tInc));
			int newY = convertValueToPixel(yLoc, height, yMax, yMin, data.get(t));
			if(oldY < yLoc) oldY = yLoc;  if(newY < yLoc) newY = yLoc; 
			//if(displayFour) g2d.setColor(fourColors[index]);
			int yBot = yLoc+height;
			
			
			g2d.setColor(Color.green);
			g2d.drawLine(oldX, oldY, newX, newY);
		}
	}
		
		
	
	
	public static int convertValueToPixel(int firstPixel, int length, double min, double max, double v) {
		double proportion = (v-min)/(max-min);
		int pixel = (int)(proportion*length)+firstPixel;
		return pixel;
	}
		
		
	
	
	public static void drawThickLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
		//draws lines near the desired point in an attempt to make a line 2 pixels wide
		
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawLine(x1+1, y1, x2+1, y2);
		g2d.drawLine(x1, y1+1, x2, y2+1);
		
	}
		
			
	public static void drawLargeFont(Graphics2D g2d, String s, int x, int y) {
		Font smallFont = g2d.getFont();
		 Font largeFont = smallFont.deriveFont(smallFont.getSize() * 1.5F);
		 g2d.setFont(largeFont);
		 g2d.drawString(s,x,y);
		 g2d.setFont(smallFont);
		
		
		
	}

	
	
	
}

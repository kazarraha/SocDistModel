


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class CNScatterPlotHandler extends JPanel {

	
	
	
	
	
	public JFrame window;
	public double[] xPoints;
	public double[] yPoints;
	public double[][] fancyYPoints; //includes data for error bars
	public String label;
	
	public String[] pointLabels;
	public Color[] pointLabelColors;
	
	public Color[] pointColors; 
	
	
	public double[] specialXTics;
	public String[] specialXTicLabels;
	public double[] specialYTics;
	public String[] specialYTicLabels;
	
	public double[] stripeyX;

	
	public int dotRadius = 4;

	
	public boolean paintManyBoolean = false;
	public double[][] xpMany;
	public double[][] ypMany;
	
	
	
	public void setMarks(double[] xM, double[] yM) {
		
		DecimalFormat df = new DecimalFormat("#.###");
		specialXTics = new double[xM.length]; specialXTicLabels = new String[xM.length]; specialYTics = new double[yM.length]; specialYTicLabels = new String[yM.length];
		
		for(int i = 0; i < xM.length; i++) {
			specialXTics[i] = xM[i];
			specialXTicLabels[i] = df.format(xM[i]);
		}
		for(int i = 0; i < yM.length; i++) {
			specialYTics[i] = yM[i];
			specialYTicLabels[i] = df.format(yM[i]);
		}
		
		repaint();		
	}
	
	
	
	
	public CNScatterPlotHandler(double[] xP, double[] yP, String lab){

		window = new JFrame(lab);
        window.add(this);
        window.setSize(1400, 1000);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        xPoints = xP;
        yPoints = yP;
        label = lab;
        repaint();
	}
	
	public CNScatterPlotHandler(double[] xP, double[][] yP, String lab){

		window = new JFrame(lab);
        window.add(this);
        window.setSize(1400, 1000);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        xPoints = xP;
        yPoints = new double[yP.length];
        for(int i = 0; i < yP.length; i++) yPoints[i] = yP[i][2];
        fancyYPoints = yP;
        label = lab;
        repaint();
	}
	
	public CNScatterPlotHandler(double max, double[] yP, String lab) {
		//assumes the x values are integer counts starting at 0
		this(linear(yP.length, max),yP, lab);
		
	}
	
	public static double[] linear(int length, double max) {
		double[] xP = new double[length];
		for(int x = 0; x < length; x++) xP[x] = (max*x)/(length-1);
		return xP;
	}
	
	public void addPointLabels(String[] pL, Color[] pLC){
		//adds labels above every point in the ScatterPlot
		pointLabels = pL;
		pointLabelColors = pLC;
		repaint();
	}
	
	
	public void paint(Graphics g){
		 Graphics2D g2d = (Graphics2D)g;
		 if(paintManyBoolean) {
			 paintMany(g2d);
			 return;
		 }
		 
		 int top = 100;
		 int left = 100;
		 int height = 800;
		 int length = 1200;
		 
		 g2d.setColor(Color.white);
		 g2d.fillRect(0, 0, 1800, 1200);
		 

		 
		 //find bounds (assume, for now, that mins are both 0_
		 double xMax = -10000;
		 for(int i = 0; i < xPoints.length; i++) if(xPoints[i] > xMax) xMax = xPoints[i];
		 double yMax = -10000;
		 if(fancyYPoints == null) {
			 for(int i = 0; i < yPoints.length; i++) if(yPoints[i] > yMax) yMax = yPoints[i];
		 }
		 else {
			 for(int i = 0; i < fancyYPoints.length; i++) for(int j = 0; j < fancyYPoints[i].length; j++) if(fancyYPoints[i][j] > yMax) yMax = fancyYPoints[i][j];
		 }
		 
		 
		 boolean scaleYMin = true;
		 double yMin = 0;
		 if(scaleYMin){
			 yMin = 10000;
			 for(int i = 0; i < yPoints.length; i++) if(yPoints[i] < yMin) yMin = yPoints[i];
			 if(yMin > 0) yMin = 0;
		 }
		 boolean scaleXMin = true;
		 double xMin = 0;
		 if(scaleXMin){
			 xMin = 10000;
			 for(int i = 0; i < xPoints.length; i++) if(xPoints[i] < xMin) xMin = xPoints[i];
			 if(xMin > 0) xMin = 0;
		 }
		 
		 if(specialYTics != null && specialYTics.length>0) {
			 yMin = specialYTics[0];
			 yMax = specialYTics[specialYTics.length-1];
		 }
	 
		 double yInc = (double)height / (yMax-yMin); //number of pixels per point in the y direction
		 //if(zoom) yInc = 400;
		 if(stripeyX != null) xMax *= 1.012;
		 double xInc = (double)length / (xMax-xMin);
	
		 if(stripeyX != null) {
			 //System.out.println("maxStripey: " + stripeyX[stripeyX.length-1]);
			 int scale = (int)(1.0/(stripeyX[stripeyX.length-1])*length);
			 //System.out.println("scale: " + scale);
			 stripeys(g2d, top, height, left, scale, stripeyX);
		 }
		 
		  
		 
		 g2d.setColor(Color.black);
		 DecimalFormat df = new DecimalFormat("#.00");
		 
		 //g2d.drawString(label, left+length/2,top/2);
		 //g2d.drawString(df.format(yMax), left-50, top);
		 if(scaleYMin && yMin != 0) g2d.drawString(df.format(yMin), left-50, top+height);
		 //g2d.drawString(df.format(xMax), left+length-50, top+height + 20);
		 if(scaleXMin && xMin != 0) g2d.drawString(df.format(xMin), left-50, top+height+20);
		 
		 g2d.drawLine(left,top,left, top+height); //vertical line at left
		 g2d.drawLine(left,top+height, left+length, top+height); //horizontal line at bottom

		 
		 int xTicInterval = (int)(10*xInc);
		 int yTicInterval = (int)(10*yInc);
		 int ticLength = 2;
		 int xTicSpot = left;
		 if(specialXTics==null) {
		 while(xTicSpot <= left+length){
			 g2d.drawLine(xTicSpot, top + height, xTicSpot, top + height - ticLength);
			 xTicSpot += xTicInterval;
		 }
		 }
		 
		 int yTicSpot = top + height;
		 if(specialYTics==null) {
		 while(yTicSpot >= top){
			 g2d.drawLine(left, yTicSpot, left + ticLength, yTicSpot);
			 yTicSpot -= yTicInterval;
		 }
		 }
		 
		 Font smallFont = g2d.getFont();
		 Font largeFont = smallFont.deriveFont(smallFont.getSize() * 2F);
		 g2d.setFont(largeFont);
		 
		 //y axis
		 if(scaleYMin && yMin < 0){
			 g2d.setColor(Color.gray);
			 g2d.drawLine(left,  top+height + (int)(yInc * yMin), left+length, top+height + (int)(yInc * yMin));
		 }
		 //x axis
		 if(scaleXMin && xMin < 0){
			 g2d.setColor(Color.gray);
			 g2d.drawLine(left - (int)(xInc*xMin),  top, left - (int)(xInc*xMin), top+height);
		 }
		 
		 //points and labels
		 for(int i = 0; i < xPoints.length; i++){
			 //
			 //TEMP HACK TO SKIP FIRST POINT
			 //if(i==0)i++;
			 
			 //
			 int xCenter = left + (int)(xInc * (xPoints[i]-xMin));
			 if(fancyYPoints == null) {
				 int yCenter = top + height - (int)(yInc * (yPoints[i]-yMin));
				 if(pointColors != null && pointColors.length > i) g2d.setColor(pointColors[i]);
				 else g2d.setColor(Color.black);			 
				 g2d.fillOval(xCenter - dotRadius, yCenter - dotRadius, 2*dotRadius+1, 2*dotRadius+1); //TODO: actual point here
				 if(pointLabels != null){
					 g2d.setColor(pointLabelColors[i]);
					 g2d.drawString(pointLabels[i], xCenter-3, yCenter-5);
				 }
			 }
			 else {
				 int[] yPixels = new int[fancyYPoints.length];
				 for(int k = 0; k < fancyYPoints[i].length; k++) yPixels[k] = top + height - (int)(yInc * (fancyYPoints[i][k]-yMin));
				 Color c = Color.gray;
				 if(pointColors != null) c = pointColors[i];
				 fancyErrorPoint(g2d, pointColors[i], xCenter, 4, yPixels);
			 }
		 }
		 
		 //Font smallFont = g2d.getFont();
		 //Font largeFont = smallFont.deriveFont(smallFont.getSize() * 2F);
		 //g2d.setFont(largeFont);
		 
		 g2d.setColor(Color.black);
		 if(specialXTics != null) {
			 int offset = 20;
			 for(int i = 0; i < specialXTics.length; i++) {
				 int xSpot = left - offset + (int)(xInc * (specialXTics[i]-xMin));
				 g2d.drawString(specialXTicLabels[i], xSpot, top+height+30);
				 g2d.drawLine(xSpot+offset, top+height, xSpot+offset, top+height-ticLength);
			 } 
		 }
		 //
		 if(specialYTics == null) {
			 double[] ends = {yMin, yMax};
			 String[] labels = {df.format(yMin), df.format(yMax)};
			 specialYTics = ends;
			 specialYTicLabels = labels;
		 }
		 //
		int offset = 60;
		for(int i = 0; i < specialYTics.length; i++) {
			int ySpot = top + height - (int)(yInc * (specialYTics[i]-yMin));
			g2d.drawString(specialYTicLabels[i], left-offset , ySpot);
			g2d.drawLine(left, ySpot, left+ticLength, ySpot);
		} 

		 
		 g2d.setFont(smallFont);
		 
		 

		 
	}
	
	
	
	
	public static CNScatterPlotHandler loglogPlotFromMin(double[] xp, double[] yp, String lab) {
		//sets min x and min y to 1 then logs both
		for(int i = 0; i < xp.length; i++) xp[i] = xp[i]+1;
		
		
		CNScatterPlotHandler loglogPlot = new CNScatterPlotHandler(logFromMin(xp), logFromMin(yp), lab);
		
		double[] sxt = {Math.log(1), Math.log(10), Math.log(25), Math.log(100), Math.log(144)};
		String[] sxtl = {"1", "10", "25", "100", "144"};
		double[] syt = {Math.log(0.05), Math.log(0.2), Math.log(0.5), Math.log(0.99)};
		String[] sytl = {"0.05", "0.2", "0.5", "1"};
		loglogPlot.specialXTics = sxt; loglogPlot.specialYTics = syt; loglogPlot.specialXTicLabels = sxtl; loglogPlot.specialYTicLabels = sytl;
		

		return loglogPlot;
	}
	
	public static double[] logFromMin(double[] points) {
		double min = 100000000;
		//for(int i = 0; i < points.length; i++) {
		for(int i = 0; i < 132; i++) { //Temp hack!
			System.out.println("points[" + i + "]: " + points[i]);
			if(points[i] < min) min = points[i];
		}
		//double jumpUp = 1-min;
		double jumpUp = 0;
		System.out.println("jumpUp: " + jumpUp);
		double[] newPoints = new double[points.length];
		//for(int i = 0; i < points.length; i++) {
		for(int i = 0; i < 132; i++) { //Temp hack for lobotomy data, make a better workaround later
			newPoints[i] = Math.log(points[i]+jumpUp);
		}
		System.out.println("min value: " + min);
		return newPoints;		
	}
	
	
	public CNScatterPlotHandler(double[][] xP, double[][] yP, String lab){
		//for multiple scatterplots on the same graph
		paintManyBoolean = true;
		xpMany = xP;
		ypMany = yP;
		label = lab;
		
		window = new JFrame("sub window");
        window.add(this);
        window.setSize(1400, 1000);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		repaint();
		

	}
	
	public void paintMany(Graphics2D g2d) {
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, 2000, 1500);
		Color[] manyColors = {Color.black, Color.green, Color.blue};
		
		for(int i = 0; i < xpMany.length; i++) paintOne(g2d, xpMany[i], ypMany[i], manyColors[i]);
		
		
	}
	
	public void paintOne(Graphics2D g2d, double[] xp, double[] yp, Color dotColor) {
		//identical to paint except with dotColor different
		int top = 100;
		 int left = 100;
		 int height = 800;
		 int length = 1200;

		 //find bounds (assume, for now, that mins are both 0_
		 double xMax = -10000;
		 for(int i = 0; i < xp.length; i++) if(xp[i] > xMax) xMax = xp[i];
		 double yMax = -10000;
		 for(int i = 0; i < yp.length; i++) if(yp[i] > yMax) yMax = yp[i];
		 
		 
		 if(specialYTics != null && specialYTics.length>0) yMax = specialYTics[specialYTics.length-1];
		 
		 boolean scaleYMin = true;
		 double yMin = 0;
		 if(scaleYMin){
			 yMin = 10000;
			 for(int i = 0; i < yp.length; i++) if(yp[i] < yMin) yMin = yp[i];
			 if(yMin > 0) yMin = 0;
		 }
		 boolean scaleXMin = true;
		 double xMin = 0;
		 if(scaleXMin){
			 xMin = 10000;
			 for(int i = 0; i < xp.length; i++) if(xp[i] < xMin) xMin = xp[i];
			 if(xMin > 0) xMin = 0;
		 }
	 
		 double yInc = (double)height / (yMax-yMin); //number of pixels per point in the y direction
		 //if(zoom) yInc = 400;
		 double xInc = (double)length / (xMax-xMin);
	
		 g2d.setColor(Color.black);
		 DecimalFormat df = new DecimalFormat("#.000");

		 if(scaleYMin && yMin != 0) g2d.drawString(df.format(yMin), left-50, top+height);

		 if(scaleXMin && xMin != 0) g2d.drawString(df.format(xMin), left-50, top+height+20);
		 
		 CNGraphHandler.drawThickLine(g2d, left,top,left, top+height); //vertical line at left
		 CNGraphHandler.drawThickLine(g2d,left,top+height, left+length, top+height); //horizontal line at bottom

		 
		 int xTicInterval = (int)(10*xInc);
		 int yTicInterval = (int)(10*yInc);
		 int ticLength = 3;
		 int xTicSpot = left;
		 if(specialXTics==null) {
		 while(xTicSpot <= left+length){
			 g2d.drawLine(xTicSpot, top + height, xTicSpot, top + height - ticLength);
			 xTicSpot += xTicInterval;
		 }
		 }
		 
		 int yTicSpot = top + height;
		 if(specialYTics==null) {
		 while(yTicSpot >= top){
			 g2d.drawLine(left, yTicSpot, left + ticLength, yTicSpot);
			 yTicSpot -= yTicInterval;
		 }
		 }
		 
		 Font smallFont = g2d.getFont();
		 Font largeFont = smallFont.deriveFont(smallFont.getSize() * 2F);
		 g2d.setFont(largeFont);
		 
		 //points and labels
		 for(int i = 0; i < xp.length; i++){
			 //
			 System.out.println("yp: " + yp[i]);
			 //
			 
			 int xCenter = left + (int)(xInc * (xp[i]-xMin));
			 int yCenter = top + height - (int)(yInc * (yp[i]-yMin));
			 g2d.setColor(dotColor);
			 g2d.fillOval(xCenter - dotRadius, yCenter - dotRadius, 2*dotRadius+1, 2*dotRadius+1);
			 if(pointLabels != null){
				 g2d.setColor(pointLabelColors[i]);
				 g2d.drawString(pointLabels[i], xCenter-3, yCenter-5);
				 g2d.setColor(Color.black);
			 }
		 }
		 //
		 System.out.println("break");
		 //
		 
		 //Font smallFont = g2d.getFont();
		 //Font largeFont = smallFont.deriveFont(smallFont.getSize() * 2F);
		 //g2d.setFont(largeFont);
		 
		 g2d.setColor(Color.black);
		 if(specialXTics != null) {
			 int offset = 20;
			 for(int i = 0; i < specialXTics.length; i++) {
				 int xSpot = left - offset + (int)(xInc * (specialXTics[i]-xMin));
				 g2d.drawString(specialXTicLabels[i], xSpot, top+height+30);
				 g2d.drawLine(xSpot+offset, top+height, xSpot+offset, top+height-ticLength);
			 } 
		 }
		 if(specialYTics != null) {
			 int offset = 60;
			 for(int i = 0; i < specialYTics.length; i++) {
				 int ySpot = top + height - (int)(yInc * (specialYTics[i]-yMin));
				 g2d.drawString(specialYTicLabels[i], left-offset , ySpot);
				 g2d.drawLine(left, ySpot, left+ticLength, ySpot);
			 } 
		 }
		 
		 g2d.setFont(smallFont);
		 
		 
		 //y axis if it shows up midgraph
		 if(scaleYMin && yMin < 0){
			 g2d.setColor(Color.gray);
			 g2d.drawLine(left,  top+height + (int)(yInc * yMin), left+length, top+height + (int)(yInc * yMin));
		 }
		 //x axis if it shows up midgraph
		 if(scaleXMin && xMin < 0){
			 g2d.setColor(Color.gray);
			 g2d.drawLine(left - (int)(xInc*xMin),  top, left - (int)(xInc*xMin), top+height);
		 }
		 
		
	}
	
	
	public void generatePointColorsByGradient(int numY, int numX) {
		pointColors = new Color[numY*numX];
		int minR = 0; int maxR = 220; int minB = 0; int maxB = 220;
		int incB = (maxB-minB)/(numY-1);
		int incR = (maxR-minR)/(numX-1);
		int index = 0;
		for(int y = 0; y < numY; y++) {
			for(int x = 0; x < numX; x++) {
				int r = minR+x*incR;
				int g = 0;
				int b = minB+y*incB;
				pointColors[index] = new Color(r,g,b);
				index++;
			}
		}
	}
	
	public void stripeys(Graphics2D g2d, int top, int height, int left, double scale, double[] xSpots) {
		//g2d.setColor(Color.LIGHT_GRAY);
		g2d.setColor(new Color(230,230,230));
		for(int xi = 0; xi < xSpots.length-1; xi+=2) {
			//System.out.println("xSpot["+xi+"]: " + xSpots[xi]);
			//System.out.println("xSpot["+(xi+1)+"]: " + xSpots[xi+1]);
			int x1 = (int)(left+scale*xSpots[xi]);
			//System.out.println("x1: " + x1);
			int len = (int)(scale*(xSpots[xi+1]-xSpots[xi]));
			//System.out.println("len: " + len);
			g2d.fillRect(x1, top, len, height);
		}
	}
	
	
	
	
	public void fancyErrorPoint(Graphics2D g2d, Color c, int xPos, int xR, int[] yPos) {
		//paints single data point with fancy error bars
		//yPos should have exactly 5 values in ascending or descending order.  center is the mean value, others are 1 and 2 std deviations away
		xR++; //hack
		
		int xb1 = xPos-2*xR;
		int xb2 = xPos+2*xR;
		
		int xs1 = xPos-xR;
		int xs2 = xPos+xR;
		
		int cr = 9;
		
		//if(c != null) {
		//	g2d.setColor(c);
		//	g2d.fillRect(xb1, yPos[1], xb2-xb1, yPos[3]-yPos[1]);
		//}
	
		g2d.setColor(Color.black);

		//square box version
		//for(int yi = 0; yi < 5; yi++) {
		//	int x1 = xb1; int x2 = xb2; int y = yPos[yi];
		//	if(yi==0 || yi == 4) {x1 = xs1; x2 = xs2;}
		//	g2d.drawLine(x1, y, x2, y);			
		//}
		//g2d.drawLine(xb1, yPos[1], xb1, yPos[3]);
		//g2d.drawLine(xb2, yPos[1], xb2, yPos[3]);
		
		CNGraphHandler.drawThickLine(g2d, xPos, yPos[0], xPos, yPos[4]);
		CNGraphHandler.drawThickLine(g2d, xb1, yPos[0], xb2, yPos[0]);
		CNGraphHandler.drawThickLine(g2d, xb1, yPos[4], xb2, yPos[4]);

		//g2d.drawLine(xPos, yPos[0], xPos, yPos[4]); //center verticle line
		//g2d.drawLine(xs1, yPos[0], xs2, yPos[0]); //top
		//g2d.drawLine(xs1, yPos[4], xs2, yPos[4]); //bottom
		if(c!= null) g2d.setColor(c);
		g2d.fillOval(xPos-cr, yPos[2]-cr, 2*cr, 2*cr);
		g2d.setColor(Color.black);
		g2d.drawOval(xPos-cr, yPos[2]-cr, 2*cr, 2*cr);
		

		
		
		
	}
	
	public static CNScatterPlotHandler loadStatsFromCSV(String name, int startX, int numX, int startY, int mainType) {
		return loadStatsFromCSV(name, startX, numX, startY, mainType, false);
	}
	
	public static CNScatterPlotHandler loadStatsFromCSV(String name, int startX, int numX, int startY, int mainType, boolean parallelHack) {
		//plots multiple scatterplots staggered and colored from data from a square grid in a CSV file
		
		boolean normY = true;
		int numTypes = 4;
		if(parallelHack) numTypes = 3;
		
		//assumes data is in a form of 4 x N mean and 4 x N std Err with one empty row between them
		double[][] rawData = DataManager.loadFromCSV(name);

		if(normY) { //temp hack
			int t = mainType; //mainType;
			for(int x = 0; x < numX; x++) {
				rawData[t+startY+1][x+startX] *=7;
			}
		}
		
		
		Color[] cTemplate = {Color.gray, Color.cyan, Color.green, Color.red};
		if(parallelHack) {
			Color[] c2 = {Color.gray, Color.cyan, Color.red};
			cTemplate = c2;
		}
		
		double xInc = rawData[startY][startX+1]-rawData[startY][startX];
		double[] xPoints = new double[numX*numTypes];
		double[][] fyPoints = new double[numX*numTypes][5];
		Color[] colors = new Color[numX*numTypes];
		double[] stripeyX = new double[numX+1];
		int index = 0;
		for(int t = 0; t < numTypes; t++) {
			for(int x = 0; x < numX; x++) {
				double mean = rawData[t+startY+1][x+startX];
				double stdErr = rawData[t+startY+numTypes+2][x+startX];
				//
				if(normY) {
					double sum = 0;
					for(int t2 = 0; t2 <  numTypes; t2++) sum += rawData[t2+startY+1][x+startX];
					mean /= sum;
					stdErr /= sum;
				}
				//
				for(int k = 0; k < 5; k++) fyPoints[index][k] = mean + (k-2)*stdErr;
				xPoints[index] = rawData[startY][x+startX]+(t+1)*(xInc/6);
				colors[index] = cTemplate[t];
				
				if(t==0 && x > 0) stripeyX[x] += xPoints[index]/2;
				if(t==numTypes-1 && x < numX-1) stripeyX[x+1] += xPoints[index]/2; //these two lines effectively average the values between the first and last points in each set
				index++;
			}
		}
		
		stripeyX[0] = 0;
		stripeyX[numX] = 2*stripeyX[numX-1]-stripeyX[numX-2];
		
		//make CNScatterPlotHandler, set parameters, and then return
		String suffix = "";
		if(startY==0) suffix = " Direct Attribution";
		if(startY==11) suffix = " 0.9 Attribution";
		if(startX==45) suffix = "Epidemic Attribution";
		if(normY) suffix += " [normalized proportions]";
		CNScatterPlotHandler scatter = new CNScatterPlotHandler(xPoints, fyPoints, "Fancy Scatterplot from " + name + suffix);
		scatter.pointColors = colors;
		scatter.stripeyX = stripeyX;
		double[] xm = {0};
		double[] ym = {0,200,400,600};
		//double[] ym = {0,500,1000};
		//double[] ym = {0,50,100};
		//double[] ym = {0,200,400};
		//double[] ym = {-500,0,2000,4000};
		//double[] ym = {-1.1,-1,0,1,1.1};
		if(normY) {
			double[] nm = {0,0.25,0.5,0.75};
			//double[] nm = {0,0.25,0.5,0.75,1};
			ym = nm;
		}
		scatter.setMarks(xm, ym);
		
		scatter.repaint();
		
		return scatter;
	}
	
	
}




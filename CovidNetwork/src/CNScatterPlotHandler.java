


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
	public String label;
	
	public String[] pointLabels;
	public Color[] pointLabelColors;
	
	
	public double[] specialXTics;
	public String[] specialXTicLabels;
	public double[] specialYTics;
	public String[] specialYTicLabels;
	
	public int dotRadius = 4;

	
	public boolean paintManyBoolean = false;
	public double[][] xpMany;
	public double[][] ypMany;
	
	
	
	public void setMarks(double[] xM, double[] yM) {
		
		DecimalFormat df = new DecimalFormat("#.000");
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

		window = new JFrame("sub window");
        window.add(this);
        window.setSize(1400, 1000);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        xPoints = xP;
        yPoints = yP;
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
		 for(int i = 0; i < yPoints.length; i++) if(yPoints[i] > yMax) yMax = yPoints[i];
		 
		 
		 if(specialYTics != null && specialYTics.length>0) yMax = specialYTics[specialYTics.length-1];
		 
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
	 
		 double yInc = (double)height / (yMax-yMin); //number of pixels per point in the y direction
		 //if(zoom) yInc = 400;
		 double xInc = (double)length / (xMax-xMin);
	
		 
		  
		 
		 g2d.setColor(Color.black);
		 DecimalFormat df = new DecimalFormat("#.000");
		 
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
		 while(xTicSpot <= left+length){
			 g2d.drawLine(xTicSpot, top + height, xTicSpot, top + height - ticLength);
			 xTicSpot += xTicInterval;
		 }
		 
		 int yTicSpot = top + height;
		 while(yTicSpot >= top){
			 g2d.drawLine(left, yTicSpot, left + ticLength, yTicSpot);
			 yTicSpot -= yTicInterval;
		 }
		 
		 Font smallFont = g2d.getFont();
		 Font largeFont = smallFont.deriveFont(smallFont.getSize() * 2F);
		 g2d.setFont(largeFont);
		 
		 //points and labels
		 for(int i = 0; i < xPoints.length; i++){
			 //
			 //TEMP HACK TO SKIP FIRST POINT
			 if(i==0)i++;
			 
			 //
			 int xCenter = left + (int)(xInc * (xPoints[i]-xMin));
			 int yCenter = top + height - (int)(yInc * (yPoints[i]-yMin));
			 g2d.fillOval(xCenter - dotRadius, yCenter - dotRadius, 2*dotRadius+1, 2*dotRadius+1);
			 if(pointLabels != null){
				 g2d.setColor(pointLabelColors[i]);
				 g2d.drawString(pointLabels[i], xCenter-3, yCenter-5);
				 g2d.setColor(Color.black);
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
		 if(specialYTics != null) {
			 int offset = 60;
			 for(int i = 0; i < specialYTics.length; i++) {
				 int ySpot = top + height - (int)(yInc * (specialYTics[i]-yMin));
				 g2d.drawString(specialYTicLabels[i], left-offset , ySpot);
				 g2d.drawLine(left, ySpot, left+ticLength, ySpot);
			 } 
		 }
		 
		 g2d.setFont(smallFont);
		 
		 
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
		 while(xTicSpot <= left+length){
			 g2d.drawLine(xTicSpot, top + height, xTicSpot, top + height - ticLength);
			 xTicSpot += xTicInterval;
		 }
		 
		 int yTicSpot = top + height;
		 while(yTicSpot >= top){
			 g2d.drawLine(left, yTicSpot, left + ticLength, yTicSpot);
			 yTicSpot -= yTicInterval;
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
	
	
}




import java.awt.Graphics2D;

public class ActionManager {

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void forceSoc(CNSimulator sim, double v) {
		for(int i = 0; i < sim.population.length; i++) {
			for(int j = 0; j < sim.population.length; j++) {
				if(i == j) sim.socialDesires[i][j] = v;
				else sim.socialDesires[i][j] = 0;
			}
		}
	}
	
	
	public static double[] simulateForce(double v) {
		CNSimulator sim = new CNSimulator();
		double[] utilities = new double[sim.population.length];
		int time = 2000;
		for(int t = 0; t < time; t++) {
			forceSoc(sim, v);
			sim.simDay();
		}
		forceSoc(sim, v);
		for(int i = 0; i < sim.population.length; i++) utilities[i] = sim.population[i].utilityEstimator(sim, sim.socialDesires[i], false);
		return utilities;	
	}
	
	public static void graphPublicGoods() {
		
		double vMin = 0;
		double vMax = 50;
		int numPoints = 100;
		double vInc = (vMax-vMin)/numPoints;
		double[][] points = new double[8][numPoints];
		
		for(int n = 0; n < numPoints; n++) {
			double v = vMin + n*vInc;
			double[] vPoint = simulateForce(v);
			for(int i = 0; i < 8; i++) points[i][n] = vPoint[i] + 10;   // TEMP BONUS OF 10 FOR GRAPHING HACK
		}
		
		//extra lines corresponding to equilibrium decision
		CNSimulator sim = new CNSimulator();
		for(int t = 0; t < 8000; t++) sim.simDay();
		
		//graph points
		for(int i = 0; i < points.length; i++) {
			String s = "Utilities as soc varies for group " + i;
			CNGraphHandler graph = new CNGraphHandler(points[i], s);
			double[] extraLine = {sim.population[i].rememberSoc/vInc,0,sim.population[i].rememberSoc/vInc ,10}; 
			graph.addExtraLine(extraLine);
		}
		
	}
	
	
	public static void graphHomophily() {
		
		int time = 5000;
		double minR = 0;
		double maxR = 1;
		//int numP = 25;
		int numP = 100;
		double rInc = (maxR - minR)/numP;
		
		double kNorm = 1; //try to preserve the social value under the curve between 0 and 1.  Part arbitrary, probably behave nicely if this is approximately where people tend to socialize per group
		double cNorm = 1.25; //area this should equal.  inconsistent with m = 3, r = 1/2?  am confused;
		// m = (c-kr)/(r^k+1) WRONG
		// m = ln(r)*(c/r - k) / (r^k -1)
		
		double[] homophilyScores = new double[numP];
		for(int p = 0; p < numP; p++) {
			double r = minR + rInc*p;
			//double m = (cNorm - kNorm * r)/(Math.pow(r, kNorm+1));
			double m = Math.log(r)*(cNorm/r - kNorm)/(Math.pow(r, kNorm) -1);
			CNSimulator sim = new CNSimulator();
			sim.applyFrictionValues(r, m, r); //normalizer = r
			for(int t = 0; t < time; t++) sim.simDay();
			//homophilyScores[p] = sim.getHomophilyScore();
			homophilyScores[p] = sim.getEIHomScore(); //modified EI homophily
			
		}
		
		
		//graph stuff, preferably scatterplot
		String lab = "Homophily as a function of social friction";
		//CNGraphHandler graph = new CNGraphHandler(homophilyScores, lab);
		double[] x = CNScatterPlotHandler.linear(numP, maxR);
		CNScatterPlotHandler scatter = new CNScatterPlotHandler(x, homophilyScores, lab);
		
		
	}
	
	
	
	public static void drawThickLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
		//draws lines near the desired point in an attempt to make a line 2 pixels wide
		
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawLine(x1+1, y1, x2+1, y2);
		g2d.drawLine(x1, y1+1, x2, y2+1);
		
	}
	
	
	
	
}

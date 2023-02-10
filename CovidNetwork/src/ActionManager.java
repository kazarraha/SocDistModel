import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.List;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ActionManager {

	public static boolean maxSocHack = false; //hardcoding this is simpler than copy/pasting multiple methods and changing a single line in each
	
	
	
	public static void actionPaction() { //called from GUI to do whatever hardcoded thing I'm doing right now
		//graphLonely2(500,500);

		//graphHomophily2D();
		//graphHomophily2DSweep();
		//actionScatterSlices("homophily2D_4", "homophily2D 4");
		//graphScatterFromSave2("inf_homophily2D_testInfScatter","homophily2D_testInfScatter","scatter");
		
		//String slot = "homophily2D_testInfScatter";
		//String slot = "homophily2D_ratio_2";
		//graphVarySquareFromSave(slot,1,1, "homophily as a function of sigmarhoratio and s", 1);
		//String infSlot = "inf_" + slot;
		//String normSlot = "homNorm_" + slot;
		//actionScatterSlices(slot, "homophily as a function of r and s");
		//graphVarySquareFromSave(normSlot,1,1, "homophily/infstddev as a function of r and s.  NORMALIZED", 20.0);
		//graphScatterFromSave2(infSlot, slot, "Homophily (y) vs Std Dev of infection (x) scatterplot");
		
		//quickFormulaTest(10000);
		
		//network
		//graphSocVersusConnection(10000);
		//graphSatVersusConnMeasure(10000, 1000);
		//testInfectionRole();
		//epidemiologyTracker(0);
		//safePocketTracker(2);
		//safePocketStatistics(3);
		
		//infectionVsMinSoc(2);
		
		//safePocketStatistics2(0);
		//statTester();
		//statisticsLoaderToCSV();
		
		//parallelNetworks(2);
		//parallelMinSoc(2);
		//convertParallelMinSocToStats(2);
		
		//homophilyVsMinSoc(2);
		//convertHomophilyMinSocToStats(2);
		//convertParallelMinSocToInfOnly(2);
		
		//blameVsMinSoc(0);
		scatterImageLoader();
		
	}
	
	
	
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
		//int time = 500;
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
		double[] xm = {0,0.5,1};
		double[] ym = {0,0.5,1};
		scatter.setMarks(xm, ym);
		
		
	}
	
	public static void graphHomophily2D() {
		boolean both = true;
		boolean testRatio = true;
		boolean normalizeByStddev = false;
		boolean extremeTest = true;
		
		int time = 10000;
		double minR = 0;
		double maxR = 1;
		double minHighRho = 30; //actual low = 30
		//double maxHighRho = 180;
		double maxHighRho = 90;
		
		//double minHighRho = 60;
		//double maxHighRho = 60;
		
		//for supplementary scenario varying soc/health ratio
		double minRatio = 0.01;
		//double maxRatio = 5.01;
		double maxRatio = 50.01; //runs into memory issue.  not sure why, might have to do with each CNSimulator storing its complete history and running each Sim for too long
		
		double minHighSoc = 4;
		double maxHighSoc = 20;
		//double maxHighSoc = 44;
		
		//double minHighSoc = 12;
		//double maxHighSoc = 12;
		
		//int numX = 101;
		//int numY = 101;
		
		int numX = 11;
		int numY = 11;
		
		double rInc = (maxR - minR)/(numX);
		double rhoInc = (maxHighRho - minHighRho)/(numY-1);
		double socInc = (maxHighSoc - minHighSoc)/(numY-1);
		double ratioInc = (maxRatio - minRatio)/(numY-1);
		
		double kNorm = 1; //try to preserve the social value under the curve between 0 and 1.  Part arbitrary, probably behave nicely if this is approximately where people tend to socialize per group
		double cNorm = 1.25; //area this should equal.  inconsistent with m = 3, r = 1/2?  am confused;
		
		double[][] homophilyScores = new double[numY][numX];
		double[][] infDev = new double[numY][numX];
		double[][] infAvg = new double[numY][numX];
		double[][] homNormalized = new double[numY][numX];
		for(int s = 0; s < numY; s++) {
			double highRho = minHighRho + rhoInc*s;
			double highSoc = minHighSoc + socInc*s;
			double ratio = minRatio + ratioInc*s;
			for(int p = 0; p < numX; p++) {
				double r = minR + rInc*p;
				double m = Math.log(r)*(cNorm/r - kNorm)/(Math.pow(r, kNorm) -1);
				
				if(r == 0) System.out.println("r0: m = " + m);
				
				CNSimulator sim = new CNSimulator();
				if(!testRatio){
					sim.population[1].healthVal = highRho; //ASRA
					sim.population[3].healthVal = highRho; //SORA
				
					if(both) {
						sim.population[2].socVal = highSoc; //SORT
						sim.population[3].socVal = highSoc; //SORA
					}
				}
				else if(testRatio) {
					for(int i = 0; i < sim.population.length; i++) {
						sim.population[i].socVal*= ratio;
						//sim.population[i].incSoc *= 1;
						if(ratio > 1 && extremeTest) sim.population[i].incSoc *= ratio;
					}
					if(extremeTest) {
						if(ratio > 1) { 
						sim.timeScale = 1.0/ratio;
						time = (int) (10000*ratio);
						}
					}
					else {
						sim.timeScale = 0.2;
						time = 50000;
					}
				}
				
				
				sim.applyFrictionValues(r, m, r); //normalizer = r
				for(int t = 0; t < time; t++) sim.simDay();
				//data collection
				homophilyScores[s][p] = sim.getEIHomScore(); //modified EI homophily
				infDev[s][p] = stdDevOfInf(sim);
				infAvg[s][p] = sim.getAverageInf();
				if(infDev[s][p] > 0) homNormalized[s][p] = homophilyScores[s][p]/infDev[s][p];
				else{
					homNormalized[s][p] = 133333337; //near infinite
					System.out.println("infDev <= 0: " + infDev[s][p]);
				}
				
				
				
				
				System.out.println("highestPInf (" + p + "," + s + "): " + sim.highestPInf);
				
			}
			System.out.println("graphHomophily2D finished s: " + s);
		}
		
		//CNVarySquare square = new CNVarySquare(homophilyScores, "homophily (red) as a function of highRho (y) and friction r (x)");
		//Color[] colors = {Color.blue, Color.yellow};
		//square.setCustomColors(homophilyScores, 1, colors);	
		//square.repaint();
		
		
		//save
		//String slot = "homophily2D_ratio_2.5";
		String slot = "homophily2D_testInfScatter";
		String infDevSlot = "infdev_" + slot;
		String infSlot = "inf_" + slot;
		String normSlot = "homNorm_" + slot;
		//DataManager.saveData(homophilyScores, "homophily2D_2");
		DataManager.saveData(homophilyScores, slot);
		DataManager.saveData(infAvg, infSlot);
		DataManager.saveData(infDev, infDevSlot);
		DataManager.saveData(homNormalized, normSlot);
		//DataManager.saveData(homophilyScores, "temp");
		
		//graphScatterSliceFromSave("homophily2D_2", 5, false, 180, "homophily (y) as a function of Soc/Health discrepancy");
		actionScatterSlices(slot, "homophily as a function of r and s");
		graphVarySquareFromSave(infSlot,1,1, "average infection as a function of [something] and s (if the settings are right the y axis is sigma/rho ratio");
		//graphVarySquareFromSave(normSlot,1,1, "homophily/infstddev as a function of r and s.  NORMALIZED", 30.0);
		//graphScatterFromSave2(infDevSlot, slot, "Homophily (y) vs Std Dev of infection (x) scatterplot");
		
	}
	
	
	public static void graphHomophily2DSweep() {
		//vary sigma and rho on y and x axes, r is left constant
		//(soc y, health x)
		int time = 10000;
		//double minR = 0;
		double fixR = 0.5;
		

		
		double socLowMult = 0.1;
		double socHighMult = 2.1;
		
		double rhoLowMult= 0.1;
		double rhoHighMult = 2.1;
		
		//int numX = 101;
		//int numY = 101;
		
		int numX = 11;
		int numY = 11;
		
		
		double rhoInc = (rhoHighMult - rhoLowMult)/(numX-1);
		double socInc = (socHighMult - socLowMult)/(numY-1);
		
		double kNorm = 1; //try to preserve the social value under the curve between 0 and 1.  Part arbitrary, probably behave nicely if this is approximately where people tend to socialize per group
		double cNorm = 12.5; //area this should equal.  inconsistent with m = 3, r = 1/2?  am confused;
		
		double[][] homophilyScores = new double[numY][numX];
		double[][] infDev = new double[numY][numX];
		for(int s = 0; s < numY; s++) {
			double socMult = socLowMult + socInc*s;
			for(int p = 0; p < numX; p++) {
				double rhoMult = rhoLowMult + rhoInc*p;
				double r = fixR;
				double m = Math.log(r)*(cNorm/r - kNorm)/(Math.pow(r, kNorm) -1);
				CNSimulator sim = new CNSimulator();
				for(int i = 0; i < sim.population.length; i++) {
					sim.population[i].healthVal*= rhoMult;
					sim.population[i].socVal*= socMult;
				}
				
				sim.applyFrictionValues(r, m, r); //normalizer = r
				for(int t = 0; t < time; t++) sim.simDay();
				//homophilyScores[p] = sim.getHomophilyScore();
				homophilyScores[s][p] = sim.getEIHomScore(); //modified EI homophily
				infDev[s][p] = stdDevOfInf(sim);
				
				System.out.println("highestPInf (" + p + "," + s + "): " + sim.highestPInf);
				
			}
			System.out.println("graphHomophily2D finished s: " + s);
		}
		
		//CNVarySquare square = new CNVarySquare(homophilyScores, "homophily (red) as a function of highRho (y) and friction r (x)");
		//Color[] colors = {Color.blue, Color.yellow};
		//square.setCustomColors(homophilyScores, 1, colors);	
		//square.repaint();
		
		
		//save
		String slot = "temp";
		//String slot = "homophily2Dbonus_2";
		String infSlot = "inf_" + slot;
		//DataManager.saveData(homophilyScores, "homophily2D_2");
		DataManager.saveData(homophilyScores, slot);
		DataManager.saveData(infDev, infSlot);
		//DataManager.saveData(homophilyScores, "temp");
		
		//graphScatterSliceFromSave("homophily2D_2", 5, false, 180, "homophily (y) as a function of Soc/Health discrepancy");
		actionScatterSlices(slot, "homophily as a function of r and s");
		graphScatterFromSave2(infSlot, slot, "Homophily (y) vs Std Dev of infection (x) scatterplot");
		
	}
	
	
	public static void drawThickLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
		//draws lines near the desired point in an attempt to make a line 2 pixels wide
		
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawLine(x1+1, y1, x2+1, y2);
		g2d.drawLine(x1, y1+1, x2, y2+1);
		
	}
	
	public static void graphLonely1(int time) {
		NetworkSimulator sim = new NetworkSimulator();
		for(int i = 0; i < time; i++) sim.simDay();
		displayLonely(sim, time);
	}
	
	public static void graphLonely2(int start, int track) {
		NetworkSimulator sim = new NetworkSimulator();
		double[] avgSoc = new double[sim.agents.length];
		for(int i = 0; i < start; i++) sim.simDay();
		for(int i = 0; i < track; i++) {
			sim.simDay();
			for(int j = 0; j < sim.agents.length; j++) {
				avgSoc[j] += sim.agents[j].rememberSoc/track;
			}
		}
		//TEMP
		int chunk = sim.agents.length/4;
		for(int j = 25; j< sim.agents.length; j+= chunk) {
			System.out.println("agent " + j + " soc: " + sim.agents[j].socVal + " health: " + sim.agents[j].healthVal);
		}
		
		
		//
		//scatterplot
		double[] ym = new double[sim.agents.length];
		for(int i = 0; i < ym.length; i++) ym[i] = i;
		CNScatterPlotHandler scatter = new CNScatterPlotHandler(avgSoc, ym, "ScatterPlot of average socialization for all agents");
		double[] xMarks = {0,1,2,5,10};
		//double[] yMarks = new double[0];
		double[] yMarks = {0,sim.agents.length};
		scatter.setMarks(xMarks, yMarks);
	}
	
	public static void displayLonely(NetworkSimulator sim, int time) {
		for(int i = 0; i < sim.threshholds.length; i++) {
			double[] portion = arrayListToArray(sim.trackLonely[i],time);
			String lab = "Lonely agents as a function of time, threshhold: " + sim.threshholds[i];
			CNGraphHandler lonelyGraph = new CNGraphHandler(portion, lab);
		}
	}
	
	
	public static double[] arrayListToArray(ArrayList<Double> AL, int l){
		int length = Math.min(l, AL.size());
		double[] array = new double[l];
		for(int i = 0; i < length; i++) array[i] = AL.get(i);
		return array;
	}
	
	public static double[] arrayListToArray(ArrayList<Double> AL){
		return arrayListToArray(AL,AL.size());
	}
	
	
	
	public static void graphScatterSliceFromSave(String slot, int k, boolean horz, double maxX, String lab) { //for taking a 1D slice of a 2D graph
		double[][] data2D = DataManager.loadData2(slot);
		double[] data = new double[data2D.length];
		if(horz) data = data2D[k];
		else for(int i = 0; i < data.length; i++) data[i] = data2D[i][k];
		double[] xm = CNScatterPlotHandler.linear(data.length,maxX);
		CNScatterPlotHandler scatter = new CNScatterPlotHandler(xm, data, lab);
		
		double[] xMarks = {0,maxX};
		double[] yMarks = {0,1};
		scatter.setMarks(xMarks, yMarks);
		
	}
	
	public static void graphVarySquareFromSave(String slot, double maxX, double maxY, String lab) {
		graphVarySquareFromSave(slot,maxX,maxY,lab,1.0);
	}
	
	public static void graphVarySquareFromSave(String slot, double maxX, double maxY, String lab, double maxColor) {
		double[][] data2D = DataManager.loadData2(slot);
		CNVarySquare square = new CNVarySquare(data2D, lab);
		
		
		Color midColor = new Color(205,50,255);
		//Color[] colors = {Color.yellow, Color.magenta, Color.blue};
		Color[] colors = {Color.yellow, midColor, Color.blue};
		
		
		
		//Color[] colors = {Color.yellow, Color.blue};
		square.setCustomColors(data2D, maxColor, colors);
		double[] extraLine1 = {0,0.5,1,0.5};
		double[] extraLine2 = {0.5,0,0.5,1};
		if(data2D.length == 101) square.height = (int)(101.0/100*square.height);
		if(data2D[0].length == 101) square.width = (int)(101.0/100*square.width);
		//square.addExtraLineInternal(extraLine1,1,1);
		//square.addExtraLineInternal(extraLine2,1,1);
		
		square.repaint();
		
		
		CNVarySquare.simpleKey(colors);
		
	}
	
	
	public static void graphScatterFromSave2(String slot1, String slot2, String lab) {
		double[] data1 = DataManager.uncurlData2to1(DataManager.loadData2(slot1));
		double[] data2 = DataManager.uncurlData2to1(DataManager.loadData2(slot2));
		CNScatterPlotHandler scatter = new CNScatterPlotHandler(data1,data2,lab);
		scatter.generatePointColorsByGradient((int)Math.sqrt(data1.length), (int)Math.sqrt(data1.length));
		
	}
	
	
	public static void actionScatterSlices(String slot, String lab) {
		//String slot = "homophily2D_3";
		//String slot = "homophily2D_socOnly2";
		//String slot = "homophily2D_healthOnly";
		//graphScatterSliceFromSave(slot, 5, false, 1, "homophily (y) as a function of Soc/Health discrepancy, vert 0.05");
		//graphScatterSliceFromSave(slot, 51, false, 1, "homophily (y) as a function of Soc/Health discrepancy, vert 0.5");
		//graphScatterSliceFromSave(slot, 5, true, 180, "homophily (y) as a function of r, horz 0.05");
		//graphScatterSliceFromSave(slot, 51, true, 180, "homophily (y) as a function of r, horz 0.5");
		graphVarySquareFromSave(slot, 1,1,lab);
	}
	
	
	public static double stdDevOfInf(CNSimulator sim) {
		double[] data = new double[sim.population.length];
		for(int i = 0; i < data.length; i++) data[i] = sim.population[i].infRate;
		return standardDeviation(data);
	}
	
	public static double standardDeviation(double[] data) {
		double avg = 0;
		for(int i = 0; i < data.length; i++) avg += data[i]/data.length;
		double dev = 0;
		for(int i = 0; i < data.length; i++) dev += (data[i]-avg)*(data[i]-avg)/data.length;
		double std = Math.sqrt(dev);
		return std;		
	}
	
	
	public static void quickFormulaTest(int time) {
		CNSimulator sim = new CNSimulator();
		for(int i = 0; i < time; i++) sim.simDay();
		
		System.out.println("Quick Formula Test:");
		double gam = sim.recoveryRate; double beta = sim.transmission;
		for(int i = 0; i < sim.population.length; i++) {
			System.out.println("Group " + i + ":");
			double sig = sim.population[i].socVal;  double rho = sim.population[i].healthVal;
			
			double formulaA = (gam * rho + sig)/(rho * beta);
			double formulaI = (sig)/(gam * rho + sig);
			System.out.println("formulaA: " + formulaA);
			System.out.println("actualA: " + sim.population[i].rememberSoc);
			System.out.println("formulaI: " + formulaI);
			System.out.println("actualI: " + sim.population[i].infRate);			
			
		}
		
		
	}
	
	
	
	public static void graphSocVersusConnection(int time) {
		NetworkSimulator sim = new NetworkSimulator();
		for(int i = 0; i < time; i++) sim.simDay();
		int[] type = new int[sim.agents.length];
		double[] soc = new double[sim.agents.length];
		double[] totConn = new double[sim.agents.length];
		double[] highConn = new double[sim.agents.length];
		double[] lowConn = new double[sim.agents.length];
		double[] sameConn = new double[sim.agents.length];
		double[] homRatio = new double[sim.agents.length];
		Color[] scatterColors = new Color[sim.agents.length];
		for(int i = 0; i < sim.agents.length; i++) {
			type[i] = sim.agents[i].groupIndex;
			soc[i] = sim.agents[i].rememberSoc;
			totConn[i] = sim.countConn(i);
			highConn[i] = sim.countHigherConn(i);
			lowConn[i] = sim.countLowerConn(i);
			sameConn[i] = sim.countSameConn(i);
			homRatio[i] = sim.getHomSocRatio(i);
			if(type[i] < 2) scatterColors[i] = new Color(0,200,250*type[i]);
			else scatterColors[i] = new Color(250*(type[i]-2),0,0);
		}
		
		CNScatterPlotHandler socTot = new CNScatterPlotHandler(totConn, soc, "Soc vs TotConn");
		socTot.pointColors = scatterColors; socTot.repaint();
		CNScatterPlotHandler socHigh = new CNScatterPlotHandler(highConn, soc, "Soc vs HighConn");
		socHigh.pointColors = scatterColors; socHigh.repaint();
		CNScatterPlotHandler socLow = new CNScatterPlotHandler(lowConn, soc, "Soc vs LowConn");
		socLow.pointColors = scatterColors; socLow.repaint();
		CNScatterPlotHandler socSame = new CNScatterPlotHandler(sameConn, soc, "Soc vs SameConn");
		socSame.pointColors = scatterColors; socSame.repaint();
		
		CNScatterPlotHandler homRatScatter = new CNScatterPlotHandler(soc, homRatio, "HomRatio vs Soc");
		homRatScatter.pointColors = scatterColors; homRatScatter.repaint();
		CNScatterPlotHandler homConn = new CNScatterPlotHandler(totConn, homRatio, "HomRatio vs totConn");
		homConn.pointColors = scatterColors; homConn.repaint();
		
		String[] labels = {"Player Type","Socialization","Total Connections", "GEQ Social Connections", "LEQ Social Connections", "Homophilic Connections"};
		double[][] data = new double[6][sim.agents.length];
		data[0] = intToDouble(type);
		data[1] = soc;
		data[2] = totConn;
		data[3] = highConn;
		data[4] = lowConn;
		data[5] = sameConn;
		String[][] cells = dataToCSVString(data, labels);
		saveToCSV(cells, "NetworkSimulatorDataTemp");
		
		makeConnectionScatter(sim);
		
	}
	
	
	public static void graphSatVersusConnMeasure(int time, int timeAvg) {
		NetworkSimulator sim = new NetworkSimulator();
		for(int i = 0; i < time; i++) sim.simDay();
		System.out.println("initialSim Complete");
		double[] satisfaction = new double[sim.agents.length];
		for(int i = 0; i < timeAvg; i++) {
			double[] instantSatisfaction = sim.computeSatisfaction();
			for(int j = 0; j < instantSatisfaction.length; j++) satisfaction[j] += instantSatisfaction[j]/timeAvg;
		}
		int numLayer = 20;
		double[][] connectivityMeasureNHB = sim.connectivityMeasure(numLayer,false);
		double[][] connectivityMeasureHB = sim.connectivityMeasure(numLayer,true);
		Color[] scatterColors = makeScatterColors(sim);
		for(int k = 0; k < numLayer; k+=6) {
			String s = "Sat Vs ConnMeasure " + k;
			CNScatterPlotHandler scatter = new CNScatterPlotHandler(connectivityMeasureNHB[k], satisfaction, s);
			scatter.pointColors = scatterColors; scatter.repaint();
		}
		for(int k = 0; k < numLayer; k+=6) {
			String s = "Sat Vs ConnMeasureHomBonus " + k;
			CNScatterPlotHandler scatter = new CNScatterPlotHandler(connectivityMeasureHB[k], satisfaction, s);
			scatter.pointColors = scatterColors; scatter.repaint();
		}
	}
	
	
	
	public static double[] intToDouble(int[] list) {
		double[] data = new double[list.length];
		for(int i = 0; i < list.length; i++) data[i] = list[i];
		return data;
	}
	
	public static String[][] dataToCSVString(double[][] data, String[] labels){
		String[][] cells = new String[data.length][data[0].length+1];
		for(int y = 0; y < labels.length; y++) {
			cells[y][0] = labels[y];
			for(int x = 0; x < data[y].length; x++) {
				cells[y][x+1] = "" + data[y][x];
			}
		}
		return cells;
	}
	
	public static String[][] dataToCSVString(int[][] data, String[] labels){
		String[][] cells = new String[data.length][data[0].length+1];
		for(int y = 0; y < labels.length; y++) {
			cells[y][0] = labels[y];
			for(int x = 0; x < data[y].length; x++) {
				cells[y][x+1] = "" + data[y][x];
			}
		}
		return cells;
	}
	
	public static void saveToCSV(String saveString, String name) {
		String folder = "C:/Users/Matthew/Desktop/Conway Tex/SIR stuff/Covid Network Stuff/eclipseSaves/";
		String path = folder + name + ".csv";
	    try (PrintWriter writer = new PrintWriter(path)) {
	        writer.write(saveString);
	        System.out.println("saveToCSV complete");

	      } catch (FileNotFoundException e) {
	        System.out.println(e.getMessage());
	      }
	}
	
	
	public static void saveToCSV(String[][] cells, String name) {
		String folder = "C:/Users/Matthew/Desktop/Conway Tex/SIR stuff/Covid Network Stuff/eclipseSaves/";
		String path = folder + name + ".csv";
	    try (PrintWriter writer = new PrintWriter(path)) {

	        StringBuilder sb = new StringBuilder();
	        for(int y = 0; y < cells.length; y++) {
	        	if(y > 0) sb.append('\n');
	        	for(int x = 0; x < cells[y].length; x++) {
	        		if(x > 0) sb.append(',');
	        		sb.append(cells[y][x]);
	        	}
	        }

	        writer.write(sb.toString());

	        System.out.println("saveToCSV complete");

	      } catch (FileNotFoundException e) {
	        System.out.println(e.getMessage());
	      }

	  }
	
	
	public static void makeConnectionScatter(NetworkSimulator sim) {
		double groupGap = 10;
		double individualGap = 2;
		//doublecounts each connection since i-j and j-i are graphed separately
		int numCon = 0;
		for(int i = 0; i < sim.connections.length; i++) {
			for(int j = 0; j < sim.connections[i].length; j++) {
				if(sim.connections[i][j] > 0) numCon ++;
			}
		}
		double offset = individualGap/3/numCon; //to help similar points not fall on top of each other
		double[] xPoints = new double[numCon];
		double[] yPoints = new double[numCon];
		int index = 0;
		for(int i = 0; i < sim.connections.length; i++) {
			int type1 = sim.agents[i].groupIndex;
			for(int j = 0; j < sim.connections[i].length; j++) {
				int type2 = sim.agents[j].groupIndex;
				if(sim.connections[i][j] > 0 && (type1 != type2 || i < j)) {
					xPoints[index] = groupGap*type1+individualGap*type2+index*offset;
					yPoints[index] = Math.min(sim.socialDesires[i][j], sim.socialDesires[j][i]);
					index++;
				}
			}
		}
		
		CNScatterPlotHandler scatter = new CNScatterPlotHandler(xPoints,yPoints, "Socialization of each Connection as a function of connection pair type");
		double[] xMarks = {0,groupGap*4};
		double[] yMarks = {0,5};
		
		
	}
	
	public static Color[] makeScatterColors(NetworkSimulator sim) {
		Color[] scatterColors = new Color[sim.agents.length];
		for(int i = 0; i < sim.agents.length; i++) {
			int type = sim.agents[i].groupIndex;
			if(type < 2) scatterColors[i] = new Color(0,200,250*type);
			else scatterColors[i] = new Color(250*(type-2),0,0);
		}
		return scatterColors;
	}
	
	public static void drawDottedLine(Graphics2D g2d, int x1, int y1, int x2, int y2, int on, int off) {
		
		double length = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		double incOn = (0.0+on)/length;
		double inc = (0.0+on+off)/length;
		for(double r = 0; r < 1-incOn; r+=inc) {
			int xx1 = (int)((1-r)*x1+r*x2);
			int yy1 = (int)((1-r)*y1+r*y2);
			double rr = r+incOn;
			int xx2 = (int)((1-rr)*x1+rr*x2);
			int yy2 = (int)((1-rr)*y1+rr*y2);
			g2d.drawLine(xx1, yy1, xx2, yy2);
		}	
	}
	
	
	public static void testInfectionRole() {
		int numPre = 10000; int numPost = 10000;
		NetworkSimulator sim = new NetworkSimulator();
		for(int i = 0; i < numPre; i++) sim.simDay();
		sim.resetHistory();
		for(int i = 0; i < numPost; i++) sim.simDay();
		double[] avgWeird = new double[4];
		double[] avgNorm = new double[4];
		int numWeird = 0;
		int numNorm = 0;
		for(int c = 0; c < sim.communities.length; c++) {
			int typeOne = sim.communities[c][0].groupIndex;
			int typeTwo = sim.communities[c][1].groupIndex;
			System.out.println("Community typeOne " + typeOne + " typeTwo " + typeTwo);
			for(int i = 0; i < 2; i++) {
				String s = "nonconformist";
				if(i == 1) s = "normal1";
				System.out.println(s);
				System.out.println("inf from inside: " + (0.0+sim.communities[c][i].roleHistory[0])/numPost);
				System.out.println("inf from outside: " + (0.0+sim.communities[c][i].roleHistory[1])/numPost);
				System.out.println("inf to inside: " + (0.0+sim.communities[c][i].roleHistory[2])/numPost);
				System.out.println("inf to outside: " + (0.0+sim.communities[c][i].roleHistory[3])/numPost);
			}
			if(typeOne != typeTwo) {
				numWeird++;
				numNorm += sim.communities[c].length-1;
				for(int q = 0; q < 4; q++) {
					avgWeird[q] += sim.communities[c][0].roleHistory[q];
					for(int i = 1; i < sim.communities[c].length; i++) {
						avgNorm[q] += sim.communities[c][i].roleHistory[q];
					}
				}
			}
		}
		for(int q = 0; q < 4; q++) {
			avgWeird[q] /= numWeird*numPost;
			avgNorm[q] /= numNorm*numPost;
			System.out.println("avgWeird[" + q + "]: " + avgWeird[q]);
			System.out.println("avgNorm[" + q + "]: " + avgNorm[q]);
		}
		
		
		
	}
	
	public static void epidemiologyTracker(int mainType) {
		epidemiologyTracker(mainType, 5, false, 0, null);
	}
	
	public static double[][] epidemiologyTracker(int mainType, int numRepeat, boolean silent, double minSoc, StatisticsManager statMan) {
		int numPre = 2000; int numPost = 10000;
		
		int numCommunities = 16; //these don't actually do anything, and are overwritten by sim
		int comSize = 10;
		
		Epidemiologist[] council = new Epidemiologist[numRepeat];
		String[] names = new String[4];
		double[][] islAll = new double[numRepeat][4];
		int[][] fullHistory;
		double[] mandatedSoc = new double[4];
		double[] voluntarySoc = new double[4];
		double totalInfections = 0;
		double numCom=16;
		double sizeCom=10;
		for(int r = 0; r < numRepeat; r++) {
			NetworkSimulator sim = new NetworkSimulator(mainType);
			for(int t = 0; t < 4; t++) names[t] = sim.population[t].name;
			for(int i = 0; i < numPre; i++) {
				if(minSoc <= 0) sim.simDay();
				else {
					if(!maxSocHack)	sim.simDayMinCom(minSoc);
					else sim.simDayMaxCom(minSoc); //TODO, maybe clean this up properly instead of hacking it like this
				}
			}
			islAll[r] = checkInternalSocLevels(sim, 1000, mainType);
			sim.resetHistory();
			sim.epi = new Epidemiologist(sim);
			for(int i = 0; i < numPost; i++) {
				if(minSoc <= 0) sim.simDay();
				else {
					if(!maxSocHack)	sim.simDayMinCom(minSoc);
					else sim.simDayMaxCom(minSoc); //same as above
				}
			}
			sim.epi.closeAllEpidemics(); 
			council[r] = sim.epi;
			numCommunities = sim.communities.length;
			comSize = sim.communities[0].length;
			totalInfections += sim.totalInfections/numRepeat;
			
			if(!maxSocHack) {
			for(int i = 0; i < sim.agents.length; i++) {
				int c = sim.findCommunityOfAgent(sim.agents[i]);
				int type = sim.agents[i].groupIndex;
				int numMandate = sim.agentToComCon(i)[c]; //number of community connections, each with minSoc
				mandatedSoc[type] += minSoc*numMandate;
				double volSoc = sim.agents[i].rememberSoc-minSoc*numMandate;
				//
				if(volSoc < 0) {
				if(volSoc < -0.01) { System.out.println("volSoc < -0.01  totSoc " + sim.agents[i].rememberSoc + " manSoc " + minSoc*numMandate);
				for(int j = 0; j < sim.agents.length; j++) System.out.print(Math.min(sim.socialDesires[i][j], sim.socialDesires[j][i])+" ");
				System.out.println("");
				//return null;
				}
				else volSoc = 0;
				}
				//
				voluntarySoc[type] += sim.agents[i].rememberSoc-minSoc*numMandate;
			}
			}
			numCom = sim.communities.length;
			sizeCom = sim.communities[0].length;
			
			if(statMan != null && r == 0) statMan.recordEpidemiologist(sim.epi); //TODO statmanRecord
		}
		
		for(int t = 0; t < 4; t++) {
			double normalizer = 1.0/(numCom*numRepeat);
			if(t == mainType) normalizer /= (sizeCom-3);
			mandatedSoc[t] *= normalizer;
			voluntarySoc[t] *= normalizer;
		}
		
		double[] isl = arrayAverager(islAll);
		//double nonconformistNormalizer = 20000.0/numCommunities/numPost/numRepeat;
		//double conformistNormalizer = 20000.0/numCommunities/numPost/numRepeat/(comSize-3);
		double nonconformistNormalizer = 1.0;
		double conformistNormalizer = 1.0/(comSize-3);
		
		int max = Epidemiologist.maxHistory;
		//double[][] histoData = new double[4][max+1];
		double[][] histoData = new double[4][max+1];
		double[] weightedSum = new double[4];
		double[] numEpidemic = new double[4];
		fullHistory = council[0].epidemicHistory2; //only functions well if numRep = 1;
		for(int r = 0; r < council.length; r++) {
		//	for(int c = 0; c < council[r].epidemicHistory2.length; c++) {
				for(int type = 0; type < council[r].epidemicHistory2.length; type++) {
		//			ArrayList<Integer> list = council[r].epidemicHistory[c][i];
		//			for(int j = 0; j < list.size(); j++) {
					double norm = nonconformistNormalizer;
					if(type == mainType) norm = conformistNormalizer;
					weightedSum[type] += norm*council[r].epidemicWeightedSum[type];
					numEpidemic[type] += norm*council[r].numEpidemics(type);
					for(int j = 0; j < council[r].epidemicHistory2[type].length; j++) {
						histoData[type][j] += norm*council[r].epidemicHistory2[type][j];
					}
					
		//		}
			}
		}
		
		//plot histoData
		if(!silent) {
		double[] bounds = new double[max+1];
		for(int i = 0; i < max+1; i++) bounds[i] = i;
		DecimalFormat df = new DecimalFormat("#");
		for(int t = 0; t < 4; t++) {
			double[] data = histoData[t];
			System.out.print(names[t]+" ");
			if(t == mainType) System.out.print("*");
			else System.out.print(" ");
			for(int i = 0; i < data.length; i++) {
				System.out.print(df.format(data[i]) + " ");
			}
			System.out.println("");
			String type = "nonconformist" + t;
			if(t == 3) type = "conformist";
			String lab = "Epidemics Caused by " + type;
			CNHistogramHandler histogram = new CNHistogramHandler(bounds, data, lab);
			System.out.println("weighted sum: " + weightedSum[t]);
			System.out.println("weighted sum div comSoc: " + weightedSum[t]/isl[t]);
		}
		}
		double[] tI = {totalInfections}; //hack to make it fit in 2D array.  consider changing to track types separately
		double[][] report = {weightedSum, mandatedSoc, voluntarySoc, numEpidemic, tI};
		return report;
		
		
	}
	
	public static double[] checkInternalSocLevels(NetworkSimulator sim, int time, int mainType) {
		
		//consider moving this to NetworkSimulator and just saving it
		int[][] indexAll = new int[sim.communities.length][sim.communities[0].length];	
		for(int c = 0; c < sim.communities.length; c++) {
			for(int i = 0; i < sim.communities[c].length; i++) {
				indexAll[c][i] = java.util.Arrays.asList(sim.agents).indexOf(sim.communities[c][i]);
			}
		}	
		double[][] islAll = new double[sim.communities.length][sim.communities[0].length];
		double[] isl = new double[4];
		for(int t = 0; t < time; t++) {
			sim.simDay();
			for(int c = 0; c < sim.communities.length; c++) {
				for(int i = 0; i < sim.communities[c].length; i++) {
					int in1 = indexAll[c][i];
					for(int j = i+1; j < sim.communities[c].length; j++) {
						int in2 = indexAll[c][j];
						double soc = Math.min(sim.socialDesires[in1][in2],sim.socialDesires[in2][in1]);
						islAll[c][i] += soc;
						islAll[c][j] += soc;
					}
				}
			}			
		}
		for(int c = 0; c < sim.communities.length; c++) {
			for(int i = 0; i < sim.communities[c].length; i++) {
				double norm = 1.0/(time)/(sim.communities.length);
				if(sim.communities[c][i].groupIndex == mainType) norm /= (sim.communities[c].length-3);
				isl[sim.communities[c][i].groupIndex] += norm*islAll[c][i];
			}
		}
		//
		//for(int i = 0; i < isl.length; i++) System.out.println("internal soc level " + i + ": " + isl[i]);
		//
		return isl;
	}
	
	
	public static double[] arrayAverager(double[][] data) {
		double[] output = new double[data[0].length];
		for(int a = 0; a < data.length; a++) {
			for(int b = 0; b < data[a].length; b++) {
				output[b] += data[a][b]/data.length;
			}
		}
		return output;
	}
	
	
	public static void safePocketTracker(int mainType) {
		//sees who never gets infected, and then tracks their relationships to each other
		
		int initial = 10000;
		int after = 5000;
		int rep = 5;
		double spon = 0.001;
		for(int r = 0; r <= rep; r++) {
			NetworkSimulator sim = new NetworkSimulator(mainType);
			sim.spontaneousGeneration = spon/Math.pow(10, r);
			System.out.println("Spontaneous Generation: " + sim.spontaneousGeneration);
			for(int t = 0; t < initial; t++) sim.simDay();
			boolean[][] clean = new boolean[sim.communities.length][sim.communities[0].length];
			for(int i = 0; i < clean.length; i++) for(int j = 0; j < clean[i].length; j++) clean[i][j] = true;
			for(int t = 0; t < after; t++) {
				sim.simDay();
				for(int i = 0; i < clean.length; i++) for(int j = 0; j < clean[i].length; j++) if(sim.communities[i][j].infected) clean[i][j] = false;
			}
			ArrayList<Agent> cleanOnes = new ArrayList<Agent>();
			for(int i = 0; i < clean.length; i++) for(int j = 0; j < clean[i].length; j++) if(clean[i][j]) cleanOnes.add(sim.communities[i][j]);
			if(cleanOnes.size()==0) System.out.println("no cleanOnes");
			else {
				paintSafeZones(sim, cleanOnes);
				System.out.println("Safe Zones.  Total clean: " + cleanOnes.size());
				ArrayList<ArrayList<int[]>> safeZones = partitionSafeZones(sim, cleanOnes);
				for(int i = 0; i < safeZones.size(); i++) {
					ArrayList<int[]> zone = safeZones.get(i);
					for(int j = 0; j < zone.size(); j++) {
						System.out.print(zone.get(j)[0] + "-" + zone.get(j)[1] +"  ");
					}
					System.out.println("");
				}
				
				
			}

		}
	}
	
	public static void safePocketStatistics(int mainType) {
	//sees who never gets infected, and then tracks their relationships to each other
	//reports number of cleanOnes over many trials
		int initial = 10000;
		int after = 5000;
		int sponNum = 5;
		int rep = 20;
		double spon = 0.001;
		double[][] data = new double[sponNum*2][rep];
		String[] lab = new String[sponNum*2];
		String saveName = "safePocketStats_"+mainType+"-"+rep;
		
		for(int s = 0; s < sponNum; s++) {
			double sSpon = spon/Math.pow(10, s);
			if(s==sponNum-1) sSpon = 0;
			lab[s] = ""+sSpon;
			lab[s+sponNum] = "b " + sSpon;
			System.out.println("Starting spon: " + sSpon);
			for(int r = 0; r < rep; r++) {
				NetworkSimulator sim = new NetworkSimulator(mainType);
				sim.spontaneousGeneration = sSpon;
				for(int t = 0; t < initial; t++) sim.simDay();
				boolean[][] clean = new boolean[sim.communities.length][sim.communities[0].length];
				for(int i = 0; i < clean.length; i++) for(int j = 0; j < clean[i].length; j++) clean[i][j] = true;
				for(int t = 0; t < after; t++) {
					sim.simDay();
					for(int i = 0; i < clean.length; i++) for(int j = 0; j < clean[i].length; j++) if(sim.communities[i][j].infected) clean[i][j] = false;
				}
				ArrayList<Agent> cleanOnes = new ArrayList<Agent>();
				for(int i = 0; i < clean.length; i++) for(int j = 0; j < clean[i].length; j++) if(clean[i][j]) cleanOnes.add(sim.communities[i][j]);
				data[s][r] = cleanOnes.size();
				if(data[s][r]>0) data[s+sponNum][r] = 1;
			}
		}
		String[][] saveString = dataToCSVString(data,lab);
		saveToCSV(saveString,saveName);
		System.out.println("safePocketStatistics complete");
	}
	
	
	public static void safePocketStatistics2(int r) {
		//saves more detailed information about connectivity for cleanOnes in one trial
			System.out.println("starting safePocketStatistics2.  r: " +r);
			int mainType = 2;
			int initial = 10000;
			int after = 5000;
			double spon = 0;
			int numClean = 32;
			double[][] data = new double[2*numClean+1][numClean];
			String[] lab = new String[2*numClean+1];
			String saveName = "safePocketConnections_"+2
					;
			
			
			NetworkSimulator sim = new NetworkSimulator(mainType);
			sim.spontaneousGeneration = spon;
			double[][] avgSoc = new double[sim.agents.length][sim.agents.length];
			for(int t = 0; t < initial; t++) sim.simDay();
			boolean[][] clean = new boolean[sim.communities.length][sim.communities[0].length];
			for(int i = 0; i < clean.length; i++) for(int j = 0; j < clean[i].length; j++) clean[i][j] = true;
			for(int t = 0; t < after; t++) {
				sim.simDay();
				for(int i = 0; i < clean.length; i++) for(int j = 0; j < clean[i].length; j++) if(sim.communities[i][j].infected) clean[i][j] = false;
				for(int i = 0; i < avgSoc.length; i++) for(int j = 0; j < avgSoc.length; j++) avgSoc[i][j]+=Math.min(sim.socialDesires[i][j],sim.socialDesires[j][i])/after;
			}
			ArrayList<Agent> cleanOnes = new ArrayList<Agent>();
			for(int i = 0; i < clean.length; i++) for(int j = 0; j < clean[i].length; j++) if(clean[i][j]) cleanOnes.add(sim.communities[i][j]);
			if(cleanOnes.size() != numClean) {
				if(r > 20) {
					System.out.println("safePocketStatistics2 looped 20 times without getting 32 cleanOnes");
					return;
				}
				else safePocketStatistics2(r+1);
			}
			else {
			for(int i = 0; i < cleanOnes.size(); i++) {
				int pi = java.util.Arrays.asList(sim.agents).indexOf(cleanOnes.get(i));
				int ci = sim.findCommunityOfAgent(cleanOnes.get(i));
				int di = sim.findIndexInCommunity(ci, cleanOnes.get(i));
				lab[i] = ci+"--"+di;
				lab[i+numClean+1] = lab[i];
				for(int j = 0; j < cleanOnes.size(); j++) {
					int pj = java.util.Arrays.asList(sim.agents).indexOf(cleanOnes.get(j));
					if(sim.connections[pi][pj] > 0) data[i][j] = 1;
					data[i+numClean+1][j] = avgSoc[pi][pj];
				}
			}
			
			String[][] saveString = dataToCSVString(data,lab);
			saveToCSV(saveString,saveName);
			paintSafeZones(sim, cleanOnes);
			System.out.println("safePocketStatistics2 complete");	
		}
	}
	
	public static ArrayList<ArrayList<int[]>> partitionSafeZones(NetworkSimulator sim, ArrayList<Agent> cleanOnes){
		//for use in safePocketTracker.  Partitions the passed agents into connected components based on nonzero socialization
		ArrayList<ArrayList<int[]>> safeZones = new ArrayList<ArrayList<int[]>>();
		while(!cleanOnes.isEmpty()) {
			ArrayList<int[]> zone = new ArrayList<int[]>();
			int c0 = sim.findCommunityOfAgent(cleanOnes.get(0));
			int d0 = sim.findIndexInCommunity(c0, cleanOnes.get(0));
			int p0 = java.util.Arrays.asList(sim.agents).indexOf(cleanOnes.get(0));
			int[] clean0 = {c0,d0,p0};
			zone.add(clean0);
			cleanOnes.remove(0); //at least one is removed every loop, guaranteeing no infinite loops
			//recursively finds everyone in the same connected component as agent 0
			for(int i = 0; i < zone.size(); i++) {
				int pi = zone.get(i)[2];
				for(int n = cleanOnes.size()-1; n >= 0; n--) {
					Agent a = cleanOnes.get(n);
					int pa = java.util.Arrays.asList(sim.agents).indexOf(a);
					if(sim.socialDesires[pi][pa] > 0 && sim.socialDesires[pa][pi] > 0) {
						//add agent a to list
						int ca = sim.findCommunityOfAgent(a);
						int da = sim.findIndexInCommunity(ca, a);
						int[] cleana = {ca,da,pa};
						zone.add(cleana);
						cleanOnes.remove(n);						
					}
				}
			}
			safeZones.add(zone);	
		}
		return safeZones;			
	}
	
	
	public static void paintSafeZones(NetworkSimulator sim, ArrayList<Agent> cleanOnes) {
		double rad = 400;
		int[] center = {450,450};
		final int smallRad = 10;
		final int[][] coords = new int[cleanOnes.size()][2];
		final String[] labels = new String[cleanOnes.size()];
		final boolean[][] conn = new boolean[cleanOnes.size()][cleanOnes.size()];
		for(int i = 0; i < cleanOnes.size(); i++) {
			double angle = i*2*Math.PI/cleanOnes.size();
			coords[i][0] = (int) (center[0] + rad*Math.cos(angle));
			coords[i][1] = (int) (center[0] + rad*Math.sin(angle));
			int pi = java.util.Arrays.asList(sim.agents).indexOf(cleanOnes.get(i));
			int ci = sim.findCommunityOfAgent(cleanOnes.get(i));
			int di = sim.findIndexInCommunity(ci, cleanOnes.get(i));
			labels[i] = ci+"-"+di;
			for(int j = i+1; j < cleanOnes.size(); j++) {
				int pj = java.util.Arrays.asList(sim.agents).indexOf(cleanOnes.get(j));
				if(sim.socialDesires[pi][pj] > 0 && sim.socialDesires[pj][pi] > 0) {
					conn[i][j] = true; conn[j][i] = true;
				}
			}
		}
		JFrame window = new JFrame("sub window");
		JPanel panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                g2d.setColor(Color.white);
                g2d.fillRect(0, 0, 2000, 2000);
                g2d.setColor(Color.black);
                for(int i = 0; i < coords.length; i++) {
                	g2d.drawOval(coords[i][0]-smallRad,coords[i][1]-smallRad,2*smallRad+1,2*smallRad+1);
                	g2d.drawString(labels[i], coords[i][0]-5, coords[i][1]-1);
                	for(int j = i+1; j < conn[i].length; j++) {
                		if(conn[i][j]) g2d.drawLine(coords[i][0], coords[i][1], coords[j][0], coords[j][1]);
                	}
                }
                
            }
        };
        window.add(panel);
        window.setSize(1400, 1000);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
	}
	
	public static void infectionVsMinSoc(int mainType) {
		//Epidemic Attribution
		
		int socNum = 20;
		//int socNum = 10;
		//int socNum = 2;
		int rep = 10;
		//int rep = 1;
		
		//double socInc = 0.05;
		//double socInc = 0.1;
		double socInc = 0.3/socNum;
		//double socInc = 1.0/socNum;
		
		double[][] data = new double[4*(socNum+1)][rep+5]; //gap and 4 slots for extra stuff on the end
		String[] lab = new String[4*(socNum+1)];
		String saveName = "minSocStatsEpivo_"+mainType+"-"+rep+"v4";
		double[][][] socInfo = new double[socNum+1][4][2];
		double[][][] epiInfo = new double[socNum+1][4][2];
		double[] totalInfections = new double[socNum+1];
		
		
		StatisticsManager[] statMans = new StatisticsManager[socNum+1]; //a separate one for each minSoc
		
		for(int s = 0; s <= socNum; s++) {
			statMans[s] = new StatisticsManager(rep);
			double minSoc = s*socInc;
			for(int i = 0; i < 4; i++) lab[4*s+i] = "m: "+minSoc+" t: "+i;
			System.out.println("Starting minSoc: " + minSoc);
			double[] totalWeightedSum = new double[4];
			double[] totalNumEpi = new double[4];
			for(int r = 0; r < rep; r++) {
				double[][] report = epidemiologyTracker(mainType,1, true, minSoc, statMans[s]);
				double[] weightedSum = report[0];
				for(int i = 0; i < 4; i++) data[4*s+i][r] = weightedSum[i];
				//
				double[] mandatedSoc = report[1];
				double[] volSoc = report[2];
				double[] numEpi = report[3];
				for(int t = 0; t < 4; t++) {
					socInfo[s][t][0] += mandatedSoc[t]/rep; //amount of socialization directly caused by minimum soc
					socInfo[s][t][1] += volSoc[t]/rep; //amount of socialization not caused by minimum soc
					epiInfo[s][t][0] += numEpi[t]/rep; //total number of epidemics caused by Type
					
					totalWeightedSum[t] += weightedSum[t];
					totalNumEpi[t] += numEpi[t];
					//epiInfo[s][t][1] += (weightedSum[t]/numEpi[t])/rep; //avg length of each epidemic (weighted incorrectly)
				}	
			}
			for(int t = 0; t < 4; t++) epiInfo[s][t][1] = totalWeightedSum[t]/totalNumEpi[t]; //avg length of epidemic weighted properly
			for(int i = 0; i < 4; i++) {
				data[4*s+i][rep+1] = socInfo[s][i][0]; //avg mandatedSoc
				data[4*s+i][rep+2] = socInfo[s][i][1]; //avg volSoc
				data[4*s+i][rep+3] = epiInfo[s][i][0]; //numEpi
				data[4*s+i][rep+4] = epiInfo[s][i][1]; //avgEpiLength
				//TODO, try to get better horizontal labels in saveToCSV
			}
			statMans[s].setParameters(mainType, 16, 10);
			statMans[s].saveSelf("statManTest_" + minSoc);
		}
		//for(int a = 0; a < data.length; a++) for(int b = 0; b < data[a].length; b++) data[a][b] = Math.round(data[a][b]);
		String[][] saveString = dataToCSVString(data,lab);
		saveToCSV(saveString,saveName);
		
		System.out.println("infectionVsMinSoc complete");
		
		
		
	}
	
	
	public static void statTester() {
		String name = "statManTest_0.4";
		StatisticsManager statMan = StatisticsManager.load(name);
		
		double[][] epiSize = statMan.statsEpidemicSize();
		System.out.println("epiSize output");
		for(int i = 0; i < epiSize.length; i++) {
			for(int j = 0; j < epiSize[i].length; j++) System.out.print(epiSize[i][j] + " ");
			System.out.println("");
		}
		double[][] epiNumber = statMan.statsNumEpidemics(10000); //make sure this matches the amount actually used by the data (TODO, save the value in the data file?)
		System.out.println("epiNumber output");
		for(int i = 0; i < epiNumber.length; i++) {
			for(int j = 0; j < epiNumber[i].length; j++) System.out.print(epiNumber[i][j] + " ");
			System.out.println("");
		}
		double[][] weightedSum = statMan.statsWeightedSum(10000); //make sure this matches the amount actually used by the data (TODO, save the value in the data file?)
		System.out.println("weightedSum output");
		for(int i = 0; i < weightedSum.length; i++) {
			for(int j = 0; j < weightedSum[i].length; j++) System.out.print(weightedSum[i][j] + " ");
			System.out.println("");
		}
	}
	
	public static void statisticsLoaderToCSV() {
		
		//String saveFolder = "C:/Users/Matthew/Desktop/Conway Tex/SIR stuff/Covid Network Stuff/eclipseSaves/";
		String saveFolder = "C:/Users/Matthew/Desktop/Conway Tex/SIR stuff/Covid Network Stuff/eclipseSaves/homMinSoc1/";

		//String[] loadNames = {"statManTest_0.0","statManTest_0.1","statManTest_0.2","statManTest_0.3","statManTest_0.4","statManTest_0.5","statManTest_0.6","statManTest_0.7","statManTest_0.8","statManTest_0.9","statManTest_1.0"};
		String[] loadNames = {"statManTest_0.0","statManTest_0.015","statManTest_0.03","statManTest_0.045","statManTest_0.06","statManTest_0.075","statManTest_0.09","statManTest_0.105","statManTest_0.12","statManTest_0.135","statManTest_0.15","statManTest_0.165","statManTest_0.18","statManTest_0.195","statManTest_0.21","statManTest_0.225","statManTest_0.24","statManTest_0.255","statManTest_0.27","statManTest_0.285","statManTest_0.3"};
		String name2 = "consolodated_min_t2";
		
		//takes a collection of CSV of raw data from epidemiologist, does statistics, and saves to a new CSV of graphable data
		double[][] data = new double[16][3*loadNames.length+2];
		String[] labels = {"Misc data","[empty row]","ASRA","ASRT","SORA","SORT","[empty row]","ASRA error","ASRT error","SORA error","SORT error", "[empty row]", "ASRA normalized","ASRT normalized","SORA normalized","SORT normalized"};
		
		for(int i = 0; i < loadNames.length; i++) {
			StatisticsManager statMan = StatisticsManager.load(loadNames[i], saveFolder);
			System.out.println("loaded " + i);
			double[][] epiSize = statMan.statsEpidemicSize();
			double[][] epiNumber = statMan.statsNumEpidemics(10000);
			double[][] weightedSum = statMan.statsWeightedSum(10000);
			int xJump = loadNames.length+1;
			if(i==0) {
				data[0][0] = statMan.mainType;
				data[0][1] = statMan.comSize;
				data[0][2] = statMan.numCom;
			}
			double wNorm = 1.0/statMan.numCom;
			double mainNorm = wNorm / statMan.comSize;
			for(int t = 0; t < 4; t++) {
				double norm = wNorm;
				if(t == statMan.mainType) norm = mainNorm;
				
				data[t+2][i] = epiSize[t][0];
				data[t+7][i] = epiSize[t][1];
				data[t+12][i] = epiSize[t][0]/epiSize[statMan.mainType][0]; //normalized by mainType
				
				data[t+2][i+xJump] = epiNumber[t][0]*norm;
				data[t+7][i+xJump] = epiNumber[t][1]*norm;
				data[t+12][i+xJump] = norm/mainNorm*epiNumber[t][0]/epiNumber[statMan.mainType][0];
				
				data[t+2][i+2*xJump] = weightedSum[t][0]*norm;
				data[t+7][i+2*xJump] = weightedSum[t][1]*norm;
				data[t+12][i+2*xJump] = norm/mainNorm*weightedSum[t][0]/weightedSum[statMan.mainType][0];
			}
			for(int t = 0; t < 4; t++) {
				
			}
			
		}
		
		String[][] saveSet = dataToCSVString(data,labels);
		saveToCSV(saveSet, name2);
		
		
	}
	
	
	
	public static void blameVsMinSoc(int mainType) {
		//copy paste most of the structure from infectionVsMinSoc and epidemiologyTracker
		//but measure blame stuff instead of discrete epidemics
		//and make more robust?
		
		boolean pseudoMode = true;
		if(pseudoMode) System.out.println("blameVsMinSoc: pseudoMode true");
		
		int socNum = 10;
		int rep = 10;
		//int socNum = 2;
		//int rep = 1;
		
		double maxSoc = 0.3;
		double socInc = maxSoc/socNum;
		
		//double[][] data = new double[4*(socNum+1)][rep+5]; //gap and 4 slots for extra stuff on the end
		double[][] data = new double[20][socNum+1];
		//String saveName = "blameMinSoc_"+mainType+"-"+rep+"-d0.9";
		String saveName = "pseudoBlameTest_"+mainType;
		//String saveName = "blameMinSocTempTest";
		
		//double[][][] blame = new double[socNum+1][rep][4];
		//double[][][] dBlame = new double[socNum+1][rep][4];
		//double[] totalInfections = new double[socNum+1];

		for(int s = 0; s <= socNum; s++) {
			double minSoc = s*socInc;
			System.out.println("Starting minSoc: " + minSoc);
			ArrayList<Double>[] rawDBlame = new ArrayList[4];
			ArrayList<Double>[] rawBlame = new ArrayList[4];
			for(int i = 0; i < 4; i++) {rawDBlame[i] = new ArrayList<Double>(); rawBlame[i] = new ArrayList<Double>();}
			
			for(int r = 0; r < rep; r++) {
				//double[][] report = epidemiologyTracker(mainType,1, true, minSoc, statMans[s]);
				int numPre = 10000; int numPost = 10000;	
				NetworkSimulator sim = new NetworkSimulator(mainType);
				if(pseudoMode) sim.pseudoInfections = true;
				int numCommunities = sim.communities.length;
				int comSize = sim.communities[0].length;
				//run sims
				for(int i = 0; i < numPre; i++) {
					if(minSoc <= 0) sim.simDay();
					else {
						if(!maxSocHack)	sim.simDayMinCom(minSoc);
						else sim.simDayMaxCom(minSoc); //TODO, maybe clean this up properly instead of hacking it like this
					}
				}
				sim.resetHistory();
				for(int i = 0; i < numPost; i++) {
					if(minSoc <= 0) sim.simDay();
					else {
						if(!maxSocHack)	sim.simDayMinCom(minSoc);
						else sim.simDayMaxCom(minSoc);
					}
				}
				for(int a = 0; a < sim.agents.length; a++) { //add blame data to arraylists
					int t = sim.agents[a].groupIndex;
					if(!pseudoMode) rawDBlame[t].add(sim.sumDBlame2[a]);
					else {
						rawDBlame[t].add(sim.sumPBlame[a]);
						//System.out.println("PBlame["+a+"] = " + sim.sumPBlame[a]+" added to type " +t);
					}
					rawBlame[t].add(sim.sumBlame2[a]);
				}
				//System.out.println("avg sponBlame: " + sim.sumSponBlame/sim.agents.length);
				
			} //end rep
			//get mean and std dev after all repetitions are gathered
			for(int t = 0; t < 4; t++) {
				data[t][s] = StatisticsManager.getMean(rawDBlame[t]);
				System.out.println("mean rawDBlame["+t+"]["+s+"]: " + data[t][s]);
				data[t+5][s] = StatisticsManager.getStdDevPop(rawDBlame[t],data[t][s]);
				data[t+10][s] = StatisticsManager.getMean(rawBlame[t]);
				data[t+15][s] = StatisticsManager.getStdDevPop(rawBlame[t],data[t+10][s]);
			}
		}
		String[] lab = new String[data.length];
		for(int i = 0; i < 4; i++) {
			String[] prefix = {"dAttMean", "dAttErr", "0.9AttMean", "0.9AttErr"};
			for(int t = 0; t < 4; t++) lab[t+5*i] = prefix[i]+"_"+t;
		}
		
		String[][] saveString = dataToCSVString(data,lab);
		saveToCSV(saveString,saveName);
		

	}

	
	
	
	public static void parallelNetworks(int mainType) {
		boolean confSwitch = false; //if false, nonconformists change to conformists.  if true, conformists change to nonconformists
		String saveName = "parallelNetworksNCBonus_v1";
		if(confSwitch) saveName = "parallelConf_v1";
		double minSoc = 0;
		parallelNetworks(mainType, confSwitch, saveName,minSoc);
	}
	
	
	public static void parallelNetworks(int mainType, boolean confSwitch, String saveName, double minSoc) {
		
		
		//makes a bunch of nearly identical networks, swaps the type for a single agent from each, and compares the change in infections
		int aSubset = 3; //only the first three are nonconformists
		int comSubset = 5; //only some communities are measured to save time
		
		
		//int numRep = 4; //repeat with multiple initial simSets to make statistics more robust
		int chunkSize = 4; int numChunks = 4;
		int numRep = numChunks * chunkSize;
		NetworkSimulator[][] simSet = new NetworkSimulator[numRep][aSubset*comSubset+1];
		int[][] typeTracker = new int[numRep][simSet[0].length];
		int[][] comTracker = new int[numRep][simSet[0].length];
		double[][] data = new double[4][numRep*(simSet[0].length+1)];
		
		int r = 0;
		for (int c = 0; c < numChunks; c++) {
			ArrayList<Thread> threads = new ArrayList<Thread>();
			for(int rc = 0; rc < chunkSize; rc++) {
				MultithreadingTest2 treddie = new MultithreadingTest2(r, simSet, typeTracker, comTracker, data, mainType, confSwitch, minSoc, aSubset, comSubset);
				treddie.start();	
				threads.add(treddie);
				r++;
			}
			for(Thread t: threads) {
		        try {
		            // waiting for thread2 to die
		            t.join();
		        }
		        catch (InterruptedException e) {
		            e.printStackTrace();
		        }
		       System.out.print("thread closed, ");    
		    }
			System.out.println("");
			System.out.println("all threads closed for chunk " + c + " minSoc " + minSoc);
		}
		
		//for(int r = 0; r < numRep; r++) {
		//			parallelNetworkOneRep(r, simSet, typeTracker, comTracker, data, mainType, confSwitch, minSoc, aSubset, comSubset);
		//}
		String[] lab = {"nonconformist type", "Total Infections","Infections in Community of Changer","Infections in Community (original)"};
		String[][] saveString = dataToCSVString(data,lab);
		saveToCSV(saveString,saveName);
	}
	
	static class MultithreadingTest2 extends Thread {
		//there has to be a better way than passing all of these in like this
		public int r; public NetworkSimulator[][] simSet; public int[][] typeTracker; public int[][] comTracker; public double[][] data; public int mainType; public boolean confSwitch; public double minSoc; public int aSubset; public int comSubset;
		public MultithreadingTest2(int R, NetworkSimulator[][] ss, int[][] tt, int[][] ct, double[][] d, int mt, boolean cs, double ms, int as, int coms) {
			r = R; simSet = ss; typeTracker = tt; comTracker = ct; data = d; mainType = mt; confSwitch = cs; minSoc = ms; aSubset = as; comSubset = coms;
			//is it possible to pass these directly into run()?
		}	
	    public void run()
	    {
	        try {
	        	parallelNetworkOneRep(r, simSet, typeTracker, comTracker, data, mainType, confSwitch, minSoc, aSubset, comSubset);
	        }
	        catch (Exception e) {
	            System.out.println("Exception is caught in ActionManager.MultithreadingTest.run()");
	        }
	    }
	}
	
	
	public static void parallelNetworkOneRep(int r, NetworkSimulator[][] simSet, int[][] typeTracker, int[][] comTracker, double[][] data, int mainType, boolean confSwitch, double minSoc, int aSubset, int comSubset){
		int numPre = 2000;
		int numPost = 10000;    int numCheck = numPost/3;
		//int numPre = 1; int numPost = 1; int numCheck = 1;
		
		
		simSet[r][0] =  new NetworkSimulator(mainType);
		typeTracker[r][0] = -1;
		
		int index = 1; //skips 0 so the first network is unmodified
		for(int i = 0; i < aSubset; i++) {
			for(int c = 1; c < comSubset+1; c++) {
				NetworkSimulator sim = simSet[r][0].hackClone();
				typeTracker[r][index] = sim.communities[c][i].groupIndex;
				comTracker[r][index] = c;
				//sim.exciseAgent(sim.findIndexInAll(sim.communities[c][i]));
				int changerIndex = i; //nonconformist in slot i changes to conformist
				int modelIndex = 4;
				if(confSwitch) {
					changerIndex = 4; //conformist in slot 4 changes to nonconformist
					modelIndex = i;
				}
				
				Agent a = sim.communities[c][changerIndex];
				Agent b = sim.communities[c][modelIndex];
				if(!confSwitch && b.groupIndex != mainType) System.out.println("warning, parallelNetworks conforming process is copying a nonconformist");
				a.groupIndex = b.groupIndex;					
				a.socVal = b.socVal;
				a.healthVal = b.healthVal;
				simSet[r][index] = sim;
				index++;
			}
		}
	
		for(int i = 0; i < numPre; i++) parallelSimDay(simSet[r],minSoc);
		for(int i = 0; i < simSet[r].length; i++) simSet[r][i].resetHistory();
		for(int i = 0; i < numPost; i++) {
			parallelSimDay(simSet[r],minSoc);
			if(i%numCheck ==0) System.out.println("finished day " + i);
		}
		//totalInfections should be what we care about
		//for(int i = 0; i < simSet[r].length; i++) System.out.println("i: "+i+" t: "+typeTracker[r][i]+" totalInfections: "+simSet[r][i].totalInfections);
		//for(int i = 0; i < simSet[r].length; i++) System.out.println("i: "+i+" t: "+typeTracker[r][i]+" comInfections: "+simSet[r][i].comInfections[comTracker[r][i]]);
		double[] avgDiff = new double[4];
		double[] avgComDiff = new double[4];
		int[] numType = new int[4];
		for(int i = 1; i < simSet[r].length; i++) {
			avgDiff[typeTracker[r][i]] += simSet[r][i].totalInfections-simSet[r][0].totalInfections; //number of additional infections in modified network
			avgComDiff[typeTracker[r][i]] += simSet[r][i].comInfections[comTracker[r][i]]-simSet[r][0].comInfections[comTracker[r][i]]; //number of additional infections restricted to modified community
			numType[typeTracker[r][i]]++;
		}
		for(int t = 0; t < 4; t++) 	if(numType[t] != 0 && r==0) System.out.println("avgDiff " + t +": "+(avgDiff[t]/numType[t]));
	
		for(int t = 0; t < 4; t++) 	if(numType[t] != 0 && r==0) System.out.println("avgComDiff " + t +": "+(avgComDiff[t]/numType[t]));
		
		for(int i = 0; i < simSet[r].length; i++) {
			data[0][i+r*(simSet[0].length+1)] = typeTracker[r][i];
			data[1][i+r*(simSet[0].length+1)] = simSet[r][i].totalInfections;
			if(i != 0){
				data[2][i+r*(simSet[0].length+1)] = simSet[r][i].comInfections[comTracker[r][i]];
				data[3][i+r*(simSet[0].length+1)] = simSet[r][0].comInfections[comTracker[r][i]];
			}
		}	
	}
	
	public static void parallelSimDay(NetworkSimulator[] simSet, double minSoc) {
		//all simulators simDay with the same set of random values for possible infections
		//any difference in resulting infection must be caused by differences in behavior in the present or past
		Random randy = new Random();
		int ag = simSet[0].agents.length;
		double[][] rSet = new double[ag][ag];
		for(int i = 0; i < ag; i++) {
			for(int j = 0; j < ag; j++) {
				rSet[i][j] = randy.nextDouble();
			}
		}
		if(minSoc <=0) for(int i = 0; i < simSet.length; i++) simSet[i].simDay(rSet);	
		else for(int i = 0; i < simSet.length; i++) simSet[i].simDayMinCom(rSet,minSoc);	
	}
	
	
	
	
	public static void parallelMinSoc(int mainType) {
		int socNum = 10;
		double minSoc = 0.3;
		double socInc = minSoc/socNum;
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		for(double soc = 0; soc <= minSoc+0.000001; soc+=socInc) {
			System.out.println("starting soc " + soc);
			String saveName = "parallelMinSoc_v4_"+soc;
			parallelNetworks(mainType,false,saveName, soc);
			//MultithreadingTest treddie = new MultithreadingTest(mainType,false,saveName, soc);
			//treddie.start();	
			//threads.add(treddie);
		}
		//for(Thread t: threads) {
	    //    try {
	            // waiting for thread2 to die
	     //       t.join();
	     //   }
	     //   catch (InterruptedException e) {
	     //       e.printStackTrace();
	     //   }
	     //   System.out.println("thread closed");      
	    //}
		//System.out.println("all threads closed, parallelMinSoc complete");
		System.out.println("parallelMinSoc complete");
	}
	
	public static void convertParallelMinSocToStats(int mainType) {
		
		int socNum = 10;
		int comSubset = 5; //make sure these match the amounts that were run in parallelNetworks when the data was generated
		int numRep = 16;  //The responsible thing to do would be to save them in the CSV files generated and then read them here
		String[] saveList = new String[socNum+1];
		double socInc = 0.3/socNum;
		double[][] finalData = new double[16][socNum+1];
		for(int i = 0; i <= socNum; i++) {
			finalData[0][i] = socNum;
			double soc = socInc*i;
			String saveName = "parallelMinSoc_v4_"+soc;
			
			int[][][][] rawData = new int[3][5][numRep][comSubset];
			//double[][] data = DataManager.loadData2(saveName);
			double[][] data = DataManager.loadFromCSV(saveName);
			//
			//System.out.println("data size ["+data.length+"]["+data[0].length+"]");
			//
			//first column is labels.  first row is group types that we can infer from comSubset and numRep
			for(int d = 0; d < 3; d++) {
				int index = 0;
				for(int r = 0; r < numRep; r++) {
					index++; //skips a blank slot before each rep in the CSV file
					for(int t = -1; t < 4; t++) {
						if(t==-1) {
							rawData[d][4][r][0] = (int) data[d+1][index];
							index++;
							continue;
						}
						else if(t == mainType) continue;
						else for(int c = 0; c < comSubset; c++) {
							rawData[d][t][r][c] = (int) data[d+1][index];
							index++;
						}
					}
				}
				
			}
			// do averages and means to rawData stuff
			double[][][] dif = new double[2][3][comSubset*numRep];
			for(int d = 0; d < 2; d++) {
				for(int t = 0; t < 3; t++) {
					int bump = 0;
					if (t >= mainType) bump = 1;
					int index = 0;
					for(int r = 0; r < numRep; r++) {
						for(int c = 0; c < comSubset; c++) {
							if(d==0) dif[d][t][index] = rawData[d][t+bump][r][c]-rawData[d][4][r][0];
							else dif[d][t][index] = rawData[d][t+bump][r][c]-rawData[d+1][t+bump][r][c];
							index++;
						}
					}
				}
			}
			double[][] statDif = new double[4][3];
			for(int d = 0; d < 2; d++) {
				for(int t = 0; t < 3; t++) {
					statDif[d][t] = StatisticsManager.getMean(dif[d][t]);
					statDif[d+2][t] = StatisticsManager.getStdDevPop(dif[d][t], statDif[d][t]);
					finalData[1+t+4*d][i]=statDif[d][t];
					finalData[9+t+4*d][i]=statDif[d+2][t];
				}
			}
		}
		String saveName = "parallelMinSocStat_v4";
		//labels
		int[] types = new int[3];
		String[] prefix = {"popDif","comDif","popErr","comErr"};
		int jn = 0; for(int i = 0; i < 4; i++) if(i != mainType) {types[jn] = i; jn++; }
		String[] lab = new String[finalData.length];
		for(int d = 0; d < 4; d++) {
			for(int j = 0; j < 3; j++){
				lab[4*d+j+1] = prefix[d]+" "+types[j];
			}
		}
		//
		String[][] saveString = dataToCSVString(finalData,lab);
		saveToCSV(saveString,saveName);		
	}
	
	public static void convertParallelMinSocToInfOnly(int mainType){
		//temp hack?
		//only extracts total infection for unchanged networks
		
		int socNum = 10;
		int comSubset = 5; //make sure these match the amounts that were run in parallelNetworks when the data was generated
		int numRep = 16;  //The responsible thing to do would be to save them in the CSV files generated and then read them here
		String[] saveList = new String[socNum+1];
		double socInc = 0.3/socNum;
		double[][] finalData = new double[3][socNum+1];
		for(int i = 0; i <= socNum; i++) {
			double soc = socInc*i;
			//hack to fix rounding error?
			if(soc > 0.3) soc = 0.3;
			finalData[0][i] = soc;
			String saveName = "parallelMinSoc_v4_"+soc;
			//
			//System.out.println("soc " + soc);
			//

			double[][] data = DataManager.loadFromCSV(saveName);
			ArrayList<Double> infData = new ArrayList<Double>();

			for(int x = 1; x < data[0].length; x++) {
				if(data[0][x] == -1) {
					infData.add(data[1][x]);
					//System.out.println("add " + data[1][x]);
				}
			}
			finalData[1][i] = StatisticsManager.getMean(infData);
			//System.out.println("mean: " + finalData[1][i]);
			finalData[2][i] = StatisticsManager.getStdDevPop(infData, finalData[1][i]);
			
		}
		String saveName = "parallelInfData_v1";
		String[] lab = {"MinSoc","Mean infections","Std Pop Err"};
		String[][] saveString = dataToCSVString(finalData,lab);
		saveToCSV(saveString,saveName);		
	}
	
	
	static class MultithreadingTest extends Thread {
		//This is probably obsolete.
		//issue, this requires massive run time before any individual thread saves
		//also, many seem to crash from memory issues.
		//MultithreadingTest2 implements multiple threads within a single minSoc, and works much better
		public int mainType;
		public boolean confSwitch;
		String saveName;
		public double minSoc;
		public MultithreadingTest(int mt, boolean b, String sn, double s) {
			mainType = mt; confSwitch = b; saveName = sn; minSoc = s;
			//is it possible to pass these directly into run()?
		}	
	    public void run()
	    {
	        try {
	        	parallelNetworks(mainType,confSwitch,saveName, minSoc);
	        }
	        catch (Exception e) {
	            System.out.println("Exception is caught in ActionManager.MultithreadingTest.run()");
	        }
	    }
	}
	
	
	
	public static void homophilyVsMinSoc(int mainType) {
		//measures two types of homophily as a function of minSoc
		int socNum = 10;

		double minSoc = 0.3;
		double socInc = minSoc/socNum;
		for(double soc = 0; soc <= minSoc+0.000001; soc+=socInc) {
			System.out.println("starting soc " + soc);
			String saveName = "homophilyMinSoc_v1_"+soc;
			homophilyOne(mainType,saveName, soc);
		}

		System.out.println("homophilyVsMinSoc complete");
	}
	
	public static void homophilyOne(int mainType, String saveName, double minSoc) {

		int numPre = 2000;
		int numPost = 10000;
		int numRep = 16;

		double[][] finalData = new double[3][160*numRep];
		
		for(int r = 0; r < numRep; r++) {
			NetworkSimulator sim = new NetworkSimulator(mainType);
			for(int i = 0; i < numPre; i++) sim.simDayMinCom(minSoc);
			double[][][] hom = new double[numRep][sim.agents.length][2];
			for(int i = 0; i < numPost; i++) {
				sim.simDayMinCom(minSoc);
				double[][] tHom = sim.getHomophilies();
				for(int a = 0; a < tHom.length; a++) for(int j = 0; j < 2; j++) {
					if(hom[r][a][j] == -7 || tHom[a][j] == -7) hom[r][a][j] = -7; //hack to indicate DNE
					else hom[r][a][j] += tHom[a][j]/numPost;	
				}
			}
			for(int x = 0; x < 160; x++) {
				finalData[0][x+160*r] = sim.agents[x].groupIndex;
				for(int j = 0; j < 2; j++) finalData[j+1][x+160*r] = hom[r][x][j];
			}	
		}
		String[] lab = {"Agent Type","Type Homophily","Community Homophily"};
		//save data to CSV here
		String[][] saveString = dataToCSVString(finalData,lab);
		saveToCSV(saveString,saveName);		
	}
	
	public static void convertHomophilyMinSocToStats(int mainType) {
		//for each minSoc
		//load data for all members, sorted by agent type
		//find average and stdErr (average should automatically normalize by number of each type and numreps)
		//save into CSV	
		
		//warning, the homophilyVsMinSoc method will pass -7s to indicate DNE, make sure those are handled gracefully
		
		int socNum = 10;
		//String[] saveList = new String[socNum+1];
		double socInc = 0.3/socNum;
		double[][] finalData = new double[20][socNum+1];
		for(int i = 0; i <= socNum; i++) {
			finalData[0][i] = socNum;
			double soc = socInc*i;
			String saveName = "homophilyMinSoc_v1_"+soc;

			double[][] data = DataManager.loadFromCSV(saveName);
			ArrayList<Double>[] agentHomList = new ArrayList[4];
			ArrayList<Double>[] comHomList = new ArrayList[4];
			for(int j = 0; j < 4; j++) {
				agentHomList[j] = new ArrayList<Double>();
				comHomList[j] = new ArrayList<Double>();
			}
			
			//first column is labels.
			for(int x = 1; x < data[0].length; x++) {
				int index = (int)data[0][x];
				if(data[1][x] != -7) agentHomList[index].add(data[1][x]);
				if(data[2][x] != -7) comHomList[index].add(data[2][x]);
			}

			// do averages and means to rawData stuff
			for(int j = 0; j < 4; j++) {
				double agentHomMean = StatisticsManager.getMean(agentHomList[j]);
				double agentHomErr = StatisticsManager.getStdDevPop(agentHomList[j], agentHomMean);
				double comHomMean = StatisticsManager.getMean(comHomList[j]);
				double comHomErr = StatisticsManager.getStdDevPop(comHomList[j], comHomMean);
				
				finalData[j][i] = agentHomMean;
				finalData[j+5][i] = comHomMean;
				finalData[j+10][i] = agentHomErr;
				finalData[j+15][i] = comHomErr;				
			}

		}
		String saveName = "homMinSocStat_v1";
		//labels
		String[] prefix = {"typeHom","comHom","typeHomErr","comHomErr"};
		String[] lab = new String[finalData.length];
		for(int d = 0; d < prefix.length; d++) {
			for(int j = 0; j < 4; j++){
				lab[5*d+j] = prefix[d]+" "+j;
			}
		}
		//
		String[][] saveString = dataToCSVString(finalData,lab);
		saveToCSV(saveString,saveName);		
		
	}
	

	public static void scatterImageLoader() {
		//plots multiple scatterplots staggered and colored from data from a square grid in a CSV file
		
		
		
		//loadStatsFromCSV(filename, startX, numX, startY, mainType)
		//mainType doesn't actually do anything unless normY is set to True in loadStatsFromCSV method, but it's usually false
		
		//Homophily
		//CNScatterPlotHandler.loadStatsFromCSV("homMinSocStat_v1", 1, 11, 0,2); //Type homophily
		//CNScatterPlotHandler.loadStatsFromCSV("homMinSocStat_v1", 1, 11, 11,2); //Community homophily
		
		//Direct Attribution, values should be 1,11,0
		CNScatterPlotHandler.loadStatsFromCSV("blameMinSoc_0-10-d0.9", 1, 11, 0,0);
		CNScatterPlotHandler.loadStatsFromCSV("blameMinSoc_1-10-d0.9", 1, 11, 0,1);
		CNScatterPlotHandler.loadStatsFromCSV("blameMinSoc_2-10-d0.9", 1, 11, 0,2);
		CNScatterPlotHandler.loadStatsFromCSV("blameMinSoc_3-10-d0.9", 1, 11, 0,3);
		
		//r Attribution, values should be 1,11,11
		//CNScatterPlotHandler.loadStatsFromCSV("blameMinSoc_0-10-d0.9", 1, 11, 11,0);
		//CNScatterPlotHandler.loadStatsFromCSV("blameMinSoc_1-10-d0.9", 1, 11, 11,1);
		//CNScatterPlotHandler.loadStatsFromCSV("blameMinSoc_2-10-d0.9", 1, 11, 11,2);
		//CNScatterPlotHandler.loadStatsFromCSV("blameMinSoc_3-10-d0.9", 1, 11, 11,3);
		
		//Epidemic Attribution, values for weighted sum should be 45,21,1
		//CNScatterPlotHandler.loadStatsFromCSV("consolodated_min_t0", 45, 21, 1,0);
		//CNScatterPlotHandler.loadStatsFromCSV("consolodated_min_t1", 45, 21, 1,1);
		//CNScatterPlotHandler.loadStatsFromCSV("consolodated_min_t2", 45, 21, 1,2);
		//CNScatterPlotHandler.loadStatsFromCSV("consolodated_min_t3", 45, 21, 1,3);
		
		//Epidemic Attribution Breakdown for t2
		//CNScatterPlotHandler.loadStatsFromCSV("consolodated_min_t2", 1, 21, 1,2); //Epi Size
		//CNScatterPlotHandler.loadStatsFromCSV("consolodated_min_t2", 23, 21, 1,2); //Num Epi
		
		
		
		
		//CNScatterPlotHandler.loadStatsFromCSV("parallelMinSocStat_v4", 14, 11, 0,2,true);  //Global Attribution
		//CNScatterPlotHandler.loadStatsFromCSV("parallelMinSocStat_v4", 14, 11, 11,2,true);  //Community Attribution
		
	}
	

}

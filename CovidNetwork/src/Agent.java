import java.text.DecimalFormat;
import java.util.ArrayList;

public class Agent {

	
	
	public double socVal;
	public double confVal;
	public double healthVal;
	
	public int groupIndex;
	public int simIndex;
	
	public boolean exposed = false;
	public boolean infected = false;
	public int infectionTime = 0;
	
	public double rememberSoc;
	
	public static double incSoc = 0.01;
	public int newChunk = 5;
	
	
	int[] roleHistory;
	
	public double maxSoc = 5; //set negative to disable max
	
	//used for assigning blame/credit for infections in the 
	//public static double legacyR = 0.01;
	public static double legacyR = 0.9;
	public double[] infectionBlame;
	public ArrayList<Agent> infectors;
	public ArrayList<Agent> pseudoInfectors;
	
	
	
	public Agent(double s, double c, double h) {
		socVal = s;
		confVal = c;
		healthVal = h;
		resetHistory();
		infectors = new ArrayList<Agent>();
		pseudoInfectors = new ArrayList<Agent>();
	}
	
	public Agent copy() {
		Agent copy = new Agent(socVal, confVal, healthVal);
		copy.groupIndex = groupIndex;
		copy.infected = infected;
		copy.infectionTime = infectionTime;
		copy.rememberSoc = rememberSoc;
		return copy;
	}
	
	
	
	public void updateSoc(NetworkSimulator sim, int i) {
		
		//80% of runtime is here right now, 10% in this method directly, 70% in utility Estimator
		//I suspect there are a lot of redundant utility calculations happening here
		//idea 1: only check updateSoc if something in the environment has actually changed for agent i (either a neighbor changed their social desires, or an infection changed)
		//idea 2: try to predict which utility calculations will be the same, store the values, and then paste them in the spot
		//idea 3: try to predict which interaction changes are obviously bad and can be skipped without explicit calculation
		
		
		//utility function = socVal * sqrt(total social) - healthVal * (probability of getting infected)
		//derivative = socVal * 1/(2sqrt(totalsocial) - healthVal * (infection rate of target group)
			
		double[] interactions = new double[sim.agents.length];
		for(int j = 0; j < sim.agents.length; j++) interactions[j] = Math.min(sim.socialDesires[i][j], sim.socialDesires[j][i]);

		double baseUtility = utilityEstimator(sim, interactions,i);
		for(int j = 0; j < sim.agents.length; j++) {
			if(sim.connections[i][j] > 0) {
			//
				//if(i==14) {
				//	DecimalFormat df = new DecimalFormat("#.000");
				//	System.out.println("connection " + i + "," + j + ": " + df.format(sim.connections[i][j]));
				//}
			//double[] copyInteractions = interactions.clone();
			//10% of CPU usage was the above line alone.
			//It feels like better practice to alter a copy of the array rather than change the original and then change it back at the end
			//But it's not worth 10% of my speed.  But since only one entry changes, now we just store that one value in oldJ, and change it back at the end
			double oldJ = interactions[j];
			double[] copyInteractions = interactions;
			copyInteractions[j] += incSoc;
			double upUtility = utilityEstimator(sim, copyInteractions,i);
			copyInteractions[j] -= 2*incSoc;  if(copyInteractions[j] < 0) copyInteractions[j] = 0;
			double downUtility = utilityEstimator(sim, copyInteractions,i);
			//
			copyInteractions[j] = oldJ;
			if(upUtility == Math.max(baseUtility, Math.max(upUtility, downUtility))) sim.socialDesires[i][j] += incSoc;
			if(downUtility == Math.max(baseUtility, Math.max(upUtility, downUtility))) {
				sim.socialDesires[i][j] -= incSoc;
				if(sim.newConnections && sim.socialDesires[i][j] <= 0 && sim.connections[i][j] == sim.strengthNew) sim.dropConnections(i, j);
			}
			

	
			if(sim.socialDesires[i][j] < 0) sim.socialDesires[i][j] = 0;
			if(maxSoc > 0 && sim.socialDesires[i][j] > maxSoc) sim.socialDesires[i][j] = maxSoc;
			if(sim.socialDesires[i][j] > sim.socialDesires[j][i] + incSoc*4) sim.socialDesires[i][j] = sim.socialDesires[j][i] + incSoc*4;	
			}
		}
		
		
	}
	
	
	public double utilityEstimator(NetworkSimulator sim, double[] interactions, int i) {
		boolean includeConf = false;
	
	//diagnostic is temp variable so it only prints on certain groups
	
	
	
		double sumInt = 0; double sumInf = 0; double sumConf = 0;
	
		for(int j = 0; j < interactions.length; j++) {
			sumInt += interactions[j] * sim.connections[i][j];
			sumInf += interactions[j] * sim.population[sim.agents[j].groupIndex].infRate * sim.transmission;
		}
		double utility = socVal * Math.sqrt(sumInt) - healthVal * sumInf;
		//double avgInt = sim.getAverageInt();
		
		//TODO, update conf for agent-based model, or remove
		//if(includeConf) utility -= confVal*(sumInt - avgInt)*(sumInt - avgInt);
		
		//if(i == 14) System.out.println("utility: " + utility);
		
		

		
	
		return utility;

	}
	
	public boolean considerConnection(NetworkSimulator sim, int i, int nw) {
		
		
		
		//utility function = socVal * sqrt(total social) - healthVal * (probability of getting infected)
		//derivative = socVal * 1/(2sqrt(totalsocial) - healthVal * (infection rate of target group)
		
	
		
		double[] interactions = new double[sim.agents.length];
		for(int j = 0; j < sim.agents.length; j++) interactions[j] = Math.min(sim.socialDesires[i][j], sim.socialDesires[j][i]);

		double baseUtility = utilityEstimator(sim, interactions,i);

		double[] copyInteractions = interactions.clone();
		copyInteractions[nw] += 5*incSoc;
		double upUtility = utilityEstimator(sim, copyInteractions,i);
		if(upUtility == Math.max(baseUtility, upUtility)) return true;
		else return false;

	}
	
	public void resetHistory() {
		roleHistory = new int[4]; //mostly used for debugging and intuition
		//0: times infected by inside Community
		//1; times infected by outside Community
		//2; times gave infection to inside Community
		//3; times gave infection to outside Community
		infectionBlame = new double[160]; //hack, properly should pass in number of agents in sim
	}
	
	public double[] directBlame() {
		//make sure this is called before applyBlame, which wipes infectors
		int d = infectors.size();
		double[] blame = new double[4];
		for(int i = 0; i < d; i++) blame[infectors.get(i).groupIndex] += 1.0/d;
		return blame;
	}
	
	public void directBlame2(double[] sumDBlame2) {
		//same as directBlame, but assigns to individuals, not grouping by type
		int d = infectors.size();
		for(int i = 0; i < d; i++)	sumDBlame2[infectors.get(i).simIndex] += 1.0/d;
	}
	
	public void directPseudoBlame(double[] sumPBlame) {
		//same as DBlame, but triggers even when already infected
		int d = pseudoInfectors.size();
		for(int i = 0; i < d; i++)	sumPBlame[pseudoInfectors.get(i).simIndex] += 1.0/d;
		pseudoInfectors = new ArrayList<Agent>();
	}
	
	public double[] applyBlame() {
		//the person actually infecting this agent is assigned 1/2 the blame
		//the person who infected that person gets 1/4, the person who infected them gets 1/8, etc...
		//or whatever exponential series is defined by legacyR
		//blame is grouped by type and stored in infectionBlame array
		//if multiple infectors, blame is further divided among them
		int d = infectors.size();
		infectionBlame = new double[4];
		for(int i = 0; i < d; i++) {
			Agent a = infectors.get(i);
			for(int t = 0; t < 4; t++) {
				infectionBlame[t] += a.infectionBlame[t]*legacyR/d;
			}
			infectionBlame[a.groupIndex] += (1-legacyR)/d;
		}
		infectors = new ArrayList<Agent>(); //clear this so it's empty next infection cycle
		return infectionBlame;
	}
	
	
	public void applyBlame2(double[] sumBlame2) {
		//same as applyBlame, but applies to individual agents instead of grouping by type
		int d = infectors.size();
		infectionBlame = new double[sumBlame2.length];
		for(int i = 0; i < d; i++) {
			Agent a = infectors.get(i);
			for(int t = 0; t < sumBlame2.length; t++) {
				infectionBlame[t] += a.infectionBlame[t]*legacyR/d;
			}
			infectionBlame[a.simIndex] += (1-legacyR)/d;
		}
		infectors = new ArrayList<Agent>(); //clear this so it's empty next infection cycle
		//return infectionBlame;
		for(int t = 0; t < sumBlame2.length; t++) sumBlame2[t] += infectionBlame[t];
	}

	
	
	
	
}

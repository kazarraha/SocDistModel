import java.text.DecimalFormat;

public class Agent {

	
	
	public double social;
	public double conformity;
	public double health;
	
	public int groupIndex;
	
	public boolean exposed = false;
	public boolean infected = false;
	public int infectionTime = 0;
	
	public double rememberSoc;
	
	double incSoc = 0.01;
	
	
	
	public Agent(double s, double c, double h) {
		social = s;
		conformity = c;
		health = h;
	}
	
	
	
	public void updateSoc(NetworkSimulator sim, int i) {
		
		
		
		
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
			//	
			double[] copyInteractions = interactions.clone();
			copyInteractions[j] += incSoc;
			double upUtility = utilityEstimator(sim, copyInteractions,i);
			copyInteractions[j] -= 2*incSoc;  if(copyInteractions[j] < 0) copyInteractions[j] = 0;
			double downUtility = utilityEstimator(sim, copyInteractions,i);
			if(upUtility == Math.max(baseUtility, Math.max(upUtility, downUtility))) sim.socialDesires[i][j] += incSoc;
			if(downUtility == Math.max(baseUtility, Math.max(upUtility, downUtility))) sim.socialDesires[i][j] -= incSoc;
			

	
			if(sim.socialDesires[i][j] < 0) sim.socialDesires[i][j] = 0;
			if(sim.socialDesires[i][j] > sim.socialDesires[j][i] + incSoc*4) sim.socialDesires[i][j] = sim.socialDesires[j][i] + incSoc*4;	
			}
		}
		
		
	}
	
	
	public double utilityEstimator(NetworkSimulator sim, double[] interactions, int i) {
		boolean includeConf = false;
	
	//diagnostic is temp variable so it only prints on certain groups
	
		double baseSoc = 1;
		double bonusSoc = 4;
		double socVal = baseSoc + bonusSoc*social;
	
		double baseHealth = 10;
		double bonusHealth = 10;
		double healthVal = baseHealth + bonusHealth*health;
	
		double baseConformity = 0.3;
		double bonusConformity = 1;
		double confVal = baseConformity + bonusConformity*conformity;
	
	
		double sumInt = 0; double sumInf = 0; double sumConf = 0;
	
		for(int j = 0; j < interactions.length; j++) {
			sumInt += interactions[j] * sim.connections[i][j];
			sumInf += interactions[j] * sim.population[sim.agents[j].groupIndex].infRate * sim.transmission;
		}
		double utility = socVal * Math.sqrt(sumInt) - healthVal * sumInf;
		double avgInt = sim.getAverageInt();
		
		//TODO, update conf for agent-based model
		if(includeConf) utility -= confVal*(sumInt - avgInt)*(sumInt - avgInt);
		
		//if(i == 14) System.out.println("utility: " + utility);
		
		

		
	
		return utility;

	}

	
	
	
}

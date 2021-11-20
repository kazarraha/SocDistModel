import java.util.ArrayList;

public class Group {

	//settings
	public boolean includeConf = false;
	public boolean logMode = true;
	//public boolean socialFriction = true; //partial homophily
	public boolean socialFriction = false;
	
	
	
	//socialFriction parameters
	double maxSocBonus = 3;
	double socDecay = 1.0/2;
	double socNormalizer = 1;
	
	
	public double social;
	public double conformity;
	public double health;
	
	public double popSize;
	public double infRate;
	
	public double rememberSoc = 1;
	
	public ArrayList<Double> infHistory;
	public ArrayList<Double> socHistory;
	
	//double incSoc = 0.001
	double incSoc = 0.002;
	
	
	
	public Group(double soc, double conf, double heal, double size, double inf) {
		social = soc;
		conformity = conf;
		health = heal;
		
		popSize = size;
		infRate = inf;
		
		infHistory = new ArrayList<Double>();
		socHistory = new ArrayList<Double>();
		
		
	}
	
	
	//public double getDesiredSoc(double averageInf) {
	//	//very crude decisionmaking mechanism
	//	double output = 1;
	//	boolean crude = true;
	//	if(crude) {
	//	if(social >= 1) output += 0.5;
	//	if(averageInf >= 0.8 || (averageInf >= 0.2 && health >= 1)) output -= 0.5; 
	//	}
		
	//	return output;
	//}
	
	
	public void updateSoc(CNSimulator sim, int i, double timeScale) {
		//TODO: indexing issue: players update lower indexed player socials immediately, which influences
		//future utility calculations
		
		
		double tincSoc = incSoc*timeScale;
		
		

		//add conf later
		
		//utility function = socVal * sqrt(total social) - healthVal * (probability of getting infected)
		//derivative = socVal * 1/(2sqrt(totalsocial) - healthVal * (infection rate of target group)
		
		//DIAGNOSTIC
		double baseSoc = 1;
		double bonusSoc = 4;
		double socVal = baseSoc + bonusSoc*social;
		
		double baseHealth = 10;
		double bonusHealth = 10;
		double healthVal = baseHealth + bonusHealth*health;
		
		int[] oldChange = new int[sim.population.length];
		double[] partTwo = new double[sim.population.length];
		
		double partOne = socVal / 2 / Math.sqrt(rememberSoc);// * tincSoc;
		
		
		for(int j = 0; j < sim.population.length; j++) {
			partTwo[j] = healthVal * sim.population[j].infRate;// * tincSoc;
		//	if(partOne - partTwo > 0) sim.socialDesires[i][j] += tincSoc;
		//	if(partOne - partTwo < 0) sim.socialDesires[i][j] -= tincSoc;
			if(partOne - partTwo[j] > 0) oldChange[j]++;
			if(partOne - partTwo[j] < 0) oldChange[j]--;
		//	if(sim.socialDesires[i][j] < 0) sim.socialDesires[i][j] = 0;
		//	if(sim.socialDesires[i][j] > sim.socialDesires[j][i] + tincSoc*4) sim.socialDesires[i][j] = sim.socialDesires[j][i] + tincSoc*4;
		}
		
		
		
		
		double[] interactions = new double[sim.population.length];
		for(int j = 0; j < sim.population.length; j++) interactions[j] = Math.min(sim.socialDesires[i][j], sim.socialDesires[j][i]);
		//temp for diagnostic
		boolean d = false;
		//if(i==0) d=true;
		if(d)System.out.print("now: ");
		//
		double baseUtility = utilityEstimator(sim, interactions,d);
		for(int j = 0; j < sim.population.length; j++) {
			//
			d=false;
			//if(i==0 && j==0) d = true;
			//
			double[] copyInteractions = interactions.clone();
			copyInteractions[j] += tincSoc;
			//
			if(d)System.out.print("up: ");
			double upUtility = utilityEstimator(sim, copyInteractions,d);
			copyInteractions[j] -= 2*tincSoc;  if(copyInteractions[j] < 0) copyInteractions[j] = 0;
			//
			if(d)System.out.print("down: ");
			double downUtility = utilityEstimator(sim, copyInteractions,d);
			if(upUtility == Math.max(baseUtility, Math.max(upUtility, downUtility))) sim.socialDesires[i][j] += tincSoc;
			if(downUtility == Math.max(baseUtility, Math.max(upUtility, downUtility))) sim.socialDesires[i][j] -= tincSoc;
			
			//TODO: troubleshooting stuff, probably remove
			//if(upUtility == Math.max(baseUtility, Math.max(upUtility, downUtility))) {
			if(false) {
				sim.socialDesires[i][j] += tincSoc;
				if(sim.numDays > 0 && oldChange[j] != 1) System.out.println("discrepancy+: i " + i + " j " + j);
			}
			//if(downUtility == Math.max(baseUtility, Math.max(upUtility, downUtility))) {
			if(false) { 
				sim.socialDesires[i][j] -= tincSoc;
				
				if(sim.numDays > 0 && oldChange[j] != -1) {
					System.out.println("discrepancy-: i " + i + " j " + j);
					System.out.println("base u: " + baseUtility);
					System.out.println("up u: " + upUtility);
					System.out.println("down u: " + downUtility);
					System.out.println("partOne: " + partOne);
					System.out.println("partTwo: " + partTwo[j]);
					System.out.println("diff: " + (partOne-partTwo[j]));
					if(i==7 && j== 7) {
						utilityEstimator(sim, interactions,true);
						System.out.println("rememberSoc: " + rememberSoc);
					}
				}
				//
			}
			
			
			
			if(sim.socialDesires[i][j] < 0) sim.socialDesires[i][j] = 0;
			if(sim.socialDesires[i][j] > sim.socialDesires[j][i] + tincSoc*4) sim.socialDesires[i][j] = sim.socialDesires[j][i] + tincSoc*4;	
			
		}
		
		
	}
	
	public double utilityEstimator(CNSimulator sim, double[] interactions, boolean d) {
		
		//diagnostic is temp variable so it only prints on certain groups
		
		double baseSoc = 4;
		double bonusSoc = 8;
		double socVal = baseSoc + bonusSoc*social;
		
		double baseHealth = 30;
		double bonusHealth = 30;
		double healthVal = baseHealth + bonusHealth*health;
		
		double baseConformity = 0.3;
		double bonusConformity = 1;
		double confVal = baseConformity + bonusConformity*conformity;
		
		

		
		
		
		double sumSoc = 0; double sumInf = 0; double sumConf = 0;
		
		for(int j = 0; j < interactions.length; j++) {
			if(socialFriction) {
				//integral of 1+ab^x from 0 to s
				double s = interactions[j];
				//double normalizer = (1+maxSocBonus)/2;
				if (s > 0) sumSoc += (s+(maxSocBonus * Math.pow(socDecay, s))/Math.log(socDecay)-maxSocBonus/Math.log(socDecay))*socNormalizer;
			}
			else sumSoc += interactions[j];
			sumConf += interactions[j];
			sumInf += interactions[j] * sim.population[j].infRate * sim.transmission; //include infectivity multiplier for disease
		}
		double utility = socVal * Math.sqrt(sumSoc) - healthVal * sumInf;
		double log = -1000000;
		if(logMode) {
			if(sumSoc > 0) log = Math.log(sumSoc);
			if(log < -1000000+sumSoc) log = -1000000+sumSoc;
			utility = socVal * log - healthVal * sumInf;
		}
		
		
		
		double avgInt = sim.getAverageInt();
		if(includeConf) utility -= confVal*(sumConf - avgInt)*(sumConf - avgInt);
		if(d) {
			//System.out.println("group " + diagnostic + ":");
			if(logMode) System.out.println("socpart(logmode): " + (socVal * log));
			else	System.out.println("socpart: " + (socVal * Math.sqrt(sumSoc)));
			
			System.out.println("healthpart: " + (healthVal * sumInf));
			System.out.println("utility: " + utility);
			
			if(includeConf) System.out.println("confpart: " + confVal*(sumConf - avgInt)*(sumConf - avgInt));
			
			if(logMode) System.out.println("formula: " + socVal + " * " + "log(" + sumSoc +") - " + healthVal + " * " + sumInf);
			else System.out.println("formula: " + socVal + " * " + "sqrt(" + sumSoc +") - " + healthVal + " * " + sumInf);
			
		}
		
		
		return utility;
		
		
		
	}
	
	
	
	public void storeHistory() {
		infHistory.add(infRate);
		socHistory.add(rememberSoc);
	}
	
	
	
}

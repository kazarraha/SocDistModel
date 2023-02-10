import java.util.ArrayList;

public class CNSimulator {

	
	public double timeScale = 1; //allows more or less fine datapoints without affecting underlying dynamics
	
	public boolean includeConf = false;
	
	
	public Group[] population;
	
	//public double [][] interactionCoeff;
	public double[][] socialDesires;
	
	public ArrayList[][] socialDesiresHistory;
	public ArrayList[][] sjHistory;
	public ArrayList[] infHistory;
	
	
	//public double homophily = 2;
	//public double homophily = 1;
	
	public double recoveryRate = 0.3;
	public double infRate = 0.1; //initial infectionf
	
	public double transmission = 1.0/8; //rate of disease spread
	
	public double highestPInf = 0;
	
	public int numDays;
	
	
	public CNSimulator() {
		initialize();
	}
	
	
	public void initialize() {
		int numGroups = 8;
		if(!includeConf) numGroups = 4;
		population = new Group[numGroups]; //should sum to 1
		
		double highSocialFreq = 0.5;
		double highConformityFreq = 0.5;
		double highHealthFreq = 0.5;
		double initialInfected = 0.01;
		double[] freqs = {highSocialFreq, highConformityFreq, highHealthFreq, initialInfected};
		
		int groupIndex = 0;
		
		double baseSoc = 4;
		double bonusSoc = 8;

		double baseHealth = 30;
		double bonusHealth = 30;
		
		double baseConformity = 0.3; //not currently used in any figures or tests
		double bonusConformity = 1;
		
		
		for(int soc = 0; soc <= 1; soc++) {
			double socVal = baseSoc + bonusSoc*soc;
			int maxConf = 1;  if(!includeConf) maxConf = 0;
			for(int conf = 0; conf <= maxConf; conf++) {
				double confVal = baseConformity + bonusConformity*conf;
				for(int health = 1; health >= 0; health--) {
						double healthVal = baseHealth + bonusHealth*health;
						double popSize = highSocialFreq * highConformityFreq * highHealthFreq;
						population[groupIndex] = new Group(socVal,confVal,healthVal, popSize, initialInfected);
						population[groupIndex].setName(soc, health);
						
						if(!includeConf) population[groupIndex].includeConf = false;
						groupIndex++;
					
				}
			}
		}
		//setupInteractionCoeff();
		setupSocialDesires();
		numDays = 0;

	}
	
	public void setupSocialDesires() {
		socialDesires = new double[population.length][population.length];
		for(int i = 0; i < population.length; i++) {
			for(int j = 0; j < population.length; j++) { 
				if(includeConf)	socialDesires[i][j] = 0.7;
				else socialDesires[i][j] = 1.4;
			}
			
		}
		
		socialDesiresHistory = new ArrayList[population.length][population.length];
		sjHistory = new ArrayList[population.length][population.length];
		infHistory = new ArrayList[population.length];
		for(int i = 0; i < population.length; i++) {
			infHistory[i] = new ArrayList<Double>();
			for(int j = 0; j < population.length; j++) {
				socialDesiresHistory[i][j] = new ArrayList<Double>();
				sjHistory[i][j] = new ArrayList<Double>();
			}
		}
		
	}
	
//	public void setupInteractionCoeff(){
		//homophily.  probably adjust later to scale based on number of shared attributes
		
//		interactionCoeff = new double[population.length][population.length];
		
//		for(int i = 0; i < population.length; i++) {
//			double sumj = 0;
//			for(int j = 0; j < population.length; j++) { 
//				double c = population[j].popSize;
//				if(i == j) c *= homophily;
//				interactionCoeff[i][j] = c;
//				sumj += c;
//			}
//			for(int j = 0; j < population.length; j++) interactionCoeff[i][j] /= sumj;
//		}
//	}
	
	//public double getSoc(Group group1, Group group2, double averageInf) {
	//	double soc1 = group1.getDesiredSoc(averageInf);
	//	double soc2 = group2.getDesiredSoc(averageInf);
	//	return Math.min(soc1, soc2);
	//}
	
	public double getAverageInt(int i) { //partners with i
		double sum = 0;
		for(int j = 0; j < population.length; j++) sum += Math.min(socialDesires[i][j],socialDesires[j][i])/population.length;
		return sum;
	}
	
	public double getAverageInt() { //all pop
		double sum = 0;
		for(int i = 0; i < population.length; i++) sum += population[i].rememberSoc/population.length;
		return sum;
	}
	
	public double getAverageInf() {
		double sum = 0;
		for(int i = 0; i < population.length; i++)	sum += population[i].infRate * population[i].popSize;
		return sum;
	}
	
	
	
	public void simDay() {
		
		
		
		numDays++;
		double[] uninf = new double[population.length]; //temp uninfected to update over time without messing with infected for future steps
		for(int i = 0; i < uninf.length; i++) uninf[i] = 1 - population[i].infRate;
		//double avgInf = getAverageInf();
		for(int i = 0; i < population.length; i++) {
			double sumInt = 0;
			double sumNewInf = 0;
			for(int j = 0; j < population.length; j++) {
				//double soc = getSoc(population[i], population[j], avgInf);
				double soc = Math.min(socialDesires[i][j], socialDesires[j][i]);
				double interact = soc;// * interactionCoeff[i][j];
				sumInt += interact;
				double newInf = uninf[i] * population[j].infRate * interact * transmission * timeScale;
				//uninf[i] -= newInf;
				sumNewInf += newInf;
				//if(uninf[i] < 0) {
				//	System.out.println("infection probability exceeded 1.  This is bad, figure out where.");
				//	uninf[i] = 0;
				//}
			}
			checkHighestInf(sumNewInf/uninf[i]);
			if(sumNewInf > uninf[i]) {
				uninf[i] = 0;
			}
			else uninf[i] -= sumNewInf;
			uninf[i] += population[i].infRate * recoveryRate * timeScale;
			population[i].rememberSoc = sumInt;
			
			//safety checker
			boolean safety = false;
			if(safety && sumInt * transmission * timeScale > 1) {
				System.out.println("infection probability would exceed 1 given high infRate. i:" + i + "sumInt: " + sumInt);
				System.out.println("sigma: " + population[i].socVal + " rho(1-gam): " + population[i].healthVal * (1-recoveryRate*timeScale));
			}
			
			
		}
		for(int i = 0; i < uninf.length; i++) {
			population[i].infRate = 1 - uninf[i];
			
		}
		
		//update
		for(int i = 0; i < population.length; i++) population[i].updateSoc(this, i, timeScale);
		
		storeHistory();

		
	}
	
	
	public void storeHistory() {
		for(int i = 0; i < population.length; i++) {
			population[i].storeHistory();
		}
		for(int i = 0; i < population.length; i++) {
			infHistory[i].add(population[i].infRate);
			for(int j = 0; j < population.length; j++) {
				socialDesiresHistory[i][j].add(socialDesires[i][j]);
				sjHistory[i][j].add(Math.min(socialDesires[i][j], socialDesires[j][i]));
			}
		}
		
		
	}
	
	public void applyFrictionValues(double r, double m, double n) {
		for(int i = 0; i < population.length; i++) {
			Group g = population[i];
			g.maxSocBonus = m;
			g.socDecay = r;
			g.socNormalizer = n;
			g.socialFriction = true;
			
		}
	}
	
	public double getHomophilyScore() {
		double sumHom = 0;
		for(int i = 0; i < population.length; i++) {
			double sumSoc = 0;
			double selfSoc = 0;
			for(int j = 0; j < population.length; j++) {
				double soc = Math.min(socialDesires[i][j], socialDesires[j][i]);
				sumSoc += soc;
				if(i == j) selfSoc = soc;				
			}
			double homScore = selfSoc/sumSoc;
			sumHom += homScore/population.length;
		}
		return sumHom;
	}
	
	public double getOneHomophily(int i) {
		double sumSoc = 0;
		double selfSoc = 0;
		for(int j = 0; j < population.length; j++) {
			double soc = Math.min(socialDesires[i][j], socialDesires[j][i]);
			sumSoc += soc;
			if(i == j) selfSoc = soc;				
		}
		double homScore = selfSoc/sumSoc;
		return homScore;
	}
	
	
	public double getEIHomScore() {
		//original EI score in the literature is (E-I)/(E+I)
		//our modified one averages E from other groups, and then negates, so 0 treats all groups the same, 1 is perfect homophily
		double sumHom = 0;
		for(int i = 0; i < population.length; i++) {
			double E = 0;
			double I = 0;
			for(int j = 0; j < population.length; j++) {
				double soc = Math.min(socialDesires[i][j], socialDesires[j][i]);
				if(i==j) I = soc;
				else E += soc/(population.length-1);				
			}
			//System.out.println("I: " + I + "  E: " + E);
			
			double homScore = (I-E)/(I+E); //modified EI homophily
			sumHom += homScore/population.length; //average over all groups
		}
		return sumHom;
		
		
	}
	
	public double checkHighestInf(double inf) { //check to see if p(inf) exceeds or approaches 1
		if(inf > 1 && highestPInf <= 1) System.out.println("infection probability exceeded 1.  This is bad, figure out where.");
		if(inf > highestPInf) highestPInf = inf;
		return highestPInf;		
	}
	
	
	
}

import java.util.ArrayList;

public class Epidemiologist {

	//pays attention to and measure epidemics in community mode
	
	NetworkSimulator sim;

	//each agent has their own slot, which stores a list of all the epidemics they initiated via importation
	public ArrayList<Integer>[] epidemicHistory;
	public int[][] epidemicHistory2; //number of epidemics caused by Type, of certain Size   [Type][Size] (capped at 20)
	public int[] epidemicWeightedSum;
	public Epidemic[] currentEpidemics;

	public static int maxHistory = 20;
	
	

	
	
	
	
	
	//whenever a previously uninfected community becomes infected by precisely one member, an epidemic is recorded
	//whenever the originator spreads the infection, those members are recorded, additional infections from outside are ignored
	//if the infection dies out, the epidemic is closed, the total number of internal infections it caused are recorded
	
	//if two infections would start epidemics simultaneously, the originator is ambiguous
	//an ambiguous epidemic is recorded as a placeholder to ensure neither get credit, but it does not get recorded longterm
	
	
	
	
	
	public class Epidemic{
		public boolean ambiguous; //if multiple originators, don't record credit to either;
		public int community;
		public int originator;
		public ArrayList<Integer> currentMembers;
		public int total;
		
		public Epidemic(int c, int o) {
			community = c;
			originator = o;
			currentMembers = new ArrayList<Integer>();
			currentMembers.add(originator);
			ambiguous = false;
			total = 1;
		}
		
		public Epidemic(int c) {
			community = c;
			ambiguous = true;
			currentMembers = new ArrayList<Integer>();
		}
		
	}
	
	
	
	public Epidemiologist(NetworkSimulator simm) {
		sim = simm;
		epidemicHistory = new ArrayList[4]; //raw value for each epidemic, for StatisticsManager
		epidemicHistory2 = new int[4][maxHistory+1]; //histogram binned data
		epidemicWeightedSum = new int[4];
		currentEpidemics = new Epidemic[sim.communities.length];
		
		
		for(int i = 0; i < 4; i++)	epidemicHistory[i] = new ArrayList<Integer>();
	}
	
	public int numInfections(int c) {
		int num = 0;
		for(int i = 0; i < sim.communities[c].length; i++) {
			if(sim.communities[c][i].infected) num++;
		}
		return num;
	}
	
	public int findInfected(int c) {
		for(int i = 0; i < sim.communities[c].length; i++) {
			if(sim.communities[c][i].infected) return i;
		}
		return -1;
	}
	
	public void scanEpidemics() {
		//looks at each community in sim
		//if a previously uninfected community has exactly one infected member, marks that as an epidemic
		//if a previously infected community has no infected members, ends that epidemic tracking
		for(int c = 0; c < sim.communities.length; c++) {
			int num = numInfections(c);
			if(currentEpidemics[c]==null) {
				if(num == 1) currentEpidemics[c] = new Epidemic(c,findInfected(c));
				if(num > 1) currentEpidemics[c] = new Epidemic(c); //ambiguous originator
			}
			else if(num == 0) closeEpidemic(c);
		}
		
		//also, go through each epidemic and see if someone needs removed
		for(int c = 0; c < sim.communities.length; c++) {
			if(currentEpidemics[c] != null) {
				//for(int a: currentEpidemics[c].currentMembers) {
				//	if(!sim.communities[c][a].infected) currentEpidemics[c].currentMembers.remove(a);
				//I think this doesn't work because the elements are also ints, so it thinks it's an index
				//}
				for(int i = 0; i < currentEpidemics[c].currentMembers.size(); i++) {
					int a = currentEpidemics[c].currentMembers.get(i);
					if(!sim.communities[c][a].infected) {
						currentEpidemics[c].currentMembers.remove(i);
						i--;
					}
				}
			}
		}
	}
	
	public void closeEpidemic(int c) {
		if(!currentEpidemics[c].ambiguous) {
			int group = sim.agents[currentEpidemics[c].originator].groupIndex;
			int t = currentEpidemics[c].total;
			if(t > maxHistory) epidemicHistory2[group][maxHistory]++;
			else epidemicHistory2[group][t]++;
			epidemicWeightedSum[group] += t-1;
			epidemicHistory[group].add(t-1);
			
			//System.out.println("close epidemic: [" + group + "]["+t+"]");
		}
		currentEpidemics[c]=null;		
	}
	
	public void checkSpread(Agent spreader, Agent exposed) {
		//first just infected second, increment this if an epidemic exists
		//this should only be called if exposed is known to be uninfected prior to this exposure
		//this is okay to call regardless of what communities or epidemics they're in
		int c = sim.findCommunityOfAgent(spreader);
		if(currentEpidemics[c] == null) return;
		if(c != sim.findCommunityOfAgent(exposed)) return;
		
		int s = sim.findIndexInCommunity(c, spreader);
		int e = sim.findIndexInCommunity(c, exposed);
		if(currentEpidemics[c].currentMembers.contains(s) && !currentEpidemics[c].currentMembers.contains(e)) {
			currentEpidemics[c].currentMembers.add(e);
			currentEpidemics[c].total++;
		}
	}
	
	public double numEpidemics(int type) {
		int sum = 0;
		for(int s = 0; s < epidemicHistory2[type].length; s++) sum += epidemicHistory2[type][s];
		return sum;
	}
	
	public void closeAllEpidemics() {
		for(int c = 0; c < currentEpidemics.length; c++)  if(currentEpidemics[c] != null) closeEpidemic(c);
	}
	
	
}

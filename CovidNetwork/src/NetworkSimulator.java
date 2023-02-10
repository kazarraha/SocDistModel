import java.util.ArrayList;
import java.util.Random;


//WARNING
//The method used to implement maximum socialization might not be doing what you think it is
//double check and fix if necessary


public class NetworkSimulator extends CNSimulator {
	
	
	//an actual network on a graph
	
	
	public boolean lowerSoc = true; //temp hack to avoid bug with infection probabilities exceeding 1?
	public double spontaneousGeneration = 0.001;
	//public double spontaneousGeneration = 0;
	
	
	//TODO: have minimum socializing for initial connections
	
	//transmission rate and agent utility functions are inherited from CNSimulator
	
	
	public Agent[] agents;
	public double[][] connections; //coefficient on how much value i and j get from interactions
	
	public double[][] socialDesires; //how much agent i wants to socialize with j
	
	
	
	//this doesn't do much any more
	public boolean lonelystuff = true;
	public ArrayList<Double>[] trackLonely; //[threshhold](time)
	public double[] threshholds = {0.04, 0.2,0.5,1.0};
	
	
	
	public int infTime = 10;  //redundant with recoveryRate
	
	
	public boolean newConnections = false;
	public int numNew = 1;
	public double strengthNew = 0.5; //this doesn't do anything except when the experimental option to forge new connections is enabled, which didn't yield nice results and was later abandoned
	
	public boolean communityMode = true;
	public Agent[][] communities;
	public int[] selectedCommunity;
	
	public boolean rememberInfSendCheck = true; //used for Direct- and r-artribution
	public boolean pseudoInfections  = false; //used to check how much Direct Attribution is affected by depletion of susceptibles
	//public double[][] rememberInfSend;
	//double rememberInfSendDecay = 0.01;
	
	public Epidemiologist epi;
	
	
	public boolean fixInitialInfection = true;
	//maxSoc is handled in Agent class
	
	public int totalInfections = 0;
	public int[] comInfections;
	//public double[] sumBlame; //uses exponential decay to share blame with distant disease-ancestors
	//public double[] sumDBlame; //assigns blame only to most recent infector(s), only splitting for multiple exposures on the same time step
	public double[] sumBlame2;  //tracks per agent instead of per type, to allow statistics
	public double[] sumDBlame2;
	public double[] sumPBlame; //DBlame, but applies even when agents already infected
	public double sumSponBlame; //infections from Spontaneous Generation
	
	
	
	public NetworkSimulator hackClone() { //I think there are official cloning conventions, which this ignores
		NetworkSimulator copy = new NetworkSimulator();
		for(int i = 0; i < agents.length; i++) {
			copy.agents[i] = agents[i].copy();
			int c = findCommunityOfAgent(agents[i]);
			int ci = findIndexInCommunity(c, agents[i]);
			copy.communities[c][ci] = copy.agents[i];
		}
		for(int i = 0; i < connections.length; i++) {
			for(int j = 0; j < connections[i].length; j++) {
				copy.connections[i][j] = connections[i][j];
				copy.socialDesires[i][j] = socialDesires[i][j];
			}
		}
		//I believe these are the only things that randomize for new networks, so the resulting networks should be effectively identical, as long as no other paramaters have been changed prior to calling this
		return copy;
	}
	
	public NetworkSimulator(int mainType) {
		super();
		
		if(lowerSoc) for(int i = 0; i < population.length; i++) population[i].socVal /= 3;
		

		formCommunityPopulationNew(16,10, mainType);
		selectedCommunity = new int[2]; //for display purposes on the GUI
		
		
		seedInfections(1.0/16);
		updateInfRates();
		resetHistory();
	}
	

	
	public NetworkSimulator() {
		super();
		
		if(lowerSoc) for(int i = 0; i < population.length; i++) population[i].socVal /= 3;
		
		if(!communityMode) {
			populateAgents();
			forgeConnections();
		}
		else{
			formCommunityPopulation(16,10);
			selectedCommunity = new int[2];
		}
		
		if(fixInitialInfection) fixInfections();
		else seedInfections(1.0/16);
		updateInfRates();
		resetHistory();
	}
	
	
	public void populateAgents() {
		//int numAgents = 64;
		int numAgents = 400;
		agents = new Agent[numAgents];
		//for(int i = 0; i < numAgents; i++) agents[i] = genRandomAgent();
		for(int i = 0; i < numAgents; i++) {
			agents[i] = genOrderedAgents(i,numAgents, 4);
			agents[i].simIndex = i;
		}
	}
	
	public Agent genRandomAgent() { //this is currently unused by anything
		Random randy = new Random();
		//int g = randy.nextInt(8);
		int g = randy.nextInt(4);
		Group gr = population[g];
		Agent a = new Agent(gr.socVal, gr.confVal, gr.healthVal);
		a.groupIndex = g;
		return a;
	}
	
	public Agent genOrderedAgents(int i, int max, int numGroups) {
		int g = (i * numGroups) /max;
		Group gr = population[g];
		Agent a = new Agent(gr.socVal, gr.confVal, gr.healthVal);
		a.groupIndex = g;
		return a;
	}

	public void forgeConnections() {
		int connType = 1;
		double connProb = 10.0/(agents.length-1);
		//currently all random.  update later to include homophily and clustering and stuff
		Random randy = new Random();
		connections = new double[agents.length][agents.length];
		socialDesires = new double[agents.length][agents.length];
		//if(rememberInfSendCheck) rememberInfSend = new double[agents.length][agents.length];
		for(int i = 0; i < connections.length; i++) {
			for(int j = i+1; j < connections.length; j++) {
				double p = randy.nextDouble();
				if(p < connProb) {
					if(connType == 0) { //random strength
						double d = 2*randy.nextDouble();
						connections[i][j] = d;	connections[j][i] = d;
						socialDesires[i][j] = 1; socialDesires[j][i] = 1;
					}
					else if(connType == 1) { //all starting connections are 1, new ones can be created at 0.5
						connections[i][j] = 1;	connections[j][i] = 1;
						socialDesires[i][j] = 1; socialDesires[j][i] = 1;
					}
				}
			}
		}
		//check isolations
		boolean isolation = false;
		for(int i = 0; i < agents.length; i++) {
			boolean iso = true;
			for(int j = 0; j < agents.length; j++) {
				if(connections[i][j] > 0) {
					iso = false;
					break;
				}
			}
			if(iso) isolation = true;
		}
		if(isolation && connProb > 0) {System.out.println("reforge Connections to avoid isolation"); forgeConnections();}
	
}
	
	//public void formCommunityPopulationNew(int numCommunities, int sizeOfCommunities, int mainType) {
	//	formCommunityPopulationNew(numCommunities,sizeOfCommunities,mainType,true); //default is true
	//}
	public void formCommunityPopulationNew(int numCommunities, int sizeOfCommunities, int mainType) {
		
		boolean bonusNC = true;
		//alternate version of formCommunityPopulation
		//every community is majority baseType, but has 3 nonconformists, one of each other type
		//nonconformists are slightly more likely to connect together
		//forms 16 communities, one for each combination of primary and offType;
		//connections are more likely and stronger within each community
		
		//numCommunities needs to be even for nonconformist pairings to work
		communities = new Agent[numCommunities][sizeOfCommunities];
		agents = new Agent[numCommunities*sizeOfCommunities];
		//if(rememberInfSendCheck) rememberInfSend = new double[agents.length][agents.length];
		int populationIndex = 0;
		for(int index = 0; index < numCommunities; index++) {
			int offIndex = 0;
			for(int offType = 0; offType < 4; offType++) {
				if(offType != mainType) {		
					Group gr = population[offType];
					Agent a = new Agent(gr.socVal, gr.confVal, gr.healthVal);
					a.groupIndex = offType;
					communities[index][offIndex] = a;
					agents[populationIndex] = a;
					a.simIndex = populationIndex;
					populationIndex++;
					offIndex++;
				}
			}
			Group gr = population[mainType];
			for(int i = 3; i < sizeOfCommunities; i++) {
				Agent a = new Agent(gr.socVal, gr.confVal, gr.healthVal);
				a.groupIndex = mainType;
				communities[index][i] = a;
				agents[populationIndex] = a;
				a.simIndex = populationIndex;
				populationIndex++;
			}
		}
		double pInternal = 6.0/(sizeOfCommunities-1);
		double pExternal = 4.0/((numCommunities-1)*sizeOfCommunities);
		double sExternal = 0.9;
		double sdi = 0.5;
		Random randy = new Random();
		connections = new double[agents.length][agents.length];
		socialDesires = new double[agents.length][agents.length];
		for(int a = 0; a < numCommunities; a++) {
			for(int b = 0; b < sizeOfCommunities; b++) {
				for(int c = a; c < numCommunities; c++) {
					for(int d = 0; d < sizeOfCommunities; d++) {
						double p = pExternal;
						double connStrength = sExternal;
						if(a==c) {
							if(b==d) p = 0;
							else p = pInternal;
							connStrength = 1;
						}
						double r = randy.nextDouble();
						if(r < p) {
							int i = java.util.Arrays.asList(agents).indexOf(communities[a][b]);
							int j = java.util.Arrays.asList(agents).indexOf(communities[c][d]);
							connections[i][j] = connStrength;	connections[j][i] = connStrength;
							socialDesires[i][j] = sdi; socialDesires[j][i] = sdi;
						}
					}
				}
			}
		}
		//each nonconformist gets a bonus connection with one other nonconformist of the same type;
		if(bonusNC) {
		for(int w = 0; w < 3; w++) {
			int[][] pairs = pairList(numCommunities);
			for(int p = 0; p < pairs.length; p++) {
				int[] pair = pairs[p];
				int i = java.util.Arrays.asList(agents).indexOf(communities[pair[0]][w]);
				int j = java.util.Arrays.asList(agents).indexOf(communities[pair[1]][w]);
				connections[i][j] = sExternal;	connections[j][i] = sExternal;
				socialDesires[i][j] = sdi; socialDesires[j][i] = sdi;
			}
		}	
		}
		//check isolation
		if(checkIsolation()) {System.out.println("reformCommunityPopulationNew to avoid isolation"); formCommunityPopulationNew(numCommunities, sizeOfCommunities,mainType);}	
	}
	
	public int[][] pairList(int size){
		int[][] pairs = new int[size/2][2];
		ArrayList<Integer> unchosen = new ArrayList<Integer>();
		for(int i = 0; i < size; i++) unchosen.add(i);
		Random randy = new Random();
		for(int i = 0; i < pairs.length; i++) {
			int firstIndex = randy.nextInt(unchosen.size());
			pairs[i][0] = unchosen.get(firstIndex);
			unchosen.remove(firstIndex);
			int secondIndex = randy.nextInt(unchosen.size());
			pairs[i][1] = unchosen.get(secondIndex);
			unchosen.remove(secondIndex);
		}
		return pairs;
	}
	
	public boolean checkIsolation() {
		boolean isolation = false;
		for(int i = 0; i < agents.length; i++) {
			boolean iso = true;
			for(int j = 0; j < agents.length; j++) {
				if(connections[i][j] > 0) {
					iso = false;
					break;
				}
			}
			if(iso) isolation = true;
		}
		return isolation;
	}
	
	public void formCommunityPopulation(int numCommunities, int sizeOfCommunities) {
		//forms 16 communities, one for each combination of primary and offType;
		//connections are more likely and stronger within each community
		
		//for now, numCommunities fixed at 16
		numCommunities = 16;
		communities = new Agent[numCommunities][sizeOfCommunities];
		agents = new Agent[numCommunities*sizeOfCommunities];
		//if(rememberInfSendCheck) rememberInfSend = new double[agents.length][agents.length];
		int index = 0;
		int populationIndex = 0;
		for(int mainType = 0; mainType < 4; mainType++) {
			for(int offType = 0; offType < 4; offType++) {
				Group gr = population[offType];
				Agent a = new Agent(gr.socVal, gr.confVal, gr.healthVal);
				a.groupIndex = offType;
				communities[index][0] = a;
				agents[populationIndex] = a;
				populationIndex++;
				gr = population[mainType];
				for(int i = 1; i < sizeOfCommunities; i++) {
					a = new Agent(gr.socVal, gr.confVal, gr.healthVal);
					a.groupIndex = mainType;
					communities[index][i] = a;
					agents[populationIndex] = a;
					populationIndex++;
				}
				index++;
			}
		}
		double pInternal = 6.0/(sizeOfCommunities-1);
		double pExternal = 4.0/((numCommunities-1)*sizeOfCommunities);
		double sExternal = 0.8;
		double sdi = 0.5;
		Random randy = new Random();
		connections = new double[agents.length][agents.length];
		socialDesires = new double[agents.length][agents.length];
		for(int a = 0; a < numCommunities; a++) {
			for(int b = 0; b < sizeOfCommunities; b++) {
				for(int c = a; c < numCommunities; c++) {
					for(int d = 0; d < sizeOfCommunities; d++) {
						double p = pExternal;
						double connStrength = sExternal;
						if(a==c) {
							if(b==d) p = 0;
							else p = pInternal;
							connStrength = 1;
						}
						double r = randy.nextDouble();
						if(r < p) {
							int i = java.util.Arrays.asList(agents).indexOf(communities[a][b]);
							int j = java.util.Arrays.asList(agents).indexOf(communities[c][d]);
							connections[i][j] = connStrength;	connections[j][i] = connStrength;
							socialDesires[i][j] = sdi; socialDesires[j][i] = sdi;
						}
					}
				}
			}
		}
		if(checkIsolation()) {System.out.println("reformCommunityPopulation to avoid isolation"); formCommunityPopulation(numCommunities, sizeOfCommunities);}
	
		
	}

	
	
	public void seedInfections(double r) {
		int numInf = (int)(agents.length * r);
		Random randy = new Random();
		if(numInf > agents.length) {
			System.out.println("seedInfections has numInf > agents.length: numInf " + numInf + " agents: " + agents);
			return;
		}
		while(numInf > 0) {
			int index = randy.nextInt(agents.length);
			if(!agents[index].infected) {
				agents[index].infected = true;
				agents[index].infectionTime = infTime;
				numInf--;
			}
		}
	}
	
	public void fixInfections() {
		//only infects conformists
		int index = 6;
		for(int c = 0; c < communities.length; c++) {
			communities[c][index].infected = true;
			agents[index].infectionTime = infTime;
		}
		
	}
	
	public void updateInfRates() {
		//measures infection among all agents and computes averages for each type
		//TODO: probably keep better long term track of agent types so the arraylist doesn't have to be redone every time this is called
		ArrayList<Agent>[] list = new ArrayList[8];
		for(int i = 0; i < population.length; i++) list[i] = new ArrayList<Agent>();
		for(int j = 0; j < agents.length; j++) list[agents[j].groupIndex].add(agents[j]);
		for(int i = 0; i < population.length; i++) {
			double sumInf = 0;
			for(int j = 0; j < list[i].size(); j++) {
				if(list[i].get(j).infected) sumInf += 1.0/list[i].size();
			}
			population[i].infRate = sumInf;
		}	
	}
	
	public void resetHistory() {
		trackLonely = new ArrayList[threshholds.length];
		for(int i = 0; i < threshholds.length; i++) trackLonely[i] = new ArrayList<Double>();
		for(int i = 0; i < agents.length; i++) agents[i].resetHistory();
		totalInfections = 0;
		comInfections = new int[communities.length];
		//sumBlame = new double[4];
		//sumDBlame = new double[4];	
		sumBlame2 = new double[agents.length];
		sumDBlame2 = new double[agents.length];
		sumPBlame = new double[agents.length];
		sumSponBlame = 0;
	}
	
	
	
	public void simDay() {
		
		//TODO: maybe redo how agent socializing works to only include mutually connected people
		//O(Nc) rather than O(N^2), where c is the average number of connections per agent
		Random randy = new Random();
		
		numDays++;
		for(int i = 0; i < agents.length; i++) {
			double sumInt = 0;
			for(int j = 0; j < agents.length; j++) {
				if(connections[i][j] > 0) {
					double soc = Math.min(socialDesires[i][j], socialDesires[j][i]);
					sumInt += soc * connections[i][j];
					if(agents[j].infected) {
						double infProb = soc * transmission;
						double r = randy.nextDouble();
						if(r < infProb) {
							agents[i].exposed = true;
							if(epi != null && !agents[i].infected) {
								epi.checkSpread(agents[j], agents[i]);
							}
							if(rememberInfSendCheck && !agents[i].infected) {
								//rememberInfSend[i][j] += rememberInfSendDecay; 
								//incHistories(agents[i],agents[j]);
								agents[i].infectors.add(agents[j]);
							}
							if(pseudoInfections) {
								agents[i].pseudoInfectors.add(agents[j]);
							}
						
						}
					}
				}			
			}
			agents[i].rememberSoc = sumInt;
			double r = randy.nextDouble();
			if(r < spontaneousGeneration) {
				if(!agents[i].exposed) sumSponBlame++;
				agents[i].exposed = true; //prevents infection from going extinct long-term
			}
			
		
		}
		//newConn and infection chance
		//System.out.println("newConnections line");
		if(newConnections) tryNewConnections(numNew);
		//else System.out.println("newConnections false");
		
		
		
		//infection and recovery
		for(int i = 0; i < agents.length; i++) {
			if(agents[i].infected) {
				agents[i].infectionTime--;
				if(agents[i].infectionTime <= 0) agents[i].infected = false;
			}
			else {
				if(agents[i].exposed) {
					agents[i].infected = true;
					agents[i].infectionTime = infTime;
					if(rememberInfSendCheck) {
						//double[] dBlame = agents[i].directBlame();
						//double[] blame = agents[i].applyBlame();
						//for(int t = 0; t < 4; t++) {
						//	sumBlame[t] += blame[t];
						//	sumDBlame[t] += dBlame[t];
						//}
						agents[i].directBlame2(sumDBlame2);  //tracks per agent instead of per type, to allow statistics
						agents[i].applyBlame2(sumBlame2);
					}
					totalInfections++;
					comInfections[findCommunityOfAgent(agents[i])]++;
				}
			}
			if(pseudoInfections) agents[i].directPseudoBlame(sumPBlame);
			agents[i].exposed = false;
		}
		updateInfRates(); //so groups can report average infections to agent's utility functions
		

		for(int i = 0; i < agents.length; i++) agents[i].updateSoc(this, i);
		
		if(lonelystuff) rememberLonely();
		//if(rememberInfSendCheck) decayRIS();
		if(epi != null) epi.scanEpidemics();
	}

	
	public void simDayMinCom(double min) {
		//simDay, but enforces a minimum socialization between adjacent community members
		if(!communityMode) {
			System.out.println("simDayMinCom called but community mode is false");
			simDay();
			return;
		}
		simDay();
		enforceMinCom(min);
	}
	
	
	public void enforceMinCom(double min) {
		for(int c = 0; c < communities.length; c++) {
			for(int i = 0; i < communities[c].length; i++) {
				int pi = java.util.Arrays.asList(agents).indexOf(communities[c][i]);
				for(int j = 0; j < communities[c].length; j++) {
					int pj = java.util.Arrays.asList(agents).indexOf(communities[c][j]);
					if(connections[pi][pj]>0 && socialDesires[pi][pj] < min) {
						socialDesires[pi][pj] = min;
					}
					//this is massively inefficient.  try to upfront this or stick in agents so only ones that changed get checked
				}
			}
		}	
	}
	
	
	public void simDayMaxCom(double max) {
		simDay();
		//enforce maximum socialization between any pair
		for(int i = 0; i < socialDesires.length; i++) {
			for(int j = 0; j < socialDesires.length; j++) {
				if(socialDesires[i][j] > max) socialDesires[i][j] = max;
			}
		}	
	}
	
	

	
	public double getAverageInt(int i) { //average for GROUP i, not agent i
		double sum = 0;
		int num = 0;
		for(int j = 0; j < agents.length; j++) {
			if(agents[j].groupIndex == i) {
				sum += agents[j].rememberSoc;
				num++;
			}
		}
		if(num > 0) sum = sum/num;
		return sum;
	}
	
	public double getAverageInt() { //all agents
		double sum = 0;
		for(int i = 0; i < agents.length; i++) sum += agents[i].rememberSoc/agents.length;
		return sum;
	}
	
	public double getAverageInf() {
		double sum = 0;
		for(int i = 0; i < agents.length; i++)	if(agents[i].infected) sum += 1.0/agents.length;
		return sum;
	}
	
	public void tryNewConnections(int n) {
		boolean report = false;
		for(int i = 0; i < n; i++) report = tryNewConnection();
		//if(report) System.out.println("tryNewConnection true");
		//else System.out.println("tryNewConnection false");
	}
	
	public boolean tryNewConnection() {
		if(agents.length < 2) {System.out.println("NetworkSimulator.tryNewConnection failed, agents.length = " + agents.length); return false;}
		Random randy = new Random();
		int i = randy.nextInt(agents.length);
		int j = -1;
		while(j == -1 || j == i) j = randy.nextInt(agents.length);
		
		//have infection chance whenever this happens
		double testSoc = 0.05;
		if(agents[i].infected) {
			double infProb = testSoc * transmission;
			double r = randy.nextDouble();
			if(r < infProb) agents[j].exposed = true;
		}
		if(agents[j].infected) {
			double infProb = testSoc * transmission;
			double r = randy.nextDouble();
			if(r < infProb) agents[j].exposed = true;
		}
		
		
		if(connections[i][j] > 0 || connections[j][i] > 0) return false;
		//temp for conn test
		connections[i][j] = strengthNew;
		connections[j][i] = strengthNew;
		boolean iWant = agents[i].considerConnection(this,i,j);
		boolean jWant = agents[j].considerConnection(this, j, i);
		if(iWant && jWant) {
			socialDesires[i][j] += agents[i].incSoc * agents[i].newChunk;
			socialDesires[j][i] += agents[j].incSoc * agents[j].newChunk;
			//
			System.out.println("NetworkSimulator.addConnection: " + i + ", " + j);
			return true;
		}
		else {
			connections[i][j] = 0;
			connections[j][i] = 0;
			return false;
		}
			

		
		//If you run this method during simDay before exposure is resolved, you don't need to update inf here
			
			
			
	}
	
	public void dropConnections(int i, int j) {
		//
		System.out.println("NetworkSimulator.dropConnections: " + i + ", " + j);
		connections[i][j] = 0;
		connections[j][i] = 0;
		socialDesires[i][j] = 0;
		socialDesires[j][i] = 0;
	}
	
	
	public int countLonelies(double threshhold) {
		int count = 0;
		for(int i = 0; i < agents.length; i++) {
			if(agents[i].rememberSoc < threshhold) count++;
		}	
		return count;			
	}
	
	public void rememberLonely() {
		for(int i = 0; i < threshholds.length; i++) {
			int count = countLonelies(threshholds[i]);
			double portion = ((double)count)/agents.length;
			trackLonely[i].add(portion);
		}
	}
	
	public int countConn(int i) {
		int numCon = 0;
		for(int j = 0; j < agents.length; j++) {
			if(connections[i][j] > 0) numCon++;
		}
		return numCon;
	}
	
	public int countHigherConn(int i) {
		int numCon = 0;
		for(int j = 0; j < agents.length; j++) {
			if(connections[i][j] > 0 && agents[i].socVal/agents[i].healthVal <= agents[j].socVal/agents[j].healthVal) numCon++;
		}
		return numCon;
	}
	
	public int countLowerConn(int i) {
		int numCon = 0;
		for(int j = 0; j < agents.length; j++) {
			if(connections[i][j] > 0 && agents[i].socVal/agents[i].healthVal >= agents[j].socVal/agents[j].healthVal) numCon++;
		}
		return numCon;
	}
	
	public int countSameConn(int i) {
		int numCon = 0;
		for(int j = 0; j < agents.length; j++) {
			if(connections[i][j] > 0 && agents[i].groupIndex == agents[j].groupIndex) numCon++;
		}
		return numCon;
	}
	
	public double getHomSocRatio(int i) {
		double homSoc = 0;
		double totSoc = 0;
		for(int j = 0; j < connections[i].length; j++) {
			if(connections[i][j] > 0) {
				double soc = Math.min(socialDesires[i][j], socialDesires[j][i]);
				totSoc += soc;
				if(agents[i].groupIndex == agents[j].groupIndex) homSoc += soc;
			}
		}
		
		if(totSoc == 0) return 0;
		else return homSoc/totSoc;
		
		
	}
	
	
	public double[] computeSatisfaction() {
		//how does each player's socialization compare to group average
		//not? capped at 1 if above average
		double[] avgSoc = new double[population.length];
		int[] numAgent = new int[population.length];
		double[] satisfaction = new double[agents.length];
		for(int i = 0; i < agents.length; i++) {
			int index = agents[i].groupIndex;
			numAgent[index]++;
			avgSoc[index] += agents[i].rememberSoc;
			satisfaction[i] = agents[i].rememberSoc;
		}
		for(int g = 0; g < population.length; g++) avgSoc[g] /= numAgent[g];
		
		for(int i = 0; i < agents.length; i++) {
			int index = agents[i].groupIndex;
			satisfaction[i] /= avgSoc[index];
			//if(satisfaction[i] > 1) satisfaction[i] = 1;
		}
		
		return satisfaction;
	}
	
	public double[][] connectivityMeasure(int layers, boolean homophilyBonus){
		//recursively defines how well connected someone is by summing the reciprocal of their neighbors connectivities
		//since popular partners have less socialization available for each one
		double cap = 2;
		double[][] connectivity = new double[layers][agents.length];
		
		for(int k = 0; k < layers; k++) {
			for(int i = 0; i < agents.length; i++) {
				double conn = 0;
				for(int j = 0; j < agents.length; j++) {
					double v;
					if(k == 0) v = 1;
					else v = 1.0/connectivity[k-1][j];
					if(v > cap) v = cap;
					//a lot of this computation is redundant and time could be saved by switching the for loops
					//but this isn't run often enough for it to matter, probably
					
					double homBonus = 1; //homophily is potentially worth more than nonhomophily
					if(homophilyBonus) {
						double sigRhoI = agents[i].socVal/agents[i].healthVal;
						double sigRhoJ = agents[j].socVal/agents[j].healthVal;
						double diff = Math.abs(sigRhoI-sigRhoJ);
						homBonus = 1 - diff/Math.max(sigRhoI, sigRhoJ);
						homBonus = homBonus*homBonus;
						
						//cosider setting homBonus based on average amount of socializing between groups
						
					}
					
					conn += homBonus*connections[i][j]*v;
				}
				connectivity[k][i] = conn;
			}
		}
		return connectivity;
	}

	public double communityInfectRate(int com) {
		if (communities==null || com < 0 || com > communities.length) return 0;
		double infTot = 0;
		for(int i = 0; i < communities[com].length; i++) {
			if(communities[com][i].infected) infTot++;
		}
		return infTot/communities[com].length;				
	}
	
	//public void decayRIS() {
		//whenever an agent is infected, increase the pairwise value (this is handled in simDay() )
		//every turn, pairwise value exponentially decays
		//longterm, this should average to the frequency of infections sent to i from j
		//with a slight emphasis on more recent interactions
		//for(int i = 0; i < rememberInfSend.length; i++) {
		//	for(int j = 0; j < rememberInfSend.length; j++) {
		//		rememberInfSend[i][j] *= 1-rememberInfSendDecay;
		//	}
		//}
	//}
	
	public int[] agentToComCon(int a) {
		//returns number of connections a has within each community
		int[] con = new int[communities.length];
		for(int i = 0; i < agents.length; i++) {
			if(connections[a][i] > 0) {
				int community = findCommunityOfAgent(agents[i]);
				con[community]++;
			}
		}
		return con;
	}
	
	public int findCommunityOfAgent(Agent a) {
		for(int c = 0; c < communities.length; c++) {
			for(int i = 0; i < communities[c].length; i++) {
				if(communities[c][i]==a) return c;
			}
		}
		System.out.println("community not found for agent: " + a);
		return -1;
	}
	
	public int findIndexInCommunity(int c, Agent a) {
		for(int i = 0; i < communities[c].length; i++) {
			if(communities[c][i]==a) return i;
		}
		System.out.println("agent not found in community " + c + ": " + a);
		return -1;
	}
	
	public int findIndexInAll(Agent a) { //easier to remember syntax for this method
		return java.util.Arrays.asList(agents).indexOf(a);
	}
	
	public double avgSocPair(){
		int denom = 0;
		double num = 0;
		for(int i = 0; i < connections.length; i++) {
			for(int j = i+1; j < connections[i].length; j++) {
				if(connections[i][j]>0) {
					num += Math.min(socialDesires[i][j], socialDesires[j][i]);
					denom++;
				}
			}
		}
		return num/denom;		
	}
	
	
	public void incHistories(Agent a, Agent b) {
		//agent a is being infected by agent b
		int c1 = findCommunityOfAgent(a);
		int c2 = findCommunityOfAgent(b);
		if(c1 == c2) {
			a.roleHistory[0]++;
			b.roleHistory[2]++;
		}
		else {
			a.roleHistory[1]++;
			b.roleHistory[3]++;
		}
	}
	
	
	public void exciseAgent(int index) {
		//severs all connections to Agent i in the network.  this effectively eliminates them for most purposes
		//and is simpler than trying to literally remove them and re-index the arrays
		for(int i = 0; i < connections.length; i++) {
			connections[i][index]=0;
			connections[index][i]=0;
			socialDesires[i][index]=0;
			socialDesires[index][i]=0;
		}
	}
	
	public void simDayMinCom(double[][] infList, double minSoc) {
		simDay(infList);
		enforceMinCom(minSoc);
	}
	
	public void simDay(double[][] infList) {
		
		//inputs a list of pre-randomized draws for probabilities between individuals
		//this allows multiple nearly-identical NetworkSimulators to be run with the same infection chances
		//such that measured differences occur entirely due to differences in structure, not to random chance

		numDays++;
		for(int i = 0; i < agents.length; i++) {
			double sumInt = 0;
			for(int j = 0; j < agents.length; j++) {
				if(connections[i][j] > 0) {
					double soc = Math.min(socialDesires[i][j], socialDesires[j][i]);
					sumInt += soc * connections[i][j];
					if(agents[j].infected) {
						double infProb = soc * transmission;
						double r = infList[i][j];
						if(r < infProb) {
							agents[i].exposed = true;
							if(epi != null && !agents[i].infected) {
								epi.checkSpread(agents[j], agents[i]);
							}
							if(rememberInfSendCheck && !agents[i].infected) {
								//rememberInfSend[i][j] += rememberInfSendDecay; 
								//incHistories(agents[i],agents[j]);
								agents[i].infectors.add(agents[j]);
							}
							if(pseudoInfections) agents[i].pseudoInfectors.add(agents[j]);
						
						}
					}
				}			
			}
			agents[i].rememberSoc = sumInt;
		}
				//infection and recovery
		for(int i = 0; i < agents.length; i++) {
			if(agents[i].infected) {
				agents[i].infectionTime--;
				if(agents[i].infectionTime <= 0) agents[i].infected = false;
			}
			else {
				if(agents[i].exposed) {
					agents[i].infected = true;
					agents[i].infectionTime = infTime;
					if(rememberInfSendCheck) {
						//double[] dBlame = agents[i].directBlame();
						//double[] blame = agents[i].applyBlame();
						//for(int t = 0; t < 4; t++) {
						//	sumBlame[t] += blame[t];
						//	sumDBlame[t] += dBlame[t];
						//}
						agents[i].directBlame2(sumDBlame2);  //tracks per agent instead of per type, to allow statistics
						agents[i].applyBlame2(sumBlame2);
					}
					totalInfections++;
					comInfections[findCommunityOfAgent(agents[i])]++;
				}
			}
			if(pseudoInfections) agents[i].directPseudoBlame(sumPBlame);
			agents[i].exposed = false;
		}
		updateInfRates(); //so groups can report average infections to agent's utility functions

		for(int i = 0; i < agents.length; i++) agents[i].updateSoc(this, i);

	}

	public double[][] getHomophilies() {
		//computes homophily by (I-E)/(I+E)
		//1st homophily is based on agent type, 2nd homophily is based on community
		//both are run simultaneously to save computation time, since right now they're only needed together
		int al = agents.length;
		double[][] homophilies = new double[al][2];
		double[][] soc = new double[al][al];
		for(int i = 0; i < al; i++) {
			for(int j = i+1; j < al; j++) {
				if(connections[i][j] == 0) continue;
				double s = Math.min(socialDesires[i][j], socialDesires[j][i]);
				soc[i][j] = s;
				soc[j][i] = s;
			}
		}
		double[] agentE = new double[al]; double[] agentI = new double[al];
		double[] comE = new double[al]; double[] comI = new double[al];
		int[] numAE = new int[al]; int[] numAI = new int[al]; //normalizers
		int[] numCE = new int[al]; int[] numCI = new int[al];
		for(int i = 0; i < al; i++) {
			for(int j = i+1; j < al; j++) {
				if(connections[i][j] == 0) continue;
				if(agents[i].groupIndex == agents[j].groupIndex) {
					agentI[i] += soc[i][j];
					agentI[j] += soc[i][j];
					numAI[i]++; numAI[j]++;
				}
				else{
					agentE[i] += soc[i][j];
					agentE[j] += soc[i][j];
					numAE[i]++; numAE[j]++;
				}
				if(findCommunityOfAgent(agents[i]) == findCommunityOfAgent(agents[j])) {
					comI[i] += soc[i][j];
					comI[j] += soc[i][j];
					numCI[i]++; numCI[j]++;
				}
				else{
					comE[i] += soc[i][j];
					comE[j] += soc[i][j];
					numCE[i]++; numCE[j]++;
				}
			}				
		}
		//normalize E and I based on the number of connections each player has of that type	
		//such that if a player has the same average socialization with people they'll equal
		//regardless of if one is more or less frequent
		for(int i = 0; i < al; i++) {
			if(numAE[i] > 0 && numAI[i] > 0 && (agentE[i]+agentI[i] > 0)) { //make sure it is meaningful
				agentE[i] /= numAE[i];
				agentI[i] /= numAI[i];
				homophilies[i][0] = (agentI[i]-agentE[i])/(agentI[i]+agentE[i]);
			}
			else {
				homophilies[i][0] = -7; //hack to indicate this value is undefined and should be ignored
				//System.out.println("-7 aHom, agent " + i + " t " + agents[i].groupIndex + " numAE " + numAE[i] + " numAI " + numAI[i] + " sumSoc " + (agentE[i]+agentI[i]));
			}
			if(numCE[i] > 0 && numCI[i] > 0 && (comE[i]+comI[i] > 0)) { //make sure it is meaningful
				comE[i] /= numCE[i];
				comI[i] /= numCI[i];
				homophilies[i][1] = (comI[i]-comE[i])/(comI[i]+comE[i]);
			}
			else {
				homophilies[i][1] = -7; //hack
				//System.out.println("-7 cHom, agent " + i + " t " + agents[i].groupIndex + " numCE " + numCE[i] + " numCI " + numCI[i] + " sumSoc " + (comE[i]+comI[i]));
			}
		}
		return homophilies;
	}
	
	
	
}

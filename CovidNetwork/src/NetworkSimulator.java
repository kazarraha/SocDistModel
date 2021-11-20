import java.util.ArrayList;
import java.util.Random;

public class NetworkSimulator extends CNSimulator {

	
	
	
	public Agent[] agents;
	public double[][] connections;
	
	public double[][] socialDesires;
	
	public int infTime = 10;  //redundant with recoveryRate
	
	
	
	
	
	public NetworkSimulator() {
		super();
		
		populateAgents();
		forgeConnections();
		seedInfections(1.0/16);
		updateInfRates();
	}
	
	
	public void populateAgents() {
		//int numAgents = 64;
		int numAgents = 200;
		agents = new Agent[numAgents];
		for(int i = 0; i < numAgents; i++) agents[i] = genRandomAgent();
	}
	
	public Agent genRandomAgent() {
		Random randy = new Random();
		int g = randy.nextInt(8);
		Group gr = population[g];
		Agent a = new Agent(gr.social, gr.conformity, gr.health);
		a.groupIndex = g;
		return a;
	}

	public void forgeConnections() {
		double connProb = 10.0/(agents.length-1);
		//currently all random.  update later to include homophily and clustering and stuff
		Random randy = new Random();
		connections = new double[agents.length][agents.length];
		socialDesires = new double[agents.length][agents.length];
		for(int i = 0; i < connections.length; i++) {
			for(int j = i+1; j < connections.length; j++) {
				double p = randy.nextDouble();
				if(p < connProb) {
					double d = randy.nextDouble();
					connections[i][j] = d;	connections[j][i] = d;
					socialDesires[i][j] = 1; socialDesires[j][i] = 1;
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
	
	public void updateInfRates() {
		//measures infection among all agents and computes averages for each type
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
	
	
	public void simDay() {
		
		//TODO: redo how agent socializing works to only include mutually connected people
		//O(Nc) rather than O(N^2), where c is the average number of connections per agent
		
		numDays++;
		for(int i = 0; i < agents.length; i++) {
			double sumInt = 0;
			for(int j = 0; j < agents.length; j++) {
				if(connections[i][j] > 0) {
					double soc = Math.min(socialDesires[i][j], socialDesires[j][i]);
					sumInt += soc * connections[i][j];
					if(agents[j].infected) {
						double infProb = soc * transmission;
						Random randy = new Random();
						double r = randy.nextDouble();
						if(r < infProb) agents[i].exposed = true;
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
				}
			}
			agents[i].exposed = false;
		}
		updateInfRates(); //so groups can report average infections to agent's utility functions
		

		for(int i = 0; i < agents.length; i++) agents[i].updateSoc(this, i);
		

		
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
	
	
	
	
}

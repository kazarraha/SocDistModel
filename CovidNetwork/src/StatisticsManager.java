import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class StatisticsManager {

	//primary purpose is to pass data into it from multiple instances of Epidemiologist without
	//having to rework a bunch of code to remember them
	//then do statistics stuff on the aggregate rather than on each individually
	
	

	
	public int mainType;
	public int numCom;
	public int comSize;
	
	public ArrayList<Integer>[][] epidemicHistories;
	public int index = 0;
	//public int[][][] epidemicHistories2; //number of epidemics caused by Type, of certain Size   [Type][Size] (capped at 20)
	//public int[][] epidemicWeightedSums;
	
	
	public StatisticsManager(int numRep) {
		
		epidemicHistories = new ArrayList[numRep][4];
		//epidemicHistories2 = new int[numRep][4][20];
		//epidemicWeightedSums = new int[numRep][4];
		
	}
	
	public void setParameters(int mt, int nc, int cs) {
		mainType = mt;
		numCom = nc;
		comSize = cs;
	}
	
	public String epiHistToCSVString(){
		StringBuilder sb = new StringBuilder();
		sb.append(mainType +"," + numCom +","+comSize+",#" + "\n");
		for(int epi = 0; epi < epidemicHistories.length; epi++) {
			if(epi > 0) { sb.append('\n'); sb.append("#"); sb.append('\n');} // would "\n#\n" work instead?  I'm not entirely sure how sb.append works
			for(int t = 0; t < epidemicHistories[epi].length; t++) {
				if(t > 0) sb.append('\n');
				//System.out.println("epi " + epi + " t " + t);
				if(epidemicHistories[epi][t] == null) System.out.println("epidemicHistory null");
				for(int i = 0; i < epidemicHistories[epi][t].size(); i++) {
					if(i > 0) sb.append(',');
	        		sb.append(epidemicHistories[epi][t].get(i));
				}
			}
		}
		return sb.toString();
	}
	
	public void saveSelf(String name) {
		ActionManager.saveToCSV(epiHistToCSVString(), name);
	}
	
	public static StatisticsManager load(String name) {
		String saveFolder = "C:/Users/Matthew/Desktop/Conway Tex/SIR stuff/Covid Network Stuff/eclipseSaves/";
		return load(name, saveFolder);
	}
	
	public static StatisticsManager load(String name, String saveFolder) {
		String savePath = saveFolder + name + ".csv";
		  
		
		File file = new File(savePath);
		try {
			Scanner s = new Scanner(file).useDelimiter("#");
			ArrayList<String> epiStrings = new ArrayList<String>();
			while(s.hasNext()) {
				epiStrings.add(s.next());
			}
			StatisticsManager statMan = new StatisticsManager(epiStrings.size()-1);
			String md = epiStrings.get(0);
			String[] miscData = md.split(",");
			statMan.mainType = Integer.parseInt(miscData[0]); statMan.numCom = Integer.parseInt(miscData[1]); statMan.comSize = Integer.parseInt(miscData[2]);
			//System.out.println("mt: " + statMan.mainType + " nc: " + statMan.numCom + " sc: " + statMan.comSize);
			for(int r = 1; r < epiStrings.size(); r++) {
				String[] rows = epiStrings.get(r).split("\n"); 
				//int bump = 0;
				//if(r>0) bump = 1;
				int bump = 1;
				for(int i = 0; i < 4; i++) { //some indicides go fom 1 to 5 because the # is surrounded by \n, but some go 0 to 4, the bump takes care of the index shift
					statMan.epidemicHistories[r-1][i] = new ArrayList<Integer>();
					String[] row = rows[i+bump].split(",");
					for(int j = 0; j < row.length; j++) {
						if(!row[j].equals("")) statMan.epidemicHistories[r-1][i].add(Integer.parseInt(row[j]));
					}
				}	
			}
			return statMan;
			
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("Could not load StatisticsManager.load, file not found: " + name);
		}
		return null; //only reached if the fileLoad fails
	}
	
	
	public void recordEpidemiologist(int in, Epidemiologist epi) {
		epidemicHistories[in] = epi.epidemicHistory; //full accounting of each epidemic, its type, and length
		//epidemicHistories2[index] = epi.epidemicHistory2; //
		//epidemicWeightedSums[index] = epi.epidemicWeightedSum;
	}
	
	public void recordEpidemiologist(Epidemiologist epi) {
		epidemicHistories[index] = epi.epidemicHistory;
		index++;
		//System.out.println("recordEpidemiologist " + (index-1));
	} 
	
	
	public static int[] arrayListToArray(ArrayList<Integer> data) {
		int[] newData = new int[data.size()];
		for(int i = 0; i < data.size(); i++) newData[i] = data.get(i);
		return newData;
	}
	
	public static double getMean(double[] data) {
		double mean = 0;
		for(int i = 0; i < data.length; i++) mean += data[i];
		mean /= data.length;
		return mean;
	}
	public static double getMean(ArrayList<Double> data) {
		double mean = 0;
		for(int i = 0; i < data.size(); i++) mean += data.get(i);
		mean /= data.size();
		return mean;
	}
	
	
	public static double getStdDev(double[] data, double mean) {
		double var = 0;
		for(int i = 0; i < data.length; i++) var += (data[i]-mean)*(data[i]-mean);
		var /= data.length;
		double stdDev = Math.sqrt(var);
		return stdDev;
	}
	public static double getStdDev(ArrayList<Double> data, double mean) {
		double var = 0;
		for(int i = 0; i < data.size(); i++) var += (data.get(i)-mean)*(data.get(i)-mean);
		var /= data.size();
		double stdDev = Math.sqrt(var);
		return stdDev;
	}
	
	
	public static double getStdDevPop(double[] data, double mean) {
		return getStdDev(data, mean)/Math.sqrt(data.length);
	}
	public static double getStdDevPop(ArrayList<Double> data, double mean) {
		return getStdDev(data, mean)/Math.sqrt(data.size());
	}
	
	
	public int getNumEpi(int type) {
		int numEpi = 0;
		for(int i = 0; i < epidemicHistories.length; i++) {
			if(epidemicHistories[i][type] == null) System.out.println("epidemicHistories["+i+"]["+type+"] null");
			numEpi +=epidemicHistories[i][type].size();
		}
		return numEpi;
	}
	
	public double[][] statsNumEpidemics(int time) {
		double[][] data = new double[4][2];
		for(int i = 0; i < 4; i++) data[i] = statsNumEpidemicsType(i, time);
		return data;
	}
	
	
	public double[] statsNumEpidemicsType(int type, int time) {
		//assumes at each time step, there is some probability of an epidemic ending (a minor approximation)
		//stdDev = sqrt(p(1-p))
		//stdDevPop = stdDev/sqrt(n)
		
		//I am not confident that this is the correct way to handle sample variance
		//given multiple runs of the simulation
		
		double rt = time * epidemicHistories.length; //original time variable should be time of a single sim, this adjusts
		double numEpi = getNumEpi(type);
		double p = numEpi/rt;
		double stdDev = Math.sqrt(p*(1-p));
		double stdDevPop = stdDev/Math.sqrt(rt);
		
		//unnormalizes by time to get total values for one sim run
		p *= time;
		stdDevPop *= time;
		
		double[] output = {p,stdDevPop};
		return output;
	}
	
	public double[][] statsEpidemicSize(){
		double[][] output = new double[4][2]; //[type][mean or stdDev]
		for(int i = 0; i < 4; i++) output[i] = statsEpidemicSizeType(i);
		return output;
	}
	

	public double[] statsEpidemicSizeType(int type) {
		int numEpi = getNumEpi(type);
		double[] data = new double[numEpi];
		int index = 0;
		for(int epi = 0; epi < epidemicHistories.length; epi++) {
			for(int j = 0; j < epidemicHistories[epi][type].size(); j++) {
				data[index] = epidemicHistories[epi][type].get(j);
				index++;
			}
		}
		//
		if(index < data.length) System.out.println("stdDevEpidemicSizeType data.length miscalculated.  index: " + index +" data.length " + data.length);
		//
		double mean = getMean(data);
		double stdDevPop = getStdDevPop(data, mean);
		double[] output = {mean, stdDevPop};
		return output;		
	}
	
	public double[][] statsWeightedSum(int time){
		double[][] output = new double[4][2]; //[type][mean or stdDev]
		for(int i = 0; i < 4; i++) output[i] = statsWeightedSumType(i, time);
		return output;
	}
	
	public double[] statsWeightedSumType(int type, int time) {
		//treats each epidemic end as a value equal to its length, and each timestep without one ending as 0
		//this might not be the best way to measure this given that epidemics actually occur over time
		//and there are 16 communities.  but it maybe works?
		int rt = time * epidemicHistories.length;
		double sum = 0;
		int numEpi = 0;
		for(int e = 0; e < epidemicHistories.length; e++) {
			for(int i = 0; i < epidemicHistories[e][type].size(); i++) sum += epidemicHistories[e][type].get(i);
		}
		double mean = sum/rt;
		double var = 0;
		for(int e = 0; e < epidemicHistories.length; e++) {
			for(int i = 0; i < epidemicHistories[e][type].size(); i++) {
				var += (epidemicHistories[e][type].get(i)-mean)*(epidemicHistories[e][type].get(i)-mean);
				numEpi++;
			}
		}
		int numZero = rt - numEpi;
		var += numZero*mean*mean;
		var /= rt;
		double stdDev = Math.sqrt(var);
		double stdDevPop = stdDev/Math.sqrt(rt);
		//unnormalize by time
		mean *= time;
		stdDevPop *= time;
		
		
		double[] output = {mean, stdDevPop};
		return output;
	}


	
	
	
}

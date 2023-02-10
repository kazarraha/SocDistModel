import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class DataManager {

	
	
	
	
	//public static String saveFolder = "C:/Users/Matthew/eclipse-workspace/CovidNetworkSaves/";
	public static String saveFolder = "C:/Users/Matthew/Desktop/Conway Tex/SIR stuff/Covid Network Stuff/eclipseSaves/";
	
	
	
	
	
	
	
	
	
	public static String arrayToString(double[] data, String d1) { //1 dimensional array
		String s = "";
		for(int i = 0; i < data.length; i++) {
			s+= data[i];
			if(i < data.length-1) s+= d1;
		}
		return s;		
	}
	
	public static String arrayToString(double[][] data, String d1, String d2) { //2 dimensional array
		String s = "";
		for(int i = 0; i < data.length; i++) {
			s+= arrayToString(data[i],d1);
			if(i < data.length-1) s+= d2;
		}
		return s;	
	}
	
	public static String arrayToString(double[][][] data, String d1, String d2, String d3) { //3 dimensional array
		String s = "";
		for(int i = 0; i < data.length; i++) {
			s+= arrayToString(data[i],d1,d2);
			if(i < data.length-1) s+= d3;
		}
		return s;	
	}
	
	public static double[] stringToArray(String s, String d1) { //1 dimensional array
		String[] s1 = s.split(d1);
		double[] data = new double[s1.length];
		for(int i = 0; i < data.length; i++) {
			try
			{ data[i] = Double.parseDouble(s1[i]);	}
			catch(NumberFormatException e)
			{ data[i] = -1; 	} //presumably data for this slot will be ignored, but just in case, -1 seems most likely to stand out if mistakes are made
			
		}
		return data;
	}
	
	public static double[][] stringToArray(String s, String d1, String d2) { //2 dimensional array
		String[] s2 = s.split(d2);
		String[] s1temp = s2[0].split(d1);
		double[][] data = new double[s2.length][s1temp.length];
		for(int i = 0; i < data.length; i++) {
			data[i] = stringToArray(s2[i], d1);
		}
		return data;
	}
	
	public static double[][][] stringToArray(String s, String d1, String d2, String d3) { //3 dimensional array
		String[] s3 = s.split(d3);
		String[] s2temp = s3[0].split(d2);
		String[] s1temp = s2temp[0].split(d1);
		double[][][] data = new double[s3.length][s2temp.length][s1temp.length];
		for(int i = 0; i < data.length; i++) {
			data[i] = stringToArray(s3[i], d1, d2);
		}
		return data;
	}
	
	
	
	
	
	public static void saveData(double[][][] data, String slot) {
		String path = saveFolder + slot;
		String content = arrayToString(data, ",","/","&");
		saveToFile(content, path);		
	}
	
	public static void saveData(double[][] data, String slot) {
		String path = saveFolder + slot;
		String content = arrayToString(data, ",","/");
		saveToFile(content, path);		
	}
	
	public static double[][][] loadData3(String slot){
		String path = saveFolder+slot;
		String content = loadFromFile(path);
		double[][][] data  = stringToArray(content, ",", "/","&");
		return data;		
	}
	
	public static double[][] loadData2(String slot){
		String path = saveFolder+slot;
		String content = loadFromFile(path);
		double[][] data  = stringToArray(content, ",", "/");
		return data;		
	}
	
	
	
	public static boolean saveToFile(String content, String path) {
		try {	  
	       File newTextFile = new File(path);
	       FileWriter fw = new FileWriter(newTextFile);
	       fw.write(content);
	       fw.close();
	       return true;
	    } catch (IOException iox) {
	          //do stuff with exception
	       iox.printStackTrace();
	       System.out.println("Save failed in DataManager.saveToFile(): " + path);
	       return false;
	    }	
	}
	
	
	public static String loadFromFile(String path) {
		File file = new File(path);
		try {
			Scanner s = new Scanner(file);
			String strin = s.next();
			return strin;

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("Could not load String, DataManager.loadFromFile: " + path);
			return "";
		}
	}
	
	
	
	public static double[] uncurlData2to1(double[][] data2) {//takes 2D array and lists it in 1D, for scatterplot purposes
		double[] data = new double[data2.length*data2[0].length];
		for(int i = 0; i < data2.length; i++) {
			for(int j = 0; j < data2[i].length; j++) {
				data[j+i*data2.length] = data2[i][j];
			}
		}
		return data;		
	}
	
	
	public static double[][] loadFromCSV(String name) {
		String saveFolder = "C:/Users/Matthew/Desktop/Conway Tex/SIR stuff/Covid Network Stuff/eclipseSaves/";
		String savePath = saveFolder + name + ".csv";
		  
		
		File file = new File(savePath);
		try {
			Scanner s = new Scanner(file).useDelimiter("\n");
			ArrayList<String> rows = new ArrayList<String>();
			while(s.hasNext()) {
				rows.add(s.next());
			}
			
			int max = 0;
			String[][] sRows = new String[rows.size()][1];
			for(int r = 0; r < rows.size(); r++) {	
				sRows[r] = rows.get(r).split(",");
				if(sRows[r].length > max) max = sRows[r].length;
			}
			double[][] data = new double[rows.size()][max];
			for(int r = 0; r < rows.size(); r++) {
				String[] row = sRows[r];
				for(int i = 0; i < row.length; i++) {
					try
					{ data[r][i] = Double.parseDouble(row[i]);	}
					catch(NumberFormatException e)
					{ data[r][i] = -1; 	} //presumably data for this slot will be ignored, but just in case, -1 seems most likely to stand out if mistakes are made
					
				}
			}
			return data;
	
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("Could not load StatisticsManager.load, file not found: " + name);
		}
		return null; //only reached if the fileLoad fails
	}
	
	
	
	
}

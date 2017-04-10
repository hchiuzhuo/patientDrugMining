package org.imeds.seqmining;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoVMSP;


/**
 * Example of how to use the VMSP algorithm in source code.
 * @author Philippe Fournier-Viger
 */
public class basicVMSP {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		
//		for(int i=0;i<4;i++){
			double thereshold = 0.1;
			String input ="data\\IMEDS\\DiabeteComorbidDS\\seqDS\\seq_drug_ingredient_only.csv";
			String output = "data\\IMEDS\\DiabeteComorbidDS\\seqptn\\seq_drug_ingredient_only_"+thereshold+"_out.txt";	
		
			AlgoVMSP algo = new AlgoVMSP(); 
//			algo.setMaximumPatternLength(3);
			
			// execute the algorithm with minsup = 2 sequences  (50 %)
			algo.runAlgorithm(input, output,thereshold );    
			algo.printStatistics();
			
//		}
		// Create an instance of the algorithm 
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = basicVMSP.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
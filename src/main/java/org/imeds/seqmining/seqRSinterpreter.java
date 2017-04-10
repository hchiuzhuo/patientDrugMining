package org.imeds.seqmining;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.imeds.data.common.CCIcode;
import org.imeds.db.ImedDB;
import org.imeds.util.CCIcsvTool;
import org.imeds.util.OSValidator;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Itemset;


public class seqRSinterpreter {
	private ArrayList<String> srcList = new ArrayList<String>() ;
	private ArrayList<ArrayList<Integer>> indataList = new ArrayList<ArrayList<Integer>>();
	private HashMap<Integer, String> cptmap = new HashMap<Integer, String>();
	private ArrayList<String> outdataList = new ArrayList<String>();
	public seqRSinterpreter() {
	
	}
	public  void processFile(String infolderName, String outfolderName) throws Exception{
		File directory = new File(infolderName);
		File[] fList = directory.listFiles();
		for (File file : fList){		
			if (file.isFile()){
				readFile(infolderName+OSValidator.getPathSep()+file.getName());
				ImedDB.getDisSemanticConcept(cptmap);
				formSemantic();
				writeFile(outfolderName+OSValidator.getPathSep()+"semantic_"+file.getName());				 
			}
		}
	}
	public void readFile(String FileName) throws FileNotFoundException{
		indataList = new ArrayList<ArrayList<Integer>>();
		cptmap = new HashMap<Integer, String>();
		srcList = new ArrayList<String>() ;
		FileInputStream fis = new FileInputStream(FileName);
		Scanner scanner = new Scanner(fis);
		
		// reading file line by line using Scanner in Java
//		System.out.println("Reading file line by line in Java using Scanner");
		int idx = 0;
		String rec =""; 
		String line = "";
		String[] linesplit;
		
		while (scanner.hasNextLine()) {
			
			rec = scanner.nextLine();
			srcList.add(rec+",");
			if(idx>0){
				line = rec.substring(rec.lastIndexOf(",")+1, rec.length());
				linesplit = line.split(" ");
				ArrayList<Integer> itemarr = new ArrayList<Integer>();
				for(int i=0;i<linesplit.length;i++){				
					String itemtmp = linesplit[i].trim();
					if(itemtmp!=null && !itemtmp.equals("")){
						Integer cpttmp = Integer.parseInt(itemtmp);
						if(cpttmp>0){
							cptmap.put(cpttmp,"");
						}
						itemarr.add(cpttmp);					
					}
					
				}
				indataList.add(itemarr);
			}
			idx++;
		}
	}
	public void formSemantic(){
	  outdataList = new ArrayList<String>();	  
      for(ArrayList<Integer> row:indataList ){
    	  	 String line="";
			 for(Integer ri:row){
				 if(this.cptmap.containsKey(ri))
				 {
					 String tmp = this.cptmap.get(ri);
					 tmp = tmp.replace(",", ".");
					 line = line + "'"+tmp+"' ";
				 }else{
					 line = line + ri+" ";
				 }
			 }
			 outdataList.add(line);
		}			    
	}
	public void writeFile(String fileName){
		
		FileWriter fstream;
		try {
			  fstream = new FileWriter(fileName);
		
		      BufferedWriter out = new BufferedWriter(fstream);
		      out.write(srcList.get(0)+"semantic");
		      out.newLine();
		      for(int i=1;i<srcList.size();i++){
		    	  out.write(srcList.get(i)+outdataList.get(i-1));
		    	  out.newLine();
		      }
		      out.close();    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		 seqRSinterpreter srsi = new  seqRSinterpreter();
		 srsi.processFile("data\\IMEDS\\DiabeteComorbidDS\\seqptn", "data\\IMEDS\\DiabeteComorbidDS\\seqptnSemantic");
		 ImedDB.closeDB();
	}

}

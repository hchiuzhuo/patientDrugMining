package org.imeds.validation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.imeds.data.SurvivalTime;
import org.imeds.db.ImedDB;
import org.imeds.db.ImedR;
import org.imeds.feature.selection.basicItemsets;
import org.imeds.seqmining.RoutSemantic;
import org.imeds.util.CCIcsvTool;
import org.imeds.util.OSValidator;
import org.imeds.util.writeException;

public class contraIndicate {
	
	private HashMap<Integer, String> contraIndimap = new HashMap<Integer, String>();
	//patterns map in semantic_seq
	private HashMap<Integer, basicItemsets> ptnmap = new HashMap<Integer, basicItemsets>();
	private HashMap<Integer, String> cptmap = new HashMap<Integer, String>();
	private final String USER_AGENT = "Mozilla/5.0";
	
	public void processSemanticSeqFile(String fileName){
		try {
			
			CCIcsvTool.semanticSeqParserDoc(fileName,ptnmap, cptmap);
			Iterator<Entry<Integer, basicItemsets>> iter = ptnmap.entrySet().iterator();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public ArrayList<Integer> getContraIndiItem(basicItemsets itemsetsList){
		ArrayList<Integer> result = new ArrayList<Integer>();
		ArrayList<TreeSet<Integer>> itemsetList=itemsetsList.getItemsets();
		for(TreeSet<Integer> itemset: itemsetList){
			Iterator iterator= itemset.iterator();
		     
		      // displaying the Tree set data
		          
		      while (iterator.hasNext()){
		    	  Integer key=(Integer) iterator.next();
		    	  if(contraIndimap.containsKey(key)) result.add(key);
		      }
		}
		
		return result;
	}
	public  void processRoutFile(String outDDIfile,String outContraFile,String statFile,String infolderName, String filetype) throws Exception{
		File directory = new File(infolderName);
		File[] fList = directory.listFiles();
		Integer fileId=-1;
		String fileName;
	
		for (File file : fList){		
			if (file.isFile()){
				fileName = infolderName+OSValidator.getPathSep()+file.getName();
				
				if(fileName.contains(filetype)){
					
					ArrayList<ptnValidate> pvList=getSignificantAttr(fileName);
					ImedR.getAdjustPvalue(pvList);
					//System.out.println(pvList);
					filterInsignificantAttr(pvList);
					System.out.println(pvList);
//					getContraIndiPtn(pvList,file.getName());
					writeFileDDIfile2(outDDIfile,outContraFile,statFile, pvList,file.getName());
					pvList=null;
					//break;
				}

			}
		}
		
	}
	public ArrayList<ptnValidate> getSignificantAttr(String fileName){
		//String fileName="data/IMEDS/Exp_06/SurvivalSeqDS_6/seqptnSemantic/Rout/seq_2014-10-10_SPADE_0.1_coverage_modelFreeScore_4.csv";
		
		ArrayList<String> strList= new ArrayList<String>();
		ArrayList<ptnValidate> pvList= new ArrayList<ptnValidate>(); 
		
		try {
			CCIcsvTool.RoutParserDoc(fileName,strList);
			for(String s:strList){
				String[] row=s.split(",");
				Integer id=-1;
				if(!row[0].equals("null")) id=Integer.parseInt(row[0]);
				
				String sid=row[1];
				Double coef=Double.parseDouble(row[2]);
				Double pv=Double.parseDouble(row[6]);
				pvList.add(new ptnValidate(sid,id,coef,pv));
				
//				System.out.println(pvList.get(pvList.size()-1));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pvList;

	}

	public void filterInsignificantAttr(ArrayList<ptnValidate> pvList){
		for(int i=0;i<pvList.size();i++){
//			if(pvList.get(i).getCoef()<=0 ||pvList.get(i).getAdjPvalue()>=0.05){
//			if(pvList.get(i).getCoef()>0 ||pvList.get(i).getAdjPvalue()>=0.05){
			if(pvList.get(i).getAdjPvalue()>0.05){
				pvList.remove(i);
				i--;
			}			
		}
	}
	
	public void getContraIndiPtn(ArrayList<ptnValidate> pvList, String filename){
		
		for(ptnValidate pv: pvList){
			if(pv.getId()>-1){
				//check contraIndicate Items
				ArrayList<Integer> result=getContraIndiItem(ptnmap.get(pv.getId()));
//				if(result.size()>0){
					System.out.println(pv.getId()+","+pv.getCoef()+", "+pv.getPvalue()+", "+pv.getAdjPvalue()+","+ptnmap.get(pv.getId()).getItemsets().toString()+","+result.toString());
//				}
					
				//check DDI
				checkDDI(ptnmap.get(pv.getId()));	
			}
		}
	}
	public static void writeFileHeader(String outDDIfile,String outContraFile,String statFile){
		
		FileWriter fstream, contraStream, contraStatStream;
		try {
			  fstream = new FileWriter(outDDIfile);		
		      BufferedWriter out = new BufferedWriter(fstream);
		      out.write("filename,DDI,seqId,coef,P-value,adjP,seq,semantic");
		      out.newLine();
		      
		      contraStream = new FileWriter(outContraFile);		
		      BufferedWriter contraout = new BufferedWriter(contraStream);
		      contraout.write("filename,seqId,coef,P-value,adjP,seq,semantic,contraIgdt");
		      contraout.newLine();
		      
		      contraStatStream = new FileWriter(statFile);		
		      BufferedWriter statout = new BufferedWriter(contraStatStream);
		      statout.write("filename,#SigPtn,#contraSigPtn,#contraPtnratio,#DDIPtn,#DDIPtnratio");
		      statout.newLine();
		      
		      
		      contraout.close();  
		      out.close(); 
		      statout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void writeFileDDIfile(String outDDIfile,String outContraFile,String statFile, ArrayList<ptnValidate> pvList, String filename){
		
		FileWriter fstream, contraStream, contraStatStream;
		try {
			  fstream = new FileWriter(outDDIfile, true);		
		      BufferedWriter out = new BufferedWriter(fstream);
		      
		      contraStream = new FileWriter(outContraFile, true);		
		      BufferedWriter contraout = new BufferedWriter(contraStream);
		      
		      contraStatStream = new FileWriter(statFile,true);		
		      BufferedWriter statout = new BufferedWriter(contraStatStream);
		      
		      double numOfSigPtn=0;
		      double numOfSigContraPtn=0;
		      double numofDDI=0;
		      for(ptnValidate pv: pvList){
					if(pv.getId()>-1){
						numOfSigPtn++;
						ArrayList<Integer> contraResult=getContraIndiItem(ptnmap.get(pv.getId()));
						if(contraResult.size()>0){
							numOfSigContraPtn++;
							contraout.write(filename+","+pv.getId()+","+pv.getCoef()+", "+pv.getPvalue()+", "+pv.getAdjPvalue()+",");
							contraout.write(formSeqStr(ptnmap.get(pv.getId()))+",");
							
							for(Integer cid: contraResult){
								contraout.write(contraIndimap.get(cid)+"|");
							}
							contraout.newLine();
						}
						//check DDI
						String result=checkDDI(ptnmap.get(pv.getId()));
						if(result!=null && !result.trim().equals("")){
							numofDDI++;
							out.write(filename+","+result+","+pv.getId()+","+pv.getCoef()+", "+pv.getPvalue()+", "+pv.getAdjPvalue()+",");
							out.write(formSeqStr(ptnmap.get(pv.getId())));
							out.newLine();
						}
					}
				}
		      
		      statout.write(filename+","+numOfSigPtn+","+numOfSigContraPtn+","+(numOfSigContraPtn/numOfSigPtn)+","+numofDDI+","+(numofDDI/numOfSigPtn));
		      statout.newLine();
		      contraout.close();  
		      out.close();  
		      statout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
public void writeFileDDIfile2(String outDDIfile,String outContraFile,String statFile, ArrayList<ptnValidate> pvList, String filename){
		
		FileWriter fstream, contraStream, contraStatStream;
		try {
			  fstream = new FileWriter(outDDIfile, true);		
		      BufferedWriter out = new BufferedWriter(fstream);
		      
		      contraStream = new FileWriter(outContraFile, true);		
		      BufferedWriter contraout = new BufferedWriter(contraStream);
		      
		      contraStatStream = new FileWriter(statFile,true);		
		      BufferedWriter statout = new BufferedWriter(contraStatStream);
		      
		      double numOfSigPtn=0;
		      double numOfSigContraPtn=0;
		      double numofDDI=0;
		      for(ptnValidate pv: pvList){
					if(pv.getId()>-1){
						numOfSigPtn++;
							numOfSigContraPtn++;
							contraout.write(filename+","+pv.getId()+","+pv.getCoef()+", "+pv.getPvalue()+", "+pv.getAdjPvalue()+",");
							contraout.write(formSeqStr(ptnmap.get(pv.getId()))+",");
							
							contraout.newLine();
						
					
					}
				}
		      
		      statout.write(filename+","+numOfSigPtn+","+numOfSigContraPtn+","+(numOfSigContraPtn/numOfSigPtn)+","+numofDDI+","+(numofDDI/numOfSigPtn));
		      statout.newLine();
		      contraout.close();  
		      out.close();  
		      statout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	private String formSeqStr(basicItemsets itemsetsList){
		String rstId="";
		String rstStr="";
		ArrayList<TreeSet<Integer>> itemsetList=itemsetsList.getItemsets();
		TreeSet<Integer> distinctItem=new TreeSet<Integer>();
		for(TreeSet<Integer> itemset: itemsetList){
			
			Iterator iterator= itemset.iterator();
		         
		      while (iterator.hasNext()){
		    	  Integer key=(Integer) iterator.next();
		    	  rstId=rstId+key+" ";
		    	  rstStr=rstStr+"'"+cptmap.get(key)+"'"+" ";
		      }		
		     rstId+="-1 ";
		     rstStr+="-1 ";
		}
			rstId=rstId+"-2 ";
			rstStr=rstStr+"-2 ";
			return rstId+","+rstStr;
	}
	private String checkDDI(basicItemsets itemsetsList){
		String result="";
		ArrayList<TreeSet<Integer>> itemsetList=itemsetsList.getItemsets();
		TreeSet<Integer> distinctItem=new TreeSet<Integer>();
		for(TreeSet<Integer> itemset: itemsetList){
			Iterator iterator= itemset.iterator();
		          
		      while (iterator.hasNext()){
		    	  Integer key=(Integer) iterator.next();
		    	  distinctItem.add(key);
		      }
		}
		ArrayList<Integer>arrDistinctItem =new ArrayList<Integer>();
		arrDistinctItem.addAll(distinctItem);
		
		if(arrDistinctItem.size()>1){
			for(int i=0;i<arrDistinctItem.size();i++){
				
				for(int j=i+1;j<arrDistinctItem.size();j++){
				
					try {
						 result =result+sendGet(cptmap.get(arrDistinctItem.get(i)).toLowerCase(),cptmap.get(arrDistinctItem.get(j)).toLowerCase());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return result;
	}
	private String sendGet(String drug1, String drug2) throws Exception {

		String result="";
		String url = "http://www.healthline.com/druginteractions?addItem="+drug1+"&addItem="+drug2;
 
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
//		System.out.println( url);
//		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		boolean isFind=false;
		boolean recordBegin=false;
		while ((inputLine = in.readLine()) != null) {
			if(recordBegin)response.append(inputLine);
			if(inputLine.contains("interaction resultsShadow")){
				response.append(inputLine);
				recordBegin=true;
			}
		
			if(inputLine.contains(drug1+"<span>+</span>"+drug2)){
	
				isFind=true;
				recordBegin=false;
				break;
			}			
		}
		in.close();
 
		//print result
		if(isFind){
			String responseStr=response.toString();
			if(responseStr.contains(drug1+"<span>+</span>"+drug2)){
				result=result+(drug1+"+"+drug2);
				if(responseStr.contains("Severe")) result+=" is Severe;";
				else if(responseStr.contains("Moderate")) result+=" is Moderate;";
				else if(responseStr.contains("Minor")) result+=" is Minor;";	
				
//				System.out.println(responseStr);
			}
			
		}
		return result;
 
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		int[] arr={1,2,3,4,5};
//		for(int i=0;i<arr.length;i++){
//			for(int j=i+1;j<arr.length;j++){
//				System.out.println("("+arr[i]+","+arr[j]+")");
//			}
//		}
		
		try {
			ImedR.connR();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		try {
			ArrayList<String> spmTypeList = new ArrayList<String>();
//			spmTypeList.add("SPADE_0.025");
//			spmTypeList.add("SPADE_0.05");
////			spmTypeList.add("SPADE_0.1");
//			spmTypeList.add("VMSP_0.025");
			spmTypeList.add("VMSP_0.05");
//			spmTypeList.add("VMSP_0.1");
			for(String spmType:spmTypeList){
			contraIndicate ci=new contraIndicate();
			CCIcsvTool.contraIndiparserDoc("data/IMEDS/Exp_06/Validate/contraIndiIgdt.txt",ci.contraIndimap);
			
			String outDDIfile	="data/IMEDS/Exp_06/validate/sanity_chk/outDDIfile_"+spmType+".csv";
			String outContraFile="data/IMEDS/Exp_06/validate/sanity_chk/contraIndifile_"+spmType+".csv";
			String statFile		="data/IMEDS/Exp_06/validate/sanity_chk/statfile_"+spmType+".csv";
			ci.processSemanticSeqFile("data/IMEDS/Exp_06/SurvivalSeqDS_6/seqptnSemantic/semantic_seq_2014-10-10_"+spmType+"_support.csv");
		
			writeFileHeader(outDDIfile,outContraFile,statFile);
			ci.processRoutFile(outDDIfile,outContraFile,statFile,"data/IMEDS/Exp_06/SurvivalSeqDS_6/seqptnSemantic/Rout","seq_2014-10-10_"+spmType+"_support_");
			ci.processRoutFile(outDDIfile,outContraFile,statFile,"data/IMEDS/Exp_06/SurvivalSeqDS_6/seqptnSemantic/Rout","seq_2014-10-10_"+spmType+"_modelFreeScore_");
			ci.processRoutFile(outDDIfile,outContraFile,statFile,"data/IMEDS/Exp_06/SurvivalSeqDS_6/seqptnSemantic/Rout","seq_2014-10-10_"+spmType+"_coverage_support_");
			ci=null;
			}
			
			ImedR.closeR();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

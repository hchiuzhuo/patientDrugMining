package org.imeds.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.imeds.data.ComorbidDataSetConfig;
import org.imeds.feature.screening.feature;
import org.imeds.feature.screening.ModelFreeScreen.MFStype;
import org.imeds.feature.selection.MMRFSConfig;
import org.imeds.feature.selection.basicItemsets;
import org.imeds.feature.selection.discrimItemsets;
import org.imeds.feature.selection.labelItemsets;
import org.imeds.seqmining.SequenceDataSetConfig;

public class SPMdocTool  implements DocumentTool{ 

	public SPMdocTool() {
		// TODO Auto-generated constructor stub
	}

	/********************
	 *  Write			*	
	 * 					*
	 ********************/
	public void createDoc(String fileName) {
		// TODO Auto-generated method stub
		
	}
	public void createFeatureFileModelFree(String fileName, ArrayList<discrimItemsets>  arrayList) {
		
		FileWriter fstream;
		try {
			  fstream = new FileWriter(fileName);
		
		      BufferedWriter out = new BufferedWriter(fstream);
		      out.write("support,modelFreeScore,seqId,seq");
			  out.newLine();
		      for(discrimItemsets row:arrayList){
		    	
		    	out.write(row.getSupport()+",");
		    	
		    	out.write(row.getGain()+",");
		    	out.write(row.getItemsets().getId()+",");
		    	for(TreeSet<Integer> ri:row.getItemsets().getItemsets()){
					String setstr = ri.toString();
					setstr = setstr.substring(setstr.indexOf("[")+1,setstr.indexOf("]")).replace(",","");						 
					out.write(setstr+" -1 ");
				}
					 out.write(" -2 ");
					 out.newLine();
		      }
		      //Close the output stream
		      out.close();    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void createFeatureFilePreSemantic(String fileName,String featureType, ArrayList<discrimItemsets>  arrayList) {
		
		FileWriter fstream;
		try {
			  fstream = new FileWriter(fileName);
		
		      BufferedWriter out = new BufferedWriter(fstream);
		      out.write(featureType+",seqId,seq");
			  out.newLine();
			  int length=arrayList.size();
			  	
			  
			      for(discrimItemsets row:arrayList){
			    	if(featureType.contains("support")){
			    		out.write(row.getSupport()+",");
			    	}else if(featureType.contains("modelFreeScore")){
			    		out.write(row.getGain()+",");
			    	}else{
			    		out.write((length--)+",");
			    	}
			    	out.write(row.getItemsets().getId()+",");
			    	for(TreeSet<Integer> ri:row.getItemsets().getItemsets()){
						String setstr = ri.toString();
						setstr = setstr.substring(setstr.indexOf("[")+1,setstr.indexOf("]")).replace(",","");						 
						out.write(setstr+" -1 ");
					}
						 out.write(" -2 ");
						 out.newLine();
			      }	
			  
		      //Close the output stream
		      out.close();    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void createFeatureFileRi(String fileName, ArrayList<discrimItemsets>  arrayList) {
		
		FileWriter fstream;
		try {
			  fstream = new FileWriter(fileName);
		
		      BufferedWriter out = new BufferedWriter(fstream);
		      out.write("notOutlier_Alive,notOutlier_Death,notOutlier_survival_rate,outlier_Alive,outlier_Death,outlier_survival_rate,seq_survival_rate,support,total_patients,fisherScore,seq");
			  out.newLine();
		      for(discrimItemsets row:arrayList){
		    	out.write(row.getDpstat().getLabel0_class1()+","); //notOutlier_alive
		    	out.write(row.getDpstat().getLabel0_class0()+","); //notOutlier_Death		  	
		    	out.write(ImedCal.double_format(row.getDpstat().not_outlier_survival_rate(), 5)+","); //notOutlier_survival_rate
		    	
		    	out.write(row.getDpstat().getLabel1_class1()+","); 
		    	out.write(row.getDpstat().getLabel1_class0()+","); 		    	
		    	out.write(ImedCal.double_format(row.getDpstat().outlier_survival_rate(), 5)+","); //Outlier_survival_rate
		    	out.write(ImedCal.double_format(row.getDpstat().seq_survival_rate(), 5)+","); //Seq_survival_rate
		    	
		    	out.write(row.getSupport()+",");
		    	out.write(row.getDatapoints().size()+",");
		    	out.write(row.getGain()+",");
					 for(TreeSet<Integer> ri:row.getItemsets().getItemsets()){
						 String setstr = ri.toString();
						 setstr = setstr.substring(setstr.indexOf("[")+1,setstr.indexOf("]")).replace(",","");
						 
						 out.write(setstr+" -1 ");
					 }
					 out.write(" -2 ");
					 out.newLine();
				}
		      //Close the output stream
		      out.close();    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void createFeatureFilePredictP(String fileName, ArrayList<discrimItemsets>  arrayList) {
		
		FileWriter fstream;
		try {
			  fstream = new FileWriter(fileName);
		
		      BufferedWriter out = new BufferedWriter(fstream);
		      out.write("Outlier_Predict_Alive,outlier_Death,outlier_Predict_Death,Outlier_Alive,support,total_patients,fisherScore,seq");
			  out.newLine();
		      for(discrimItemsets row:arrayList){
		    	
			    out.write(row.getDpstat().getLabel1_class0()+","); 		
			    out.write(row.getDpstat().getLabel0_class0()+","); 
		    	out.write(row.getDpstat().getLabel0_class1()+","); 
		    	out.write(row.getDpstat().getLabel1_class1()+","); 	  	
		    	
		    	    	
		    	//out.write(ImedCal.double_format(row.getDpstat().seq_survival_rate(), 5)+","); //Seq_survival_rate
		    	
		    	out.write(row.getSupport()+",");
		    	out.write(row.getDatapoints().size()+",");
		    	out.write(row.getGain()+",");
					 for(TreeSet<Integer> ri:row.getItemsets().getItemsets()){
						 String setstr = ri.toString();
						 setstr = setstr.substring(setstr.indexOf("[")+1,setstr.indexOf("]")).replace(",","");
						 
						 out.write(setstr+" -1 ");
					 }
					 out.write(" -2 ");
					 out.newLine();
				}
		      //Close the output stream
		      out.close();    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/********************
	 *  Read			*	
	 * 					*
	 ********************/
	public void parserDoc(String fileName) {
		// TODO Auto-generated method stub
		
	}
	public void parserDoc(String fileName,SequenceDataSetConfig cdsc) {
		File inputXml = new File(fileName);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element L0 = document.getRootElement();
			for (Iterator L1 = L0.elementIterator(); L1.hasNext();) {
				Element L1_emt = (Element) L1.next();
				if(L1_emt.getName().equals("SequenceDataSetConfig")){
					String enable = L1_emt.elementText("enable");
					if(enable!=null && enable.trim().equalsIgnoreCase("1"))
						cdsc.setEnable(true);
					cdsc.setInputFolder(L1_emt.elementText("inputFolder"));
					cdsc.setOutputFolder(L1_emt.elementText("outputFolder"));
					
				}
				if(L1_emt.getName().equals("VMSPConfig")){
					String enable = L1_emt.elementText("enable");
					if(enable!=null && enable.trim().equalsIgnoreCase("1"))
						cdsc.setVMSPenable(true);
					String thresholdStr = L1_emt.elementText("threshold");
					if(thresholdStr !=null && !thresholdStr.trim().equals("")){
						String threshold[]=thresholdStr.split(",");
						for(int i=0;i<threshold.length;i++){
							if(threshold[i]!=null && !threshold[i].trim().equals("")){
								cdsc.addVMSPthreshold(Double.parseDouble(threshold[i].trim()));
							}
						}
					}else{
						cdsc.addVMSPthreshold(0.0);
					}
					String maxLenStr = L1_emt.elementText("maxlength");
					if(maxLenStr !=null && !maxLenStr.trim().equals("")){
						String maxLen[]=maxLenStr.split(",");
						for(int i=0;i<maxLen.length;i++){
							if(maxLen[i]!=null && !maxLen[i].trim().equals("")){
								cdsc.addVMSPMaxLen(Integer.parseInt(maxLen[i].trim()));
							}
						}
					}
					cdsc.setVMSPinputFolder(L1_emt.elementText("inputFolder"));
					cdsc.setVMSPoutputFolder(L1_emt.elementText("outputFolder"));					
				}
				
				if(L1_emt.getName().equals("MMRFSConfig")){
					String enable = L1_emt.elementText("enable");
					if(enable!=null && enable.trim().equalsIgnoreCase("1"))
						cdsc.setMMRFSenable(true);
					
					cdsc.setMMRFSbasicItemsetsFolder(L1_emt.elementText("basicItemsetsFileName"));
					cdsc.setMMRFSdiscrimItemsetsFolder(L1_emt.elementText("discrimItemsetsFileName"));
					cdsc.setMMRFSoutlierSourceFolder(L1_emt.elementText("outlierSource"));
					cdsc.setMMRFSfeatureItemsetFolder(L1_emt.elementText("featureItemsetFileName"));
					cdsc.setMMRFSlabelBase(L1_emt.elementText("labelBase"));
					
					String thresholdStr = L1_emt.elementText("labelDefineThreshold");
					if(thresholdStr !=null && !thresholdStr.trim().equals("")){
						String threshold[]=thresholdStr.split(",");
						for(int i=0;i<threshold.length;i++){
							if(threshold[i]!=null && !threshold[i].trim().equals("")){
								cdsc.addMMRFSlabelDefineThreshold(Double.parseDouble(threshold[i].trim()));
							}
						}
					}else{
						cdsc.addMMRFSlabelDefineThreshold(3.0); //default
					}
					String maxLenStr = L1_emt.elementText("coverageRate");
					if(maxLenStr !=null && !maxLenStr.trim().equals("")){
						String maxLen[]=maxLenStr.split(",");
						for(int i=0;i<maxLen.length;i++){
							if(maxLen[i]!=null && !maxLen[i].trim().equals("")){
								cdsc.addMMRFScoverage(Double.parseDouble(maxLen[i].trim()));
							}
						}
					}else{
						cdsc.addMMRFScoverage(1.0);//default
					}
					
				}

				if(L1_emt.getName().equals("SurvivalSequenceDataSetConfig")){
					String enable = L1_emt.elementText("enable");
					if(enable!=null && enable.trim().equalsIgnoreCase("1"))
						cdsc.setSVIenable(true);
					
					cdsc.setSVIverticalSeqFile(L1_emt.elementText("verticalSeqFile"));
					cdsc.setSVIsurvivalFile(L1_emt.elementText("survivalFile"));
					cdsc.setSVIfilterFile(L1_emt.elementText("filterFile"));
					cdsc.setSVIfilterCriteria(L1_emt.elementText("filterCriteria").trim().toLowerCase());
					cdsc.setSVIoutputFileName(L1_emt.elementText("outputFileName"));
					cdsc.setSVIoutlierThreshold(Double.parseDouble(L1_emt.elementText("outlierThreshold").trim()));
					
				}
				if(L1_emt.getName().equals("SurvivalFSConfig")){
					String enable = L1_emt.elementText("enable");
					if(enable!=null && enable.trim().equalsIgnoreCase("1"))
						cdsc.setSVIFSenable(true);
					
					cdsc.setSVIFSsurvivalFile(L1_emt.elementText("survivalFile"));
					cdsc.setSVIFSseqFile(L1_emt.elementText("seqFile"));
					cdsc.setSVIFSseqptnFolder(L1_emt.elementText("seqptnFolder"));
					cdsc.setSVIFSseqCoxFolder(L1_emt.elementText("seqCoxFolder"));
					cdsc.setSVIFSseqFeatureFolder(L1_emt.elementText("seqFeatureFolder"));
					cdsc.setSVIFSseqPreSemanticFolder(L1_emt.elementText("seqPreSemanticFolder"));
				}
				
				if(L1_emt.getName().equals("ModelFreeScreenConfig")){
					String enable = L1_emt.elementText("enable");
					if(enable!=null && enable.trim().equalsIgnoreCase("1"))
						cdsc.setMFreeenable(true);
					
					cdsc.setMFreexStart(Integer.parseInt(L1_emt.elementText("xStart").trim()));
					
					String yTitleStr = L1_emt.elementText("yTitle");
					if(yTitleStr !=null && !yTitleStr.trim().equals("")){
						String yTitle[]=yTitleStr.split(",");
						for(int i=0;i<yTitle.length;i++){
							cdsc.getMFreeyTitle().add(yTitle[i].trim());
						}
					}

					cdsc.setMFreecoxIter(Integer.parseInt(L1_emt.elementText("coxIter").trim()));

					cdsc.setMFreeStep(Integer.parseInt(L1_emt.elementText("step").trim()));
					String featurestart = L1_emt.elementText("featureStart").trim();
					if(featurestart !=null && !featurestart.equals("")){
						cdsc.setMFreefeatureStart(Integer.parseInt(featurestart));
					}else{

						cdsc.setMFreefeatureStart(1);
					}
					cdsc.setMFreeStep(Integer.parseInt(L1_emt.elementText("step").trim()));
					String featurestop = L1_emt.elementText("featureStop").trim();
					if(featurestart !=null && !featurestart.equals("")){
						cdsc.setMFreefeatureStop(Integer.parseInt(featurestop));
					}else{
						cdsc.setMFreefeatureStop(1000);

					}
					String sctypeStr = L1_emt.elementText("screenType").trim().toLowerCase();
					if(sctypeStr!=null && sctypeStr.equals(MFStype.General.name().toLowerCase())){
						cdsc.setMFreescreenType(MFStype.General);
					}else if (sctypeStr!=null && sctypeStr.equals(MFStype.Survival.name().toLowerCase())){
						cdsc.setMFreescreenType(MFStype.Survival);
					}else if (sctypeStr!=null && sctypeStr.equals(MFStype.SurvivalStd.name().toLowerCase())){
						cdsc.setMFreescreenType(MFStype.SurvivalStd);
					}else {
						cdsc.setMFreescreenType(MFStype.General);
					}
					
					cdsc.setMFreeaucFolder(L1_emt.elementText("aucFolder").trim());
					
					String featureScoreStr = L1_emt.elementText("featureScore");
					if(featureScoreStr !=null && !featureScoreStr.trim().equals("")){
						String featureScore[]=featureScoreStr.split(",");
						for(int i=0;i<featureScore.length;i++){
							cdsc.addMFreefeatureScore(featureScore[i].trim());
						}
					}
					
					
				}


			}
			
		} catch (DocumentException e) {
			System.out.println(e.getMessage());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void parserConfigDoc(String fileName,MMRFSConfig cdsc) {
		File inputXml = new File(fileName);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element L0 = document.getRootElement();
			for (Iterator L1 = L0.elementIterator(); L1.hasNext();) {
				Element L1_emt = (Element) L1.next();
				if(L1_emt.getName().equals("MMRFSConfig")){
					
					cdsc.setBasicItemsetsFileName(L1_emt.element("basicItemsetsFileName").getText());
					cdsc.setDiscrimItemsetsFileName(L1_emt.element("discrimItemsetsFileName").getText());
					cdsc.setLabelDefineThreshold(Double.parseDouble(L1_emt.element("labelDefineThreshold").getText().trim()));
					cdsc.setCoverageRate(Double.parseDouble(L1_emt.element("coverageRate").getText().trim()));
					cdsc.setOutlierSource(L1_emt.element("outlierSource").getText().trim());
//					cdsc.setOutlierThreshold(Double.parseDouble(L1_emt.element("outlierThreshold").getText().trim()));
					cdsc.setFeatureItemsetFileName(L1_emt.element("featureItemsetFileName").getText().trim());
				}else{
					
				}
			}
			
		} catch (DocumentException e) {
			System.out.println(e.getMessage());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	public void parserDiscrimDoc(String FileName, ArrayList<discrimItemsets> discrimSeqList) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(FileName);
			Scanner scanner = new Scanner(fis);
			
			while (scanner.hasNextLine()) {
				
				String line =scanner.nextLine();
				Integer support = Integer.parseInt(line.substring((line.indexOf(":")+1), line.length()).trim());
				if(line.contains("#"))line = line.substring(0, line.indexOf("#"));
				else line = line.substring(0, line.indexOf("SUP"));
				basicItemsets<Integer> itemsets = new  basicItemsets<Integer>();
				genItemsets(line,itemsets);
				
				if(itemsets.getItemsets().size()>0){
					discrimItemsets oneSeq = new discrimItemsets();
					oneSeq.setItemsets(itemsets);
					oneSeq.setSupport(support);
					discrimSeqList.add(oneSeq);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void genItemsets(String line, basicItemsets<Integer> oneSeq){
		
		 oneSeq.setItemsets(new ArrayList<TreeSet<Integer>>());
		String[] itemsets = line.split("-1");
		for(String itemset:itemsets){
			if(itemset!=null && itemset.trim()!=null && !itemset.trim().equals("")){
				String[] items=itemset.split(" ");
				TreeSet<Integer>set = new TreeSet<Integer>();
				for(String item:items){
					if(item !=null && item.trim()!=null && !item.trim().equals("")){
						
						set.add(Integer.parseInt(item.trim()));
					}
				}
			
				oneSeq.setItemset(set);
			}
		}
		
	}
	public void parserLabelDoc(String FileName, ArrayList<labelItemsets> SeqList, HashMap<Long, Integer> labelList) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(FileName);
			Scanner scanner = new Scanner(fis);
			
			while (scanner.hasNextLine()) {
				
				String line =scanner.nextLine();
				String[] id_itemsets = line.split(",");
				Long id = Long.parseLong(id_itemsets[0].trim());
				
				basicItemsets<Integer> itemsets = new  basicItemsets<Integer>(id);
				genItemsets(id_itemsets[1],itemsets);
				if(labelList.containsKey(id)){
					labelItemsets labelItems = new labelItemsets();
					labelItems.setItemsets(itemsets);
					labelItems.setLabel(labelList.get(id));
					SeqList.add(labelItems);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	public void parserPreseqDoc(String FileName, ArrayList<basicItemsets> SeqList ) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(FileName);
			Scanner scanner = new Scanner(fis);
			
			while (scanner.hasNextLine()) {
				
				String line =scanner.nextLine();
				String[] id_itemsets = line.split(",");
				Long id = Long.parseLong(id_itemsets[0].trim());
				
				basicItemsets<Integer> itemsets = new  basicItemsets<Integer>(id);
				genItemsets(id_itemsets[1],itemsets);

				SeqList.add(itemsets);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}

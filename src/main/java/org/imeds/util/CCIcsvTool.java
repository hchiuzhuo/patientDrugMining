package org.imeds.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.imeds.data.SparkLRDataSetWorker.DataPoint;
import org.imeds.data.SurvivalTime;
import org.imeds.data.common.CCIcode;
import org.imeds.data.common.seqItemPair;
import org.imeds.db.ImedDB;
import org.imeds.feature.screening.Tuple;
import org.imeds.feature.screening.feature;
import org.imeds.feature.screening.measureScore;
import org.imeds.feature.selection.basicItemsets;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Itemset;

public class CCIcsvTool implements DocumentTool{ 
	private static Double infinity=1000000.0;
	public CCIcsvTool() {
		// TODO Auto-generated constructor stub
	}
	public void ComorbidDataSetCreateDoc(String fileName, ArrayList<String> arrayList, HashMap<Long, ArrayList<Double>> features, boolean append, boolean withTitle) {
	
		//CSV Write Example using CSVPrinter
//		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
		try {
			
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName, append), format);

	        Iterator<Entry<Long, ArrayList<Double>>> iter =features.entrySet().iterator();
	        
	        if(withTitle)printer.printRecord(arrayList);
	        
			while (iter.hasNext()) { 
				Entry<Long, ArrayList<Double>> entry = iter.next(); 
				 ArrayList<Double> feature = entry.getValue();
				 printer.print(entry.getKey());
				 feature.remove(0);
				 
				 printer.printRecord(feature);
			}
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
public static void SurvivalDataSetCreateDoc(String fileName, ArrayList<String> arrayList, HashMap<Long, SurvivalTime> features) {
		
		//CSV Write Example using CSVPrinter
//		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
		try {
			
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), format);

	        Iterator<Entry<Long, SurvivalTime>> iter =features.entrySet().iterator();
	        
	        printer.printRecord(arrayList);
	        
			while (iter.hasNext()) { 
				Entry<Long, SurvivalTime> entry = iter.next(); 
				 SurvivalTime feature = entry.getValue();
				// if(feature.getCensored_date().after(feature.getObs_start_date())){ 
					 printer.print(entry.getKey());
					 printer.print(ImedDateFormat.format(feature.getObs_start_date()));
					 printer.print(ImedDateFormat.format(feature.getObs_end_date()));
					 printer.print(ImedDateFormat.format(feature.getDeath_date()));
					 printer.print(ImedDateFormat.format(feature.getDis_index_date()));
//					 printer.print(feature.getSurvival_length());
//					 printer.print(feature.getSurvival_start());
//					 printer.print(feature.getSurvival_end());
//					 printer.print(feature.isFailed());
					 printer.println();					 
				 //}
			}
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
	public static void SurvivalCensoredDataSetCreateDoc(String fileName, ArrayList<String> arrayList, ArrayList<SurvivalTime> features) {
				
		//CSV Write Example using CSVPrinter
//		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
		try {
			
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), format);

//	        Iterator<Entry<Long, SurvivalTime>> iter =features.entrySet().iterator();
	        
	        printer.printRecord(arrayList);
	        for(SurvivalTime feature:features){
//			while (iter.hasNext()) { 
//				Entry<Long, SurvivalTime> entry = iter.next(); 
//				 SurvivalTime feature = entry.getValue();
				 if(feature.getCensored_date().after(feature.getDis_index_date()) && feature.getSurvival_length()>0){ 
//					 printer.print(entry.getKey());
					 printer.print(feature.getId());
					 printer.print(ImedDateFormat.format(feature.getObs_start_date()));
					 printer.print(ImedDateFormat.format(feature.getObs_end_date()));
					 printer.print(ImedDateFormat.format(feature.getDeath_date()));
					 printer.print(ImedDateFormat.format(feature.getDis_index_date()));
					 printer.print(feature.getSurvival_length());
					 printer.print(feature.getSurvival_start());
					 printer.print(feature.getSurvival_end());
					 printer.print(feature.isFailed());
					 printer.println();					 
				 }
			}
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
	public static void SurvivalDataSetCreateDoc(String fileName, ArrayList<String> arrayList, ArrayList<SurvivalTime> features) {
		
		//CSV Write Example using CSVPrinter
//		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
		try {
			
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), format);
	        printer.printRecord(arrayList);
	        for(SurvivalTime feature:features){
					 printer.print(feature.getId());
					 printer.print(feature.getSurvival_length());
					 printer.print(feature.isFailed());
					 printer.print(ImedDateFormat.format(feature.getDis_index_date()));
					 printer.println();					 
			}
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}

	public static void SurvivalDataSetCreateDoc(String fileName,HashMap<Long,SurvivalTime> DataPoints,Map<Integer, Long> ridx_map) {
		
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
		try {
			
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), format);

	        Iterator<Entry<Long, SurvivalTime>> iter =DataPoints.entrySet().iterator();
	        
	        //printer.printRecord(arrayList);
	        printer.print("ID");
	        printer.print("Period");
	        printer.print("Failed");
	       
	        boolean lock=false;
	        int rid=0;
			while (iter.hasNext()) { 
				Entry<Long, SurvivalTime> entry = iter.next(); 
				 SurvivalTime feature = entry.getValue();
				 if(!lock){
					 for(int k=0;k<feature.getFeatures().size();k++){
				        	printer.print("D"+k);
				        }
					 printer.println();
					 lock = true;
				 }
					 printer.print(entry.getKey());
					
					 printer.print(feature.getSurvival_length());
					
					 if(feature.isFailed())printer.print(1);
					 else printer.print(0);
					 
					 if(ridx_map!=null)ridx_map.put(rid, entry.getKey());
					 
					 for(Double dt:feature.getFeatures()){
						 printer.print(dt);
					 }
					 printer.println();					 
					 rid++;
			}
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
	
	public static void SurvivalDataSetCreateDoc(String fileName,ArrayList<SurvivalTime> DataPoints) {
		
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
		try {
			
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), format);

//	        Iterator<Entry<Long, SurvivalTime>> iter =DataPoints.entrySet().iterator();
	        
	        //printer.printRecord(arrayList);
	        printer.print("ID");
	        printer.print("Period");
	        printer.print("Failed");
	       
	        boolean lock=false;
	        int rid=0;
//			while (iter.hasNext()) { 
			for(SurvivalTime feature: DataPoints){	
//				Entry<Long, SurvivalTime> entry = iter.next(); 
//				 SurvivalTime feature = entry.getValue();
				 if(!lock){
					 for(int k=0;k<feature.getFeatures().size();k++){
				        	printer.print("D"+k);
				        }
					 if(feature.getAddiFeatures().size()>0){
						 for(int k=0;k<feature.getAddiFeatures().size();k++){
								printer.print("S"+k);
						 }
					 }
					
					 printer.println();
					 lock = true;
				 }
					 printer.print(feature.getId());
					
					 printer.print(feature.getSurvival_length());
					
					 if(feature.isFailed())printer.print(1);
					 else printer.print(0);
					
					 for(Double dt:feature.getFeatures()){
						 printer.print(dt);
					 }
					 if(feature.getAddiFeatures().size()>0){
						 for(Double dt:feature.getAddiFeatures()){
							 printer.print(dt);
						 }
					 }
					
					 printer.println();					 
					 rid++;
			}
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
	
	public static void SurvivalDataSetCreateDoc(String fileName,ArrayList<SurvivalTime> DataPoints, int bitsize) {
		
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
		try {
			
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), format);

//	        Iterator<Entry<Long, SurvivalTime>> iter =DataPoints.entrySet().iterator();
	        
	        //printer.printRecord(arrayList);
	        printer.print("ID");
	        printer.print("Period");
	        printer.print("Failed");
	       
	        boolean lock=false;
	        int rid=0;
//			while (iter.hasNext()) { 
			for(SurvivalTime feature: DataPoints){	
//				Entry<Long, SurvivalTime> entry = iter.next(); 
//				 SurvivalTime feature = entry.getValue();
				 if(!lock){
					 for(int k=0;k<feature.getFeatures().size();k++){
				        	printer.print("D"+k);
				        }
//					 if(feature.getAddiFeatures().size()>0){
					 if(bitsize>0){	 
//						 for(int k=0;k<feature.getAddiFeatures().size();k++){
							 for(int k=0;k<bitsize;k++){	 
								printer.print("S"+k);
						 }
					 }
					
					 printer.println();
					 lock = true;
				 }
					 printer.print(feature.getId());
					
					 printer.print(feature.getSurvival_length());
					
					 if(feature.isFailed())printer.print(1);
					 else printer.print(0);
					
					 for(Double dt:feature.getFeatures()){
						 printer.print(dt);
					 }
//					 if(feature.getAddiFeatures().size()>0){
//						 for(Double dt:feature.getAddiFeatures()){
//							 printer.print(dt);
//						 }
//					 }
					if(bitsize>0){
						for(int i=0;i<bitsize;i++){
							if(feature.getBitFeatures().get(i))printer.print(1);
							else printer.print(0);
						}
					}
					 printer.println();					 
					 rid++;
			}
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
	public static void OutlierCreateDoc(String fileName,  Map<Long, ArrayList<Double>> list) {
		
		//CSV Write Example using CSVPrinter
//		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
       
		try {
			
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), format);
			printer.print("Id");
	        printer.print("TrainP");
	        printer.print("PredictP");
	        printer.print("Ri");
	        printer.println();
	        
	        Iterator<Entry<Long, ArrayList<Double>>> iter =list.entrySet().iterator();
		        
			while (iter.hasNext()) { 
				 Entry<Long, ArrayList<Double>> entry = iter.next(); 
				 printer.print(entry.getKey());
					
				 printer.print(entry.getValue().get(0));
				 printer.print(entry.getValue().get(1));
				 printer.print(entry.getValue().get(2));
				 printer.println();
			}
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
	
	public static void preSeqCreateDoc(String fileName, HashMap<Long, ArrayList<seqItemPair>> list) {
		
		//CSV Write Example using CSVPrinter
//		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
       
		try {
			
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), format);
			printer.print("person_id");
	        printer.print("date_t");
	        printer.print("concept_id");
	        printer.println();
	        
	        Iterator<Entry<Long, ArrayList<seqItemPair>>> iter =list.entrySet().iterator();
		        
			while (iter.hasNext()) { 
				 Entry<Long, ArrayList<seqItemPair>> entry = iter.next();
				 for(seqItemPair pair: entry.getValue()){
					 printer.print(entry.getKey());
					 printer.print(pair.getTimestamp());
					 printer.print(pair.getItemId());
					 printer.println();
				 }
			}
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
	
	public static void SequenceDataSetCreateDoc(String fileName, HashMap<Long, ArrayList<String>> mapList) {
		
		SequenceDataSetCreateDoc(fileName,new ArrayList<ArrayList<String>>(mapList.values()));
		FileWriter fstream;
		try {
			 fstream = new FileWriter(fileName.substring(0,fileName.indexOf("."))+"_withId.csv");
		
		      BufferedWriter out = new BufferedWriter(fstream);
		      Iterator<Entry<Long, ArrayList<String>>> itemIter = mapList.entrySet().iterator();
		      while(itemIter.hasNext()){
		    	  Entry<Long, ArrayList<String>> item = itemIter.next();	
		    	  out.write(item.getKey()+",");
		    	  for(String row:item.getValue())out.write(row);
		    	  out.newLine();
		      }
		      //Close the output stream
		      out.close();    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void SequenceDataSetCreateDoc(String fileName, ArrayList<ArrayList<String>> arrayList) {
	
		 FileWriter fstream;
		try {
				fstream = new FileWriter(fileName);
		
		      BufferedWriter out = new BufferedWriter(fstream);
		      for(ArrayList<String> row:arrayList){
					 for(String ri:row)out.write(ri);
					 out.newLine();
				}
		      //Close the output stream
		      out.close();    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
public static void AucCreateDoc(String fileName, ArrayList<measureScore> mscore) {
		
		//CSV Write Example using CSVPrinter
//		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
       
		try {
			
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), format);
			printer.print("Count");
	        printer.print("AUC");
	        printer.print("Comment");
	        printer.print("TrainTime");
	        printer.print("TestTime");
	        printer.println();
	        for(measureScore ms:mscore){
	        	printer.print(ms.getId());
	        	printer.print(ms.getScore());
	        	printer.print(ms.getComment());
	        	printer.print(ms.getTrainTimeStr());
	        	printer.print(ms.getTestTimeStr());			       
	        	printer.println();
	        }
	    
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
	/***********************
	 * Csv Read
	 * 
	 ************************/
	public void DeyoCCIparserDoc(String fileName, HashMap<String, CCIcode>  codeList) {
		
		 //Create the CSVFormat object
		
        CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
         
        //initialize the CSVParser object
        CSVParser parser;
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        	CCIcode ccicode = new CCIcode();
	        	ccicode.setID(Integer.parseInt(record.get("Id").trim()));
	            ccicode.setName(record.get("Name").trim());
	        	ccicode.setWeight(Integer.parseInt(record.get("Weight").trim()));
	        	ccicode.setIcdList(record.get("ICD-9").trim());
	        	codeList.put(record.get("Name"), ccicode);
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void LRPredictResultParserDoc(String fileName,List<DataPoint> DataPointList) {
		
		 //Create the CSVFormat object
		
        CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
         
        //initialize the CSVParser object
        CSVParser parser;
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        	Long Id = Long.parseLong(record.get("Id").trim());
	        	Double label = Double.parseDouble(record.get("Label"));
	        	Double predict = Double.parseDouble(record.get("Prediction"));
	        	
	        	String[] vector = record.get("Feature").split(",");
	        	double[] feature = new double[vector.length];
	        	for(int i=0;i<vector.length;i++) feature[i]=Double.parseDouble(vector[i]);
	        	
	        	LabeledPoint lpt = new LabeledPoint(label, Vectors.dense(feature));
	        	
	        	DataPointList.add(new DataPoint(Id,lpt,predict));
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void preSequenceDataParserDoc(String fileName,ArrayList<ArrayList<String>> DataPointList) {

		 //Create the CSVFormat object
		
        CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
         
        //initialize the CSVParser object
        CSVParser parser;
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        	
	        	ArrayList<String> row = new ArrayList<String>();
	        	row.add(record.get("person_id").trim());
	        	row.add(record.get("date_t").trim());
	        	row.add(record.get("concept_id").trim());
	       
	        	DataPointList.add(row);
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static HashMap<Long,Integer> OutlierParserDoc(String fileName) {
		return OutlierParserDoc(fileName, 0.0);
	}
	
	
	//discriminative set is normal and outlier
	public static void OutlierClassParserDoc(String fileName, Double threshold, HashMap<Long, Integer> labelList, HashMap<Long, Double> classList) {
		
		 CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
         
	        //initialize the CSVParser object
	        CSVParser parser;
			try {
				parser = new CSVParser(new FileReader(fileName), format);
				
		        for(CSVRecord record : parser){
		        	Long id = Long.parseLong(record.get("Id"));
		        	//FIXME: OUTLIERS IN THIS SET MAY NOT HAVE DRUG SEQ PTN 
		        	
		        	if(Math.abs(Double.parseDouble(record.get("Ri")))>=threshold)labelList.put(id, LabelType.yesOutlier);
		        	else labelList.put(id, LabelType.notOutlier);
		        	
		        	
		        	classList.put(id, Double.parseDouble(record.get("TrainP")));
		        }
		        //close the parser
		        parser.close();
		     //   System.out.println(codeList);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
	//discriminate set is better than expected and worse than expected
	public static void PredictClassParserDoc(String fileName, Double threshold, HashMap<Long, Integer> labelList, HashMap<Long, Double> classList) {
		
		 CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
        
	        //initialize the CSVParser object
	        CSVParser parser;
			try {
				parser = new CSVParser(new FileReader(fileName), format);
				
		        for(CSVRecord record : parser){
		        	Long id = Long.parseLong(record.get("Id"));
		        	//FIXME: OUTLIERS IN THIS SET MAY NOT HAVE DRUG SEQ PTN 
		        	//if( Double.parseDouble(record.get("Ri"))>=threshold){
		        		if( Double.parseDouble(record.get("PredictP"))>=threshold)labelList.put(id, LabelType.PredictAlive);
		        		else labelList.put(id, LabelType.PredictDeath);
		        	
		        		classList.put(id, Double.parseDouble(record.get("TrainP")));
		        	//}
		        }
		        //close the parser
		        parser.close();
		     //   System.out.println(codeList);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
	
	public static HashMap<Long, Integer> OutlierParserDoc(String fileName, Double threshold) {
		HashMap<Long,Integer> labelItemSet = new HashMap<Long,Integer>();
		 CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
         
	        //initialize the CSVParser object
	        CSVParser parser;
			try {
				parser = new CSVParser(new FileReader(fileName), format);
				
		        for(CSVRecord record : parser){
		        	Long id = Long.parseLong(record.get("Id"));
		        	//FIXME: OUTLIERS IN THIS SET MAY NOT HAVE DRUG SEQ PTN 
		        	if( Double.parseDouble(record.get("Ri"))>=threshold)labelItemSet.put(id, 1); //1 is outlier
		        	else labelItemSet.put(id, 0);
		        }
		        //close the parser
		        parser.close();
		     //   System.out.println(codeList);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return labelItemSet;
   
	}
	public static void OutlierParserDoc(String fileName,Integer flush, Integer fileId, Logger logger) throws Exception {
		Map<Long, ArrayList<Double>> list = new HashMap<Long,ArrayList<Double>>();
		//CSV Write Example using CSVPrinter
		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
	
		CSVParser parser;
		//data\IMEDS\CgstHfComorbidDS\outlier\trainDS_5000_10_prol.csv
		try {
			
			parser = new CSVParser(new FileReader(fileName), format);
			int counter = 0;
	        for(CSVRecord record : parser){
	        	ArrayList<Double> arr = new ArrayList<Double>();
	        	arr.add(Double.parseDouble(record.get("TrainP")));
	        	arr.add(Double.parseDouble(record.get("PredictP")));
	        	if(record.get("Ri").trim().equalsIgnoreCase("Infinity")){
	        		arr.add(infinity);
	        	}else{
	        		arr.add(Double.parseDouble(record.get("Ri")));
	        	}
	        	list.put(Long.parseLong(record.get("Id")), arr);
	        	counter++;
	        	if((counter%flush)==0){
	        		logger.info(fileName+" finish "+counter);
	        		ImedDB.writeOutlier(list, fileId);
	        		list =  new HashMap<Long,ArrayList<Double>>();
	        		
	        	}
	        	
	        }
	        if(list.size()>0){
	        	ImedDB.writeOutlier(list, fileId);
	        }
	        //close the parser
	        parser.close();	        
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
	public static void SurvivalDataSetParserDoc(String fileName,HashMap<Long,SurvivalTime> DataPoints) throws Exception{
		SurvivalDataSetParserDoc(fileName,DataPoints,null);
	}
	public static void SurvivalDataSetParserDoc(String fileName,ArrayList<SurvivalTime> DataPoints) throws Exception{
		SurvivalDataSetParserDoc(fileName,null,DataPoints);
	}
	public static void SurvivalDataSetParserDoc(String fileName,HashMap<Long,SurvivalTime> DataPoints,ArrayList<SurvivalTime> ArrDataPoints) throws Exception {
		
		 //Create the CSVFormat object
		
       CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
        
       //initialize the CSVParser object
       CSVParser parser;
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        		        	
	        	Long Id = Long.parseLong(record.get("ID").trim());
	        	Integer survival_length = Integer.parseInt(record.get("Period"));
	        	Boolean failed = Boolean.parseBoolean(record.get("Failed"));
	        	Date dis_idx_date = ImedDateFormat.parse(record.get("dis_idx_date"));
	        	SurvivalTime st= new SurvivalTime(Id, survival_length, failed, dis_idx_date);
	        	if(DataPoints!=null)DataPoints.put(Id, st);
	        	if(ArrDataPoints!=null)ArrDataPoints.add(st);
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void R_SurvivalDataSetParserDoc(String fileName,HashMap<Long,SurvivalTime> DataPoints,ArrayList<SurvivalTime> ArrDataPoints) throws Exception {
		
		 //Create the CSVFormat object
		
      CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
       
      //initialize the CSVParser object
      CSVParser parser;
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        		        	
	        	Long Id = Long.parseLong(record.get("ID").trim());
	        	Integer survival_length = Integer.parseInt(record.get("Period"));
	        	Integer failedval= Integer.parseInt(record.get("Failed"));
	        	
	        	Boolean failed;// = Boolean.parseBoolean(record.get("Failed"));
	        	if(failedval==0)failed=false;
	        	else failed=true;
	        		
	        	ArrayList<Double> addiFeatures = new ArrayList<Double>();
	        	//FIXED ME. THIS IS HARD CODED. TRY TO MAKE IT FLEXIBLE LATER
	        	for(int i=0;i<18;i++){
	        		addiFeatures.add(Double.parseDouble(record.get("D"+i)));
	        	}
	        	SurvivalTime st= new SurvivalTime(Id, survival_length, failed);
	        	st.setFeatures(addiFeatures);
	        	
	        	if(DataPoints!=null)DataPoints.put(Id, st);
	        	if(ArrDataPoints!=null)ArrDataPoints.add(st);
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void SurvivalDataSetFeatureParserDoc(String fileName,HashMap<Long,SurvivalTime> DataPoints) {
		
		 //Create the CSVFormat object
		
       CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
        
       //initialize the CSVParser object
       CSVParser parser;
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        	//ID,Gender,Age,Location,
	        	Long Id = Long.parseLong(record.get("ID").trim());
	        	if(DataPoints.containsKey(Id)){
	        		String str=record.toString();
	        		str = str.replace("[", "");
	        		str = str.replace("]", "");
	        		str = str.substring(str.indexOf(",")+1, str.length());
	        		
	        		DataPoints.get(Id).setFeatures(str);
	        	}
	        	
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	//public static void SurvivalTrainDataSetParserDoc(String fileName,HashMap<Long,SurvivalTime> DataPoints) {
	public static void SurvivalTrainDataSetParserDoc(String fileName,ArrayList<SurvivalTime> DataPoints) {
				
		 //Create the CSVFormat object
		
      CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
       
      //initialize the CSVParser object
      CSVParser parser;
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        	//ID	obs_startDate	obs_endDate	death_date	Period	PeriodStart	PeriodEnd	Failed
	        	Long Id = Long.parseLong(record.get("ID").trim());
	        	Date obs_startDate = ImedDateFormat.parse(record.get("obs_startDate"));
	        	Date obs_endDate = ImedDateFormat.parse(record.get("obs_endDate"));
	        	
	        	Date death_Date = ImedDateFormat.parse(record.get("death_date"));
	        	Date dis_idx_date = ImedDateFormat.parse(record.get("dis_idx_date"));
	        	
	        	
	        	SurvivalTime st= new SurvivalTime(Id, obs_startDate, obs_endDate,death_Date,dis_idx_date);
	        	DataPoints.add(st);
//	        	DataPoints.put(Id, st);
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void SurvivalPatientParserDoc(String fileName,HashMap<Long, ArrayList<seqItemPair>> patients) {
		
		 //Create the CSVFormat object
		
      CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
       
      //initialize the CSVParser object
      CSVParser parser;
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        	//ID,Gender,Age,Location,
	        	Long Id = Long.parseLong(record.get("ID").trim());
	        	patients.put(Id, new ArrayList<seqItemPair>());
	        	
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void RegressionDatasetParserDoc(String fileName,ArrayList<Tuple> DataPointList,String ID, ArrayList<String> yTitle,Integer xStart) {
		
		 //Create the CSVFormat object
		
       CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
        
       //initialize the CSVParser object
       CSVParser parser;
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			Long Id;
	        for(CSVRecord record : parser){
	        	Tuple tuple =new Tuple();
	        	tuple.setBitFeatures(new BitSet(record.size()-xStart));
	        	tuple.setFeatureSize(record.size()-xStart);
	        	if(ID!=null) tuple.setId(Long.parseLong(record.get(ID).trim()));
	        	
	        	for(String yItem:yTitle)tuple.addyList(Double.parseDouble(record.get(yItem)));
	        	
	        	for(int i=xStart;i<record.size();i++){
//	        		tuple.addxList(Double.parseDouble(record.get(i)));
	        		if(Integer.parseInt(record.get(i))==1){
	        			tuple.setBitFeatures((i-xStart));
	        		}
	        	}
	        	
	        	DataPointList.add(tuple);
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void dsmcParserDoc(String fileName, String featureId, String featureScoreType, String featureDscp, ArrayList<feature> list){
		
		 //Create the CSVFormat object
		
     CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
      
     //initialize the CSVParser object
     CSVParser parser;
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        	//support	modelFreeScore	seqId
	        	
	        	Long Id = Long.parseLong(record.get(featureId).trim());
	        	
	        	Double score = Double.parseDouble(record.get(featureScoreType.trim()).trim());
	        	
	        	feature ft=new feature(Id,score);
	        	if(featureDscp!=null && !featureDscp.trim().equals("")){
	        		ft.setDescription(featureDscp);
	        	}
	        	list.add(ft);
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void semanticSeqParserDoc(String fileName,ArrayList<String> strList) throws Exception {
		
		 //Create the CSVFormat object
		
      CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
       
      //initialize the CSVParser object
      CSVParser parser;
     
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        	StringBuffer sbf= new StringBuffer();
	        	sbf.append(record.get(0).trim()+",");
	        	sbf.append(record.get(1).trim()+",");
	        	sbf.append("'"+record.get(2)+"',");
	        	sbf.append("'"+record.get(3).replace("'", "\\'")+"'");
	        	strList.add(sbf.toString());
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void semanticSeqParserDoc(String fileName,HashMap<Integer, basicItemsets> seqMap, HashMap<Integer, String> cptmap) throws Exception {
		
		 //Create the CSVFormat object
		
     CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
      
     //initialize the CSVParser object
     CSVParser parser;
    
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        	Integer seqid=Integer.parseInt(record.get(1).trim());
	        	String itemsetId=record.get(2).trim();
	        		   itemsetId=itemsetId.substring(0,itemsetId.indexOf("-2"));
	        	String itemsetStr=record.get(3).trim();
	        		   itemsetStr=itemsetStr.substring(0,itemsetStr.indexOf("-2"));
	            basicItemsets<Integer> itemsets = new  basicItemsets<Integer>();
//	            System.out.println("process "+seqid);
	            genItemsets(itemsetId,itemsets);
	            genCptMap(itemsetId, itemsetStr, cptmap);
	            seqMap.put(seqid, itemsets);
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	public static void genItemsets(String line, basicItemsets<Integer> oneSeq){
		
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
	public static void genCptMap(String lineId, String lineStr, HashMap<Integer, String> cptmap){
		
		String[] itemsetsId = lineId.split("-1");
		String[] itemsetsStr = lineStr.split("-1");
		for(int i=0;i<itemsetsId.length;i++){
			String itemsetId=itemsetsId[i];
			String itemsetStr=itemsetsStr[i];
			if(itemsetId!=null && itemsetId.trim()!=null && !itemsetId.trim().equals("")){
				String[] itemsId=itemsetId.trim().split(" ");
				String[] itemsStr=itemsetStr.trim().split("' '");
				for(int j=0;j<itemsId.length;j++){
					if(itemsId[j]!=null && itemsId[j].trim()!=null && !itemsId[j].trim().equals("")){
						cptmap.put(Integer.parseInt(itemsId[j]), itemsStr[j].replace("'", ""));
					}
				}
			}
		}		
	}
	public static void contraIndiparserDoc(String fileName,HashMap<Integer, String> contraIndimap) throws Exception {
		
		 //Create the CSVFormat object
		
     CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
      
     //initialize the CSVParser object
     CSVParser parser;
    
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        	contraIndimap.put(Integer.parseInt(record.get(0)), record.get(1).trim());
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void RoutParserDoc(String fileName,ArrayList<String> strList) throws Exception {
		
		 //Create the CSVFormat object
		
     CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
      
     //initialize the CSVParser object
     CSVParser parser;
     try {
			parser = new CSVParser(new FileReader(fileName), format);
			
	        for(CSVRecord record : parser){
	        	if(!record.toString().contains("NA") && !record.toString().contains("na")){
		        	StringBuffer sbf= new StringBuffer();
		    		
		        	String stmp=record.get(0).trim();
		        	if(stmp.contains("S"))sbf.append(stmp.substring(1)+",");
		        	else sbf.append("null,");
		        	
		        	String str=record.toString();
	        		str = str.replace("[", "");
	        		str = str.replace("]", "");
		        	str = str.substring(str.indexOf(","));
	        		
		        	sbf.append("'"+stmp+"'"+str);
		        	
		        	strList.add(sbf.toString());
	        	}
	        }
	        //close the parser
	        parser.close();
	     //   System.out.println(codeList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void parserDoc(String fileName) {
		// TODO Auto-generated method stub
		
	}

	public void createDoc(String fileName) {
		// TODO Auto-generated method stub
		
	}
	
}

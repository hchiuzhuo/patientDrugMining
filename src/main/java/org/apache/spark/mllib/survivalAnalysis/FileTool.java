package org.apache.spark.mllib.survivalAnalysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.imeds.data.ComorbidDataSetConfig;
import org.imeds.data.SurvivalDataSetConfig;
import org.imeds.data.SurvivalTime;
import org.imeds.db.ImedDB;
import org.imeds.feature.selection.basicItemsets;
import org.imeds.util.ImedDateFormat;

public class FileTool {
	
	public void parserDoc(String fileName,CoxProcessConfig cdsc) throws Exception {
		
		File inputXml = new File(fileName);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element L0 = document.getRootElement();
			for (Iterator L1 = L0.elementIterator(); L1.hasNext();) {
				Element L1_emt = (Element) L1.next();
				if(L1_emt.getName().equals("ParamGrid")){
					cdsc.setAlgCfgList(L1_emt.elements("ParamMap"));
					
				}else if(L1_emt.getName().equals("DataSet")){
					cdsc.setDatasetPath(L1_emt.element("DatasetPath").getText().trim());
					cdsc.setSchemaPath(L1_emt.element("SchemaPath").getText().trim());
					cdsc.setX_varListPath(L1_emt.element("X_varListPath").getText().trim());
					cdsc.setCensored_var(L1_emt.element("Censored_var").getText().trim());
					cdsc.setY_var(L1_emt.element("Y_var").getText().trim());
					Scanner scanner = new Scanner( new FileInputStream(cdsc.getX_varListPath()));
					StringBuffer X_varList = new StringBuffer();
					while (scanner.hasNextLine()) {
						X_varList.append(scanner.nextLine());
					}
					cdsc.setX_varList(X_varList.toString());
					if(L1_emt.element("enableKfold").getText().trim()!=null && L1_emt.element("enableKfold").getText().trim().toLowerCase().equals("true")){
						cdsc.setEnableKfold(true);
					}
					if(L1_emt.element("Kfold").getText().trim()!=null)cdsc.setKfold(Integer.parseInt(L1_emt.element("Kfold").getText()));
					
				}else if(L1_emt.getName().equals("ResultSet")){
					if(L1_emt.element("enablePvalue").getText().trim()!=null && L1_emt.element("enablePvalue").getText().trim().toLowerCase().equals("true")){
						cdsc.setEnablePvalue(true);
					}
					if(L1_emt.element("enableCstats").getText().trim()!=null && L1_emt.element("enableCstats").getText().trim().toLowerCase().equals("true")){
						cdsc.setEnableCstats(true);
					}
					cdsc.setModelOutputPath(L1_emt.element("ModelOutputPath").getText().trim());
					cdsc.setStatsSummaryPath(L1_emt.element("StatsSummaryPath").getText().trim());
					cdsc.setPredictionResultPath(L1_emt.element("PredictionResultPath").getText().trim());
					
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
	public ArrayList<String> parserParseComma(String FileName) {
		System.out.println("FileName: "+FileName);
		ArrayList<String> result = new ArrayList<String>();
		FileInputStream fis;
		try {
			fis = new FileInputStream(FileName);
			Scanner scanner = new Scanner(fis);
			
			while (scanner.hasNextLine()) {
				
				String line =scanner.nextLine();
				for (String str: line.split(",")) {
				  str=str.trim().replace("\"", "");
				  
			      result.add(str);    	    	
			    }	  
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;		
	}

	
	public void writeCoxModel(String fileName, CoxModel model, Boolean printPval){
		
		//CSV Write Example using CSVPrinter
//		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
		try {
			
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), format);
	        printer.printRecord(model.header);
	        for(int i=0;i<model.getVar().size();i++){
	        	printer.print(model.getVar().get(i));
	        	printer.print(model.getCoef().get(i));
	        	printer.print(Math.exp(model.getCoef().get(i)));
	        	if(printPval){
		        	printer.print(model.getTestStats().get(i)); 
		        	printer.print(model.getSe().get(i)); 
		        	printer.print(model.getpValue().get(i));
	        	}
	        	printer.println();		
	        }
	       
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	public void writeCoxStatAgg(String fileName, CoxModel model){
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
		
			CSVPrinter printer;
			try {
				printer = new CSVPrinter(new FileWriter(fileName, true), format);
				printer.println();	
		        printer.print("Para Setting");printer.print(model.getConfigDesp());
		        printer.println();
		        printer.print("Avg c_stats");printer.print(model.getC_statistic());
		        printer.println();
		        printer.print("RecordTime");printer.print(getCurrentTimeStamp() );
		        printer.println();
		        printer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public void writeCoxStat(String fileName, CoxModel model){
		
		//CSV Write Example using CSVPrinter
//		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		CSVFormat format = CSVFormat.RFC4180.withDelimiter(',');
		
		try {
			CSVPrinter printer = new CSVPrinter(new FileWriter(fileName, true), format);
			printer.println();	
	       // printer.printRecord("Attr, Value");
	        printer.print("Censored");printer.print(model.getCensoredN());
	        printer.println();
	        
	        printer.print("Failed");printer.print(model.getFailedN());
	        printer.println();
	        
	        printer.print("Total");printer.print(model.getCensoredN()+model.getFailedN());
	        printer.println();
	        
	        long[] diffTrain=model.getTrainingTime();
	        printer.print("trainTime");printer.print("Training Time is "+diffTrain[1]+" hour(s), "+diffTrain[2]+" minute(s),"+ diffTrain[3]+" second(s) and "+diffTrain[4]+" millisecond(s)");
	        printer.println();
	        
	        printer.print("c_statistic");printer.print(model.getC_statistic());
	        printer.println();	

	        printer.print("dataFile");printer.print(model.getDataFilePath());
	        printer.println();
	        
	        printer.print("modelFile");printer.print(model.getModelFilePath());
	        printer.println();
	        
	        printer.print("configFile");printer.print(model.getConfigFilePath());
	        printer.println();	
	        
	        printer.print("RecordTime");printer.print(getCurrentTimeStamp() );
	        printer.println();
			 printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	public static String getCurrentTimeStamp() {
	    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    return strDate;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FileTool ft = new FileTool();
//		ArrayList<String> schemaString = ft.parserParseComma("data/cox-data/kidneySchema.csv");
		 try {
			ft.parserDoc("data/cox-data/coxTestConfig.xml",new CoxProcessConfig());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

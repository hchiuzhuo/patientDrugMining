package org.imeds.daemon;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.imeds.data.sampleConfig;
import org.imeds.util.OSValidator;

public class ImedsDaemonConfig {
	
	 private static String omopDbName ="db_OMOPS";
	 private static String omopDbDriver;
	 private static String omopDbUrl;
	 private static String omopDbUser;
	 private static String omopDbPassword;
	 private static String omopDbSearchPath;
	 private static String private_search_path;
	 private static boolean omopDbEnable=false;
	 private static ArrayList<String> patientFeatureExpFolders = new ArrayList<String>();  	
	 private static ArrayList<String> pearsonOutlierExpFolders = new ArrayList<String>();  
	 private static ArrayList<String> pearsonOutlierToDB = new ArrayList<String>(); 
	 private static ArrayList<String> preSeqDsPrepareFolders = new ArrayList<String>();
	 private static ArrayList<String> seqPtnPrepareFolders = new ArrayList<String>();
	 private static ArrayList<String> CoxDevianceResidualOutlierFolders = new ArrayList<String>();
	 private static Integer flush;
	 public static String getOmopDbName() {
		return omopDbName;
	}
	public static void setOmopDbName(String omopDbName) {
		ImedsDaemonConfig.omopDbName = omopDbName;
	}
	public static String getOmopDbDriver() {
		return omopDbDriver;
	}
	public static void setOmopDbDriver(String omopDbDriver) {
		ImedsDaemonConfig.omopDbDriver = omopDbDriver;
	}
	public static String getOmopDbUrl() {
		return omopDbUrl;
	}
	public static void setOmopDbUrl(String omopDbUrl) {
		ImedsDaemonConfig.omopDbUrl = omopDbUrl;
	}
	public static String getOmopDbUser() {
		return omopDbUser;
	}
	public static void setOmopDbUser(String omopDbUser) {
		ImedsDaemonConfig.omopDbUser = omopDbUser;
	}
	public static String getOmopDbPassword() {
		return omopDbPassword;
	}
	public static void setOmopDbPassword(String omopDbPassword) {
		ImedsDaemonConfig.omopDbPassword = omopDbPassword;
	}
	public static String getOmopDbSearchPath() {
		return omopDbSearchPath;
	}
	public static void setOmopDbSearchPath(String omopDbSearchPath) {
		ImedsDaemonConfig.omopDbSearchPath = omopDbSearchPath;
	}
	public static boolean isOmopDbEnable() {
		return omopDbEnable;
	}
	public static void setOmopDbEnable(boolean omopDbEnable) {
		ImedsDaemonConfig.omopDbEnable = omopDbEnable;
	}
	public static String getPrivate_search_path() {
		return private_search_path;
	}
	public static void setPrivate_search_path(String private_search_path) {
		ImedsDaemonConfig.private_search_path = private_search_path;
	}

	public static ArrayList<String> getPatientFeatureExpFolders() {
		return patientFeatureExpFolders;
	}
	public static void setPatientFeatureExpFolders(
		ArrayList<String> patientFeatureExpFolders) {
		ImedsDaemonConfig.patientFeatureExpFolders = patientFeatureExpFolders;
	}
	public static ArrayList<String> getPearsonOutlierExpFolders() {
		return pearsonOutlierExpFolders;
	}
	public static void setPearsonOutlierExpFolders(
		ArrayList<String> pearsonOutlierExpFolders) {
		ImedsDaemonConfig.pearsonOutlierExpFolders = pearsonOutlierExpFolders;
	}
	public static ArrayList<String> getPearsonOutlierToDB() {
		return pearsonOutlierToDB;
	}
	public static void setPearsonOutlierToDB(ArrayList<String> pearsonOutlierToDB) {
		ImedsDaemonConfig.pearsonOutlierToDB = pearsonOutlierToDB;
	}
	public static ArrayList<String> getSeqPtnPrepareFolders() {
		return seqPtnPrepareFolders;
	}
	public static void setSeqPtnPrepareFolders(
			ArrayList<String> seqPtnPrepareFolders) {
		ImedsDaemonConfig.seqPtnPrepareFolders = seqPtnPrepareFolders;
	}
	public static ArrayList<String> getPreSeqDsPrepareFolders() {
		return preSeqDsPrepareFolders;
	}
	public static void setPreSeqDsPrepareFolders(
			ArrayList<String> preSeqDsPrepareFolders) {
		ImedsDaemonConfig.preSeqDsPrepareFolders = preSeqDsPrepareFolders;
	}
	public static ArrayList<String> getCoxDevianceResidualOutlierFolders() {
		return CoxDevianceResidualOutlierFolders;
	}
	public static void setCoxDevianceResidualOutlierFolders(
			ArrayList<String> coxDevianceResidualOutlier) {
		CoxDevianceResidualOutlierFolders = coxDevianceResidualOutlier;
	}
	public static Integer getFlush() {
		return flush;
	}
	public static void setFlush(Integer flush) {
		ImedsDaemonConfig.flush = flush;
	}
	public ImedsDaemonConfig() {
		// TODO Auto-generated constructor stub
	}
	public static void loadImedsDaemonConfig(String fileName) {
		File inputXml = new File(fileName);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element L0 = document.getRootElement();
			for (Iterator L1 = L0.elementIterator(); L1.hasNext();) {
				Element L1_emt = (Element) L1.next();
				if(L1_emt.getName().equals("dbConfig")){
					
					omopDbDriver		= L1_emt.element(omopDbName).elementText("db_Driver");
					omopDbUrl			= L1_emt.element(omopDbName).elementText("dbURL");
					omopDbUser			= L1_emt.element(omopDbName).elementText("dbUser");
					omopDbPassword		= L1_emt.element(omopDbName).elementText("dbPassword");
					omopDbSearchPath	= L1_emt.element(omopDbName).elementText("search_path");
					private_search_path = L1_emt.element(omopDbName).elementText("private_search_path");

					String enable = L1_emt.element(omopDbName).elementText("enable");

					if(enable!=null && enable.trim().equalsIgnoreCase("1"))
						omopDbEnable = true;
					String private_search_path = L1_emt.element(omopDbName).elementText("private_search_path");
				}else if(L1_emt.getName().equals("expConfig")){
					List<Element> expList = L1_emt.element("patientFeaturePrepare").elements("folder");
					getFolderList(expList, patientFeatureExpFolders);
					
					expList = L1_emt.element("pearsonOutlier").elements("folder");
					getFolderList(expList,pearsonOutlierExpFolders);
					
					expList = L1_emt.element("pearsonOutlierToDB").elements("folder");
					getFolderList(expList,pearsonOutlierToDB);
					
					expList = L1_emt.element("CoxDevianceResidualOutlier").elements("folder");
					getFolderList(expList,CoxDevianceResidualOutlierFolders);
					
					String flushstr = L1_emt.element("pearsonOutlierToDB").elementText("flush");
					if(flushstr!=null && flushstr.trim()!="")flush = Integer.parseInt(flushstr);
					else flush = 1;
					expList = L1_emt.element("seqPtnPrepare").elements("folder");
					getFolderList(expList,seqPtnPrepareFolders);
					
					expList = L1_emt.element("preSeqDsPrepare").elements("folder");
					getFolderList(expList,preSeqDsPrepareFolders);
					
//					for(Element ele: expList){
//						String folderName=ele.getText();
//						
//						if(!OSValidator.isWindows()){folderName = folderName.replace("\\", "/");}
//						patientFeatureExpFolders.add(folderName);											
//					}
					
					
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
	public static void getFolderList(List<Element> expList, List folderlist){
		
		for(Element ele: expList){
			String folderName=ele.getText();
			
			if(!OSValidator.isWindows()){folderName = folderName.replace("\\", "/");}
			folderlist.add(folderName);											
		}
	}
	public static String getConfigString() {
		 
		return	"omopDbName	= " + omopDbName + "\n" + 
				"omopDbDriver	= " + omopDbDriver + "\n" +
				"omopDbUrl	= " + omopDbUrl + "\n" +
				"omopDbUser	= " + omopDbUser	+ "\n" +
				"omopDbPassword	= " + omopDbPassword + "\n" +
				"omopDbSearchPath= "+ omopDbSearchPath + "\n" ;
	}

}

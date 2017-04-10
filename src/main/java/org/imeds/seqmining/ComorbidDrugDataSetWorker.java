package org.imeds.seqmining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.imeds.data.ComorbidDataSetWorker;
import org.imeds.data.common.CCIDictionary;
import org.imeds.data.common.seqItemPair;
import org.imeds.db.ImedDB;
import org.imeds.util.CCIcsvTool;
import org.imeds.util.OSValidator;

public class ComorbidDrugDataSetWorker extends ComorbidDataSetWorker {

	
	private String olFolder;
	private String folderP;
	public ComorbidDrugDataSetWorker(String folderP, String configFile, CCIDictionary ccid) {
		super(configFile, ccid);
		if(!OSValidator.isWindows()){folderP = folderP.replace("\\", "/");}
		this.folderP=folderP;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void prepare() {
		super.prepare();
		
	}
	@Override
	public void ready() {
		this.olFolder = getCdsc().getPreseqOutputFolder();
		if(!OSValidator.isWindows()){this.olFolder = this.olFolder.replace("\\", "/");}
		ArrayList<String> modelId = getCdsc().getFittedSparkLRmodelParas();
		if(modelId.size()>0)
		{	for(String mid: modelId){
				String marr[]=mid.split("_");
				Integer fileId = Integer.parseInt(marr[2].trim()); //"para = 500_1_0"			
				String fileName = this.folderP+OSValidator.getPathSep()+this.olFolder+OSValidator.getPathSep()+"trainDS_"+marr[0]+"_"+marr[1]+"_preseq_"+fileId+".csv"; //trainDS_500_1_preseq_0
				buildPatientTreatmentFeature(fileId, getCdsc().getOutlierThreshold(), fileName);
			}
		}else{
			String fileName = this.folderP+OSValidator.getPathSep()+this.olFolder+OSValidator.getPathSep()+"trainDS_preseq.csv"; //trainDS_500_1_preseq_0			
			buildPatientTreatmentFeature(getCdsc().getTargetFileName(), fileName);
		}
	}
	public void buildPatientTreatmentFeature(Integer fileId, Double riThld, String outputFN){
		try {
			HashMap<Long, ArrayList<seqItemPair>> patients = ImedDB.getOutlierPatient(fileId, riThld);
			buildPatientTreatmentFeature(patients ,outputFN);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void buildPatientTreatmentFeature(String fileName, String outputFN){
		try {
			HashMap<Long, ArrayList<seqItemPair>> patients = new HashMap<Long, ArrayList<seqItemPair>> ();
			CCIcsvTool.SurvivalPatientParserDoc(fileName,patients) ;
			buildPatientTreatmentFeature(patients ,outputFN);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void buildPatientTreatmentFeature(HashMap<Long, ArrayList<seqItemPair>> patients , String outputFN){
			
			//1. Select all patient with Diabetes
			try{
				
	
				//2. Select the first date when the patient was diagnosed with Diabetes. Iterate patients one by one
				Iterator<Entry<Long, ArrayList<seqItemPair>>> iter = patients.entrySet().iterator();
				int count=0;
				while (iter.hasNext()) {
					if(count%100==0)System.out.println(count+"......");
					count++;
					
					Entry<Long, ArrayList<seqItemPair>> entry = iter.next();
					//3. treatment timestamp and treatment_id after the index date
					Long key = entry.getKey(); 			    												
					ArrayList<seqItemPair> feature =  ImedDB.getPatientDrugFeature(key,cptlistTotal);
					entry.setValue(feature);
				}
				getCsvparser().preSeqCreateDoc(outputFN, patients);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	

}

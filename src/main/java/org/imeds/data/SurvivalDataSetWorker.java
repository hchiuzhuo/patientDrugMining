package org.imeds.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.imeds.data.common.CCIDictionary;
import org.imeds.db.ImedDB;
import org.imeds.util.ImedDateFormat;
import org.imeds.util.LabelType;
import org.imeds.util.OSValidator;
import org.imeds.util.writeException;

public class SurvivalDataSetWorker extends ComorbidDataSetWorker {
	private SurvivalDataSetConfig cdsc = new SurvivalDataSetConfig();
	private Logger logger;
	
	public SurvivalDataSetConfig getCdsc() {
		return cdsc;
	}
	public void setCdsc(SurvivalDataSetConfig cdsc) {
		this.cdsc = cdsc;
	}
	public SurvivalDataSetWorker(String configFile, CCIDictionary ccid, Logger logger) {
		super(configFile, ccid);
		this.logger=logger;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void prepare() {
		//Initialize config file
		try {
			this.cfgparser.parserDoc(this.configFile,this.cdsc);
		} catch (Exception e) {
			
			this.logger.error(writeException.toString(e));
			
		}		
		//Map DeyoCCI ID to my config col id
		MapFeature(this.cdsc);
		this.cptlistTotal =  getCspListTotal(this.cdsc);
	}
	@Override
	public void ready() {
		
		boolean withHeader=true;
		sampleConfig sc =this.cdsc.getSurvivalDataSet();
		HashMap<Long, ArrayList<Double>> patients =  new HashMap<Long, ArrayList<Double>>();
		HashMap<Long, SurvivalTime> patientsSurvival =  new HashMap<Long, SurvivalTime>();
		try {
			ImedDB.getPatientsWithIndexDiagnoseSurvivalData(patients,patientsSurvival,this.cptlistTotal, getCdsc().getColList(), sc.getSample_range_start(),sc.getSample_range_end(),sc.getSample_random());
			formCharlsonFeature(patients, this.cptlistTotal, this.cdsc, patientsSurvival );
			csvparser.ComorbidDataSetCreateDoc(getCdsc().getTargetFileName(),getCdsc().getColList(), patients,sc.getSample_append(),  withHeader);
			
//			for(Date cend: this.cdsc.getCensorDate()){
//				Iterator<Entry<Long,SurvivalTime>> iter = patientsSurvival.entrySet().iterator();
//				
//				while (iter.hasNext()) {					
//					SurvivalTime csvFeature = iter.next().getValue();
//					csvFeature.setCensored_date(cend);
//				}
//				//String filename = getCdsc().getSvltargetFileName()+OSValidator.getPathSep()+ImedDateFormat.format(cend)+"svltrainDS.csv";
//				csvparser.SurvivalDataSetCreateDoc(getCdsc().getSvltargetFileName(),this.cdsc.getSvlcolList(), patientsSurvival);				
//			}
			csvparser.SurvivalDataSetCreateDoc(getCdsc().getSvltargetFileName(),this.cdsc.getSvlcolList(), patientsSurvival);
			
		} catch (Exception e) {
			
			this.logger.error(writeException.toString(e));
		}
		
	}
	
	
}

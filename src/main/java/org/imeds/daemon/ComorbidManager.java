package org.imeds.daemon;

import org.apache.log4j.Logger;
import org.imeds.data.ComorbidDataSetWorker;
import org.imeds.data.SurvivalDataSetWorker;
import org.imeds.data.common.CCIDictionary;
import org.imeds.data.outlier.CoxDevianceResidualOutlier;
import org.imeds.data.outlier.PearsonResidualOutlier;
import org.imeds.seqmining.ComorbidDrugDataSetWorker;
import org.imeds.util.writeException;

public class ComorbidManager extends ImedsManager {
	private static CCIDictionary cdt;
	private static Logger logger = Logger.getLogger(ComorbidManager.class);
	private static String DSConfig = "DSConfig.xml";
	public ComorbidManager(String DeyoCCIPath) {

		cdt = new CCIDictionary(DeyoCCIPath);
		cdt.buildDictionary();
		try {
			if(ImedsDaemonConfig.isOmopDbEnable()){
				logger.info("buildCptMap.");
		
				cdt.buildCptMap();
			}else{
				logger.info("Run without buildCptMap.");
			}

		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("CCIDictionary build fail"+writeException.toString(e));
		}
	}

	public void run(){
		//Stage 1. Generate train.csv in each disease folder. 
    	//		   Related config file: ImedsDaemonConfig, DSConfig
		if(ImedsDaemonConfig.getPatientFeatureExpFolders().size()>0){
    		GenPatientFeature();
		}
		//Stage 2.1. Train and predict LR model. Done by Spark.
		
		//Stage 2.2. Generate Pearson outlier.
		if(ImedsDaemonConfig.getPearsonOutlierExpFolders().size()>0){
			for(String folderP:ImedsDaemonConfig.getPearsonOutlierExpFolders()){
				logger.info("Processing PearsonOutlierGen: "+folderP);
				PearsonResidualOutlier prlo = new PearsonResidualOutlier(folderP+DSConfig);
				prlo.oulierGen();
				prlo = null;
			}
		}
		//Stage 2.2. Generate cox deviance outlier.
		if(ImedsDaemonConfig.getCoxDevianceResidualOutlierFolders().size()>0){
			for(String folderP:ImedsDaemonConfig.getCoxDevianceResidualOutlierFolders()){
				logger.info("Processing CoxOutlierGen: "+folderP);
				CoxDevianceResidualOutlier prlo = new CoxDevianceResidualOutlier(folderP,DSConfig);
				prlo.genSvlData();
				prlo.oulierGen();
				prlo = null;
			}
		}
		//Stage 2.3. Write outlier into database
		if(ImedsDaemonConfig.getPearsonOutlierToDB().size()>0){
			for(String folderP:ImedsDaemonConfig.getPearsonOutlierToDB()){
				logger.info("Processing Outlier To DB: "+folderP);
				PearsonResidualOutlier prlo =  new PearsonResidualOutlier(folderP+DSConfig);
				prlo.writeOulierToDB();
				prlo = null;
			}
		}
		
		//Stage 2.4. Generate outlier seqPre dataset
		if(ImedsDaemonConfig.getPreSeqDsPrepareFolders().size()>0){
			for(String folderP:ImedsDaemonConfig.getPreSeqDsPrepareFolders()){
				logger.info("Processing Outlier Seq: "+folderP);
				ComorbidDrugDataSetWorker prlo = new ComorbidDrugDataSetWorker(folderP,folderP+DSConfig, cdt);	
				prlo.prepare();
				prlo.ready();
				prlo = null;
			}
		}
	}
	public void GenPatientFeature(){
		for(String folderP:ImedsDaemonConfig.getPatientFeatureExpFolders()){
			logger.info("Processing GenPatientFeature: "+folderP);

			
			ComorbidDataSetWorker cdsw = new ComorbidDataSetWorker(folderP+DSConfig, cdt);
    		cdsw.prepare();
    		if(cdsw.getCdsc().isLRsampleEnable())
    		{	
    			logger.info("GenLRsampleFeature: "+folderP);
    			cdsw.ready();
    			cdsw.go();
    		}
    		
    		
    		SurvivalDataSetWorker svlcdsw = new SurvivalDataSetWorker(folderP+DSConfig, cdt, logger);    		
    		svlcdsw.prepare();
    		if(svlcdsw.getCdsc().isSurvivalsampleEnable())
    		{
    			logger.info("GenSurvivalsampleFeature: "+folderP);
    			svlcdsw.ready();
    			svlcdsw.go();
    		}
		}
	}
	
}

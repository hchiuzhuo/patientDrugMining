package org.imeds.daemon;

import org.apache.log4j.Logger;
import org.imeds.data.ComorbidDataSetWorker;
import org.imeds.seqmining.SequenceDataSetWorker;
import org.imeds.seqmining.SurvivalSequenceWorker;
import org.imeds.seqmining.seqRSinterpreter;
import org.imeds.seqmining.seqRStoDB;
import org.imeds.util.OSValidator;
import org.imeds.util.writeException;

public class SeqptnManager extends ImedsManager {
	private static Logger logger = Logger.getLogger(SeqptnManager.class);
	@Override
	public void run() {
		
		//SeqPtnDataPrepare();
		SurvivalSeqPtnDataPrepare();
	}
	public void SeqPtnDataPrepare(){
		for(String folderP:ImedsDaemonConfig.getSeqPtnPrepareFolders()){
			logger.info("Seq Ptn Prepare Processing "+folderP);
			
			SequenceDataSetWorker sdsw = new SequenceDataSetWorker(folderP,"SPMConfig.xml",logger);
			sdsw.prepare();
			if(sdsw.getCdsc().getEnable())sdsw.ready();
			if(sdsw.getCdsc().getVMSPenable())sdsw.go();
			if(sdsw.getCdsc().isMMRFSenable()) sdsw.done();
			
			if(ImedsDaemonConfig.isOmopDbEnable()){
			seqRSinterpreter srip = new seqRSinterpreter();
				try {

					srip.processFile(folderP+"seqptnPreSemantic", folderP+"seqptnSemantic");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("fail to transform to semantic meaning"+writeException.toString(e));
					
				}
			}
				
		}
	}
	public void SurvivalSeqPtnDataPrepare(){
		for(String folderP:ImedsDaemonConfig.getSeqPtnPrepareFolders()){
			logger.info("Seq Ptn Prepare Processing "+folderP);
			
			SurvivalSequenceWorker sdsw = new SurvivalSequenceWorker(folderP,"SPMConfig.xml",logger);
			sdsw.prepare();
			if(sdsw.getCdsc().isSVIenable())sdsw.ready();
			if(sdsw.getCdsc().getVMSPenable())sdsw.go();
			if(sdsw.getCdsc().isSVIFSenable()) sdsw.done();

			if(sdsw.getCdsc().isMFreeenable())
				try {
					sdsw.SeqptnRanking();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					logger.error("fail to rank"+writeException.toString(e1));
				}
		
			if(ImedsDaemonConfig.isOmopDbEnable()){
//				logger.info("transform to semantic meaning.");
//				seqRSinterpreter srip = new seqRSinterpreter();
				try {

//					srip.processFile(folderP+"seqptnPreSemantic", folderP+"seqptnSemantic");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("fail to transform to semantic meaning"+writeException.toString(e));
					
				}
				
				logger.info("seqptnSemantic test");
				seqRStoDB stdb = new seqRStoDB();
				try {
					//stdb.processSemanticSeqFile(folderP+"seqptnSemantic");
					//stdb.processRoutFile(folderP+"seqptnSemantic"+OSValidator.getPathSep()+"Rout"); 
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}

		}
	}
}

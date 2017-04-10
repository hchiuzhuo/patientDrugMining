package org.imeds.feature.selection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.imeds.daemon.ComorbidManager;
import org.imeds.data.ComorbidDataSetConfig;
import org.imeds.data.Worker;
import org.imeds.util.CCIcsvTool;
import org.imeds.util.ComorbidDSxmlTool;
import org.imeds.util.LabelType;
import org.imeds.util.SPMdocTool;

public class MMRFSworker extends Worker {
	private static Logger logger = Logger.getLogger(MMRFSworker.class);
	private ArrayList<labelItemsets> labelSeqList = new  ArrayList<labelItemsets>();
	private ArrayList<discrimItemsets> discrimSeqList = new  ArrayList<discrimItemsets>();
	
	//labelList <patientId, label> label is outlier or not
	private HashMap<Long, Integer> labelList = new HashMap<Long, Integer>();
	
	//classList <patientId, class> class is patient actual dead=0 or alive=1
	private HashMap<Long, Double> classList = new HashMap<Long, Double>();

	private String configFile="";
	private MMRFSConfig cdsc = new MMRFSConfig();
	private SPMdocTool cfgparser = new SPMdocTool();
	
	private ArrayList<discrimItemsets> featureSeqList = new  ArrayList<discrimItemsets>();

	public MMRFSworker() {
		// TODO Auto-generated constructor stub
		
	}
	public MMRFSworker(String configFile) {
		this.configFile = configFile;
		//Init MMRFSConfig
		this.cfgparser.parserConfigDoc(this.configFile,this.cdsc);
				
	}
	public MMRFSworker(MMRFSConfig cdsc) {
		this.cdsc = cdsc;
	}


	@Override
	public void prepare() {
		
		//Read in frequent seq ptn
		this.cfgparser.parserDiscrimDoc(this.cdsc.getDiscrimItemsetsFileName(), this.discrimSeqList);
		
		
		if(this.cdsc.getLabelBase().equalsIgnoreCase("Ri")){
			//label patient as outlier(1) or not(0)	
			//get patient actual class. died (0) or alive (1)
			CCIcsvTool.OutlierClassParserDoc(this.cdsc.getOutlierSource(), this.cdsc.getLabelDefineThreshold(), this.labelList, this.classList);
		}else if(this.cdsc.getLabelBase().equalsIgnoreCase("PredictP")){
			//label patient as better than expected(1) or not(0)	
			//get patient actual class. died (0) or alive (1)
			CCIcsvTool.PredictClassParserDoc(this.cdsc.getOutlierSource(), this.cdsc.getLabelDefineThreshold(), this.labelList, this.classList);
		}
		
		//get patient druglist with pateint id
		this.cfgparser.parserLabelDoc(this.cdsc.getBasicItemsetsFileName(), this.labelSeqList, this.labelList);
	}

	@Override
	public void ready() {
		// Calculate fisher gain for each frequent seq ptn
		int i=0;
		for(discrimItemsets dscmset:this.discrimSeqList){
			i++;
			for(labelItemsets lbset:this.labelSeqList){
				//if pateint drug list contain frequent seq ptn, mark as 1.0
				if(lbset.getItemsets().isContained(dscmset.getItemsets())){					
					//label: outlier(1) or not(0), feature include(1.0 not include (0), patient id
					
					dscmset.addDatapoints(new label(lbset.getLabel(), LabelType.yesFeature,(long)lbset.getItemsets().getId(),this.classList.get(lbset.getItemsets().getId())));
					
					//System.out.println(i+" lb: "+lbset.getLabel()+" yesF:"+LabelType.yesFeature+" : "+dscmset.getDatapoints());
				}else{
					dscmset.addDatapoints(new label(lbset.getLabel(), LabelType.notFeature,(long)lbset.getItemsets().getId(),this.classList.get(lbset.getItemsets().getId())));
				}
			}
			dscmset.getGain(discrimItemsets.TYPE_FISHER_GAIN);
		}
	}


	@Override
	public void go() {
		// Feature selection
		Collections.sort(this.discrimSeqList, new Comparator<discrimItemsets>(){			 
			public int compare(discrimItemsets e1, discrimItemsets e2) {
		        if(e1.getGain() < e2.getGain()){
		            return 1;
		        } else {
		            return -1;
		        }
		    }
		});
		
		Collections.sort(this.labelSeqList, new Comparator<labelItemsets>(){			 
			public int compare(labelItemsets e1, labelItemsets e2) {
		        if(e1.getItemsets().getId() > e2.getItemsets().getId()){
		            return 1;
		        } else {
		            return -1;
		        }
		    }
		});
		int i=0;
		Integer[] cnt= new Integer[1];
		cnt[0]=0; // total cover
		
		while(this.discrimSeqList.size()>0){
			discrimItemsets dscmset = this.discrimSeqList.get(i);
			Boolean isDscmset = isDiscriminative(dscmset,cnt);
			if(isDscmset){
				dscmset.calDpstat();
				this.featureSeqList.add(dscmset);
			}
			i++;
			if((i>=this.discrimSeqList.size())|| (cnt[0]>=this.labelSeqList.size()))break;
		}
		
		this.logger.info(i+" discriminative ptn done. "+"total patient sequences: "+this.labelSeqList.size()+"/classify patients "+cnt[0]);
	}

	public Boolean isDiscriminative(discrimItemsets dscmset, Integer[] cnt){
		Boolean rs = false;
		Collections.sort(dscmset.getDatapoints(), new Comparator<label>(){			 
			public int compare(label e1,label e2) {
		        if(e1.getData_id()> e2.getData_id()){
		            return 1;
		        } else {
		            return -1;
		        }
		    }
		});
		//Todo: add calculate outlier, not outlier, die, alive
		
		for(label lb: dscmset.getDatapoints()){
			if(lb.getFeature_v()==LabelType.yesFeature){			
				for(labelItemsets e1:this.labelSeqList){
					if(((long)e1.getItemsets().getId()==lb.getData_id())){
						if(e1.getItemsets().getReferenceCount()==0){	
							//data has not been covered
							rs = true;
							cnt[0]++;
						}
						e1.getItemsets().increaseReferenceCount();						
					}					
				}
			}
		}
		//System.out.println(dscmset.getItemsets().toString()+" total: "+sca[0]+" normal: "+sca[1]+" outlier: "+sca[2]+ " fisher: "+dscmset.getGain());
		return rs;
	}

	@Override
	public void done() {
		logger.info(this.cdsc.getFeatureItemsetFileName());
		if(this.cdsc.getLabelBase().equalsIgnoreCase("Ri")){
			this.cfgparser.createFeatureFileRi(this.cdsc.getFeatureItemsetFileName(), this.featureSeqList);
		}else if(this.cdsc.getLabelBase().equalsIgnoreCase("PredictP")){
			this.cfgparser.createFeatureFilePredictP(this.cdsc.getFeatureItemsetFileName(), this.featureSeqList);
		}
		printNotCoveredSeq();
	}
	
	public void printNotCoveredSeq(){
		int notclassify=0;
		for(labelItemsets e1:this.labelSeqList){
			if(e1.getItemsets().getReferenceCount()==0)
			{	
			//	System.out.println(e1.getItemsets().toString());
				notclassify++;
			}else if(e1.getItemsets().getReferenceCount()>1){
				//logger.info(e1.getItemsets().toString());
			}
			
		}
		logger.info("not be classify: "+notclassify);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MMRFSworker mrfw = new MMRFSworker("data\\IMEDS\\DiabeteComorbidDS\\SPMConfig.xml");
		mrfw.prepare();
		mrfw.ready();
		mrfw.go();
		mrfw.done();
	}

}

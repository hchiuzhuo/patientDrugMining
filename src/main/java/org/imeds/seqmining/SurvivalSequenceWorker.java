package org.imeds.seqmining;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.imeds.data.SurvivalTime;
import org.imeds.db.ImedR;
import org.imeds.feature.screening.ModelFreeScreen;
import org.imeds.feature.screening.ModelFreeScreen.MFStype;
import org.imeds.feature.screening.Tuple;
import org.imeds.feature.screening.feature;
import org.imeds.feature.screening.measureScore;
import org.imeds.feature.selection.basicItemsets;
import org.imeds.feature.selection.discrimItemsets;
import org.imeds.feature.selection.label;
import org.imeds.feature.selection.labelItemsets;
import org.imeds.util.CCIcsvTool;
import org.imeds.util.ImedDateFormat;
import org.imeds.util.LabelType;
import org.imeds.util.OSValidator;
import org.imeds.util.SPMdocTool;
import org.imeds.util.TimeDiff;
import org.imeds.util.writeException;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Itemset;

public class SurvivalSequenceWorker extends SequenceDataSetWorker {
	private SPMdocTool cfgparser = new SPMdocTool();
	private ModelFreeScreen mfs = new  ModelFreeScreen();
	public enum FilterCriteria {outlier, notoutlier, Folder};
	public SurvivalSequenceWorker(){
		
	}
	public SurvivalSequenceWorker(String folderP, String string, Logger logger) {
	
		super(folderP,string, logger);
	}
	@Override
	public void ready() {
		// STAGE 3. transform data format for SPM and generate survival dataset format
//		seqTrainSetGen();
		seqTrainSetGenWithCharlsonIdx();
	}
	public void seqTrainSetGen(){
		// STAGE 3.1. Select training patient dataset for sequential pattern generation
		
		//take out outlier result
		//labelList <patientId, label> label is outlier or not
		HashMap<Long, Integer> labelList = new HashMap<Long, Integer>();	
		//classList <patientId, class> class is patient actual dead=0 or alive=1, Dummy here. useless just for satisfy function parameter
		HashMap<Long, Double> classList = new HashMap<Long, Double>();
		String outlierFile=this.getCdsc().getSVIfilterFile();//"data/IMEDS/CgstHfComorbidDS/outlier/trainDS_2005-10-10_prol.csv";
		Double outlierThreshold=this.getCdsc().getSVIoutlierThreshold();
		CCIcsvTool.OutlierClassParserDoc(outlierFile, outlierThreshold, labelList, classList);
	
		//take out vertical history
		String verticalSFile=this.getCdsc().getSVIverticalSeqFile();//"data/IMEDS/CgstHfComorbidDS/preSeq/trainDS_preseq.csv";
		ArrayList<ArrayList<String>> DataPointList = new ArrayList<ArrayList<String>>();
		CCIcsvTool.preSequenceDataParserDoc(verticalSFile,DataPointList);
		HashMap<Long, HashMap<Date, Itemset>> sequences = processSequence(DataPointList);		
		HashMap<Long, ArrayList<String>> seqList = transSequencesToList(sequences);
		
		//take out survival data
		HashMap<Long,SurvivalTime> survivalDataPoints = new HashMap<Long,SurvivalTime>();
		String svFileName = this.getCdsc().getSVIsurvivalFile();//"data/IMEDS/CgstHfComorbidDS/coxRegressionPredict/2005-10-10svltrainDS.csv";	
		try {
			CCIcsvTool.SurvivalDataSetParserDoc(svFileName, survivalDataPoints);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.logger.error(writeException.toString(e));
		}
		
		//select experiment sequential pattern data
		Iterator<Entry<Long,Integer>> iter = labelList.entrySet().iterator();
		HashMap<Long, ArrayList<String>> selectedSeqList = new HashMap<Long, ArrayList<String>>(); 
		ArrayList<SurvivalTime> selectedDataPoints = new ArrayList<SurvivalTime>();
		while (iter.hasNext()) {			
			Entry<Long,Integer> entry = iter.next();						
//			if(entry.getValue()==LabelType.yesOutlier && seqList.get(entry.getKey())!=null){
			if(selectCriteria(entry, this.getCdsc().getSVIfilterCriteria()) && seqList.get(entry.getKey())!=null){	
				selectedSeqList.put(entry.getKey(), seqList.get(entry.getKey()));
				selectedDataPoints.add(survivalDataPoints.get(entry.getKey()));
			}
		}
		seqList=null;
		survivalDataPoints=null;
//		CCIcsvTool.SequenceDataSetCreateDoc("data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS"+OSValidator.getPathSep()+"seq_"+"2005-10-10.csv",selectedSeqList);		
		CCIcsvTool.SequenceDataSetCreateDoc(this.folderpath+OSValidator.getPathSep()+"seq_"+this.getCdsc().getSVIoutputFileName(),selectedSeqList);		
		
		//select experiment survival data
		ArrayList<String> title=new ArrayList<String>();		
		title.add("ID");
		title.add("Period");
		title.add("Failed");
		title.add("dis_idx_date");
		Collections.sort(selectedDataPoints);
		CCIcsvTool.SurvivalDataSetCreateDoc(this.folderpath+OSValidator.getPathSep()+"svi_"+this.getCdsc().getSVIoutputFileName(),title, selectedDataPoints);
			
	}
	public void seqTrainSetGenWithCharlsonIdx(){
		// STAGE 3.1. Select training patient dataset for sequential pattern generation
		
		//take out outlier result
		//labelList <patientId, label> label is outlier or not
		HashMap<Long, Integer> labelList = new HashMap<Long, Integer>();	
		//classList <patientId, class> class is patient actual dead=0 or alive=1, Dummy here. useless just for satisfy function parameter
		HashMap<Long, Double> classList = new HashMap<Long, Double>();
		String outlierFile=this.getCdsc().getSVIfilterFile();//"data/IMEDS/CgstHfComorbidDS/outlier/trainDS_2005-10-10_prol.csv";
		Double outlierThreshold=this.getCdsc().getSVIoutlierThreshold();
		CCIcsvTool.OutlierClassParserDoc(outlierFile, outlierThreshold, labelList, classList);
	
		//take out vertical history
		String verticalSFile=this.getCdsc().getSVIverticalSeqFile();//"data/IMEDS/CgstHfComorbidDS/preSeq/trainDS_preseq.csv";
		ArrayList<ArrayList<String>> DataPointList = new ArrayList<ArrayList<String>>();
		CCIcsvTool.preSequenceDataParserDoc(verticalSFile,DataPointList);
		HashMap<Long, HashMap<Date, Itemset>> sequences = processSequence(DataPointList);		
		HashMap<Long, ArrayList<String>> seqList = transSequencesToList(sequences);
		
		//take out survival data
		HashMap<Long,SurvivalTime> survivalDataPoints = new HashMap<Long,SurvivalTime>();
		String svFileName = this.getCdsc().getSVIsurvivalFile();	
//		String svFileName = "data/IMEDS/CgstHfComorbidDS/outlier/2014-10-10svltrainDS_R.csv  include charlson index";
		try {
			CCIcsvTool.R_SurvivalDataSetParserDoc(svFileName, survivalDataPoints, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.logger.error(writeException.toString(e));
		}
		
		//select experiment sequential pattern data
		Iterator<Entry<Long,Integer>> iter = labelList.entrySet().iterator();
		HashMap<Long, ArrayList<String>> selectedSeqList = new HashMap<Long, ArrayList<String>>(); 
		ArrayList<SurvivalTime> selectedDataPoints = new ArrayList<SurvivalTime>();
		while (iter.hasNext()) {			
			Entry<Long,Integer> entry = iter.next();						
//			if(entry.getValue()==LabelType.yesOutlier && seqList.get(entry.getKey())!=null){
			if(selectCriteria(entry, this.getCdsc().getSVIfilterCriteria()) && seqList.get(entry.getKey())!=null){	
				selectedSeqList.put(entry.getKey(), seqList.get(entry.getKey()));
				selectedDataPoints.add(survivalDataPoints.get(entry.getKey()));
			}
		}
		seqList=null;
		survivalDataPoints=null;
		CCIcsvTool.SequenceDataSetCreateDoc(this.folderpath+OSValidator.getPathSep()+"seq_"+this.getCdsc().getSVIoutputFileName(),selectedSeqList);		

		Collections.sort(selectedDataPoints);
		CCIcsvTool.SurvivalDataSetCreateDoc(this.folderpath+OSValidator.getPathSep()+"svi_"+this.getCdsc().getSVIoutputFileName(),selectedDataPoints);
		 	
	}
	public boolean selectCriteria(Entry<Long,Integer> entry, String filterCriteria){
		
		boolean result=false;
		if(filterCriteria!=null && filterCriteria.equals(FilterCriteria.outlier.name())){
			return (entry.getValue()==LabelType.yesOutlier);
		}else if(filterCriteria!=null && filterCriteria.equals(FilterCriteria.notoutlier.name())){
			return (entry.getValue()==LabelType.notOutlier);
		}else if(filterCriteria!=null && filterCriteria.contains(FilterCriteria.Folder.name())){
			String[] ctr=filterCriteria.split("_");
			
			if(ctr[0].equals(FilterCriteria.outlier.name())){
				
			}
		}
		return result;		
	}
	@Override
	public void go() {
		super.go(); //call data mining alg
		genSvlDataWithCharlsonIdx();

	}
	@Override
	public void done() {

		try {
			modelFreeScreenSeqptn();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void genSvlData(){
		
		//1. read patient survival data
		ArrayList<SurvivalTime> DataPoints = new ArrayList<SurvivalTime>();
		String svFileName = this.folderpath+OSValidator.getPathSep()+this.getCdsc().getSVIFSsurvivalFile();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/svi_2005-10-10.csv";	
		HashMap<Long, Integer> patientSurvivalTimeMap=new HashMap<Long, Integer>();
		try {
			CCIcsvTool.SurvivalDataSetParserDoc(svFileName, DataPoints);
			for(int i=0;i<DataPoints.size();i++){
				patientSurvivalTimeMap.put(DataPoints.get(i).getId(), i);
			}
		} catch (Exception e) {
			this.logger.error(writeException.toString(e));
		}
		//2. get patient druglist with pateint id
		ArrayList<basicItemsets> seqList = new  ArrayList<basicItemsets>();
		String BasicItemsetFileName = this.folderpath+OSValidator.getPathSep()+this.getCdsc().getSVIFSseqFile();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/seq_2005-10-10_withId.csv";
		this.cfgparser.parserPreseqDoc(BasicItemsetFileName, seqList);
		
		
		String seqptnFolder= this.folderpath+OSValidator.getPathSep()+this.getCdsc().getSVIFSseqptnFolder();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/seqptn";
		String seqCoxFolder= this.folderpath+OSValidator.getPathSep()+this.getCdsc().getSVIFSseqCoxFolder();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/seqDsmc";
		File directory = new File(seqptnFolder);
		File[] fList = directory.listFiles();
		for (File file : fList){		
			if (file.isFile() && file.getName().contains(".txt")){
				String filename = file.getName();
				
				//3. read sequential pattern data from folder seqptn
				//Read in frequent seq ptn
				String DiscrimItemsetsFileName=seqptnFolder+OSValidator.getPathSep()+filename;
				ArrayList<discrimItemsets> discrimSeqList = new  ArrayList<discrimItemsets>();
				this.cfgparser.parserDiscrimDoc(DiscrimItemsetsFileName ,discrimSeqList);
				
				for(basicItemsets lbset:seqList){
//					ArrayList<Double> features=new ArrayList<Double>();
					BitSet dpBitset = new BitSet(discrimSeqList.size()); 
					for(int i=0;i<discrimSeqList.size();i++){
						if(lbset.isContained(discrimSeqList.get(i).getItemsets())){
//							features.add(LabelType.featureContained);
							
							dpBitset.set(i);	
						}else{
//							features.add(LabelType.featureNotContained);
						}
					}
//					DataPoints.get(patientSurvivalTimeMap.get(lbset.getId())).setFeatures(features);
					DataPoints.get(patientSurvivalTimeMap.get(lbset.getId())).setBitFeatures(dpBitset);
					System.out.println("bitsetsize");
				}
				
				String rFileName=seqCoxFolder+OSValidator.getPathSep()+filename.substring(0,filename.lastIndexOf("."))+"_R.csv";
//				CCIcsvTool.SurvivalDataSetCreateDoc(rFileName, DataPoints);	
				CCIcsvTool.SurvivalDataSetCreateDoc(rFileName, DataPoints,discrimSeqList.size());
			}
		}				
		//4. build dataset for R
		System.out.println("genSvlData done");
	}

	public void genSvlDataWithCharlsonIdx(){
		
		//1. read patient survival data
		ArrayList<SurvivalTime> DataPoints = new ArrayList<SurvivalTime>();
		String svFileName = this.folderpath+OSValidator.getPathSep()+this.getCdsc().getSVIFSsurvivalFile();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/svi_2005-10-10.csv";	
		HashMap<Long, Integer> patientSurvivalTimeMap=new HashMap<Long, Integer>();
		try {
			CCIcsvTool.R_SurvivalDataSetParserDoc(svFileName, null, DataPoints);
			
			for(int i=0;i<DataPoints.size();i++){
				patientSurvivalTimeMap.put(DataPoints.get(i).getId(), i);
			}
		} catch (Exception e) {
			this.logger.error(writeException.toString(e));
		}
		//2. get patient druglist with pateint id
		ArrayList<basicItemsets> seqList = new  ArrayList<basicItemsets>();
		String BasicItemsetFileName = this.folderpath+OSValidator.getPathSep()+this.getCdsc().getSVIFSseqFile();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/seq_2005-10-10_withId.csv";
		this.cfgparser.parserPreseqDoc(BasicItemsetFileName, seqList);
		
		
		String seqptnFolder= this.folderpath+OSValidator.getPathSep()+this.getCdsc().getSVIFSseqptnFolder();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/seqptn";
		String seqCoxFolder= this.folderpath+OSValidator.getPathSep()+this.getCdsc().getSVIFSseqCoxFolder();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/seqDsmc";
		File directory = new File(seqptnFolder);
		File[] fList = directory.listFiles();
		for (File file : fList){		
			if (file.isFile() && file.getName().contains(".txt")){
				String filename = file.getName();
				System.out.println(filename);
				//3. read sequential pattern data from folder seqptn
				//Read in frequent seq ptn
				String DiscrimItemsetsFileName=seqptnFolder+OSValidator.getPathSep()+filename;
				ArrayList<discrimItemsets> discrimSeqList = new  ArrayList<discrimItemsets>();
				this.cfgparser.parserDiscrimDoc(DiscrimItemsetsFileName ,discrimSeqList);
				
//				for(basicItemsets lbset:seqList){
//					ArrayList<Double> features=new ArrayList<Double>();
//					for(int i=0;i<discrimSeqList.size();i++){
//						if(lbset.isContained(discrimSeqList.get(i).getItemsets())){
//							features.add(LabelType.featureContained);
//						}else{
//							features.add(LabelType.featureNotContained);
//						}
//					}
//					DataPoints.get(patientSurvivalTimeMap.get(lbset.getId())).setAddiFeatures(features);
//				}
//				String rFileName=seqCoxFolder+OSValidator.getPathSep()+filename.substring(0,filename.lastIndexOf("."))+"_R.csv";
//				CCIcsvTool.SurvivalDataSetCreateDoc(rFileName, DataPoints);	
				
				for(basicItemsets lbset:seqList){
//					ArrayList<Double> features=new ArrayList<Double>();
					BitSet dpBitset = new BitSet(discrimSeqList.size()); 
					for(int i=0;i<discrimSeqList.size();i++){
						if(lbset.isContained(discrimSeqList.get(i).getItemsets())){
//							features.add(LabelType.featureContained);
							
							dpBitset.set(i);	
						}else{
//							features.add(LabelType.featureNotContained);
						}
					}
//					DataPoints.get(patientSurvivalTimeMap.get(lbset.getId())).setAddiFeatures(features);
					DataPoints.get(patientSurvivalTimeMap.get(lbset.getId())).setBitFeatures(dpBitset);
					
				}
				
				String rFileName=seqCoxFolder+OSValidator.getPathSep()+filename.substring(0,filename.lastIndexOf("."))+"_R.csv";
//				CCIcsvTool.SurvivalDataSetCreateDoc(rFileName, DataPoints);	
				CCIcsvTool.SurvivalDataSetCreateDoc(rFileName, DataPoints,discrimSeqList.size());
			}
		}				
		//4. build dataset for R
		System.out.println("genSvlData done");
	}
	public void modelFreeScreenSeqptn() throws Exception{
		String seqptnFolder= this.folderpath+this.getCdsc().getSVIFSseqptnFolder();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/seqptn";
		String seqCoxFolder= this.folderpath+this.getCdsc().getSVIFSseqCoxFolder();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/seqDsmc";
		String seqFeatureFolder=this.folderpath+this.getCdsc().getSVIFSseqFeatureFolder();
		String seqPreSemanticFolder=this.folderpath+this.getCdsc().getSVIFSseqPreSemanticFolder();
		File directory = new File(seqptnFolder);
		File[] fList = directory.listFiles();
		ArrayList<String> yTitle=new ArrayList<String>();
		yTitle.add(this.getCdsc().getMFreeyTitle().get(1));
		yTitle.add(this.getCdsc().getMFreeyTitle().get(2));
		for (File file : fList){		
			if (file.isFile() && file.getName().contains(".txt")){
				String rFileName=seqCoxFolder+OSValidator.getPathSep()+file.getName().substring(0,file.getName().lastIndexOf("."))+"_R.csv";

				HashMap<Integer, Double> survFunc = ImedR.getSurvFunc(rFileName);
				
				ArrayList<Tuple> DataPointList = new ArrayList<Tuple>();
				ArrayList<discrimItemsets> discrimSeqList = new  ArrayList<discrimItemsets>();
				ArrayList<discrimItemsets> featureSeqList = new  ArrayList<discrimItemsets>();
				
				//Calculate margin score for each feature
				String ID=this.getCdsc().getMFreeyTitle().get(0);//"ID";				
				Integer xStart=this.getCdsc().getMFreexStart();
				CCIcsvTool.RegressionDatasetParserDoc(rFileName, DataPointList, ID, yTitle, xStart);

				
				Date d1 = new Date();
				ArrayList<feature> result=new ArrayList<feature>();
				if(this.getCdsc().getMFreescreenType()==MFStype.General){
					//System.out.printf(MFStype.General.name());
					result = this.mfs.generalScreen(DataPointList);			
				}else if(this.getCdsc().getMFreescreenType()==MFStype.Survival){	
					//System.out.printf(MFStype.Survival.name());
					result = this.mfs.generalScreenCox(DataPointList, survFunc);
				}else if(this.getCdsc().getMFreescreenType()==MFStype.SurvivalStd){	
					//System.out.printf(MFStype.SurvivalStd.name());
//					result = this.mfs.generalScreenCoxStd(DataPointList, survFunc);
					result = this.mfs.generalScreenCoxStdBitMap(DataPointList, survFunc);					
				}
				Date d0 = new Date();
				long[] diff = TimeDiff.getTimeDifference(d0, d1);
				System.out.printf(file.getName()+","+"n:"+DataPointList.size()+",d:"+result.size()+",");
				System.out.printf(diff[1]+","+diff[2]+","+diff[3]+","+diff[4]);
		        System.out.printf(",model_FreeScore,Time difference is  %d hour(s), %d minute(s), %d second(s) and %d millisecond(s)\n",
		                diff[1], diff[2], diff[3], diff[4]);  
				//take out freqSeqptn
				this.cfgparser.parserDiscrimDoc(seqptnFolder+OSValidator.getPathSep()+file.getName(), discrimSeqList);
				
				//add final feature result to featureSeqList
				for(feature ft:result){
					discrimSeqList.get(ft.getId().intValue()).getItemsets().setId(ft.getId());
					discrimSeqList.get(ft.getId().intValue()).setGain(ft.getScore());
					discrimSeqList.get(ft.getId().intValue()).setDataPointsBitset(new BitSet(DataPointList.size()));
					featureSeqList.add(discrimSeqList.get(ft.getId().intValue()));
				}
				this.cfgparser.createFeatureFileModelFree(seqFeatureFolder+OSValidator.getPathSep()+file.getName().substring(0,file.getName().lastIndexOf("."))+"_dsmc.csv", featureSeqList);
				//wirte ptn code to ....
				
				for(String featureType:this.getCdsc().getMFreefeatureScore()){
					
					Date d4 = new Date();
					if(featureType.contains("support")){
						
				  		Collections.sort(discrimSeqList, new Comparator<discrimItemsets>(){			 
							public int compare(discrimItemsets e1, discrimItemsets e2) {
								if (e1.getSupport() < e2.getSupport()){
							        return 1;           // Neither val is NaN, thisVal is smaller
								}else if (e1.getSupport() > e2.getSupport()){
						            return -1;            // Neither val is NaN, thisVal is larger
								}else{
									if(e1.getItemsets().getId()<e2.getItemsets().getId())
						        		return 1;
						        	else return -1;
								}
						          
						    }
						});
			    	}else if(featureType.contains("modelFreeScore")){
			    		Collections.sort(discrimSeqList, new Comparator<discrimItemsets>(){			 
							public int compare(discrimItemsets e1, discrimItemsets e2) {
			    		        if(e1.getGain() < e2.getGain()){
			    		            return 1;
			    		        }else if(e1.getGain() > e2.getGain()){
			    		            return -1;
			    		        }else{
			    		        	if(e1.getItemsets().getId()<e2.getItemsets().getId())
						        		return 1;
						        	else return -1;
			    		        }
			    		    }
			    		});
			    		
			    	}
					this.cfgparser.createFeatureFilePreSemantic(seqPreSemanticFolder+OSValidator.getPathSep()+file.getName().substring(0,file.getName().lastIndexOf("."))+"_"+ featureType+".csv", featureType, discrimSeqList);
					if(featureType.contains("coverage")){
						
						int startidx= orderByCoverageBitSet( DataPointList, discrimSeqList);
//						int startidx= reorderByCoverage( DataPointList, discrimSeqList);
						Date d3 = new Date();
						diff = TimeDiff.getTimeDifference(d4, d3);
						System.out.printf(file.getName()+","+"n:"+DataPointList.size()+",d:"+discrimSeqList.size()+",");						
						System.out.printf(diff[1]+","+diff[2]+","+diff[3]+","+diff[4]);
				        System.out.printf(","+featureType+",Time difference is  %d hour(s), %d minute(s), %d second(s) and %d millisecond(s)\n",
				                diff[1], diff[2], diff[3], diff[4]);  
						
						//System.out.println("start idx"+startidx+" "+file.getName()+featureType);
						
						featureSeqList= new  ArrayList<discrimItemsets>();
						featureSeqList.addAll(startidx, discrimSeqList);
						this.cfgparser.createFeatureFilePreSemantic(seqPreSemanticFolder+OSValidator.getPathSep()+file.getName().substring(0,file.getName().lastIndexOf("."))+"_"+ featureType+".csv", featureType, featureSeqList);				
					}
					
				}
				
			}
		}
	}
	public void SeqptnRanking() throws Exception{
		String seqCoxFolder= this.folderpath+this.getCdsc().getSVIFSseqCoxFolder();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/seqDsmc";
//		String seqFeatureFolder=this.folderpath+this.getCdsc().getSVIFSseqFeatureFolder();
		String seqPreSemanticFolder=this.folderpath+this.getCdsc().getSVIFSseqPreSemanticFolder();
		
		File directory = new File(seqCoxFolder);
		File[] fList = directory.listFiles();
		ArrayList<String> yTitle=new ArrayList<String>();
		yTitle.add(this.getCdsc().getMFreeyTitle().get(1));
		yTitle.add(this.getCdsc().getMFreeyTitle().get(2));
		for (File file : fList){		
			if (file.isFile() && file.getName().contains(".csv")){
				String rFileName=seqCoxFolder+OSValidator.getPathSep()+file.getName();
				ImedR.loadRisksetAUCdata(rFileName);
				//--For k-fold test only
				String rTeFileName=this.folderpath+"test"+OSValidator.getPathSep()+file.getName();
				ImedR.loadRisksetAUCTestdata(rTeFileName);
				//--For k-fold test only
				
				for(String featureScoreType:this.getCdsc().getMFreefeatureScore()){
					ArrayList<feature> result =new ArrayList<feature>();
					
					ArrayList<measureScore> mscore =new ArrayList<measureScore>();
					String fFileName=seqPreSemanticFolder+OSValidator.getPathSep()+file.getName().substring(0,file.getName().lastIndexOf("_"))+"_"+featureScoreType+".csv";

					CCIcsvTool.dsmcParserDoc(fFileName, "seqId", featureScoreType, this.getCdsc().getMFreefeatureDescription(), result);
					
					int hardThreshold = (int)Math.floor(result.size()/Math.log(result.size()));		
					int stopC;
					
					if(hardThreshold <this.getCdsc().getMFreefeatureStop()) stopC=Math.min(result.size(), this.getCdsc().getMFreefeatureStop());
					else stopC =stopC=Math.min(hardThreshold, this.getCdsc().getMFreefeatureStop());
					
					int step = (int)Math.ceil(stopC/this.getCdsc().getMFreeStep());
					if(step==0)step=1;
					
					stopC=Math.min(stopC+step, result.size());
				
//					(int)Math.ceil(result.size()/this.getCdsc().getMFreeStep());
					
					
					String aucFileName = this.folderpath+this.getCdsc().getMFreeaucFolder()+OSValidator.getPathSep()+file.getName().substring(0,file.getName().lastIndexOf("_"))+"_"+featureScoreType+"_R.csv";
					String routput=this.folderpath+this.getCdsc().getMFreeaucFolder()+OSValidator.getPathSep()+"Rout"+OSValidator.getPathSep()+file.getName().substring(0,file.getName().lastIndexOf("_"))+"_"+featureScoreType+"_";
					mscore = ImedR.getrisksetAUC(routput, rFileName,result, this.getCdsc().getMFreefeatureStart(), step, this.getCdsc().getMFreecoxIter(),stopC, yTitle);
					
					CCIcsvTool.AucCreateDoc(aucFileName, mscore); 
					result = null;
					mscore = null;
				}
				
			}
		}
	}
	/**
	public void SeqptnRanking() throws Exception{
		String seqCoxFolder= this.folderpath+this.getCdsc().getSVIFSseqCoxFolder();//"data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/seqDsmc";
//		String seqFeatureFolder=this.folderpath+this.getCdsc().getSVIFSseqFeatureFolder();
		String seqFeatureFolder=this.folderpath+this.getCdsc().getSVIFSseqPreSemanticFolder();
		
		File directory = new File(seqCoxFolder);
		File[] fList = directory.listFiles();
		ArrayList<String> yTitle=new ArrayList<String>();
		yTitle.add(this.getCdsc().getMFreeyTitle().get(1));
		yTitle.add(this.getCdsc().getMFreeyTitle().get(2));
		for (File file : fList){		
			if (file.isFile() && file.getName().contains(".csv")){
				String rFileName=seqCoxFolder+OSValidator.getPathSep()+file.getName();
				
				for(String featureScoreType:this.getCdsc().getMFreefeatureScore()){
					ArrayList<feature> result =new ArrayList<feature>();
					ArrayList<feature> resulttmp=new ArrayList<feature>();
					ArrayList<measureScore> mscore =new ArrayList<measureScore>();
					String fFileName=seqFeatureFolder+OSValidator.getPathSep()+file.getName().substring(0,file.getName().lastIndexOf("_"))+"_"+featureScoreType+".csv";
//					String fFileName=seqFeatureFolder+OSValidator.getPathSep()+file.getName().substring(0,file.getName().lastIndexOf("_"))+"_dsmc.csv";
					
//					if(featureScoreType.contains("coverage")){
//						CCIcsvTool.dsmcParserDoc(fFileName, "seqId", featureScoreType.substring(featureScoreType.indexOf("_")+1 ,featureScoreType.length()), this.getCdsc().getMFreefeatureDescription(),resulttmp);
//						for(feature ft:resulttmp) System.out.print(ft.getId()+"/"+ft.getScore()+", ");
//						System.out.println();
//						System.out.println("before sort");
//						Collections.sort(resulttmp);
//						for(feature ft:resulttmp) System.out.print(ft.getId()+"/"+ft.getScore()+", ");
//						System.out.println("after sort");
//						
//						ArrayList<Tuple> DataPointList = new ArrayList<Tuple>();
//						
//						//Calculate margin score for each feature
//						String ID=this.getCdsc().getMFreeyTitle().get(0);//"ID";				
//						Integer xStart=this.getCdsc().getMFreexStart();
//						CCIcsvTool.RegressionDatasetParserDoc(rFileName, DataPointList, ID, yTitle, xStart);
//						System.out.println(rFileName+featureScoreType);
//						
//						for(feature ft:resulttmp) System.out.print(ft.getId()+"/"+ft.getScore()+",");
//						System.out.println();
//						
//						result = orderByCoverage(DataPointList,resulttmp);
//					}else{
						CCIcsvTool.dsmcParserDoc(fFileName, "seqId", featureScoreType, this.getCdsc().getMFreefeatureDescription(), resulttmp);
//						Collections.sort(resulttmp);
						result =resulttmp;
//					}
					int step =  (int)Math.ceil(result.size()/this.getCdsc().getMFreeStep());
					if(step==0)step=1;
					if(this.getCdsc().getMFreefeatureStart()< 0){
						mscore = ImedR.getrisksetAUC(rFileName,result, (int)Math.floor(result.size()/Math.log(result.size())), step, this.getCdsc().getMFreecoxIter(), yTitle);
					}else{
						mscore = ImedR.getrisksetAUC(rFileName,result, this.getCdsc().getMFreefeatureStart(), step, this.getCdsc().getMFreecoxIter(), yTitle);					
					}
					String aucFileName = this.folderpath+this.getCdsc().getMFreeaucFolder()+OSValidator.getPathSep()+file.getName().substring(0,file.getName().lastIndexOf("_"))+"_"+featureScoreType+".csv";
					
					CCIcsvTool.AucCreateDoc(aucFileName, mscore); 
					result = null;
					mscore = null;
				}
				
			}
		}
	}**/
	public ArrayList<feature> orderByCoverage(ArrayList<Tuple> DataPointList,ArrayList<feature> result){		
		ArrayList<feature> featureSeqList = new  ArrayList<feature>();
		feature maxFt=result.get(0);
		int maxcvg=0;
		boolean cvgall=false;
		for(Tuple e1:DataPointList){
			e1.resetReferenceCount();
		}
		while(cvgall==false || result.size()>0){
			for(feature lb: result){
				int cvg=0;
				for(Tuple e1:DataPointList){
					if(e1.getxList().get(lb.getId().intValue())==1){
						if(e1.getReferenceCount()==0){	
							//data has not been covered
							cvg++;
						}											
					}					
				}
				if(cvg>maxcvg){
					maxFt=lb;
					maxcvg=cvg;
				}
			}
			if(maxFt!=null){
				cvgall=true;
				for(Tuple e1:DataPointList){
					if(e1.getxList().get(maxFt.getId().intValue())==1){				
						e1.increaseReferenceCount();						
					}
					if(e1.getReferenceCount()==0) cvgall=false;
				}
				featureSeqList.add(maxFt);
				result.remove(maxFt);
//				System.out.print("*"+maxFt.getId()+"/"+maxcvg);
//				System.out.println(maxFt);
				maxFt=null;
				maxcvg=0;
			}else{
				for(Tuple e1:DataPointList){
					e1.resetReferenceCount();
				}
				if(result.size()>0){
					maxFt=result.get(0);
					maxcvg=0;
				}else{
					break;
				}
			}
		}
		System.out.println();
		return featureSeqList;		
	}
	public int reorderByCoverage(ArrayList<Tuple> DataPointList,ArrayList<discrimItemsets> result){		
	
		discrimItemsets maxFt=result.get(0);
		int maxcvg=0;
		boolean cvgall=false;
		for(Tuple e1:DataPointList){
			e1.resetReferenceCount();
		}
		int i=result.size();
		while(cvgall==false || i>0){
			for(int j=0;j<i;j++){
				int cvg=0;
				for(Tuple e1:DataPointList){
					if(e1.getxList().get(result.get(j).getItemsets().getId().intValue())==1){
						if(e1.getReferenceCount()==0){	
							//data has not been covered
							cvg++;
						}											
					}					
				}
				if(cvg>maxcvg){//vital! make sure higher score will be selected if both have same coverage
					maxFt=result.get(j);
					maxcvg=cvg;
					
				}
			}
			if(maxFt!=null){
				cvgall=true;
				for(Tuple e1:DataPointList){
					if(e1.getxList().get(maxFt.getItemsets().getId().intValue())==1){				
						e1.increaseReferenceCount();						
					}
					if(e1.getReferenceCount()==0) cvgall=false;
				}
//				featureSeqList.add(maxFt);
				result.remove(maxFt);
				result.add(maxFt);
				i--;
				System.out.print(maxFt.getItemsets().getId()+"/"+maxcvg+",");
				maxFt=null;
				maxcvg=0;
			}else{
				System.out.println("reset");
				for(Tuple e1:DataPointList){
					e1.resetReferenceCount();
				}
				if(i>0){
					maxFt=result.get(0);
					maxcvg=0;
				}else{
					break;
				}
			}
		}
//		System.out.println();
		return i;
			
	}
	public int orderByCoverageBitSet(ArrayList<Tuple> DataPointList,ArrayList<discrimItemsets> result){		
		
		discrimItemsets maxFt=result.get(0);
		int maxcvg=0;
		BitSet maxBitset=new BitSet(DataPointList.size()); 
		boolean cvgall=false;
		
		BitSet dpBitset = new BitSet(DataPointList.size()); 
		for(int i=0;i<DataPointList.size();i++){
			for(discrimItemsets dsitm:result){
//				if(DataPointList.get(i).getxList().get(dsitm.getItemsets().getId().intValue())==1){
				if(DataPointList.get(i).getBitFeatures().get(dsitm.getItemsets().getId().intValue())){	
					dsitm.setDataPointsBitset(i);
				}
			}
		}
		int i=result.size();
		while(cvgall==false || i>0){
			for(int j=0;j<i;j++){
				int cvg=0;
//				int cvgtmp=0;
//				for(Tuple e1:DataPointList){
//					if(e1.getxList().get(result.get(j).getItemsets().getId().intValue())==1){
//						if(e1.getReferenceCount()==0){	
//							//data has not been covered
//							cvgtmp++;
//						}											
//					}					
//				}
				
				BitSet dsitmBitsetClone = (BitSet) result.get(j).getDataPointsBitset().clone();
				dsitmBitsetClone.andNot(dpBitset);
				cvg = dsitmBitsetClone.cardinality();
//				System.out.println("D"+result.get(j).getItemsets().getId()+" maxcvg: "+maxcvg+"/cvgtmp: "+cvgtmp+"/cvg: "+cvg);
				if(cvg>maxcvg){//vital! make sure higher score will be selected if both have same coverage
					maxFt=result.get(j);
					maxcvg=cvg;
					maxBitset=dsitmBitsetClone;
				}
			}
			if(maxFt!=null){
				cvgall=true;
//				for(Tuple e1:DataPointList){
//					if(e1.getxList().get(maxFt.getItemsets().getId().intValue())==1){				
//						e1.increaseReferenceCount();						
//					}
//					if(e1.getReferenceCount()==0) cvgall=false;
//				}
				dpBitset.or(maxBitset);
				if(dpBitset.cardinality()<DataPointList.size())cvgall=false;

				result.remove(maxFt);
				result.add(maxFt);
				i--;
//				System.out.println("\n"+maxFt.getItemsets().getId()+"/"+maxcvg+"");
				maxFt=null;
				maxBitset=null;
				maxcvg=0;
			}else{
//				System.out.println("reset");
//				for(Tuple e1:DataPointList){
//					e1.resetReferenceCount();
//				}
				dpBitset = new BitSet(DataPointList.size()); 
				if(i>0){
					maxFt=result.get(0);
					maxcvg=0;
				}else{
					break;
				}
			}
		}
//		System.out.println();
		return i;
			
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	//	SequenceDataSetWorker sdsw = new SequenceDataSetWorker("data\\IMEDS\\DiabeteComorbidDS\\DSConfig.xml");
	//	sdsw.prepare();
	//	sdsw.ready();
		SurvivalSequenceWorker  sdsw = new SurvivalSequenceWorker();
//		sdsw.prepare();
//		sdsw.ready();
//		sdsw.go();
		sdsw.genSvlData();
		
	}
	
}

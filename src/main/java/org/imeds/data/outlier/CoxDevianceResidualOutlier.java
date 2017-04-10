package org.imeds.data.outlier;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.imeds.daemon.ImedsDaemonConfig;
import org.imeds.data.SparkLRDataSetWorker.DataPoint;
import org.imeds.data.ComorbidDataSetConfig;
import org.imeds.data.SurvivalDataSetConfig;
import org.imeds.data.SurvivalTime;
import org.imeds.db.ImedDB;
import org.imeds.db.ImedR;
import org.imeds.feature.selection.discrimItemsets;
import org.imeds.util.CCIcsvTool;
import org.imeds.util.ImedDateFormat;
import org.imeds.util.OSValidator;
import org.imeds.util.writeException;
import org.la4j.matrix.Matrix;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

public class CoxDevianceResidualOutlier extends Outlier {
	private String outFileName;
	private HashMap<Long,SurvivalTime> DataPoints;
	private Map<Long,ArrayList<Double>> OutlierList;
	private Map<Integer, Long> ridx_map;
	
	private SurvivalDataSetConfig cdsc = new SurvivalDataSetConfig();
	public CoxDevianceResidualOutlier(String configpath, String configFile) {
		this.configFile = configpath+OSValidator.getPathSep()+configFile;
		try {
			this.cfgparser.parserDoc(this.configFile,this.cdsc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.lrFolder = configpath+this.cdsc.getCoxResidualOutlierInputFolder();
		if(!OSValidator.isWindows()){this.lrFolder = this.lrFolder.replace("\\", "/");}
		
		this.olFolder = configpath+this.cdsc.getCoxResidualOutlierOutputFolder();
		if(!OSValidator.isWindows()){this.olFolder = this.olFolder.replace("\\", "/");}
		
		this.targetfileName = this.cdsc.getTargetFileName();
	}
	
	public SurvivalDataSetConfig getCdsc() {
		return cdsc;
	}
	public void setCdsc(SurvivalDataSetConfig cdsc) {
		this.cdsc = cdsc;
	}
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	public void genSvlData(){
		//HashMap<Long, SurvivalTime> patientsSurvival =  new HashMap<Long, SurvivalTime>();
		ArrayList<SurvivalTime> patientsSurvival = new ArrayList<SurvivalTime>();
		getCsvparser().SurvivalTrainDataSetParserDoc(this.cdsc.getSvltargetFileName(), patientsSurvival);
		for(Date cend: this.cdsc.getCensorDate()){
//			Iterator<Entry<Long,SurvivalTime>> iter = patientsSurvival.entrySet().iterator();
//			
//			while (iter.hasNext()) {					
//				SurvivalTime csvFeature = iter.next().getValue();
//				csvFeature.setCensored_date(cend);
//			}
			for(SurvivalTime st:patientsSurvival){	
				st.setCensored_date(cend);				
			}
			String filename = this.lrFolder+OSValidator.getPathSep()+ImedDateFormat.format(cend)+"svltrainDS.csv";
			
			Collections.sort(patientsSurvival);
			getCsvparser().SurvivalCensoredDataSetCreateDoc(filename,this.cdsc.getSvlcolList(), patientsSurvival);				
		}
	}
	public void init( String svFileName, String rFileName ){
		
		
		
    	//String svfilename="/Users/cheryl/DevWorkSpace/demo/data/IMEDS/TestComorbidDS/2010-10-10svltrainDS.csv";
    	//String trainDSname="/Users/cheryl/DevWorkSpace/demo/data/IMEDS/TestComorbidDS/trainDS.csv";
    	try {
			CCIcsvTool.SurvivalDataSetParserDoc(svFileName, this.DataPoints);
			CCIcsvTool.SurvivalDataSetFeatureParserDoc(this.targetfileName, this.DataPoints) ;
	    	
			//String rfileName = "/Users/cheryl/DevWorkSpace/demo/data/IMEDS/TestComorbidDS/2010-10-10svltrainDSforR.csv";
		    CCIcsvTool.SurvivalDataSetCreateDoc(rFileName, this.DataPoints, this.ridx_map);	
		   	
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    	
	}
	@Override
	public void oulierGen() {
		File directory = new File(this.lrFolder);
		File[] fList = directory.listFiles();
		for (File file : fList){		
			if (file.isFile() && file.getName().contains(".csv")){
				this.DataPoints = new HashMap<Long,SurvivalTime>();
				this.OutlierList = new HashMap<Long,ArrayList<Double>>();
				this.ridx_map = new HashMap<Integer, Long>();
				String filename = file.getName();		
				String rFilename =  this.olFolder+OSValidator.getPathSep()+filename.substring(0, filename.indexOf("."))+"_R.csv";
				this.outFileName=this.olFolder+OSValidator.getPathSep()+"trainDS_"+filename.substring(0, filename.indexOf("s"))+"_prol.csv";
				init(this.lrFolder+OSValidator.getPathSep()+filename,rFilename);
				
				ArrayList<Double> coxResidual= new ArrayList<Double>();
				ArrayList<Double> coxPredict = new ArrayList<Double>();
				try {
					ImedR.getCoxOutliler(rFilename,coxResidual,coxPredict);
					for(int i=0;i<coxResidual.size();i++){
						
						ArrayList<Double> arr= new ArrayList<Double>();
						arr.add(this.DataPoints.get(ridx_map.get(i)).getfailed()); //original label						
						arr.add(coxPredict.get(i));		 //predict score
						arr.add(coxResidual.get(i));					 //residual
						
						this.OutlierList.put(this.ridx_map.get(i), arr);			
					
						
					}
					CCIcsvTool.OutlierCreateDoc(this.outFileName, this.OutlierList);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
	}
	 

}

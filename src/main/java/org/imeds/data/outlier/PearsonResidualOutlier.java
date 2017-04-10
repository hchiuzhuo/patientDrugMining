package org.imeds.data.outlier;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.imeds.daemon.ComorbidManager;
import org.imeds.daemon.ImedsDaemonConfig;
import org.imeds.data.ComorbidDataSetConfig;
import org.imeds.data.SparkLRDataSetWorker;
import org.imeds.data.SparkLRDataSetWorker.DataPoint;
import org.imeds.db.ImedDB;
import org.imeds.util.CCIcsvTool;
import org.imeds.util.ComorbidDSxmlTool;
import org.imeds.util.OSValidator;
import org.imeds.util.writeException;
import org.la4j.inversion.GaussJordanInverter;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import scala.reflect.internal.Trees.This;

public class PearsonResidualOutlier extends Outlier{
//	private String lrFolder;
//	private String olFolder;
	private List<DataPoint> DataPointList;
	private Map<Long,ArrayList<Double>> OutlierList;
	private Double threshold = 0.0;
	private String outFileName;
	
	private String configFile="";
	private ComorbidDataSetConfig cdsc = new ComorbidDataSetConfig();
	private ComorbidDSxmlTool cfgparser = new ComorbidDSxmlTool();
	private static Logger logger = Logger.getLogger(PearsonResidualOutlier.class);
	public PearsonResidualOutlier() {
		
	}
	public PearsonResidualOutlier(String configpath) {
		this.configFile = configpath;
		this.cfgparser.parserDoc(this.configFile,this.cdsc);
		
		this.lrFolder = this.cdsc.getPearsonResidualOutlierInputFolder();
		if(!OSValidator.isWindows()){this.lrFolder = this.lrFolder.replace("\\", "/");}
		
		this.olFolder = this.cdsc.getPearsonResidualOutlierOutputFolder();
		if(!OSValidator.isWindows()){this.olFolder = this.olFolder.replace("\\", "/");}
		
		this.threshold = this.cdsc.getPearsonResidualThreshold();
		
	}

	public PearsonResidualOutlier(String lrFolder,String olFolder, Double threshold) {
		
		
		this.lrFolder = lrFolder;
		this.olFolder = olFolder;
		this.threshold = threshold;
	
	}

	public void init(String fileName, String outFileName, Double threshold) {
		DataPointList = new ArrayList<DataPoint>();
		OutlierList = new HashMap<Long,ArrayList<Double>>();
		this.outFileName = outFileName;
		this.threshold = threshold;
		CCIcsvTool.LRPredictResultParserDoc(fileName, this.DataPointList);
	
	}
	public List<DataPoint> getDataPointList() {
		return DataPointList;
	}

	public void setDataPointList(List<DataPoint> dataPointList) {
		DataPointList = dataPointList;
	}


	public Map<Long, ArrayList<Double>> getOutlierList() {
		return OutlierList;
	}

	public void setOutlierList(Map<Long, ArrayList<Double>> outlierList) {
		OutlierList = outlierList;
	}

	@Override
	public void oulierGen() {
		File directory = new File(this.lrFolder);
		File[] fList = directory.listFiles();
		for (File file : fList){		
			if (file.isFile()){
				String filename = file.getName();		
				
				init(this.lrFolder+OSValidator.getPathSep()+filename, this.olFolder+OSValidator.getPathSep()+filename.substring(0, filename.indexOf("."))+"_prol.csv", this.threshold);
				Matrix hMatrix = calXtVX(getDataPointList());
				
				for(DataPoint dl:this.DataPointList){
					Double Ri = isOutlier(hMatrix, dl);
					ArrayList<Double> arr= new ArrayList<Double>();
					arr.add(dl.getTrainP().label()); //original label
					arr.add(dl.getPredictP());		 //predict score
					arr.add(Ri);					 //residual
					
					if(Ri>=this.threshold){
						this.OutlierList.put(dl.getId(), arr);			
					}						
				}
				CCIcsvTool.OutlierCreateDoc(this.outFileName,  (HashMap<Long, ArrayList<Double>>) this.OutlierList);
			}
		}
		
	}
	
	public static Double isOutlier(Matrix hMatrix,DataPoint v1){
		List<DataPoint> dl = new ArrayList<DataPoint>();
		dl.add(v1);
		Basic2DMatrix Xi = formMx(dl);
		Matrix Mi = Xi.multiply(hMatrix).multiply(Xi.transpose());
		Double Pi = v1.getPredictP();
		Double Hi = Pi*(1-Pi)*Mi.get(0, 0);
//		if(Hi>1)System.out.println("Hi: "+Hi);
		
		Double Yi = v1.getTrainP().label();
		Double Ri = (Yi-Pi)/Math.pow((Pi*(1-Pi)*(1-Hi)),0.5);
		Ri = Math.abs(Ri);
		return Ri;
	}


	 public static  Matrix calXtVX(List<DataPoint> DataPoint){
		 
		  Basic2DMatrix mX = formMx(DataPoint);
		  Basic2DMatrix mV = formMv(DataPoint);
		  Matrix XtVX = mX.transpose().multiply(mV).multiply(mX);
		  GaussJordanInverter GJ = new GaussJordanInverter(XtVX);
		  try{
				XtVX =  GJ.inverse();
//				System.out.println("XVXinverse\n"+GJ.inverse().toString());
			}catch(Exception e){
				logger.error("fail to cal Inverse matrix. "+writeException.toString(e));
				e.printStackTrace();
			}
//		  System.out.println("Verify\n"+XtVX.multiply(GJ.inverse()));
		  
		  return XtVX;
	  }

	  public static  Basic2DMatrix formMx(List<DataPoint> DataPoint){
		  
		  int rowN = DataPoint.size();
		  int colN = DataPoint.get(0).getTrainP().features().size()+1;
		  Basic2DMatrix  mX = new Basic2DMatrix(rowN,colN);
		  
		  for (int i = 0; i < rowN; i++) {
			  mX.set(i,0,DataPoint.get(i).getTrainP().label());
			  double[] arr = DataPoint.get(i).getTrainP().features().toArray();
			  for(int j=1; j< colN; j++){
				  mX.set(i, j, arr[j-1]);
			  }
		  }
		  
		 
//		  System.out.println("X\n"+mX.toString());
		  return mX;
	  }
	  public static Basic2DMatrix formMv(List<DataPoint> DataPoint){
		  int rowN = DataPoint.size();
		  int colN = rowN;
		  Basic2DMatrix  mV = new Basic2DMatrix(rowN,colN);
		  double minVal=0;
		  for (int i = 0; i < rowN; i++) {	
			  minVal = DataPoint.get(i).getPredictP();
			  for(int j=0; j< colN; j++){
				  if(i==j){
					  mV.set(i, j, minVal*(1-minVal));
//					  System.out.println(minVal*(1-minVal));
				  }else mV.set(i, j, 0);
			  }
		  }
		 
//		  System.out.println("V\n"+mV.toString());
		  return mV;
	  }
	  
//	  public void writeOulierToDB() {
//			File directory = new File(this.olFolder);
//			File[] fList = directory.listFiles();
//			for (File file : fList){		
//				if (file.isFile()){
//					
//					String filename = file.getName();		
//					String totalPath = this.olFolder+OSValidator.getPathSep()+file.getName();
//					try {
//						Integer fileId = ImedDB.getOutlierFileId(totalPath);
//						this.logger.info("prepare to write <file Id "+fileId+"> "+totalPath+" into DB.");
//						CCIcsvTool.OutlierParserDoc(totalPath, ImedsDaemonConfig.getFlush(), fileId, this.logger);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						//e.printStackTrace();
//						this.logger.error("fail to write outlier file "+totalPath+"\n"+writeException.toString(e));
//						
//					}
////					init(this.lrFolder+OSValidator.getPathSep()+filename, this.olFolder+OSValidator.getPathSep()+filename.substring(0, filename.indexOf("."))+"_prol.csv", this.threshold);
//					
////					CCIcsvTool.OutlierCreateDoc(this.outFileName,  (HashMap<Long, ArrayList<Double>>) this.OutlierList);
//				}
//			}
//			
//		}  
	public static void main(String[] args) {
//		PearsonResidualOutlier prlo = new PearsonResidualOutlier("data\\IMEDS\\DiabeteComorbidDS\\trainDSf_300_1.0.csv","data\\IMEDS\\DiabeteComorbidDS\\trainDSf_300_1.0_ol.csv", -1.0);
		PearsonResidualOutlier prlo = new PearsonResidualOutlier();
		if(args.length ==1){
			 prlo = new PearsonResidualOutlier(args[0]);
			 
	    }else if(args.length==3){
	    	 prlo = new PearsonResidualOutlier(args[0],args[1],Double.parseDouble(args[2]));
	    }else{
	      System.err.println("Usage: PearsonResidualOutlier <DSConfig_path> <output_dir> <threshold>");
	      System.exit(1);
	    }
    
		prlo.oulierGen();
//		System.out.println(prlo.getOutlierList().toString());
	}
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	
}

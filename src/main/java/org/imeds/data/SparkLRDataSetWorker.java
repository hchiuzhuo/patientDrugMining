/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.imeds.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.imeds.data.outlier.PearsonResidualOutlier;
import org.imeds.util.CCIcsvTool;
import org.imeds.util.ComorbidDSxmlTool;
import org.imeds.util.ImedDateFormat;
import org.imeds.util.OSValidator;
import org.la4j.matrix.Matrix;

import scala.Tuple2;


/**
 * Purpose: find out outlier set which classified by Logistic regression
 * Logistic regression based classification using ML Lib.
 */
public class SparkLRDataSetWorker extends Worker implements Serializable {

	private static final long serialVersionUID = 7572888959583425286L;
	private static final int idIdx		= 0; //id column
	private static final int labelIdx	= 1; //label column
	private static final int ftStartIdx = 2; //feature start column
	private transient JavaSparkContext sc;
	private String  resIn;
	private Double  stepSize;
	private Integer iterations;
	private Double  threshold;
	private Double  chiSqrtThreshold;
	private List<DataPoint> result;
	

	private String configFile="";
	private transient ComorbidDataSetConfig cdsc = new ComorbidDataSetConfig();
	private transient ComorbidDSxmlTool cfgparser = new ComorbidDSxmlTool();
	public String getResIn() {
		return resIn;
	}

	public void setResIn(String resIn) {
		this.resIn = resIn;
	}

	public Double getStepSize() {
		return stepSize;
	}

	public void setStepSize(Double stepSize) {
		this.stepSize = stepSize;
	}

	public Integer getIterations() {
		return iterations;
	}

	public void setIterations(Integer iterations) {
		this.iterations = iterations;
	}

	public Double getThreshold() {
		return threshold;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}
	
	public Double getChiSqrtThreshold() {
		return chiSqrtThreshold;
	}

	public void setChiSqrtThreshold(Double chiSqrtThreshold) {
		this.chiSqrtThreshold = chiSqrtThreshold;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public ComorbidDataSetConfig getCdsc() {
		return cdsc;
	}

	public void setCdsc(ComorbidDataSetConfig cdsc) {
		this.cdsc = cdsc;
	}

	public ComorbidDSxmlTool getCfgparser() {
		return cfgparser;
	}

	public void setCfgparser(ComorbidDSxmlTool cfgparser) {
		this.cfgparser = cfgparser;
	}
  public static class DataPoint implements Serializable {
	    long id;
	  	LabeledPoint trainP;
	    double predictP;
	    public DataPoint(long id, LabeledPoint trainP, double predictP) {
	      this.id = id;
	      this.trainP = trainP;
	      this.predictP = predictP;
	    }
	    DataPoint(long id, LabeledPoint trainP){
	    	this.id = id;
	    	this.trainP = trainP;
	    }
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public LabeledPoint getTrainP() {
			return trainP;
		}


		public void setTrainP(LabeledPoint trainP) {
			this.trainP = trainP;
		}


		public double getPredictP() {
			return predictP;
		}


		public void setPredictP(double predictP) {
			this.predictP = predictP;
		}
	    
	   
		public String toString(){
	    	String fe = trainP.features().toString();
	    	fe = fe.replace("[", "\"");
	    	fe = fe.replace("]", "\"");
	    	return id+","+trainP.label()+","+fe+","+predictP;
	    }
  }
  static class ParsePredictPoint implements Function<String, DataPoint> {
	    private static final Pattern COMMA = Pattern.compile(",");
	    public DataPoint call(String line) {
	      String[] parts = COMMA.split(line);
	      long 	 id = Long.parseLong(parts[idIdx]);
	      double label = Double.parseDouble(parts[labelIdx]);	     
	      double[] feature = new double[parts.length-ftStartIdx];
	      for (int i =0; i < feature.length; ++i) {
	        feature[i] = Double.parseDouble(parts[i+ ftStartIdx]);
	      }
	      return new DataPoint(id,new LabeledPoint(label, Vectors.dense(feature)));
	    }
  }
  static class ParsePoint implements Function<String, LabeledPoint> {
    private static final Pattern COMMA = Pattern.compile(",");
    private static final Pattern SPACE = Pattern.compile(" ");

    public LabeledPoint call(String line) {
      String[] parts = COMMA.split(line);
      double label = Double.parseDouble(parts[labelIdx]);
      double[] feature = new double[parts.length];
      for (int i = ftStartIdx; i < parts.length; ++i) {
        feature[i] = Double.parseDouble(parts[i]);
      }
      return new LabeledPoint(label, Vectors.dense(feature));
    }
  }
  
 
	@Override
  public void prepare() {
		SparkAppInit();
	}
	
	@Override
  public void ready() {
		  JavaRDD<String> lines = sc.textFile(this.resIn);
		  JavaRDD<DataPoint> points = lines.map(new ParsePredictPoint()).cache();
		  JavaRDD<LabeledPoint> pointsForModel = points.map(
				  new Function<DataPoint,LabeledPoint>(){			  		
						public LabeledPoint call(DataPoint v1) throws Exception {
				  			return v1.getTrainP();
				  		}			      	
				  }
		  );
		
		final LogisticRegressionModel model = LRmodelBuild(pointsForModel);
		result =LRpredict(model, points);
	//	System.out.println("result "+result.toString());
		try {			
			if(this.getCdsc()!=null && this.getCdsc().getPearsonResidualOutlierInputFolder().trim()!=null){			
				ToFile(result,this.getCdsc().getPearsonResidualOutlierInputFolder()+this.resIn.substring(this.resIn.lastIndexOf("/"), this.resIn.indexOf("."))+"_"+this.iterations+"_"+this.stepSize+".csv");				
			}else{
				ToFile(result,this.resIn.substring(0, this.resIn.indexOf("."))+"_"+this.iterations+"_"+this.stepSize+".csv");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
  public void go() {
		//because pearson outlier may induce memory heap problem, i use standardize residual instead.
		//memory heap is generated from matrix inverse.
//		PROutlier();
		stdRi();
	}
  
	@Override
  public void done() {
		// TODO Auto-generated method stub
		SparkAppStop();
	}
  public  void paraInit(String confPath){
	  String[] paras = confPath.split(",");
	  
	  this.resIn 		= paras[0];
	  this.stepSize		= Double.parseDouble(paras[1]);
	  this.iterations 	= Integer.parseInt(paras[2]);
	  this.threshold	= Double.parseDouble(paras[3]);
	  this.chiSqrtThreshold=Double.parseDouble(paras[4]);
  }
  
  public void SparkAppInit(){
	  SparkConf sparkConf = new SparkConf().setAppName(this.getClass().getName());
	  this.sc = new JavaSparkContext(sparkConf);
  }
  public void SparkAppStop(){
	  this.sc.stop();
  }
 
  
  public LogisticRegressionModel LRmodelBuild(JavaRDD<LabeledPoint> pointsForModel){

	  LogisticRegressionWithSGD lr = new LogisticRegressionWithSGD();
      lr.optimizer().setNumIterations(iterations)
                   .setStepSize(stepSize)
                   .setMiniBatchFraction(1.0);
      lr.setIntercept(true);
      final LogisticRegressionModel model = lr.train(pointsForModel.rdd(),  iterations, stepSize);     
      return model;
      
  }
 
  public  List<DataPoint> LRpredict(final LogisticRegressionModel model,JavaRDD<DataPoint> points){
//	  System.out.println("Final w: " + model.weights()+" b: "+model.intercept());
	  model.clearThreshold();
      //predicting each point
      JavaRDD<DataPoint> predictPoints = points.map(new Function<DataPoint,DataPoint>(){
    	
		public DataPoint call(DataPoint v1) throws Exception {
		
			v1.setPredictP(model.predict(v1.getTrainP().features()));
//			System.out.println(v1.getId()+","+v1.getTrainP().label()+","+model.predict(v1.getTrainP().features()));
			return v1;
		}      	
      });
      
      
      List<DataPoint> result = predictPoints.collect();
      return result;
     
  }
  public static void ToFile(List<DataPoint> result, String fileName) throws IOException{
	  
		if(!OSValidator.isWindows()){fileName = fileName.replace("\\", "/");}
		
	
	  FileWriter fstream = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write("Id,Label,Feature,Prediction");
      out.newLine();
      for(DataPoint dp: result){
    	  out.write(dp.toString());
    	  out.newLine();    	  
      }

      //Close the output stream
      out.close();    
  }
  public void chi_sqrt_test(String fileName, Double chi_sqrt, Double chi_sqrt_threshold) throws IOException{
	  String chi_sqrt_static=this.getCdsc().getPearsonResidualOutlierOutputFolder()+OSValidator.getPathSep()+"chi_sqrt_test.txt";
	  if(!OSValidator.isWindows()){
		  chi_sqrt_static = chi_sqrt_static.replace("\\", "/");
		  fileName = fileName.replace("\\", "/");
		 }
		
	  FileWriter fstream = new FileWriter(chi_sqrt_static, true);
      BufferedWriter out = new BufferedWriter(fstream);
      if(chi_sqrt<= chi_sqrt_threshold) out.write(ImedDateFormat.formatTime(new Date())+" "+fileName+ " "+chi_sqrt+ " <= "+chi_sqrt_threshold);
      else out.write(ImedDateFormat.formatTime(new Date())+" "+fileName+ " "+chi_sqrt+ " > "+chi_sqrt_threshold+" No outlier output generated");
      out.newLine();
      //Close the output stream
      out.close();   
  }
  public void stdRi(){
	  JavaRDD<DataPoint> predictPoints =  sc.parallelize(result) ;
		
	    JavaPairRDD<Long, ArrayList<Double>> pearsonPoints = predictPoints.mapToPair(new PairFunction<DataPoint, Long, ArrayList<Double>>(){
			public Tuple2<Long, ArrayList<Double>>  call(DataPoint v1) throws Exception {
				
				ArrayList<Double> arr= new ArrayList<Double>();
				arr.add(v1.getTrainP().label()); //original label
				arr.add(v1.getPredictP());		 //predict score
				
				Double Pi = v1.getPredictP();
				Double Yi = v1.getTrainP().label();
				if(Pi==0)Pi=0.000001;
				else if(Pi==1) Pi=0.99999;
				Double Ri = (Yi-Pi)/Math.pow((Pi*(1-Pi)),0.5);
				Ri = Math.abs(Ri);
				arr.add(Ri);					 //residual
				
				return new Tuple2<Long, ArrayList<Double>>(v1.getId(),arr);
			  }    	  
	    	 }
		).filter(new Function<Tuple2<Long,ArrayList<Double>>,Boolean>(){

			public Boolean call(Tuple2<Long, ArrayList<Double>> v1) throws Exception {
				if(v1._2.get(2) > getThreshold())return true;
				return false;
			}			
		});
	   
	    
	    double chi_sqrt= pearsonPoints.map(new Function<Tuple2<Long, ArrayList<Double>>,Double>() {

			public Double call(Tuple2<Long, ArrayList<Double>> v)
					throws Exception {
				// TODO Auto-generated method stub
				//System.out.println(v._1+" : "+v._2.get(2)+ " pow: "+Math.pow(v._2.get(2), 2.0));
				return Math.pow(v._2.get(2), 2.0);
			}	  
	      }).reduce(new Function2<Double, Double, Double>(){

			public Double call(Double arg0, Double arg1) throws Exception {
				// TODO Auto-generated method stub
				//System.out.println("pow("+arg0+",2.0)="+Math.pow(arg0, 2.0)+" "+"pow("+arg1+",2.0)="+Math.pow(arg1, 2.0));
				return (arg0+arg1);
			}
	    	  
	      });
	   // System.out.println("chi_sqrt "+chi_sqrt);
	  String fileName =this.getCdsc().getPearsonResidualOutlierOutputFolder()+this.resIn.substring(this.resIn.lastIndexOf("/"), this.resIn.indexOf("."))+"_"+this.iterations+"_"+this.stepSize.intValue()+"_prol.csv";
	  if(!OSValidator.isWindows()){fileName = fileName.replace("\\", "/");}  
	    
	  try {
		chi_sqrt_test(fileName, chi_sqrt, this.chiSqrtThreshold);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  if(chi_sqrt <= this.chiSqrtThreshold){		    
		  Map<Long,ArrayList<Double>> pearsonOL = pearsonPoints.collectAsMap();
		 		
		  CCIcsvTool.OutlierCreateDoc(fileName,  pearsonOL);
	  }
	 
  }
  public void PROutlier(){
	  JavaRDD<DataPoint> predictPoints =  sc.parallelize(result) ;
		final Matrix hMatrix =  PearsonResidualOutlier.calXtVX(result);
	    
	    JavaPairRDD<Long, ArrayList<Double>> pearsonPoints = predictPoints.mapToPair(new PairFunction<DataPoint, Long, ArrayList<Double>>(){
			public Tuple2<Long, ArrayList<Double>>  call(DataPoint v1) throws Exception {
				Double Ri = PearsonResidualOutlier.isOutlier(hMatrix,v1);
				ArrayList<Double> arr= new ArrayList<Double>();
				arr.add(v1.getTrainP().label()); //original label
				arr.add(v1.getPredictP());		 //predict score
				arr.add(Ri);					 //residual
				
				return new Tuple2<Long, ArrayList<Double>>(v1.getId(),arr);
			  }    	  
	    	 }
		).filter(new Function<Tuple2<Long,ArrayList<Double>>,Boolean>(){

			public Boolean call(Tuple2<Long, ArrayList<Double>> v1) throws Exception {
				if(v1._2.get(2) > getThreshold())return true;
				return false;
			}			
		});
	  Map<Long,ArrayList<Double>> pearsonOL = pearsonPoints.collectAsMap();
	     
	 
	  String fileName =this.resIn.substring(this.resIn.lastIndexOf("/"), this.resIn.indexOf("."))+"_"+this.iterations+"_"+this.stepSize+"_sol.csv";
	  CCIcsvTool.OutlierCreateDoc(this.getCdsc().getPearsonResidualOutlierOutputFolder()+fileName,  pearsonOL);
	 
  }

  public static void main(String[] args) {
    SparkLRDataSetWorker og = new SparkLRDataSetWorker();
    og.prepare();
    if(args.length==4){
    	og.paraInit(args[0]+","+args[1]+","+args[2]+","+args[3]);    	
        og.ready();
        if(og.getThreshold()>=0) og.go();
       
    }else if(args.length==1){
    	og.setConfigFile(args[0]);
    	og.getCfgparser().parserDoc(og.getConfigFile(),og.getCdsc());
    	for(String input_file_path:og.getCdsc().getSparkLRmodelDataSets()){
    		for(String paras:og.getCdsc().getSparkLRmodelParas()){
    			String[] para = paras.split(",");
    			og.paraInit(input_file_path+","+para[0]+","+para[1]+","+og.getCdsc().getPearsonResidualThreshold()+","+og.getCdsc().getChiSqrtThreshold());    	    	
    	        og.ready();
    	        if(og.getThreshold()>=0) og.go();
    	       
    		}
    	}
    }
    
    og.done();
    
    

   // System.out.println("outlier test done!");

  }




}

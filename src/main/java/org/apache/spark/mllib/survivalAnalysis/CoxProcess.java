package org.apache.spark.mllib.survivalAnalysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.DoubleFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.survivalAnalysis.CoxRegression.tempV;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.imeds.util.TimeDiff;

import scala.Tuple2;

public class CoxProcess implements Serializable {

	private static final long serialVersionUID = 278501432972379859L;
	private String cpCfgFilePath;
	private transient CoxProcessConfig cpCfg = new CoxProcessConfig();	
	private transient FileTool fileParser = new FileTool();
	
	private transient JavaSparkContext sc;
	private transient SQLContext sqlContext; 
	private static final Logger logger = Logger.getLogger(CoxRegression.class);
	public static final int TRAIN=01;
	public static final int TEST=02;

	public CoxProcess(String cpCfgFilePath) {
		super();
		this.cpCfgFilePath = cpCfgFilePath;
	}

	public void prepare(){
		try {
			this.fileParser.parserDoc(this.cpCfgFilePath, this.cpCfg);
			logger.info("Config file"+this.cpCfg.toString());
			SparkAppInit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void ready(){
		ArrayList<String> schemaString = this.fileParser.parserParseComma(this.cpCfg.getSchemaPath());
		loadDataset(schemaString);
		
		Long foldIdx=(long) 0;
		int modK=this.cpCfg.getKfold();		
		if(this.cpCfg.getEnableKfold()){
			
			int iterN=this.cpCfg.getKfold();
			while(foldIdx<iterN){
				logger.info("iter: "+iterN+", foldIdx: "+foldIdx);
//				logger.info("TRAIN: ");
				JavaPairRDD<Double, SurvivalLabelPoint> trainDataSetReady= prepareDataset(schemaString,TRAIN,foldIdx, modK);
//				countSurvivalLabelPoint(trainDataSetReady);
//				logger.info("TEST: ");
				JavaPairRDD<Double, SurvivalLabelPoint> testDataSetReady= prepareDataset(schemaString,TEST,foldIdx, modK);
//				countSurvivalLabelPoint(testDataSetReady);
				runCoxRegressionForConfigs(trainDataSetReady,testDataSetReady);
				foldIdx=foldIdx+1;
			}
		}else{		
			JavaPairRDD<Double, SurvivalLabelPoint> trainDataSetReady= prepareDataset(schemaString);
			JavaPairRDD<Double, SurvivalLabelPoint> testDataSetReady= prepareDataset(schemaString);
			runCoxRegressionForConfigs(trainDataSetReady,testDataSetReady);
		}
		
//		for(CoxRegressionConfig crcfg:this.cpCfg.getAlgCfgList()){
//			crcfg.setDim(this.cpCfg.getDim());		
//			CoxRegression coxRegression = new CoxRegression(crcfg,DataSetReady);
//			//model train
//			CoxModel cmdl = coxRegression.fit(this.cpCfg.getEnablePvalue());
//			
//			//form model result
//			cmdl.setX_varList(this.cpCfg.getX_varList());
//			
//			//model predict
//			cmdl.setC_statistic(coxRegression.c_statistic(cmdl.getCoef(),DataSetReady));
//			formCoxStats(cmdl, DataSetReady);
//			
//			//output result to file
//			this.fileParser.writeCoxModel(this.cpCfg.getModelOutputPath(), cmdl,this.cpCfg.getEnablePvalue());
//			this.fileParser.writeCoxStat(this.cpCfg.getStatsSummaryPath(), cmdl);									
//		}
	}
	public void runCoxRegressionForConfigs(JavaPairRDD<Double, SurvivalLabelPoint> trainDataSet,JavaPairRDD<Double, SurvivalLabelPoint> testDataSet){
		for(CoxRegressionConfig crcfg:this.cpCfg.getAlgCfgList()){
			
			crcfg.setDim(this.cpCfg.getDim());					
			CoxRegression coxRegression = new CoxRegression(crcfg,trainDataSet);
			//model train
			CoxModel cmdl = coxRegression.fit(this.cpCfg.getEnablePvalue());
			
			//form model result
			cmdl.setX_varList(this.cpCfg.getX_varList());
			
			//model predict
			cmdl.setC_statistic(coxRegression.c_statistic(cmdl.getCoef(),testDataSet));
			formCoxStats(cmdl, trainDataSet);
			
			//output result to file
			this.fileParser.writeCoxModel(this.cpCfg.getModelOutputPath(), cmdl,this.cpCfg.getEnablePvalue());
			this.fileParser.writeCoxStat(this.cpCfg.getStatsSummaryPath(), cmdl);									
		}
	}
	
	public void runCoxRegressionForConfigs(){
		ArrayList<String> schemaString = this.fileParser.parserParseComma(this.cpCfg.getSchemaPath());
		loadDataset(schemaString);
		JavaPairRDD<Double, SurvivalLabelPoint> trainDataSetReady;
		JavaPairRDD<Double, SurvivalLabelPoint> testDataSetReady;
		
		for(CoxRegressionConfig crcfg:this.cpCfg.getAlgCfgList()){
			CoxModel cmdlAll=new CoxModel();
			cmdlAll.setConfigDesp(crcfg.toString());
			
			crcfg.setDim(this.cpCfg.getDim());			
			Long foldIdx=(long) 0;
			int modK=this.cpCfg.getKfold();		
			if(this.cpCfg.getEnableKfold()){
				
				int iterN=this.cpCfg.getKfold();
				while(foldIdx<iterN){
					trainDataSetReady= prepareDataset(schemaString,TRAIN,foldIdx, modK);
					testDataSetReady= prepareDataset(schemaString,TEST,foldIdx, modK);
					
					CoxModel cmdl=getCoxModel(crcfg,trainDataSetReady,testDataSetReady);
					cmdlAll.setC_statistic(cmdlAll.getC_statistic()+cmdl.getC_statistic());
					//output result to file
					this.fileParser.writeCoxModel(this.cpCfg.getModelOutputPath()+"_"+crcfg.getId()+".csv", cmdl,this.cpCfg.getEnablePvalue());
					this.fileParser.writeCoxStat(this.cpCfg.getStatsSummaryPath()+"_"+crcfg.getId()+".csv", cmdl);
					foldIdx=foldIdx+1;
				}
				cmdlAll.setC_statistic(cmdlAll.getC_statistic()/(iterN*1.0));
				this.fileParser.writeCoxStatAgg(this.cpCfg.getStatsSummaryPath()+"_"+crcfg.getId()+".csv", cmdlAll);;
			}else{		
				trainDataSetReady= prepareDataset(schemaString);
				testDataSetReady= prepareDataset(schemaString);
				
				//FIXME:add this will generate the exception:org.apache.spark.SparkException: Kryo serialization failed: Buffer overflow.
//				trainDataSetReady.sortByKey(false).collect();
//				testDataSetReady.sortByKey(false).collect();
				
				CoxModel cmdl=getCoxModel(crcfg,trainDataSetReady,testDataSetReady);
				this.fileParser.writeCoxModel(this.cpCfg.getModelOutputPath()+".csv", cmdl,this.cpCfg.getEnablePvalue());
				this.fileParser.writeCoxStat(this.cpCfg.getStatsSummaryPath()+".csv", cmdl);
			}								
		}
	}
	public CoxModel getCoxModel(CoxRegressionConfig crcfg,JavaPairRDD<Double, SurvivalLabelPoint> trainDataSetReady,JavaPairRDD<Double, SurvivalLabelPoint> testDataSetReady){
		CoxRegression coxRegression = new CoxRegression(crcfg,trainDataSetReady);
		//model train
		CoxModel cmdl = coxRegression.fit(this.cpCfg.getEnablePvalue());
		
		
		//form model result
		cmdl.setX_varList(this.cpCfg.getX_varList());
		
		//model predict
		if(this.cpCfg.getEnableCstats())cmdl.setC_statistic(coxRegression.c_statistic(cmdl.getCoef(),testDataSetReady));
		else cmdl.setC_statistic(0.0);
		formCoxStats(cmdl, trainDataSetReady);
		return cmdl;
	}
	public void countSurvivalLabelPoint( JavaPairRDD<Double, SurvivalLabelPoint> DataSetReady){
		double failed = DataSetReady.filter(new Function<Tuple2<Double, SurvivalLabelPoint>, Boolean>(){
			public Boolean call(Tuple2<Double, SurvivalLabelPoint> arg0)
					throws Exception {
				logger.info("failed: "+arg0._2.getId());
				return (arg0._2.getFailed()==1.0);
			}			
		}).mapToDouble(new DoubleFunction<Tuple2<Double,SurvivalLabelPoint>>(){
			public double call(Tuple2<Double, SurvivalLabelPoint> arg0)
					throws Exception {
				return 1.0;
			}
			
		}).reduce(new Function2<Double, Double,Double>(){
			public Double call(Double arg0, Double arg1) throws Exception {
				return (arg0+arg1);
			}			
		});
		
		double censored = DataSetReady.filter(new Function<Tuple2<Double, SurvivalLabelPoint>, Boolean>(){

			public Boolean call(Tuple2<Double, SurvivalLabelPoint> arg0)
					throws Exception {
//				logger.info("censoreded: "+arg0._2.getId());
				return (arg0._2.getFailed()==0.0);
			}
			
		}).mapToDouble(new DoubleFunction<Tuple2<Double,SurvivalLabelPoint>>(){

			public double call(Tuple2<Double, SurvivalLabelPoint> arg0)
					throws Exception {
				// TODO Auto-generated method stub
				return 1.0;
			}
			
		}).reduce(new Function2<Double, Double,Double>(){

			public Double call(Double arg0, Double arg1) throws Exception {
				// TODO Auto-generated method stub
				return (arg0+arg1);
			}			
		});
		logger.info(" total: "+(failed+censored));
	}
 	public void formCoxStats(CoxModel cmdl, JavaPairRDD<Double, SurvivalLabelPoint> DataSetReady){
		double failed = DataSetReady.filter(new Function<Tuple2<Double, SurvivalLabelPoint>, Boolean>(){
			public Boolean call(Tuple2<Double, SurvivalLabelPoint> arg0)
					throws Exception {
				return (arg0._2.getFailed()==1.0);
			}			
		}).mapToDouble(new DoubleFunction<Tuple2<Double,SurvivalLabelPoint>>(){
			public double call(Tuple2<Double, SurvivalLabelPoint> arg0)
					throws Exception {
				return 1.0;
			}
			
		}).reduce(new Function2<Double, Double,Double>(){
			public Double call(Double arg0, Double arg1) throws Exception {
				return (arg0+arg1);
			}			
		});
		
		double censored = DataSetReady.filter(new Function<Tuple2<Double, SurvivalLabelPoint>, Boolean>(){

			public Boolean call(Tuple2<Double, SurvivalLabelPoint> arg0)
					throws Exception {
				return (arg0._2.getFailed()==0.0);
			}
			
		}).mapToDouble(new DoubleFunction<Tuple2<Double,SurvivalLabelPoint>>(){

			public double call(Tuple2<Double, SurvivalLabelPoint> arg0)
					throws Exception {
				// TODO Auto-generated method stub
				return 1.0;
			}
			
		}).reduce(new Function2<Double, Double,Double>(){

			public Double call(Double arg0, Double arg1) throws Exception {
				// TODO Auto-generated method stub
				return (arg0+arg1);
			}			
		});
		
		cmdl.setCensoredN(censored);
		cmdl.setFailedN(failed);
		cmdl.setTotalN((censored+failed));
		cmdl.setDataFilePath(this.cpCfg.getDatasetPath());
		cmdl.setModelFilePath(this.cpCfg.getModelOutputPath());
		cmdl.setConfigFilePath(this.cpCfgFilePath);
		
		
	}
	public void go(){
		runCoxRegressionForConfigs();
	}
	
	public void done(){
		SparkAppStop();
	}
	
	public void SparkAppInit(){
		SparkConf sparkConf = new SparkConf().setAppName(this.getClass().getName());
		this.sc = new JavaSparkContext(sparkConf);
		this.sqlContext = new SQLContext(this.sc);
	}
	public void SparkAppStop(){
		this.sc.stop();
		
	}

    static class ParsePointKeyPair implements PairFunction<Row, Double, SurvivalLabelPoint> {

	  public Tuple2<Double, SurvivalLabelPoint>  call(Row row) {
		
				  Long id   =Long.parseLong(row.getString(0));
				  double label =Double.parseDouble(row.getString(1).trim());
			      double failed=Double.parseDouble(row.getString(2).trim());
			     
			      double[] feature = new double[row.length()-3];
			      for(int i=0;i<row.length()-3;i++){
		//	    	  System.out.println(row.getString(i+2).trim()+","+Double.parseDouble(row.getString(i+2).trim()));
			    	  feature[i]=Double.parseDouble(row.getString(i+3).trim());
			      }
			      SurvivalLabelPoint slpt = new SurvivalLabelPoint(id,failed, label, Vectors.dense(feature));
		//	      System.out.println(slpt.toString());
			      
			      return new Tuple2<Double, SurvivalLabelPoint>(label,slpt);
			 
	    }
	}
	/* CORRECT BASE VERSION
	public static void getDerivation(JavaPairRDD<Tuple2<Double,  tempV>, Tuple2<Double, tempV>> riskSet,Double[] deri){
		Double BetaFirstDeri=0.0, BetaSecondDeri=1.0;
		//Calculate BetaFirstDri
		//Cal sumXi
	    Double sumXi= riskSet.mapToPair(new PairFunction<Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>>,Double, Double>(){
			public Tuple2<Double, Double> call(
					Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>> t){	
				
				return new Tuple2<Double, Double>(t._1._1, t._1._2.xi);
			}
		}).distinct().mapToDouble(new DoubleFunction<Tuple2<Double,Double>>(){
			public double call(Tuple2<Double, Double> arg0)
					throws Exception {
				return arg0._2;
			}					
		}).reduce(new Function2<Double, Double, Double>() {
	        public Double call(Double d1, Double d2) {
	          return d1+d2;
	        }
	      });
	    
	   //Cal Riskset Weight
	    JavaPairRDD<Double, tempV> riskSetExpSumTempV=riskSet.mapToPair(new PairFunction<Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>>,Double, tempV>(){
			public Tuple2<Double, tempV> call(
					Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>> t){							
				return new Tuple2<Double, tempV>(t._1._1, t._2._2);
			}
		});
//		List<Tuple2<Double, tempV>> ts=riskSetExpSumTempV.collect();
//		for(Tuple2<Double, tempV> tt:ts)System.out.println("--"+tt);
	    
	    JavaPairRDD<Double, tempV> riskSetSumTempV= riskSetExpSumTempV.reduceByKey(new Function2<tempV,tempV,tempV>(){
	        public tempV call(tempV i1, tempV i2) {
	        	tempV tv = new tempV();
//				tv.xi=i1.xi+i2.xi;
				tv.exp=i1.exp+i2.exp;
				tv.xexp=i1.xexp+i2.xexp;
				tv.x2exp=i1.x2exp+i2.x2exp;
				return tv;
	        }	    			
	    });				
//		List<Tuple2<Double, tempV>> riskSetExpSumresultTempV= riskSetSumTempV.collect();
//	    for(Tuple2<Double, tempV> p:riskSetExpSumresultTempV){
//	    	System.out.println("--"+p);
//	    }
	    
	    //Cal firstRiskSet
	    Double firstRiskSet= riskSetSumTempV.mapToDouble(new DoubleFunction<Tuple2<Double,tempV>>(){
			public double call(Tuple2<Double, tempV> tp) throws Exception {
				// TODO Auto-generated method stub				
				return (tp._2.xexp/tp._2.exp);
			}	    	
	    }).reduce(new Function2<Double,Double,Double>(){
			public Double call(Double arg0, Double arg1) throws Exception {
				// TODO Auto-generated method stub
				return arg0+arg1;
			}	    	
	    });	    
		BetaFirstDeri=-sumXi+firstRiskSet;
		
		
		//Cal BetaSecondDeri
		Double xSqrtRiskSet= riskSetSumTempV.mapToDouble(new DoubleFunction<Tuple2<Double,tempV>>(){
			public double call(Tuple2<Double, tempV> tp) throws Exception {
				// TODO Auto-generated method stub				
				return (tp._2.x2exp/tp._2.exp);
			}	    	
	    }).reduce(new Function2<Double,Double,Double>(){
			public Double call(Double arg0, Double arg1) throws Exception {
				// TODO Auto-generated method stub
				return arg0+arg1;
			}	    	
	    });	    
		Double firstRiskSetSqrt= riskSetSumTempV.mapToDouble(new DoubleFunction<Tuple2<Double,tempV>>(){
				public double call(Tuple2<Double, tempV> tp) throws Exception {
					// TODO Auto-generated method stub				
					return Math.pow((tp._2.xexp/tp._2.exp),2);
				}	    	
		    }).reduce(new Function2<Double,Double,Double>(){
				public Double call(Double arg0, Double arg1) throws Exception {
					// TODO Auto-generated method stub
					return arg0+arg1;
				}	    	
		    });	    
		BetaSecondDeri=xSqrtRiskSet-firstRiskSetSqrt;
	
		deri[0]=BetaFirstDeri;
		deri[1]=BetaSecondDeri;
//		logger.info("----deri: "+Arrays.toString(deri));
	}

	public static double getUpdateValue(JavaPairRDD<Tuple2<Double,  tempV>, Tuple2<Double, tempV>> riskSet,String regularMethod, double betaj, double regularSigma){
		
		double BetaFirstDeri=0, BetaSecondDeri=1;
		//Calculate BetaFirstDri
		//Cal sumXi
	    Double sumXi= riskSet.mapToPair(new PairFunction<Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>>,Double, Double>(){
			public Tuple2<Double, Double> call(
					Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>> t){	
				
				return new Tuple2<Double, Double>(t._1._1, t._1._2.xi);
			}
		}).distinct().mapToDouble(new DoubleFunction<Tuple2<Double,Double>>(){
			public double call(Tuple2<Double, Double> arg0)
					throws Exception {
				return arg0._2;
			}					
		}).reduce(new Function2<Double, Double, Double>() {
	        public Double call(Double d1, Double d2) {
	          return d1+d2;
	        }
	      });
	    
	   //Cal Riskset Weight
	    JavaPairRDD<Double, tempV> riskSetExpSumTempV=riskSet.mapToPair(new PairFunction<Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>>,Double, tempV>(){
			public Tuple2<Double, tempV> call(
					Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>> t){							
				return new Tuple2<Double, tempV>(t._1._1, t._2._2);
			}
		});
//		List<Tuple2<Double, tempV>> ts=riskSetExpSumTempV.collect();
//		for(Tuple2<Double, tempV> tt:ts)System.out.println("--"+tt);
	    
	    JavaPairRDD<Double, tempV> riskSetSumTempV= riskSetExpSumTempV.reduceByKey(new Function2<tempV,tempV,tempV>(){
	        public tempV call(tempV i1, tempV i2) {
	        	tempV tv = new tempV();
//				tv.xi=i1.xi+i2.xi;
				tv.exp=i1.exp+i2.exp;
				tv.xexp=i1.xexp+i2.xexp;
				tv.x2exp=i1.x2exp+i2.x2exp;
				return tv;
	        }	    			
	    });				
//		List<Tuple2<Double, tempV>> riskSetExpSumresultTempV= riskSetSumTempV.collect();
//	    for(Tuple2<Double, tempV> p:riskSetExpSumresultTempV){
//	    	System.out.println("--"+p);
//	    }
	    
	    //Cal firstRiskSet
	    Double firstRiskSet= riskSetSumTempV.mapToDouble(new DoubleFunction<Tuple2<Double,tempV>>(){
			public double call(Tuple2<Double, tempV> tp) throws Exception {
				// TODO Auto-generated method stub				
				return (tp._2.xexp/tp._2.exp);
			}	    	
	    }).reduce(new Function2<Double,Double,Double>(){
			public Double call(Double arg0, Double arg1) throws Exception {
				// TODO Auto-generated method stub
				return arg0+arg1;
			}	    	
	    });	    
		BetaFirstDeri=-sumXi+firstRiskSet;
		
		
		//Cal BetaSecondDeri
		Double xSqrtRiskSet= riskSetSumTempV.mapToDouble(new DoubleFunction<Tuple2<Double,tempV>>(){
			public double call(Tuple2<Double, tempV> tp) throws Exception {
				// TODO Auto-generated method stub				
				return (tp._2.x2exp/tp._2.exp);
			}	    	
	    }).reduce(new Function2<Double,Double,Double>(){
			public Double call(Double arg0, Double arg1) throws Exception {
				// TODO Auto-generated method stub
				return arg0+arg1;
			}	    	
	    });	    
		Double firstRiskSetSqrt= riskSetSumTempV.mapToDouble(new DoubleFunction<Tuple2<Double,tempV>>(){
				public double call(Tuple2<Double, tempV> tp) throws Exception {
					// TODO Auto-generated method stub				
					return Math.pow((tp._2.xexp/tp._2.exp),2);
				}	    	
		    }).reduce(new Function2<Double,Double,Double>(){
				public Double call(Double arg0, Double arg1) throws Exception {
					// TODO Auto-generated method stub
					return arg0+arg1;
				}	    	
		    });	    
		BetaSecondDeri=xSqrtRiskSet-firstRiskSetSqrt;
		double updateValue=0;
		if(regularMethod.equals("NONE")){
			updateValue= (BetaFirstDeri/BetaSecondDeri)*-1.0;
			return updateValue;
		}else if(regularMethod.equals("L2")){
			double tau = Math.pow(regularSigma, 2);
			BetaFirstDeri = BetaFirstDeri+ (betaj/tau);
			BetaSecondDeri = BetaSecondDeri+ (1/tau);
			updateValue= (BetaFirstDeri/BetaSecondDeri)*-1.0;
			return updateValue;
		}else if(regularMethod.equals("L1")){
			double gammaSqrt = Math.sqrt(2.0)/regularSigma;
			if(Math.signum(betaj)==0){
				double tempBetaFirstDeri=0;
				double tempDeltaBeta=0;
				//set signBeta=1
				double sign=1.0;
				tempBetaFirstDeri=BetaFirstDeri+gammaSqrt*sign;
				tempDeltaBeta=(tempBetaFirstDeri/BetaSecondDeri)*-1.0;
				if(Math.signum(tempDeltaBeta)==sign) return tempDeltaBeta;
				else{
					sign=-1.0;
					tempBetaFirstDeri=BetaFirstDeri+gammaSqrt*sign;
					tempDeltaBeta=(tempBetaFirstDeri/BetaSecondDeri)*-1.0;
					if(Math.signum(tempDeltaBeta)==sign) return tempDeltaBeta;
					else return 0.0;					
				}
			}else{
				double tempBetaFirstDeri = BetaFirstDeri+gammaSqrt*(Math.signum(betaj));
				double tempDeltaBeta=(tempBetaFirstDeri/BetaSecondDeri)*-1.0;
				if(Math.signum(betaj) != Math.signum(betaj+tempDeltaBeta)) return betaj*(-1.0);
				else return tempDeltaBeta;
			}
		}
		
		return updateValue;
//		return (BetaFirstDeri/BetaSecondDeri)*-1.0;
		
	}
	*/
	static class getTupleType implements Function<Tuple2<Double,SurvivalLabelPoint>,Boolean> {
		    private int dataType=TRAIN;
		    private long foldK=0;
		    private long modK=1;

		    getTupleType(int dataType,Long foldK,long modK) {
			    this.dataType=dataType;
			    this.foldK=foldK;
			    this.modK=modK;
			 }


			public Boolean call(Tuple2<Double, SurvivalLabelPoint> t)
					throws Exception {
				if(dataType==TRAIN) return ((t._2.getId()%modK)!=foldK);
				else return ((t._2.getId()%modK)==foldK);
			}
	}
    public JavaPairRDD<Double, SurvivalLabelPoint> prepareDataset(ArrayList<String> schemaString){
	    	
		    String projectCol="HashID,"+this.cpCfg.getY_var()+","+this.cpCfg.getCensored_var()+","+this.cpCfg.getX_varList();
		    DataFrame results = sqlContext.sql("SELECT "+projectCol+" FROM coxDataSet where "+schemaString.get(0)+" <> '"+schemaString.get(0)+"'");
		   
		    return results.toJavaRDD().mapToPair(new ParsePointKeyPair()).cache();

	}
    public JavaPairRDD<Double, SurvivalLabelPoint> prepareDataset(ArrayList<String> schemaString, int dataType,Long foldK, int modK){
    	
	    String projectCol="HashID,"+this.cpCfg.getY_var()+","+this.cpCfg.getCensored_var()+","+this.cpCfg.getX_varList();
//	    DataFrame results=null;
	    String t="";
	    DataFrame results = sqlContext.sql("SELECT "+projectCol+" FROM coxDataSet where "+schemaString.get(0)+" <> '"+schemaString.get(0)+"'");
		 
	    
	    if(dataType==TRAIN){
	    	t="SELECT "+projectCol+" FROM coxDataSet where "+schemaString.get(0)+" <> '"+schemaString.get(0)+"' and (HashID % "+modK+" <> "+ foldK+")" ;
	      results = sqlContext.sql(t);
	    }else if(dataType==TEST){
	    	t="SELECT "+projectCol+" FROM coxDataSet where "+schemaString.get(0)+" <> '"+schemaString.get(0)+"' and (HashID % "+modK+" = "+ foldK+")" ;	 	   
		  results = sqlContext.sql(t);
	    }
	    logger.info(t);
//	    return results.toJavaRDD().mapToPair(new ParsePointKeyPair()).filter(new getTupleType(dataType,foldK,(long)modK)).cache();
	    return results.toJavaRDD().mapToPair(new ParsePointKeyPair()).cache();

    }

    public JavaPairRDD<Double, SurvivalLabelPoint> prepareDataset(){
    	 JavaRDD<String> lines = this.sc.textFile(this.cpCfg.getDatasetPath());//"data/lr-data/people.csv"
    	 JavaPairRDD<Double, SurvivalLabelPoint> rowRDD = lines.filter(new Function<String,Boolean>(){

			public Boolean call(String arg0) throws Exception {
				// TODO Auto-generated method stub
				return (!arg0.contains("Period"));
			}

    	 }
    	 ).mapToPair(new PairFunction<String, Double, SurvivalLabelPoint>() {
    	 

    				  public Tuple2<Double, SurvivalLabelPoint>  call(String row) {
    	 	 	          String[] fields = row.split(",");
    	 	 	         	
    							  Long id   =Math.abs((long)row.hashCode());
    							  double label =Double.parseDouble(fields[1].trim());
    						      double failed=Double.parseDouble(fields[2].trim());
    						     
    						      double[] feature = new double[fields.length-3];
    						      for(int i=3;i<fields.length;i++){
    						    	  feature[i-3]=Double.parseDouble(fields[i].trim());
    						      }
    						      SurvivalLabelPoint slpt = new SurvivalLabelPoint(id,failed, label, Vectors.dense(feature));
    						      
    						      return new Tuple2<Double, SurvivalLabelPoint>(label,slpt);
    						 
    				    }
    				  }	 
 	 	      ).cache();
    	 return rowRDD;
 	    
    }
    public void loadDataset(ArrayList<String> schemaString){
	    // Load a text file and convert each line to a JavaBean.
	    JavaRDD<String> lines = this.sc.textFile(this.cpCfg.getDatasetPath());//"data/lr-data/people.csv"
	    // The schema is encoded in a string
//	    ArrayList<String> schemaString = this.fileParser.parserParseComma(this.cpCfg.getSchemaPath());
	    // Generate the schema based on the string of schema
	    ArrayList<StructField> fields = new ArrayList<StructField>();	    
	    for (String fieldName: schemaString) {
	      fields.add(new StructField(fieldName, DataTypes.StringType, true, Metadata.empty()) );    	    	
	    }	    
	    fields.add(new StructField("HashID", DataTypes.StringType, true, Metadata.empty()) );
	    StructType schema = new StructType(fields.toArray(new StructField[fields.size()]));

	    // Convert records of the RDD (people) to Rows.
	    JavaRDD<Row> rowRDD = lines.map(
	      new Function<String, Row>() {
	        public Row call(String record) throws Exception {
	          	
	          Long hashcode= Math.abs((long)record.hashCode());
	          record=record+","+hashcode.toString();
	          String[] fields = record.split(",");
	          return RowFactory.create(fields);
	        }
	    });
	    
	    // Apply the schema to the RDD.
	    DataFrame coxDataFrame = sqlContext.createDataFrame(rowRDD, schema);
	    // Register the DataFrame as a table.
	    coxDataFrame.registerTempTable("coxDataSet");

	        
//	    // SQL can be run over RDDs that have been registered as tables.
//	    String projectCol="HashID,"+this.cpCfg.getY_var()+","+this.cpCfg.getCensored_var()+","+this.cpCfg.getX_varList();
//	    DataFrame results = sqlContext.sql("SELECT "+projectCol+" FROM coxDataSet where "+schemaString.get(0)+" <> '"+schemaString.get(0)+"'");
//	   
//	    return results.toJavaRDD().mapToPair(new ParsePointKeyPair()).cache();
	
	}
	public static void main(String[] args) {
		CoxProcess singleton = new CoxProcess(args[0]);
//		 CoxProcess singleton = new CoxProcess("./data/cox-data/coxTestConfig.xml");
		 singleton.prepare();
//		 singleton.ready();
		 singleton.go();
	 }
	 
}

package org.apache.spark.mllib.survivalAnalysis;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.DoubleFunction;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.imeds.daemon.imedsDaemon;
import org.imeds.util.TimeDiff;

import Jama.Matrix;
import scala.Tuple2;


public final class CoxRegression implements Serializable {
	
	private static final long serialVersionUID = 7532494323058935813L;
	private transient CoxRegressionConfig cfg;
	private transient DecimalFormat a1 = new DecimalFormat( "#.00");
	private JavaPairRDD<Double, SurvivalLabelPoint> DataSet;
	private static final Random rand = new Random(42); //TODO feed time as seed
	private static final Logger logger = Logger.getLogger(CoxRegression.class);
	public CoxRegression(CoxRegressionConfig cfg, JavaPairRDD<Double, SurvivalLabelPoint> readyDataSet) {
		super();
		this.cfg = cfg;
		this.DataSet = readyDataSet;
	}

	public CoxModel fit(){
		return fit(false);
	}
	public CoxModel fit(Boolean enablePvalue){
		
		int ITERATION=0;
		logger.info("====================Config Setting===========");
		logger.info(this.cfg.toString());
		logger.info("=============================================");

		// Initialize w to a random value and init trust region	  
		Date trainStart = new Date();
	    double[] weights = new double[this.cfg.getDim()];
	    double[] deltaJ  = new double[this.cfg.getDim()];  //trust region
	    BitSet isConverge = new BitSet(this.cfg.getDim()); 
	    Arrays.fill(deltaJ, 1.0);
	    if(this.cfg.getRegularizationType().trim()!=null && this.cfg.getRegularizationType().trim().equals("L1"))initWeights(this.cfg.getDim());
	    else Arrays.fill(weights, 0.0);
	    isConverge.clear();
	    
	    //TODO: add converge criteria
	    while(ITERATION<this.cfg.getMaxIter() && (isConverge.cardinality()!= this.cfg.getDim())){
	  
	    	logger.info("=================ITERATION "+ITERATION+"=================");
	 	    logger.info("weights: "+Arrays.toString(weights));
	 	    logger.info("tregion: "+Arrays.toString(deltaJ));
	 	    logger.info("==========================================================");
	 	    
	    	for(int j=0;j<this.cfg.getDim();j++){
	    		if(!isConverge.get(j)){
	    			/**
//		    	    JavaPairRDD<Double, tempV> expValueL=this.DataSet.mapToPair(new ComputeExpValue(weights,j));
		    	    JavaPairRDD<Double, tempV> expValueL=this.DataSet.mapToPair(new ComputeExpValue(weights,j)).filter(new Function<Tuple2<Double,tempV>,Boolean>(){

						public Boolean call(Tuple2<Double, tempV> v1)
								throws Exception {
							// TODO Auto-generated method stub
							return (v1._2.getY_failed()==1.0);
							
						}
		    	    	
		    	    });
		    	    JavaPairRDD<Double, tempV> expValueR=this.DataSet.mapToPair(new ComputeExpValue(weights,j));
		    	    //Form Riskset
//		    	    JavaPairRDD<Tuple2<Double,  tempV>, Tuple2<Double, tempV>> riskSet=expValueL.cartesian(expValueR).filter(new Function<Tuple2<Tuple2<Double,  tempV>, Tuple2<Double, tempV>>,Boolean>(){
//		    			public Boolean call(
//		    					Tuple2<Tuple2<Double,  tempV>, Tuple2<Double,  tempV>> v1)
//		    					throws Exception {
//		    				return (v1._1._2.getY_failed()==1.0)&&(v1._1._2.getY_survivalTime()<=v1._2._2.getY_survivalTime());
//		    			}	    	    	
//		    	    });
		    	    JavaPairRDD<Tuple2<Double,  tempV>, Tuple2<Double, tempV>> riskSet=expValueL.cartesian(expValueR).filter(new Function<Tuple2<Tuple2<Double,  tempV>, Tuple2<Double, tempV>>,Boolean>(){
		    			public Boolean call(
		    					Tuple2<Tuple2<Double,  tempV>, Tuple2<Double,  tempV>> v1)
		    					throws Exception {
		    				return (v1._1._2.getY_survivalTime()<=v1._2._2.getY_survivalTime());
		    			}	    	    	
		    	    });**/
		    	    double oldBetaj = weights[j];
		    	    double betaj=weights[j];
		    	    double deltaj=deltaJ[j];
//		    	    double deltaVj=getUpdateValue(riskSet, this.cfg.getRegularizationType(),betaj, this.cfg.getRegularizationPara());
		    	    double deltaVj=getUpdateValue(this.DataSet,weights,j,this.cfg.getRegularizationType(),betaj, this.cfg.getRegularizationPara());
			    	   
		    	    double deltaBetaj= Math.min(Math.max(deltaVj, deltaj*-1.0), deltaj);
		    	    betaj = betaj+deltaBetaj;
//		    	    logger.info("weights: "+betaj);
//		    	    logger.info("region=(-"+deltaj+"~"+deltaj+") step="+deltaVj);
//		    	    logger.info("updateWeights: "+betaj);
		    	    weights[j]=betaj;
		    	    deltaJ[j]= Math.max(2.0*Math.abs(deltaBetaj), deltaj/2.0);
		    	    if(Math.abs(betaj-oldBetaj)<this.cfg.getConvergeThreshold()) isConverge.set(j, true);;
//		    	    logger.info("convergeMap: "+isConverge.toString());
	    		}    
	    	}	
	    	ITERATION++;
	    }
	    Date trainEnd = new Date();
		long[] diffTrain = TimeDiff.getTimeDifference(trainStart, trainEnd);
		printTime("Model Training Time",diffTrain);
	    CoxModel cmdl= new CoxModel();
		cmdl.setCoef(weights);
		cmdl.setTrainingTime(diffTrain);
		if(enablePvalue)return formCoxModelPvalue(this.DataSet,cmdl,weights);
		else return cmdl;
	    
	}
	//Fail. this will generate wrong estimation
	public CoxModel fitParallelTest(Boolean enablePvalue){
		
		int ITERATION=0;
		logger.info("====================Config Setting===========");
		logger.info(this.cfg.toString());
		logger.info("=============================================");

		// Initialize w to a random value and init trust region	  
		Date trainStart = new Date();
	    double[] weights = new double[this.cfg.getDim()];
	    double[] weightsNew = new double[this.cfg.getDim()];
	    double[] deltaJ  = new double[this.cfg.getDim()];  //trust region
	    BitSet isConverge = new BitSet(this.cfg.getDim()); 
	    Arrays.fill(deltaJ, 1.0);
	    Arrays.fill(weightsNew, 0.0);
	    if(this.cfg.getRegularizationType().trim()!=null && this.cfg.getRegularizationType().trim().equals("L1"))initWeights(this.cfg.getDim());
	    else Arrays.fill(weights, 0.0);
	    isConverge.clear();
	    
	    //TODO: add converge criteria
	    while(ITERATION<this.cfg.getMaxIter() && (isConverge.cardinality()!= this.cfg.getDim())){
	  
	    	logger.info("=================ITERATION "+ITERATION+"=================");
	 	    logger.info("weights: "+Arrays.toString(weights));
	 	    logger.info("tregion: "+Arrays.toString(deltaJ));
	 	    logger.info("==========================================================");
	 	    
	    	for(int j=0;j<this.cfg.getDim();j++){
	    		if(!isConverge.get(j)){
		    	    double oldBetaj = weights[j];
		    	    double betaj=weights[j];
		    	    double deltaj=deltaJ[j];
		    	    double deltaVj=getUpdateValue(this.DataSet,weights,j,this.cfg.getRegularizationType(),betaj, this.cfg.getRegularizationPara());
			    	   
		    	    double deltaBetaj= Math.min(Math.max(deltaVj, deltaj*-1.0), deltaj);
		    	    betaj = betaj+deltaBetaj;
		    	    weightsNew[j]=betaj;
		    	    deltaJ[j]= Math.max(2.0*Math.abs(deltaBetaj), deltaj/2.0);
		    	    if(Math.abs(betaj-oldBetaj)<this.cfg.getConvergeThreshold()) isConverge.set(j, true);
	    		}    
	    	}
	    	for(int i=0;i<this.cfg.getDim();i++) weights[i]=weightsNew[i];
	    	ITERATION++;
	    }
	    Date trainEnd = new Date();
		long[] diffTrain = TimeDiff.getTimeDifference(trainStart, trainEnd);
		printTime("Model Training Time",diffTrain);
	    CoxModel cmdl= new CoxModel();
		cmdl.setCoef(weights);
		cmdl.setTrainingTime(diffTrain);
		if(enablePvalue)return formCoxModelPvalue(this.DataSet,cmdl,weights);
		else return cmdl;
	    
	}
	public Double c_statistic(ArrayList<Double> coef, JavaPairRDD<Double, SurvivalLabelPoint> testData){
		class testTemp{
			 Long key;
			 double y_survivalTime;
			 double y_failed;
			 double riskVal;
		}
		Double c_stat=0.0;
		final double[] weights= new double[coef.size()];
		for(int i=0;i<coef.size();i++)weights[i]=coef.get(i);
		JavaPairRDD<Double, testTemp> riskValAll=testData.mapToPair(new PairFunction<Tuple2<Double, SurvivalLabelPoint>, Double, testTemp>(){
			
			public Tuple2<Double, testTemp> call(
					Tuple2<Double, SurvivalLabelPoint> v1) throws Exception {
				testTemp tv=new testTemp();
				Double expdot = Math.exp(dot(v1._2.features().toArray(),weights));
				tv.y_failed=v1._2.getFailed();
				tv.y_survivalTime=v1._2.label();
				tv.riskVal=expdot;    
				tv.key=v1._2.getId();
				return new Tuple2<Double, testTemp>(v1._1,tv);
			}			
		});
		JavaPairRDD<Double, testTemp> riskValFailed = riskValAll.filter(new Function<Tuple2<Double,testTemp>,Boolean>(){
				public Boolean call(Tuple2<Double, testTemp> v1)throws Exception {
					return (v1._2.y_failed==1.0);					
				}
 	    });
	    JavaPairRDD<Tuple2<Double,testTemp>, Tuple2<Double, testTemp>> comparableSet=riskValFailed.cartesian(riskValAll).filter(new Function<Tuple2<Tuple2<Double,testTemp>, Tuple2<Double,testTemp>>,Boolean>(){
			public Boolean call(
					Tuple2<Tuple2<Double,testTemp>, Tuple2<Double,testTemp>> v1)
					throws Exception {
				return (v1._1._1<v1._2._1);
			}	    	    	
	    });
	    Double comparableN=comparableSet.map(new Function<Tuple2<Tuple2<Double, testTemp>,Tuple2<Double,testTemp>>,Double>(){
			public Double call(
					Tuple2<Tuple2<Double, testTemp>, Tuple2<Double, testTemp>> arg0)
					throws Exception {
				return 1.0;
			}
	    }).reduce(new Function2<Double,Double,Double>(){

			public Double call(Double arg0,Double arg1) throws Exception {
				return arg0+arg1;
			}
	    });
	    Double concordantN=comparableSet.filter(new Function<Tuple2<Tuple2<Double,testTemp>, Tuple2<Double,testTemp>>,Boolean>(){
			public Boolean call(
					Tuple2<Tuple2<Double,testTemp>, Tuple2<Double,testTemp>> v1)
					throws Exception {
				return (v1._1._2.riskVal>v1._2._2.riskVal);
			}	    	    	
	    }).map(new Function<Tuple2<Tuple2<Double, testTemp>,Tuple2<Double,testTemp>>,Double>(){
			public Double call(
					Tuple2<Tuple2<Double, testTemp>, Tuple2<Double, testTemp>> arg0)
					throws Exception {
				return 1.0;
			}
	    }).reduce(new Function2<Double,Double,Double>(){

			public Double call(Double arg0,Double arg1) throws Exception {
				return arg0+arg1;
			}
	    });
		c_stat=(concordantN/comparableN);
		logger.info("Concordant: "+concordantN+ " Comparable: "+comparableN+ " c_stat: "+c_stat);
		return c_stat;
	}
	/*******************
	 * Inner temp class*
	 *******************/

	static class tempV implements Serializable{
		  double y_survivalTime;
		  double y_failed;
		  double xi;
		  double exp;
		  double xexp;
		  double x2exp;
		  
		public double getY_survivalTime() {
			return y_survivalTime;
		}
		public void setY_survivalTime(double y_survivalTime) {
			this.y_survivalTime = y_survivalTime;
		}
		public double getY_failed() {
			return y_failed;
		}
		public void setY_failed(double y_failed) {
			this.y_failed = y_failed;
		}
		public double getExp() {
			return exp;
		}
		public void setExp(double exp) {
			this.exp = exp;
		}
		  
		public double getXi() {
			return xi;
		}
		public void setXi(double xi) {
			this.xi = xi;
		}
		public double getexp() {
			return exp;
		}
		public void setexp(double exp) {
			this.exp = exp;
		}
		public double getXexp() {
			return xexp;
		}
		public void setXexp(double xexp) {
			this.xexp = xexp;
		}
		public double getX2exp() {
			return x2exp;
		}
		public void setX2exp(double x2exp) {
			this.x2exp = x2exp;
		}
		@Override
		public String toString() {
			return "tempV [y_Time=" + y_survivalTime + ", y_failed="
					+ y_failed + ", xi=" + xi +"exp=" +exp + "]";//, xexp="
					//+ a1.format(xexp) + "]";//, x2exp=" + a1.format(x2exp) + "]";
		}
		  
	  }
	/*******************
	 * Inner function*
	 *******************/
	public static void getDerivation(JavaPairRDD<Double, SurvivalLabelPoint> tempValueMap,double[] weights, int xIdx, Double[] deri){
		class tempAgg{
			private double dieN=0;
			double xSum=0.0;
			double xexpSum=0.0;
			double x2expSum=0.0;
			double expSum=0.0;
			
			public double getxSum() {
				return xSum;
			}

			public void setxSum(double xSum) {
				this.xSum = xSum;
			}

			public void update(double xexpSum, double x2expSum, double expSum){
				
				this.xexpSum=xexpSum;
				this.x2expSum=x2expSum;
				this.expSum=expSum;
			}

			public double getDieN() {
				return dieN;
			}

			public void setDieN(double dieN) {
				this.dieN = dieN;
			}
			public void addDieN(){
				this.dieN=this.dieN+1;
			}

			@Override
			public String toString() {
				return "tempAgg [dieN=" + dieN + ", xSum=" + xSum
						+ ", xexpSum=" + xexpSum + ", x2expSum=" + x2expSum
						+ ", expSum=" + expSum + "]";
			}
			
			
		}
		double updateValue=0;
		HashMap<Double, tempAgg> failedSetMap= new HashMap<Double,tempAgg>();
//		List<Tuple2<Double, tempV>> expValue= tempValueMap.mapToPair(new ComputeExpValue(weights,xIdx)).mapToPair(
//				new PairFunction<Tuple2<Double,tempV>, Double,tempV>(){
//						public Tuple2<Double, tempV> call(Tuple2<Double, tempV> v1)
//								throws Exception {
//							return new Tuple2<Double, tempV>(v1._2.getY_survivalTime(),v1._2);
//						}
//		    	    }
//				).sortByKey(false).collect();
		List<Tuple2<Double, tempV>> expValue= tempValueMap.mapToPair(new ComputeExpValue(weights,xIdx)).sortByKey(false).collect();
		
		double xSumAll=0.0;
		double xexpSumAll=0.0;
		double x2expSumAll=0.0;
		double expSumAll=0.0;
		for(Tuple2<Double, tempV> pt: expValue){
//			logger.info(pt._1+" "+pt._2.toString());
			xexpSumAll = xexpSumAll+ pt._2.xexp;
			x2expSumAll = x2expSumAll+ pt._2.x2exp;
			expSumAll = expSumAll+ pt._2.exp;
			if(pt._2.y_failed==1){
				
				if(!failedSetMap.containsKey(pt._1))failedSetMap.put(pt._1, new tempAgg());
				
				failedSetMap.get(pt._1).addDieN();
				xSumAll=failedSetMap.get(pt._1).getxSum()+pt._2.xi;
				failedSetMap.get(pt._1).setxSum(xSumAll);
			}
			if(failedSetMap.containsKey(pt._1)){
				failedSetMap.get(pt._1).update(xexpSumAll, x2expSumAll, expSumAll);
//				logger.info(pt._1+" "+failedSetMap.get(pt._1).toString());
			}			
		}
		Double BetaFirstDeri=0.0, BetaSecondDeri=1.0;
		Double sumXi=0.0, firstRiskSet=0.0, xSqrtRiskSet=0.0, firstRiskSetSqrt=0.0;
		for (tempAgg value : failedSetMap.values()) {
		    sumXi=sumXi+value.getxSum();
		    firstRiskSet=firstRiskSet+(value.xexpSum/value.expSum)*value.getDieN();
		    xSqrtRiskSet=xSqrtRiskSet+(value.x2expSum/value.expSum)*value.getDieN();
		    firstRiskSetSqrt=firstRiskSetSqrt+Math.pow((value.xexpSum/value.expSum), 2)*value.getDieN();//Math.pow((tp._2.xexp/tp._2.exp),2);
		}
		
		BetaFirstDeri=-sumXi+firstRiskSet;
		BetaSecondDeri=xSqrtRiskSet-firstRiskSetSqrt;
		updateValue= (BetaFirstDeri/BetaSecondDeri)*-1.0;
		deri[0]=BetaFirstDeri;
		deri[1]=BetaSecondDeri;
		failedSetMap=null;
		
	}
	private CoxModel formCoxModelPvalue(JavaPairRDD<Double, SurvivalLabelPoint> tempValueMap,CoxModel cmdl, double[] weights){
		class infoTemp{
			double dieN=0;		
			double xjExpSum=0.0;
			double xkExpSum=0.0;
			double xkxjExpSum=0.0;
			double expSum=0.0;
		}
		
		
		infoTemp[][] information;
		double[][] informationMatrix = new double[this.cfg.getDim()][this.cfg.getDim()];
		Map<Double, Integer> failedSetMap= tempValueMap.filter(new Function<Tuple2<Double, SurvivalLabelPoint>,Boolean>(){
			public Boolean call(Tuple2<Double, SurvivalLabelPoint> v1)throws Exception {
				return (v1._2.getFailed()==1.0);				
			}	    	
	    }).mapToPair(new PairFunction<Tuple2<Double, SurvivalLabelPoint>,Double, Integer>(){
			public Tuple2<Double, Integer> call(Tuple2<Double, SurvivalLabelPoint> t){			
				return new Tuple2<Double, Integer>(t._1, 1);
			}
		}).reduceByKey(new Function2<Integer,Integer,Integer>(){
			public Integer call(Integer i1, Integer i2) {
			return i1 + i2;
			}
		}).collectAsMap();
		List<Tuple2<Double, SurvivalLabelPoint>> expValue= tempValueMap.sortByKey(false).collect();
		Double expdot;
		information = new infoTemp[this.cfg.getDim()][this.cfg.getDim()];
		 for(int j=0;j<this.cfg.getDim();j++){
			 for(int k=j;k<this.cfg.getDim();k++){
				 information[j][k]= new infoTemp();
			 }
		 }
		double[][] iMatrixtmp=  new double[this.cfg.getDim()][this.cfg.getDim()];
		double oldTime;//=expValue.get(1)._1;
		double xj, xk;
		Date trainStart = new Date();	
		for(int i=0; i<expValue.size();i++){
			 if(i<expValue.size()-1)oldTime=expValue.get(i+1)._1;
			 else oldTime=-1.0;
			 expdot= Math.exp(dot(expValue.get(i)._2.features().toArray(),weights));			 
			 for(int j=0;j<this.cfg.getDim();j++){
				 for(int k=j;k<this.cfg.getDim();k++){
					 infoTemp tmp=information[j][k];
					 tmp.expSum+=expdot;
					 xj = expValue.get(i)._2.features().apply(j);
					 xk = expValue.get(i)._2.features().apply(k);
					 if(xj!=0 && xk!=0){
						 tmp.xjExpSum+=expdot*xj;
						 tmp.xkExpSum+=expdot*xk;
						 tmp.xkxjExpSum+=expdot*xk*xj;
					 }
						 
					 information[j][k]=tmp;
					 if(failedSetMap.containsKey(expValue.get(i)._1)){
						 iMatrixtmp[j][k]=((tmp.xkxjExpSum/tmp.expSum)-(tmp.xjExpSum*tmp.xkExpSum/Math.pow(tmp.expSum, 2)));
					 }
				  }				 
			  }
			 if(oldTime!=expValue.get(i)._1){
				 if(failedSetMap.containsKey(expValue.get(i)._1)){
					 for(int j=0;j<this.cfg.getDim();j++){
						 for(int k=j;k<this.cfg.getDim();k++){
							 informationMatrix[j][k]= informationMatrix[j][k]+iMatrixtmp[j][k]*failedSetMap.get(expValue.get(i)._1);
							 informationMatrix[k][j]=informationMatrix[j][k];
							 iMatrixtmp[j][k]=0;
							 iMatrixtmp[k][j]=0;
						 }
					 }
				 }
			 }
			 
		}
		Date trainEnd = new Date();
	    long[] diffTrain = TimeDiff.getTimeDifference(trainStart, trainEnd);
	    printTime("infoMatrix build Time",diffTrain);
		trainStart = new Date();		
		Matrix iMatrix = new Matrix(informationMatrix);
		double[][] variance = iMatrix.inverse().times(1.0).getArray();
        for (int s = 0; s < this.cfg.getDim(); s++)
        {
//        	logger.info(variance[s][s]);
            cmdl.addTestStats(weights[s] / Math.sqrt(variance[s][s]), s);	
        }
        trainEnd = new Date();
        diffTrain = TimeDiff.getTimeDifference(trainStart, trainEnd);
        printTime("Matrix Inverse Time",diffTrain);
	    return cmdl;    
			
	}
	
	public void printTime(String title,long[] diffTrain){
		logger.info(title+": "+diffTrain[1]+" hour(s), "+diffTrain[2]+" minute(s),"+ diffTrain[3]+" second(s) and "+diffTrain[4]+" millisecond(s)");
		
	}
	
//	public static double getUpdateValue(JavaPairRDD<Tuple2<Double,  tempV>, Tuple2<Double, tempV>> riskSet,String regularMethod, double betaj, double regularSigma){
	public static double getUpdateValue(JavaPairRDD<Double, SurvivalLabelPoint> tempValueMap, double[] weights, int xIdx,String regularMethod, double betaj, double regularSigma){
		
		Double BetaFirstDeri=0.0, BetaSecondDeri=0.0;
		Double[] deri={0.0,0.0}; //First Deri, Second Deri
		
		getDerivation(tempValueMap,weights, xIdx,deri);
		BetaFirstDeri=deri[0];
		BetaSecondDeri=deri[1];
		
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
	static class ComputeExpValue implements PairFunction<Tuple2<Double,SurvivalLabelPoint>, Double,tempV> {
		    private final double[] weights;
		    private final int Xi;

		    ComputeExpValue(double[] weights, int Xi) {
			      this.weights = weights;
			      this.Xi=Xi;
			 }

			public Tuple2<Double,tempV> call(Tuple2<Double, SurvivalLabelPoint> t)
					throws Exception {
				    tempV tv=new tempV(); 
				                             
			        Double expdot = Math.exp(dot(t._2.features().toArray(),this.weights));
			        tv.setXi(t._2.features().apply(Xi));
			        tv.setY_failed(t._2.getFailed());
			        tv.setY_survivalTime(t._2.label());
			        tv.setexp(expdot);
			        tv.setXexp(t._2.features().apply(Xi)*expdot);
			        tv.setX2exp(Math.pow(t._2.features().apply(Xi),2.0)*expdot);
			        
			        //TODO change the key by using a hash function
			        double k = 0;
			        for(int i=0;i<t._2.features().size();i++)k=k+t._2.features().apply(i);
			        Double key = t._2.getId().doubleValue();
//			        logger.info(Arrays.toString(weights)+"tv: "+tv.toString());
//			        System.out.println(Arrays.toString(weights)+" "+t._2.toString()+" exp: "+expdot+" "+dot(weights, t._2.features().toArray()));
//			        System.out.println(tv.toString());
				return new Tuple2<Double, tempV>(tv.getY_survivalTime(),tv);
//				return new Tuple2<Double, tempV>(key,tv);
			}
	}
	public static double dot(double[] a, double[] b) {
		    double x = 0;
		    for (int i = 0; i < a.length; i++) {
		      if(a[i]!=0 && b[i]!=0)x += a[i] * b[i];
		    }
		    return x;
    }

	 public static void printWeights(double[] a) {
	    System.out.println(Arrays.toString(a));
	 }
	
	 public static double[] initWeights(int dim) {
		 double[] w=new double[dim];
	    for (int i = 0; i < dim; i++) {
		      w[i] = 2 * rand.nextDouble() - 1;
		}    
	    return w;
	 }
	 
	 //FIXME
	 //this part has some problems related to reduce by key.
	 //when i use tempV as a key, somehow it cannot reduce correctly.
	 //go deeper into combineByKey func...
	public static void problemCode(JavaPairRDD<Tuple2<Double,  tempV>, Tuple2<Double, tempV>> riskSet){
			    JavaPairRDD<tempV, tempV> riskSetExpSum=riskSet.mapToPair(new PairFunction<Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>>,tempV, tempV>(){
				public Tuple2<tempV, tempV> call(
		Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>> t)
		throws Exception {
		return new Tuple2<tempV, tempV>(t._1._2, t._2._2);
		}
		});
		List<Tuple2<tempV, tempV>> t=riskSetExpSum.collect();
		for(Tuple2<tempV, tempV> tt:t)System.out.println(tt);
		
		
		JavaPairRDD<tempV, tempV> riskSetSum= riskSetExpSum.reduceByKey(new Function2<tempV, tempV, tempV>(){
		
		public tempV call(tempV v1, tempV v2) throws Exception {
		tempV tv = new tempV();
		tv.xi=v1.xi+v2.xi;
		return tv;
		}
		});
		
		
		//JavaPairRDD<Double, Integer> riskSetExpSum=riskSet.mapToPair(new PairFunction<Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>>,Double, Integer>(){
		//public Tuple2<Double, Integer> call(
		//Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>> t){
		//
		//return new Tuple2<Double, Integer>(t._1._1, 1);
		//}
		//});
		//List<Tuple2<Double, Integer>> t=riskSetExpSum.collect();
		//for(Tuple2<Double, Integer> tt:t)System.out.println(tt);
		//
		//JavaPairRDD<Double, Integer> riskSetSum= riskSetExpSum.reduceByKey(new Function2<Integer,Integer,Integer>(){
		//public Integer call(Integer i1, Integer i2) {
		//return i1 + i2;
		//}
		//
		//});
		//List<Tuple2<Double, Integer>> riskSetExpSumresult= riskSetSum.collect();
		//for(Tuple2<Double, Integer> p:riskSetExpSumresult){
		//System.out.println(p);
		//}
		
		
		
		
//		List<Tuple2<Double, SurvivalLabelPoint>> pairPoints = this.DataSet.collect();
		
//		for(Tuple2<Double, SurvivalLabelPoint> p:pairPoints){
		//System.out.println(p._1+","+p._2.toString());
//		}
		//System.out.println("size of DataPoint:"+pairPoints.size());
		
//		List<Tuple2<Double, tempV>> expValueresult= expValueL.collect();
//		for(Tuple2<Double, tempV> p:expValueresult){
		//System.out.println(p._1+","+p._2.toString());
//		}
		//System.out.println("size of expValueresult:"+expValueresult.size());
		
//		List<Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>>> riskSetresult= riskSet.collect();
//		for(Tuple2<Tuple2<Double, tempV>, Tuple2<Double, tempV>> p:riskSetresult){
		//System.out.println(p._1.toString()+","+p._2.toString());
//		}
		//System.out.println("size of riskSetresult:"+expValueresult.size());
		//   
		//   List<Tuple2<Double, Double>> riskSetSumresult= riskSetSum.collect();
		//   for(Tuple2<Double, Double> p:riskSetSumresult){
		//System.out.println(p._1.toString()+","+p._2.toString());
		//   }
		//   System.out.println("size of riskSetSumresult:"+expValueresult.size());
		//   List<Tuple2<Double, Double>> riskSetExpSumresult= riskSetExpSum.collect();
		//   for(Tuple2<Double, Double> p:riskSetExpSumresult){
		//System.out.println(p._1.toString()+","+p._2.toString());
		//   }
		//   System.out.println("size of riskSetresult:"+riskSetExpSumresult.size());
			
	}
}

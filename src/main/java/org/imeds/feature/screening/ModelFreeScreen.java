package org.imeds.feature.screening;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.imeds.feature.selection.label;
import org.imeds.feature.selection.statInfo;

public class ModelFreeScreen {
	public enum MFStype {General, Survival, SurvivalStd};
	public ArrayList<feature> generalScreen(ArrayList<Tuple> DataPointList){
		Collections.sort(DataPointList);		
		ArrayList<feature> xMargin=new ArrayList<feature>();
		double culmulatemar=0;
		double currentmar=0;
		double lastmar=0;
		double currentY=0;
		double lastY=0;
		double totalN= DataPointList.size();
		double stdx=0;
		for(int i=0;i<DataPointList.get(0).getxList().size();i++){
			culmulatemar=0;
			currentmar=0;
			lastmar=0;
			statInfo sinfo=new statInfo();
			for(int j=0;j<DataPointList.size();j++){
				sinfo.addCnt();
				sinfo.addSum(DataPointList.get(j).getxList().get(i));
				sinfo.addSumSquare(DataPointList.get(j).getxList().get(i));
			}
			for(int j=1;j<DataPointList.size();j++){
				currentY=DataPointList.get(j).getyList().get(0);				
				lastY=DataPointList.get(j-1).getyList().get(0);
				

				stdx=(DataPointList.get(j-1).getxList().get(i)-sinfo.getMean())/sinfo.getStd();
				currentmar = currentmar+stdx;
				if(currentY>lastY){
					culmulatemar+=Math.pow((currentmar),2);
					lastmar=currentmar;
				}else{
					culmulatemar+=Math.pow((lastmar),2);
				}

				
//				double sumtmp=0;				
//				for(int k=0;k<j;k++){
//					lastY=DataPointList.get(k).getyList().get(0);
//					if(currentY>lastY){
//						//System.out.println(sinfo.getMean()+"/"+sinfo.getStd());
//						stdx=(DataPointList.get(k).getxList().get(i)-sinfo.getMean())/sinfo.getStd();
//						sumtmp+=stdx;
//					}
//				}
//				culmulatemar+=Math.pow((sumtmp),2);
			}
			sinfo=null;
			xMargin.add(new feature((long)i,culmulatemar/Math.pow(totalN,3)));
		}
		Collections.sort(xMargin);
		return xMargin;
	}
	public ArrayList<feature> generalScreenCox(ArrayList<Tuple> DataPointList,HashMap<Integer, Double> survFunc){
		Collections.sort(DataPointList);		
		ArrayList<feature> xMargin=new ArrayList<feature>();
		double culmulatemar=0;
		double currentmar=0;
		double lastmar=0;
		double currentY=0;
		double lastY=0;
		double totalN= DataPointList.size();
		double stdx=0;
		for(int i=0;i<DataPointList.get(0).getxList().size();i++){
			culmulatemar=0;
			currentmar=0;
			lastmar=0;
			statInfo sinfo=new statInfo();
			for(int j=0;j<DataPointList.size();j++){
				sinfo.addCnt();
				sinfo.addSum(DataPointList.get(j).getxList().get(i));
				sinfo.addSumSquare(DataPointList.get(j).getxList().get(i));
			}
			for(int j=1;j<DataPointList.size();j++){
				currentY=DataPointList.get(j).getyList().get(0);
				
				lastY=DataPointList.get(j-1).getyList().get(0);
				
				double surv= Math.pow(survFunc.get((int)lastY), 2);
//				currentmar = currentmar+DataPointList.get(j-1).getxList().get(i)*DataPointList.get(j-1).getyList().get(1)/surv;			
//				if(currentY>lastY){
//					culmulatemar+=Math.pow((currentmar/totalN),2);
//					lastmar=currentmar;
//				}else{
//					culmulatemar+=Math.pow((lastmar/totalN),2);
//				}
				stdx=(DataPointList.get(j-1).getxList().get(i)-sinfo.getMean())/sinfo.getStd();
				currentmar = currentmar+stdx*DataPointList.get(j-1).getyList().get(1)/surv;
				if(currentY>lastY){
					culmulatemar+=Math.pow((currentmar),2);
					lastmar=currentmar;
				}else{
					culmulatemar+=Math.pow((lastmar),2);
				}
				
			}
			sinfo=null;
			xMargin.add(new feature((long)i,culmulatemar/Math.pow(totalN,3)));
		}
		Collections.sort(xMargin);
		return xMargin;
	}
	public ArrayList<feature> generalScreenCoxStd(ArrayList<Tuple> DataPointList,HashMap<Integer, Double> survFunc){
		Collections.sort(DataPointList);		
		ArrayList<feature> xMargin=new ArrayList<feature>();
		double culmulatemar=0;
		double currentmar=0;
		double lastmar=0;
		double currentY=0;
		double lastY=0;
		double totalN= DataPointList.size();
		double stdx=0;
		for(int i=0;i<DataPointList.get(0).getxList().size();i++){
			culmulatemar=0;
			currentmar=0;
			lastmar=0;
			statInfo sinfo=new statInfo();
			for(int j=0;j<DataPointList.size();j++){
				sinfo.addCnt();
				sinfo.addSum(DataPointList.get(j).getxList().get(i));
				sinfo.addSumSquare(DataPointList.get(j).getxList().get(i));
			}
			
			for(int j=1;j<DataPointList.size();j++){
				currentY=DataPointList.get(j).getyList().get(0);				
				lastY =DataPointList.get(j-1).getyList().get(0);
				
				stdx=(DataPointList.get(j-1).getxList().get(i)-sinfo.getMean())/sinfo.getStd();
				currentmar = currentmar+stdx*DataPointList.get(j-1).getyList().get(1);
				if(currentY>lastY){
					culmulatemar+=Math.pow((currentmar),2);
					lastmar=currentmar;
				}else{
					culmulatemar+=Math.pow((lastmar),2);
				}
				

				
//				double sumtmp=0;
//				for(int k=0;k<j;k++){
//					lastY=DataPointList.get(k).getyList().get(0);
//					if(currentY>lastY){
//						stdx=(DataPointList.get(k).getxList().get(i)-sinfo.getMean())/sinfo.getStd();
//						sumtmp+=stdx*DataPointList.get(k).getyList().get(1);
//						
//					}
//				}
//				culmulatemar+=Math.pow((sumtmp),2);
			}
			sinfo=null;
		
			xMargin.add(new feature((long)i,culmulatemar/Math.pow(totalN,3)));
		}
		Collections.sort(xMargin);
		return xMargin;
	}
	public ArrayList<feature> generalScreenCoxStdBitMap(ArrayList<Tuple> DataPointList,HashMap<Integer, Double> survFunc){
		Collections.sort(DataPointList);		
		ArrayList<feature> xMargin=new ArrayList<feature>();
		double culmulatemar=0;
		double currentmar=0;
		double lastmar=0;
		double currentY=0;
		double lastY=0;
		double totalN= DataPointList.size();
		double stdx=0;
		for(int i=0;i<DataPointList.get(0).getFeatureSize();i++){
			culmulatemar=0;
			currentmar=0;
			lastmar=0;
			statInfo sinfo=new statInfo();
			for(int j=0;j<DataPointList.size();j++){
				sinfo.addCnt();
				if(DataPointList.get(j).getBitFeatures().get(i)){
					sinfo.addSum(1.0);
					sinfo.addSumSquare(1.0);
				}
				
			}
			
			for(int j=1;j<DataPointList.size();j++){
				currentY=DataPointList.get(j).getyList().get(0);				
				lastY =DataPointList.get(j-1).getyList().get(0);
				double xval=0.0;
				if(DataPointList.get(j-1).getBitFeatures().get(i)){
					xval=1.0;
				}
				stdx=(xval-sinfo.getMean())/sinfo.getStd();
				currentmar = currentmar+stdx*DataPointList.get(j-1).getyList().get(1);
				if(currentY>lastY){
					culmulatemar+=Math.pow((currentmar),2);
					lastmar=currentmar;
				}else{
					culmulatemar+=Math.pow((lastmar),2);
				}
				

				
//				double sumtmp=0;
//				for(int k=0;k<j;k++){
//					lastY=DataPointList.get(k).getyList().get(0);
//					if(currentY>lastY){
//						stdx=(DataPointList.get(k).getxList().get(i)-sinfo.getMean())/sinfo.getStd();
//						sumtmp+=stdx*DataPointList.get(k).getyList().get(1);
//						
//					}
//				}
//				culmulatemar+=Math.pow((sumtmp),2);
			}
			sinfo=null;
		
			xMargin.add(new feature((long)i,culmulatemar/Math.pow(totalN,3)));
		}
		Collections.sort(xMargin);
		return xMargin;
	}
}

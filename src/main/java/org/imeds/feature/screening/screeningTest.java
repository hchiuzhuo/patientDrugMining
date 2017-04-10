package org.imeds.feature.screening;

import java.util.ArrayList;
import java.util.Collections;

import org.imeds.util.CCIcsvTool;
import org.imeds.util.OSValidator;

public class screeningTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String seqCoxFolder="data/IMEDS/CgstHfComorbidDS/SurvivalSeqDS/seqCox";
		String fileName = seqCoxFolder+OSValidator.getPathSep()+"seq_2005-10-10_VMSP_0.05_R.csv";
		ArrayList<Tuple> DataPointList = new ArrayList<Tuple>();
		String ID="ID";
		ArrayList<String> yTitle=new ArrayList<String>();
		yTitle.add("Period");
		yTitle.add("Failed");
		Integer xStart=3;
		CCIcsvTool.RegressionDatasetParserDoc(fileName, DataPointList, ID, yTitle, xStart);

		Collections.sort(DataPointList);		
		ArrayList<feature> xMargin=new ArrayList<feature>();
		double culmulatemar=0;
		double currentmar=0;
		double lastmar=0;
		double currentY=0;
		double lastY=0;
		double totalN= DataPointList.size();
		for(int i=0;i<DataPointList.get(0).getxList().size();i++){
			culmulatemar=0;
			currentmar=0;
			lastmar=0;
			for(int j=1;j<DataPointList.size();j++){
				currentY=DataPointList.get(j).getyList().get(0);
				
				lastY=DataPointList.get(j-1).getyList().get(0);
				currentmar = currentmar+DataPointList.get(j-1).getxList().get(i);				
				if(currentY>lastY){
					culmulatemar+=Math.pow((currentmar/totalN),2);
					lastmar=currentmar;
				}else{
					culmulatemar+=Math.pow((lastmar/totalN),2);
				}
				
//				double sumtmp=0;				
//				for(int k=0;k<j;k++){
//					lastY=DataPointList.get(k).getyList().get(0);
//					if(currentY>lastY){
//						sumtmp+=DataPointList.get(k).getxList().get(i);
//					}
//				}
//				culmulatemar+=Math.pow((sumtmp/totalN),2);
			}
			
			xMargin.add(new feature((long)i,culmulatemar/totalN));
		}
		Collections.sort(xMargin);
		int i=0;
		for(feature f:xMargin){
			if(i<250)System.out.print("D"+f.getId()+"+");
			i++;
//			System.out.println(f);
		}
		
	}

}

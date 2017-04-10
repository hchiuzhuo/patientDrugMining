package org.imeds.data.common;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CCIcode extends DIScode{
	
	protected ArrayList<IcdPair> icdDouble = new ArrayList<IcdPair>();
	private ArrayList<Integer> icdCptId = new ArrayList<Integer>();
	private String icdList="";
	
	private NumberFormat numberFormat = NumberFormat.getNumberInstance(); 
	
	public CCIcode() {
		// TODO Auto-generated constructor stub
		super();
		numberFormat.setMaximumFractionDigits(2);  
		numberFormat.setMinimumIntegerDigits(3);
	}
	public String getIcdList() {
		return icdList;
	}
	public void setIcdList(String icdList) {
		this.icdList = icdList;
	}
	
	
	public ArrayList<IcdPair> getIcdDouble() {
		return icdDouble;
	}
	public ArrayList<Integer> getIcdCptId() {
		return icdCptId;
	}
	public void setIcdCptId(ArrayList<Integer> icdCptId) {
		this.icdCptId = icdCptId;
	}
	public void setIcdDouble(ArrayList<IcdPair> icdDouble) {
		this.icdDouble = icdDouble;
	}
	
	public void tranIcdList(){
		if(icdList.trim() !=null){
			String range[] = icdList.split(",");
			for(int i=0;i<range.length;i++){
				String rge = range[i];
				if(rge.trim()!=null){
					if(rge.contains("-")){
						String pair[] = range[i].split("-");
						this.icdDouble.add(getByRange(pair[0],pair[1]));
					}else{
						this.icdDouble.add(getByOneValue(rge));
					}
				}
				
			}
		}
	}
	public IcdPair getByRange(String st, String ed){
		IcdPair ipr = new IcdPair();
		if(st.trim()!=null && ed.trim()!=null){
			
			if(!isContainLetter(st) && !isContainLetter(ed)){				
				ipr.setStart(Double.parseDouble(st));
				ipr.setEnd(getEndValue(Double.parseDouble(ed)));								
				ipr.setSStart(numberFormat.format(Double.parseDouble(st)));
				ipr.setSEnd( numberFormat.format(getEndValue(Double.parseDouble(ed))));
			}else{
				//TODO ADD ICD9 CODE WITH LETTER
			}
		}else{
			//TODO ADD NULL EXCEPTION
		}
		return ipr;
	}
	public IcdPair getByOneValue(String str){
		IcdPair ipr = new IcdPair();
		if(!isContainLetter(str)){			
			ipr.setStart(Double.parseDouble(str));
			ipr.setEnd(getEndValue(Double.parseDouble(str)));
			ipr.setSStart(numberFormat.format(Double.parseDouble(str)));
			ipr.setSEnd( numberFormat.format(getEndValue(Double.parseDouble(str))));
			
		}else{
			//TODO ADD ICD9 CODE WITH LETTER
		}
		return ipr;
	}
	public boolean isContainLetter(String str){
		 Pattern datePattern = Pattern.compile("[a-zA-Z]+");
		 Matcher dateMatcher = datePattern.matcher(str);
		 return dateMatcher.find();
	}
	
	public Double getEndValue(Double dv){
		int tmp =  (int)(Math.round(dv*100));
		
		if	   ((tmp%100)==0) 	dv = (tmp+100)/100.00;
		else if((tmp%10)==0)    dv = (tmp+10)/100.00;				
		else 				    dv =  tmp/100.00 ;
		
		return dv;
	}
	
	@Override
	public String toString() {
		return "CCIcode [icd9Double=" + icdDouble.toString() + ",\n icdList=" + icdList
				+ ", ID=" + ID + ", Name=" + Name + "]\n";
	}
	
	
}

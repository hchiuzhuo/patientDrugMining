package org.imeds.data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class SurvivalTime  implements Comparable<SurvivalTime> {
	private Long id;
	private Date obs_start_date;
	private Date obs_end_date;
	private Date death_date;
	private Date censored_date;
	private Date dis_index_date;
	private Integer survival_length;
	private Integer survival_start;
	private Integer survival_end;
	private boolean failed;
	private ArrayList<Double> addiFeatures=new ArrayList<Double>();
	private BitSet BitFeatures; 
	private ArrayList<Double> features=new ArrayList<Double>();
	public ArrayList<Double> getAddiFeatures() {
		return addiFeatures;
	}
	public void setAddiFeatures(ArrayList<Double> addiFeatures) {
		this.addiFeatures = addiFeatures;
	}
	public BitSet getBitFeatures() {
		return BitFeatures;
	}
	public void setBitFeatures(BitSet bitFeatures) {
		BitFeatures = bitFeatures;
	}
	public ArrayList<Double> getFeatures() {
		return features;
	}
	public void setFeatures(ArrayList<Double> features) {
		this.features = features;
	}
	public void setFeatures(String features) {
		//System.out.println(features);
		String[] fe= features.split(",");
		for(String s:fe){
		  
		  this.features.add(Double.parseDouble(s.trim()));
		}
		
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(this.dis_index_date);
	    int year = cal.get(Calendar.YEAR);
		Double age = year - this.features.get(1);
		this.features.add(age);
	}
	public SurvivalTime(){
		
	}
	public SurvivalTime(Long id, Integer survival_length, boolean failed) {
		super();
		this.id = id;
		this.survival_length = survival_length;
		this.failed = failed;
	}
	public SurvivalTime(Long id, Integer survival_length, boolean failed,Date dis_idx_date) {
		super();
		this.id = id;
		this.survival_length = survival_length;
		this.failed = failed;
		this.dis_index_date = dis_idx_date;
	}
	public SurvivalTime(Long id, Date obs_startDate, Date obs_endDate,
			Date death_Date,Date dis_idx_date) {
		// TODO Auto-generated constructor stub
		this.id = id;
		this.obs_start_date=obs_startDate;
		this.obs_end_date=obs_endDate;
		this.death_date=death_Date;
		this.dis_index_date=dis_idx_date;

	}
	public Long getId() {
		return id;
	}
	public void setId(Long l) {
		this.id = l;
	}
	public Date getObs_start_date() {
		return obs_start_date;
	}
	public void setObs_start_date(Date obs_start_date) {
		this.obs_start_date = obs_start_date;
	}
	public Date getObs_end_date() {
		return obs_end_date;
	}
	public void setObs_end_date(Date obs_end_date) {
		this.obs_end_date = obs_end_date;
	}
	public Date getDeath_date() {
		return death_date;
	}
	public void setDeath_date(Date death_date) {
		this.death_date = death_date;
	}
	public Date getCensored_date() {
		return censored_date;
	}
	public void setCensored_date(Date censored_date) {
		this.censored_date = censored_date;
		int svvlen=0;
		Date startdate=this.dis_index_date;
//		Date startdate=this.obs_start_date;
		if(this.death_date!=null){ //Death happened			
			
			if(!this.death_date.after(this.censored_date)){ //Death happened before censored date
				setFailed(true);
				svvlen=Days.daysBetween(new DateTime(this.death_date),new DateTime(startdate)).getDays();
				
			}else{//Death happend after censored date, right censored.
				setFailed(false);
				svvlen=Days.daysBetween(new DateTime(this.censored_date),new DateTime(startdate)).getDays();
			}
		}else { //Death not happen
			setFailed(false);
			if(!this.obs_end_date.after(this.censored_date)){
				svvlen=Days.daysBetween(new DateTime(this.obs_end_date),new DateTime(startdate)).getDays();
			}else{
				svvlen=Days.daysBetween(new DateTime(this.censored_date),new DateTime(startdate)).getDays();
			}
		}
		svvlen = Math.abs(svvlen);
		setSurvival_length(svvlen);
		
		setSurvival_start(0);
		setSurvival_end(this.survival_length);
		
	}
	public Date getDis_index_date() {
		return dis_index_date;
	}
	public void setDis_index_date(Date dis_index_date) {
		this.dis_index_date = dis_index_date;
	}
	public Integer getSurvival_length() {
		return survival_length;
	}
	public void setSurvival_length(Integer survival_length) {
		this.survival_length = survival_length;
	}
	public Integer getSurvival_start() {
		return survival_start;
	}
	public void setSurvival_start(Integer survival_start) {
		this.survival_start = survival_start;
	}
	public Integer getSurvival_end() {
		return survival_end;
	}
	public void setSurvival_end(Integer survival_end) {
		this.survival_end = survival_end;
	}
	public boolean isFailed() {
		return failed;
	}
	public double getfailed() {
		if(failed)return 1;
		else return 0;		
		
	}
	public void setFailed(boolean failed) {
		this.failed =failed;
	}

	@Override
	public String toString() {
		return id + "," + obs_start_date
				  + "," + obs_end_date 
				  + "," + death_date 
				  + "," + censored_date
			  	  + "," + survival_length 
			  	  + "," + survival_start
				  + "," + survival_end
				  + "," + failed;
	}

//	public int compare(SurvivalTime o1, SurvivalTime o2) {
//		// TODO Auto-generated method stub
//		int result = Integer.compare(o1.getSurvival_length(), o2.getSurvival_length());
//	    if (result != 0) {
//	      return result;
//	    }
//	    
//		return 0;
//	}
	public int compareTo(SurvivalTime o) {
		// TODO Auto-generated method stub
		int result = Integer.compare(this.getSurvival_length(), o.getSurvival_length());
	    if (result != 0) {
	      return result;
	    }
	    
		return 0;	
	}
	

	
}

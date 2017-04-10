package org.imeds.db;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.imeds.daemon.ImedsDaemonConfig;
import org.imeds.daemon.imedsDaemon;
import org.imeds.data.ComorbidDataSetWorker;
import org.imeds.data.SurvivalTime;
import org.imeds.data.common.CCIDictionary;
import org.imeds.data.common.seqItemPair;
import org.imeds.seqmining.RoutSemantic;
import org.imeds.util.ImedDateFormat;
import org.imeds.util.ImedStringFormat;
import org.imeds.util.LabelType;
import org.imeds.util.writeException;

public class ImedDB {

	public ImedDB() {
		// TODO Auto-generated constructor stub
	}
	protected static Connection conn = null;
	protected static Statement stmt = null;
	
	private static Logger logger = Logger.getLogger(ImedDB.class);
	
	public static void connDB(String dbDriver, String dbURL, String dbUser, String dbPassword, String search_path) throws Exception {
	     
		try{
	            Class.forName(dbDriver);
	       
	            conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);
	            logger.info("SQL Connection to database established!");
	            stmt = conn.createStatement();
	            logger.info("SQL Statement is prepared!");
	            PreparedStatement s = conn.prepareStatement("set search_path to "+search_path+";"); 
	            s.execute(); 
	          
	     } catch (SQLException e) {
	            logger.error("Connection Failed! "+writeException.toString(e));
	            throw e;
	     }
	        
	  }
	public static void closeDB() {

	        try
	        {
	            if(conn != null)conn.close();            
	            if(stmt != null) stmt.close();
	            logger.info("Connection closed !!");
	        } catch (SQLException e) {
	        	logger.error("connection close fail\n"+writeException.toString(e));
	            
	        }
	}  

	public static ArrayList<Integer> getCptCatMap(String range) throws Exception{
        ResultSet rs;
        ArrayList<Integer> value = new ArrayList<Integer>();
        try {
            synchronized (ImedDB.class) {
            	StringBuffer queryStr = new StringBuffer();

            	queryStr.append(" SELECT DISTINCT concept_id FROM condition_vocab_cache WHERE " + range);            	
  //          	System.out.printf(queryStr.toString());
                rs = stmt.executeQuery(queryStr.toString());
        
                while (rs.next()) {
                   value.add(rs.getInt("concept_id"));
//                	System.out.println(rs.getInt("concept_id"));
                }
        
                rs.close();
            }
        }catch (Exception ex) {
            throw ex;
        }
        return value;
	}
	
	public static void  getPatientsWithIndexDiagnoseSurvivalData( HashMap<Long, ArrayList<Double>> value,HashMap<Long, SurvivalTime> patientsSurvival, ArrayList<Integer> cptIdList, ArrayList<String> colList, int sample_start, int sample_end, boolean random) throws Exception{
		  ResultSet rs;
	       
	        try {
	            synchronized (ImedDB.class) {
	            	StringBuffer queryStr = new StringBuffer();  
	            	
	            	queryStr.append(" SELECT * ");
	            	queryStr.append(" FROM ( ");
	            	queryStr.append(" SELECT min(op.observation_period_start_date) as obs_startDate,  max(op.observation_period_end_date)as obs_endDate,t.* ");
	            	queryStr.append(" FROM  observation_period op, ");
	            	queryStr.append("	( SELECT DISTINCT p.person_id as ID, death.death_date as death_date, p.gender_concept_id as Gender, year_of_birth as Age, race_concept_id as Race, ethnicity_concept_id as Ethnicity, ");
	            	queryStr.append(" location_id as Location ");
	            	queryStr.append(" FROM condition_occurrence co, person p ");
	            	queryStr.append(" LEFT OUTER JOIN death  ON (p.person_id = death.person_id)  ");
	            	queryStr.append(" WHERE p.person_id = co.person_id AND condition_concept_id IN ("+ImedStringFormat.tranListIn(cptIdList)+") ");
	            	queryStr.append(" )  AS t");
	            	queryStr.append(" WHERE  t.ID=op.person_id ");
	            	queryStr.append(" GROUP BY op.person_id, t.id, t.gender, t.age, t.race, t.ethnicity, t.location, t.death_date");
	            	queryStr.append(" ORDER BY random() LIMIT "+(sample_end - sample_start)+" OFFSET "+sample_start);
	            	queryStr.append(" ) ORDER BY obs_endDate");
	            	
	            	logger.info("Query getPatientsWithIndexDiagnose :"+ queryStr.toString()+"\n" );
	                rs = stmt.executeQuery(queryStr.toString());
	               
	                while (rs.next()) {
	                   ArrayList<Double> tmp = new ArrayList<Double>();
	 
	                   tmp.add(rs.getDouble("ID"));
	        		   if(colList.contains("Gender")){tmp.add((rs.getDouble("Gender")-8500));}
	        		   if(colList.contains("Age")){ tmp.add(rs.getDouble("Age"));}
	        		   if(colList.contains("Race")) tmp.add(rs.getDouble("Race"));
	        		   if(colList.contains("Ethnicity")) tmp.add(rs.getDouble("Ethnicity"));
	        		   if(colList.contains("Location")){
	        			   if(rs.getDouble("Location")>0)
	        			   tmp.add(rs.getDouble("Location")/10000000);
	        		   }
	        		   value.put(rs.getLong("ID"), tmp);
	        		   
	        		   SurvivalTime st= new SurvivalTime();
	        		   st.setId(rs.getLong("ID"));
	        		   st.setObs_start_date(rs.getDate("obs_startDate"));
	        		   st.setObs_end_date(rs.getDate("obs_endDate"));
	        		   if(rs.getDate("death_date")!=null)st.setDeath_date(rs.getDate("death_date"));
	        		   patientsSurvival.put(rs.getLong("ID"), st);
	                 
	                  
	                }
	        
	                rs.close();
	            }
	        }catch (Exception ex) {
	            throw ex;
	        }
	        
	}
	
	
	public static HashMap<Long, ArrayList<Double>>  getPatientsWithIndexDiagnose(ArrayList<Integer> cptIdList, ArrayList<String> colList, int sample_start, int sample_end, boolean random,  int Label) throws Exception{
		String orderCdt="";
		String labelCdt="";		
		HashMap<Long, ArrayList<Double>> value = new  HashMap<Long, ArrayList<Double>>();

		
		switch(Label){
		case LabelType.death:
			labelCdt = " WHERE label IS NOT NULL "; //death
			break;
		case LabelType.alive:
			labelCdt = " WHERE label IS NULL "; //alive
			break;
		default:
			labelCdt ="";
			break;
		}
		
		if(random){

			orderCdt = " ORDER BY random() LIMIT "+(sample_end - sample_start)+" OFFSET "+sample_start;			
		}else{
			//TODO experiment sample method is waiting for further modify
			orderCdt = " LIMIT "+(sample_end - sample_start)+" OFFSET "+sample_start;	
		}
		value = getPatientsWithIndexDiagnose(cptIdList, colList, labelCdt+orderCdt);
		return value;
	}
	public static  HashMap<Long, ArrayList<Double>> getPatientsWithIndexDiagnose(ArrayList<Integer> cptIdList, ArrayList<String> colList, String Cdt) throws Exception{
        ResultSet rs;
        HashMap<Long, ArrayList<Double>> value = new  HashMap<Long, ArrayList<Double>>();
        try {
            synchronized (ImedDB.class) {
//				SELECT * from (
//				select p.person_id as ID, p.gender_concept_id as Gender, year_of_birth as Age, race_concept_id as Race, ethnicity_concept_id as Ethnicity, location_id as Location, death.person_id as Label
//				FROM condition_occurrence co, person p LEFT OUTER JOIN death  ON (p.person_id = death.person_id)  
//				WHERE p.person_id = co.person_id AND condition_concept_id 
//
//				IN (201820,201826,201254,40482801,40484648,443727,443734,439770,4096666,201530,201531,443592,443735,321822,443729,318712)
//				) --WHERE label IS  NULL
//				ORDER BY random()
//				LIMIT 1000;
            	StringBuffer queryStr = new StringBuffer();            	
            	queryStr.append(" SELECT * FROM (");
            	queryStr.append(" SELECT DISTINCT p.person_id as ID, p.gender_concept_id as Gender, year_of_birth as Age, race_concept_id as Race, ethnicity_concept_id as Ethnicity, location_id as Location, death.person_id as Label ");            	
            	queryStr.append(" FROM condition_occurrence co, person p LEFT OUTER JOIN death  ON (p.person_id = death.person_id) ");
            	queryStr.append(" WHERE p.person_id = co.person_id AND condition_concept_id IN ("+ImedStringFormat.tranListIn(cptIdList)+")");
            	queryStr.append(" ) "+Cdt);
            	
            	logger.info("Query getPatientsWithIndexDiagnose :"+ queryStr.toString()+"\n" );
                rs = stmt.executeQuery(queryStr.toString());
               
                while (rs.next()) {
                   ArrayList<Double> tmp = new ArrayList<Double>();
 
                   tmp.add(rs.getDouble("ID"));
        		   if(rs.getLong("Label")>0) tmp.add((double) LabelType.death);  //means death
                   else tmp.add((double) LabelType.alive);
        		   if(colList.contains("Gender")){tmp.add((rs.getDouble("Gender")-8500));}
        		   if(colList.contains("Age")){
            		   Date now = new Date();
        			   Calendar cal = Calendar.getInstance();
        			   cal.setTime(now);
        			    int year = cal.get(Calendar.YEAR);
        			   tmp.add((year-rs.getDouble("Age"))/10);
        		   }
        		   if(colList.contains("Race")) tmp.add(rs.getDouble("Race"));
        		   if(colList.contains("Ethnicity")) tmp.add(rs.getDouble("Ethnicity"));
        		   if(colList.contains("Location")){
        			   if(rs.getDouble("Location")>0)
        			   tmp.add(rs.getDouble("Location")/10000000);
        		   }
                   
                  value.put(rs.getLong("ID"), tmp);
                }
        
                rs.close();
            }
        }catch (Exception ex) {
            throw ex;
        }
        return value;
	}

	public static Date getPatientDisIndexDay(Long pid, ArrayList<Integer> cptIdList) throws Exception{
		ResultSet rs;
        ArrayList<Integer> value = new ArrayList<Integer>();
        try {
            synchronized (ImedDB.class) {
            	StringBuffer queryStr = new StringBuffer();

            	queryStr.append(" SELECT person_id, condition_start_date as START_TIME " );
            	queryStr.append(" FROM condition_occurrence co_d ");
            	queryStr.append(" WHERE condition_concept_id IN ("+ImedStringFormat.tranListIn(cptIdList)+") AND person_id ="+pid);
            	queryStr.append(" ORDER BY condition_start_date LIMIT 1");            	            	
            	logger.info("Query getPatientDisFeature "+queryStr.toString()+"\n");
                rs = stmt.executeQuery(queryStr.toString());
                
                if (rs.next()) {
                   ResultSet fs;
                   queryStr = new StringBuffer();
                   Date IdxDisStart = rs.getTimestamp("START_TIME");  
                   return IdxDisStart;
                  
                }
        
                rs.close();
            }
        }catch (Exception ex) {
            throw ex;
        }
       return null;
	}
	public static ArrayList<Integer> getPatientDisFeature(Long pid, ArrayList<Integer> cptIdList,SurvivalTime svtime) throws Exception{
        ResultSet rs;
        ArrayList<Integer> value = new ArrayList<Integer>();
        try {
            synchronized (ImedDB.class) {
               StringBuffer queryStr = new StringBuffer();
               queryStr = new StringBuffer();
               Date IdxDisStart = getPatientDisIndexDay(pid, cptIdList);
               
               if(svtime!=null)svtime.setDis_index_date(IdxDisStart);
               queryStr.append(" SELECT DISTINCT  condition_concept_id FROM condition_occurrence "); 
               queryStr.append(" WHERE person_id = "+pid+" AND condition_start_date <= '"+ new ImedDateFormat().format(IdxDisStart)+"'");
              
               logger.info("Query getPatientDisFeature "+queryStr.toString()+"\n");
               rs = stmt.executeQuery(queryStr.toString());
               while(rs.next()){
            	   value.add(rs.getInt("condition_concept_id"));                	   
               }
               rs.close();
               
            }
        }catch (Exception ex) {
            throw ex;
        }
        return value;
	}
	
	public static ArrayList<seqItemPair> getPatientDrugFeature(Long pid, ArrayList<Integer> cptIdList) throws Exception{
        ResultSet rs;
        ArrayList<seqItemPair> value = new ArrayList<seqItemPair>();
        try {
            synchronized (ImedDB.class) {
               StringBuffer queryStr = new StringBuffer();
             
               queryStr = new StringBuffer();
               Date IdxDisStart = getPatientDisIndexDay(pid, cptIdList);                              
               queryStr.append(" SELECT de.drug_era_start_date as date_t, de.drug_concept_id as concept_id ");
               queryStr.append(" FROM drug_era de"); 
               queryStr.append(" WHERE de.person_id = "+pid+" AND de.drug_era_start_date >= '"+ new ImedDateFormat().format(IdxDisStart)+"'");
               queryStr.append(" ORDER BY de.drug_era_start_date,de.drug_concept_id ");
              
               logger.info("Query getPatientDrugFeature "+queryStr.toString()+"\n");
               rs = stmt.executeQuery(queryStr.toString());
               while(rs.next()){
            	   value.add(new seqItemPair(rs.getDate("date_t"), rs.getInt("concept_id")));                	   
               }
               rs.close();
                
            }
        }catch (Exception ex) {
            throw ex;
        }
        return value;
	}
/**	public static ArrayList<Integer> getPatientDisFeature(Long pid, ArrayList<Integer> cptIdList) throws Exception{
        ResultSet rs;
        ArrayList<Integer> value = new ArrayList<Integer>();
        try {
            synchronized (ImedDB.class) {
            	StringBuffer queryStr = new StringBuffer();

            	queryStr.append(" SELECT person_id, condition_start_date as START_TIME " );
            	queryStr.append(" FROM condition_occurrence co_d ");
            	queryStr.append(" WHERE condition_concept_id IN ("+ImedStringFormat.tranListIn(cptIdList)+") AND person_id ="+pid);
            	queryStr.append(" ORDER BY condition_start_date LIMIT 1");            	            	
            	logger.info("Query getPatientDisFeature "+queryStr.toString()+"\n");
                rs = stmt.executeQuery(queryStr.toString());
                
                if (rs.next()) {
                   ResultSet fs;
                   queryStr = new StringBuffer();
                   Date IdxDisStart = rs.getTimestamp("START_TIME");  
                   
                   
                   queryStr.append(" SELECT DISTINCT  condition_concept_id FROM condition_occurrence "); 
                   queryStr.append(" WHERE person_id = "+pid+" AND condition_start_date <= '"+ new ImedDateFormat().format(IdxDisStart)+"'");
                  
                   logger.info("Query getPatientDisFeature "+queryStr.toString()+"\n");
                   fs = stmt.executeQuery(queryStr.toString());
                   while(fs.next()){
                	   value.add( fs.getInt("condition_concept_id"));                	   
                   }
                   fs.close();
                }
        
                rs.close();
            }
        }catch (Exception ex) {
            throw ex;
        }
        return value;
	}**/
	
	public static HashMap<Long, ArrayList<seqItemPair>> getOutlierPatient(Integer fileId, Double riThld) throws Exception{
		HashMap<Long, ArrayList<seqItemPair>> patients = new HashMap<Long, ArrayList<seqItemPair>>();
		ResultSet rs;
		 try {
	            synchronized (ImedDB.class) {
	            	StringBuffer queryStr = new StringBuffer();
	
	            	queryStr.append(" SELECT distinct id " );
	            	queryStr.append(" FROM ucla.lroutliers uts ");
	            	queryStr.append(" WHERE uts.fileid = "+fileId+" AND uts.ri>="+riThld);
	            	            	
	            	logger.info("Query getOutlierPatient "+queryStr.toString()+"\n");
	                rs = stmt.executeQuery(queryStr.toString());
	                
	               while (rs.next()) {
	                   patients.put(rs.getLong("id"), new ArrayList<seqItemPair>());
	                }
	        
	                rs.close();
	            }
	        }catch (Exception ex) {
	            throw ex;
	        }
	        
		return patients;
	}
/**
	public static ArrayList<seqItemPair> getPatientDrugFeature(Long pid, ArrayList<Integer> cptIdList) throws Exception{
        ResultSet rs;
        ArrayList<seqItemPair> value = new ArrayList<seqItemPair>();
        try {
            synchronized (ImedDB.class) {
            	StringBuffer queryStr = new StringBuffer();

            	queryStr.append(" SELECT person_id, condition_start_date as START_TIME " );
            	queryStr.append(" FROM condition_occurrence co_d ");
            	queryStr.append(" WHERE condition_concept_id IN ("+ImedStringFormat.tranListIn(cptIdList)+") AND person_id ="+pid);
            	queryStr.append(" ORDER BY condition_start_date LIMIT 1");            	            	
            	logger.info("Query getPatientDrugFeature "+queryStr.toString()+"\n");
                rs = stmt.executeQuery(queryStr.toString());
         
                if (rs.next()) {
                   ResultSet fs;
                   queryStr = new StringBuffer();
                   Date IdxDisStart = rs.getTimestamp("START_TIME");                  
                   queryStr.append(" SELECT de.drug_era_start_date as date_t, de.drug_concept_id as concept_id ");
                   queryStr.append(" FROM drug_era de"); 
                   queryStr.append(" WHERE de.person_id = "+pid+" AND de.drug_era_start_date >= '"+ new ImedDateFormat().format(IdxDisStart)+"'");
                   queryStr.append(" ORDER BY de.drug_era_start_date,de.drug_concept_id ");
                  
                   logger.info("Query getPatientDrugFeature "+queryStr.toString()+"\n");
                   fs = stmt.executeQuery(queryStr.toString());
                   while(fs.next()){
                	   value.add(new seqItemPair(fs.getDate("date_t"), fs.getInt("concept_id")));                	   
                   }
                   fs.close();
                }
        
                rs.close();
            }
        }catch (Exception ex) {
            throw ex;
        }
        return value;
	}**/
	public static void getDisSemanticConcept(HashMap<Integer, String> cptmap) throws Exception{
        ResultSet rs = null;
        try {
            synchronized (ImedDB.class) {
            	StringBuffer queryStr = new StringBuffer();
            	String cptkeys = cptmap.keySet().toString();
            	cptkeys = cptkeys.substring(1, cptkeys.length()-1);
            	queryStr.append(" SELECT concept_id, concept_name ");
            	queryStr.append(" FROM  vocabulary.concept ");
            	queryStr.append(" WHERE concept_id in ("+cptkeys+")");
            	logger.debug(queryStr.toString());
                   rs = stmt.executeQuery(queryStr.toString());
                   while(rs.next()){
                	   cptmap.put(rs.getInt("concept_id"), rs.getString("concept_name"));            	   
                   }
                rs.close();
            }
        }catch (Exception ex) {
            throw ex;
        }
   
	}
	public static Integer getOutlierFileId(String filename) throws Exception{
        ResultSet rs = null;
        Integer fileId = -1;
        filename = filename.replace("\\", "\\\\");
        try {
            synchronized (ImedDB.class) {
            	StringBuffer queryStr = new StringBuffer();
            	
            	queryStr.append(" SELECT fileId ");
            	queryStr.append(" FROM  "+ImedsDaemonConfig.getPrivate_search_path()+".LrOutlierFile ");
            	queryStr.append(" WHERE filename ='"+ filename +"'");

                   rs = stmt.executeQuery(queryStr.toString());
             //      logger.debug(queryStr.toString());
                   if(rs.next()){
                	  fileId = rs.getInt("fileId");
                	  String deloutlier = "delete from "+ImedsDaemonConfig.getPrivate_search_path()+".LrOutliers where fileId ="+fileId;
                	  stmt.executeUpdate(deloutlier);
                   }else{
                	 
	                	   String queryMaxId = "select (max(fileId)+1) nextId  from "+ImedsDaemonConfig.getPrivate_search_path()+".LrOutlierFile ";
	                	   rs = stmt.executeQuery(queryMaxId);
	                	   if(rs.next())fileId = rs.getInt("nextId");
	           //     	   logger.debug(queryMaxId);
	                	   String sql = "INSERT INTO " + ImedsDaemonConfig.getPrivate_search_path()+".LrOutlierFile "+
	                               "VALUES ("+fileId+", '"+filename+"',getdate())";
	                	   stmt.executeUpdate(sql);
	           //     	   logger.debug(sql);
	                	   rs = stmt.executeQuery(queryStr.toString());
	                       if(rs.next()){
	                    	  fileId = rs.getInt("fileId"); 
	                       }
                	  }
                   
                rs.close();
            }
        }catch (Exception ex) {
            throw ex;
        }
        return fileId;
	}
	public static void writeOutlier(Map<Long, ArrayList<Double>> list, Integer fileId) throws Exception{
       
        try {
            synchronized (ImedDB.class) {
            	StringBuffer queryStr = new StringBuffer();
            	
            	queryStr.append(" insert into "+ImedsDaemonConfig.getPrivate_search_path()+".LrOutliers values");

                Iterator<Entry<Long, ArrayList<Double>>> iter =list.entrySet().iterator();
   		        
       			while (iter.hasNext()) { 
       				Entry<Long, ArrayList<Double>> entry = iter.next(); 
       				
                	queryStr.append("("+fileId+","+entry.getKey()+","+entry.getValue().get(0)+","+entry.getValue().get(1)+","+entry.getValue().get(2)+"),");
       				
       			}   
       			queryStr.deleteCharAt(queryStr.lastIndexOf(","));
       			//logger.debug(queryStr.toString());
       			stmt.executeUpdate(queryStr.toString());
              
            }
        }catch (Exception ex) {
            throw ex;
        }
   
	}
	public static Integer getFileId(String filename, String tableName) throws Exception{
        ResultSet rs = null;
        Integer fileId = -1;
        filename = filename.replace("\\", "\\\\");
        try {
            synchronized (ImedDB.class) {
            	StringBuffer queryStr = new StringBuffer();
            	
            	queryStr.append(" SELECT fileId ");
            	queryStr.append(" FROM  "+ImedsDaemonConfig.getPrivate_search_path()+".LrOutlierFile ");
            	queryStr.append(" WHERE filename ='"+ filename +"'");

                   rs = stmt.executeQuery(queryStr.toString());
                   logger.debug(queryStr.toString());
                   if(rs.next()){
                	  fileId = rs.getInt("fileId");
                	  deleteFile(fileId, tableName);
                   }else{
                	 
	                	   String queryMaxId = "select (max(fileId)+1) nextId  from "+ImedsDaemonConfig.getPrivate_search_path()+".LrOutlierFile ";
	                	   rs = stmt.executeQuery(queryMaxId);
	                	   if(rs.next())fileId = rs.getInt("nextId");
	                	   String sql = "INSERT INTO " + ImedsDaemonConfig.getPrivate_search_path()+".LrOutlierFile "+
	                               "VALUES ("+fileId+", '"+filename+"',getdate())";
	                	   logger.debug(sql);
	                	   stmt.executeUpdate(sql);
	                	   rs = stmt.executeQuery(queryStr.toString());
	                       if(rs.next()){
	                    	  fileId = rs.getInt("fileId"); 
	                       }
                	  }
                   
                rs.close();
            }
        }catch (Exception ex) {
            throw ex;
        }
        return fileId;
	}
	public static Integer deleteFile(Integer fileId,String tableName) throws Exception{
       
        try {
            synchronized (ImedDB.class) {
            	String deloutlier = "delete from "+ImedsDaemonConfig.getPrivate_search_path()+"."+tableName+" where fileId ="+fileId;
            	stmt.executeUpdate(deloutlier);                
            }
        }catch (Exception ex) {
            throw ex;
        }
        return fileId;
	}
	
	public static void InsertFileContent(ArrayList<String> list, Integer fileId,String tableName) throws Exception{
	       
        try {
            synchronized (ImedDB.class) {
            	StringBuffer queryStr = new StringBuffer();
            	
            	queryStr.append(" insert into "+ImedsDaemonConfig.getPrivate_search_path()+"."+tableName+" values");
                
       			for(String str:list){	       				
                	queryStr.append("("+fileId+","+str+"),");       				
       			}   
       			queryStr.deleteCharAt(queryStr.lastIndexOf(","));
       			logger.debug(queryStr.toString());
       			stmt.executeUpdate(queryStr.toString());
              
            }
        }catch (Exception ex) {
            throw ex;
        }  
	}
	
	public static void InsertRoutSemanticRel(RoutSemantic rstc ) throws Exception{
		ResultSet rs = null;   
        try {
            synchronized (ImedDB.class) {
        		
//        		for(RoutSemantic rstc:RoutSemanticRel){
	        		StringBuffer queryStr = new StringBuffer();
	            	queryStr.append(" SELECT RoutId ");
	            	queryStr.append(" FROM  "+ImedsDaemonConfig.getPrivate_search_path()+".RoutSemantic ");
	            	queryStr.append(" WHERE RoutId ="+ rstc.getRoutId());
	
	                   rs = stmt.executeQuery(queryStr.toString());
	                   logger.debug(queryStr.toString());
	                   if(rs.next()){
	                	  
	                	String deloutlier = "delete from "+ImedsDaemonConfig.getPrivate_search_path()+".RoutSemantic where RoutId ="+rstc.getRoutId();
	                  	stmt.executeUpdate(deloutlier); 
	                   }
	                	 
                	   String sql = "INSERT INTO " + ImedsDaemonConfig.getPrivate_search_path()+".RoutSemantic "+
                               "VALUES ("+rstc.getRoutId()+","+rstc.getSemanticSeqId()+","+rstc.getAttrNum()+")";
                	   logger.debug(sql);
                	   stmt.executeUpdate(sql);
		                
//        		}   
                rs.close();
        		
            }
        }catch (Exception ex) {
            throw ex;
        }  
	}
	
	
//	public static String tranListIn(ArrayList<Integer> lst){
//		StringBuffer str = new StringBuffer();
//		
//		for(Integer id: lst){
//			str.append(id+",");
//		}
//		str.delete(str.lastIndexOf(","), str.length());
//		return str.toString();
//	}
	public static void main(String[] args) throws FileNotFoundException {
		
		ImedDB.logger.info("test");
	}
}

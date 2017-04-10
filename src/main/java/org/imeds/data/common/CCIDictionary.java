package org.imeds.data.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.imeds.db.ImedDB;
import org.imeds.util.CCIcsvTool;

public class CCIDictionary {
	private String CCIstandard;
	private String CCIfileName;
	protected CCIcsvTool csvCCI = new CCIcsvTool();
	private HashMap<String, CCIcode> codeList = new HashMap<String, CCIcode>();
	private HashMap<Integer,Integer> cptCat = new HashMap<Integer, Integer>();
	public CCIDictionary() {
		// TODO Auto-generated constructor stub
	}
	public CCIDictionary(String fileName) {
		// TODO Auto-generated constructor stub
		this.CCIfileName = fileName;
	}
	public String getCCIstandard() {
		return CCIstandard;
	}
	public void setCCIstandard(String cCIstandard) {
		CCIstandard = cCIstandard;
	}
	public String getCCIfileName() {
		return CCIfileName;
	}
	public void setCCIfileName(String cCIfileName) {
		CCIfileName = cCIfileName;
	}
	public HashMap<String, CCIcode> getCodeList() {
		return codeList;
	}
	public void setCodeList( HashMap<String, CCIcode> codeList) {
		this.codeList = codeList;
	}
	
	public HashMap<Integer, Integer> getCptCat() {
		return cptCat;
	}
	public void setCptCat(HashMap<Integer, Integer> cptCat) {
		this.cptCat = cptCat;
	}
	public void buildDictionary(){
		this.csvCCI.DeyoCCIparserDoc(this.CCIfileName, this.codeList);
		Iterator<Entry<String, CCIcode>> iter = codeList.entrySet().iterator(); 
		while (iter.hasNext()) { 
			Entry<String, CCIcode> entry = iter.next(); 
//		    Object key = entry.getKey(); 
		    CCIcode code = (CCIcode) entry.getValue(); 
			code.tranIcdList();
//		    System.out.println(entry.toString());
		} 
		
//		System.out.println(this.codeList.toString());
	}
	
	public void buildCptMap() throws Exception{
		Iterator<Entry<String, CCIcode>> iter = codeList.entrySet().iterator(); 

		while (iter.hasNext()) { 
			ArrayList<Integer> cptTmp = new ArrayList<Integer>();
			Entry<String, CCIcode> entry = iter.next(); 
		    CCIcode code = (CCIcode) entry.getValue(); 
			
		    String range = getRangeStr(code.getIcdDouble());
		
		    cptTmp = ImedDB.getCptCatMap(range);		    
		    code.setIcdCptId(cptTmp);
		    
		    for(Integer cid:cptTmp){
		    	this.cptCat.put(cid, code.getID());
		    }
		} 
	}
	
	public String getRangeStr(ArrayList<IcdPair> pair){

		StringBuffer str = new StringBuffer();
		for(IcdPair ipr:pair){
			str.append(" (source_concept_code >= '"+ipr.getSStart()+"' AND source_concept_code < '"+ipr.getSEnd()+"')	OR ");			
		}
		
		str.delete(str.lastIndexOf("OR"), str.length()-1);
	
		return str.toString();
		
	}
	
	
}

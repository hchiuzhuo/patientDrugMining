package org.imeds.seqmining;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.imeds.db.ImedDB;
import org.imeds.util.CCIcsvTool;
import org.imeds.util.OSValidator;

public class seqRStoDB {
	
	private HashMap<String, Integer> semanticSeqFileId=new HashMap<String, Integer>();
	private ArrayList<RoutSemantic> RoutSemanticRel=new ArrayList<RoutSemantic>();
	public  void processSemanticSeqFile(String infolderName) throws Exception{
		File directory = new File(infolderName);
		File[] fList = directory.listFiles();
		Integer fileId=-1;
		String fileName;
		String semanticSeqTableName="semanticSeq";
		for (File file : fList){		
			if (file.isFile() && file.getName().contains("semantic")){
				fileName = infolderName+OSValidator.getPathSep()+file.getName();
				ArrayList<String> strList= new ArrayList<String>();
				CCIcsvTool.semanticSeqParserDoc(fileName,strList); 
				fileId = ImedDB.getFileId(fileName, semanticSeqTableName);
				ImedDB.InsertFileContent(strList, fileId,semanticSeqTableName);
				semanticSeqFileId.put(file.getName(), fileId);
			}
		}
		
	}
	public  void processRoutFile(String infolderName) throws Exception{
		File directory = new File(infolderName);
		File[] fList = directory.listFiles();
		Integer fileId=-1;
		String fileName;
		String TableName="Rout";
		for (File file : fList){		
			if (file.isFile()){
				fileName = infolderName+OSValidator.getPathSep()+file.getName();
				ArrayList<String> strList= new ArrayList<String>();
				CCIcsvTool.RoutParserDoc(fileName,strList); 
				fileId = ImedDB.getFileId(fileName, TableName);
				ImedDB.InsertFileContent(strList, fileId,TableName);
				RoutSemantic rstc=formRoutSemanticRel(file.getName(), fileId);
				ImedDB.InsertRoutSemanticRel(rstc); 
			}
		}
		
		
	}

	public RoutSemantic formRoutSemanticRel(String RoutfileName, Integer fileId){
		String fileStr=RoutfileName.substring(0, RoutfileName.lastIndexOf("_"));
		String semanticSeqName ="semantic_"+fileStr+".csv";
		Integer semanticSeqId =semanticSeqFileId.get(semanticSeqName) ;
		
		String rnum=RoutfileName.substring(RoutfileName.lastIndexOf("_")+1, RoutfileName.lastIndexOf("."));
		Integer attrNum=Integer.parseInt(rnum);
		
		RoutSemantic rsmc = new RoutSemantic();
		rsmc.setRoutId(fileId);
		rsmc.setRoutName(RoutfileName);
		rsmc.setAttrNum(attrNum);
		rsmc.setSemanticSeqId(semanticSeqId);
		rsmc.setSemanticSeqName(semanticSeqName);
		RoutSemanticRel.add(rsmc);
		return rsmc;
		
	}
	public void main(){
		
	}
}

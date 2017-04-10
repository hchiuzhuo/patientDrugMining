package org.imeds.data.outlier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.imeds.daemon.ImedsDaemonConfig;
import org.imeds.data.ComorbidDataSetConfig;
import org.imeds.data.SparkLRDataSetWorker.DataPoint;
import org.imeds.db.ImedDB;
import org.imeds.util.CCIcsvTool;
import org.imeds.util.ComorbidDSxmlTool;
import org.imeds.util.OSValidator;
import org.imeds.util.writeException;

public abstract class Outlier {	
	protected Double threshold = 0.0;
	protected String configFile="";
	protected ComorbidDataSetConfig cdsc = new ComorbidDataSetConfig();
	protected ComorbidDSxmlTool cfgparser = new ComorbidDSxmlTool();
	protected CCIcsvTool csvparser = new CCIcsvTool();
	protected String lrFolder;
	protected String olFolder;
	protected String targetfileName;
	protected static Logger logger = Logger.getLogger(Outlier.class);

	public Outlier() {
		
	}
	public Outlier(String configpath) {
		this.configFile = configpath;
		this.cfgparser.parserDoc(this.configFile,this.cdsc);
		
		this.lrFolder = this.cdsc.getPearsonResidualOutlierInputFolder();
		if(!OSValidator.isWindows()){this.lrFolder = this.lrFolder.replace("\\", "/");}
		
		this.olFolder = this.cdsc.getPearsonResidualOutlierOutputFolder();
		if(!OSValidator.isWindows()){this.olFolder = this.olFolder.replace("\\", "/");}
		
		this.threshold = this.cdsc.getPearsonResidualThreshold();
		
		this.targetfileName = this.cdsc.getTargetFileName();
	}
	
	public ComorbidDSxmlTool getCfgparser() {
		return cfgparser;
	}
	public void setCfgparser(ComorbidDSxmlTool cfgparser) {
		this.cfgparser = cfgparser;
	}
	
	public CCIcsvTool getCsvparser() {
		return csvparser;
	}
	public void setCsvparser(CCIcsvTool csvparser) {
		this.csvparser = csvparser;
	}
	public void writeOulierToDB() {
		  	File directory = new File(this.olFolder);
			File[] fList = directory.listFiles();
			for (File file : fList){		
				if (file.isFile()){
					
					String filename = file.getName();		
					String totalPath = this.olFolder+OSValidator.getPathSep()+file.getName();
					try {
						Integer fileId = ImedDB.getOutlierFileId(totalPath);
						this.logger.info("prepare to write <file Id "+fileId+"> "+totalPath+" into DB.");
						CCIcsvTool.OutlierParserDoc(totalPath, ImedsDaemonConfig.getFlush(), fileId, this.logger);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						this.logger.error("fail to write outlier file "+totalPath+"\n"+writeException.toString(e));						
					}
//					init(this.lrFolder+OSValidator.getPathSep()+filename, this.olFolder+OSValidator.getPathSep()+filename.substring(0, filename.indexOf("."))+"_prol.csv", this.threshold);
					
//					CCIcsvTool.OutlierCreateDoc(this.outFileName,  (HashMap<Long, ArrayList<Double>>) this.OutlierList);
				}
			}
			
		}  
	public abstract void init();
	public abstract void oulierGen();
}

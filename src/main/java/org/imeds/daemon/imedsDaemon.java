package org.imeds.daemon;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imeds.data.ComorbidDataSetWorker;
import org.imeds.data.common.CCIDictionary;
import org.imeds.db.ImedDB;
import org.imeds.db.ImedR;
import org.imeds.util.OSValidator;
import org.imeds.util.writeException;


public class imedsDaemon {
	/*####################################################################################*
     * Static fields
     *####################################################################################*/

    private static imedsDaemon singleton = null;
    
    private static Logger logger = Logger.getLogger(imedsDaemon.class);
    private String ImedsDaemonConfigPath = "data\\IMEDS\\ImedsDaemonConfig.xml";
    private String DeyoCCIPath = "data\\IMEDS\\DeyoCCI.csv";
    private ImedsManager manager ;
	public imedsDaemon() {
		 
	   
		OsPathChk();
		String log4jpath = "target\\log4j.properties" ;
		if(!OSValidator.isWindows()){
			log4jpath =log4jpath.replace("\\", "/");			
		}
		 PropertyConfigurator.configure(log4jpath ); 
		logger.info("Load data\\IMEDS\\ImedsDaemonConfig.xml.");
		ImedsDaemonConfig.loadImedsDaemonConfig(ImedsDaemonConfigPath);
		
	
		if(ImedsDaemonConfig.isOmopDbEnable()){
			logger.info("IMED database connection.");
			DbInit();
		}else{
			logger.info("Run without IMED database.");
		}

		RInit();
		
	}
	/*####################################################################################*
     * Public Instance methods
     *####################################################################################*/
	public void OsPathChk(){
		if(!OSValidator.isWindows()){
			ImedsDaemonConfigPath =ImedsDaemonConfigPath.replace("\\", "/");
			DeyoCCIPath = DeyoCCIPath.replace("\\", "/");
		}
//		System.out.println(ImedsDaemonConfigPath);
	}
    public void DbInit(){
    
		try {
			ImedDB.connDB(ImedsDaemonConfig.getOmopDbDriver(), ImedsDaemonConfig.getOmopDbUrl(),ImedsDaemonConfig.getOmopDbUser(), ImedsDaemonConfig.getOmopDbPassword(), ImedsDaemonConfig.getOmopDbSearchPath());
		} catch (Exception e) {
			logger.error("IMED database connection fail!");
			logger.error(writeException.toString(e));
		}
    }
    public static void DbClose(){
    		ImedDB.closeDB();
    }
    public void RInit(){
        
		try {
			ImedR.connR();
		} catch (Exception e) {
			logger.error("R connection fail!");
			logger.error(writeException.toString(e));
		}
    }
    public static void RClose(){
    		ImedR.closeR();
    }
   
    public void service() {
        logger.info("Start scheduling.....");
    		manager = new ComorbidManager(DeyoCCIPath);
    		manager.run();
    		
    		manager = new SeqptnManager();
    		manager.run();
    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		singleton = new imedsDaemon();
		singleton.service();
		//DbClose();
		RClose();
	}

}

package org.imeds.feature.selection;

import java.util.HashMap;
import java.util.Map;

import org.imeds.util.CCIcsvTool;


public class DataLabelFactory {
	public static final int TYPE_OUTLIER_PATIENT_CSV					= 0001;
	public DataLabelFactory() {
		// TODO Auto-generated constructor stub
	}
	public static HashMap<Long, Integer>  createDataLabel(Integer type,  MMRFSConfig  cfg) {
		HashMap<Long, Integer> labelItemSet=null;
		
		switch (type) {
		case TYPE_OUTLIER_PATIENT_CSV:
//			labelItemSet = CCIcsvTool.OutlierParserDoc(cfg.getCsvFileName(),cfg.getLabelDefineThreshold());
		}
		return labelItemSet;
	}
}

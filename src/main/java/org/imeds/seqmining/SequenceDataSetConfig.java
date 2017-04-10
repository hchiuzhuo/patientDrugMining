package org.imeds.seqmining;

import java.util.ArrayList;

import org.imeds.data.common.CCIDictionary;
import org.imeds.feature.screening.ModelFreeScreen.MFStype;

public class SequenceDataSetConfig {
	
	private boolean enable=false;
	private String inputFolder; //seqDataset input folder, preSeq
	private String outputFolder; //seqDataset output folder, seqDS
	private boolean VMSPenable=false;
	private ArrayList<Integer> VMSPMaxLen = new ArrayList<Integer>();
	private ArrayList<Double> VMSPthreshold = new ArrayList<Double>();
	private String VMSPinputFolder;
	private String VMSPoutputFolder;
	
	private boolean MMRFSenable=false;
	private String MMRFSbasicItemsetsFolder;
	private String MMRFSdiscrimItemsetsFolder;
	private String MMRFSoutlierSourceFolder;
	private String MMRFSlabelBase;
	private ArrayList<Double> MMRFSlabelDefineThreshold = new ArrayList<Double>();
	private ArrayList<Double> MMRFScoverage = new ArrayList<Double>();		
	private String MMRFSfeatureItemsetFolder;
	
	private boolean SVIenable=false;
	private String SVIverticalSeqFile;
	private String SVIsurvivalFile;
	private String SVIfilterFile;
	private String SVIfilterCriteria;
	private String SVIoutputFileName;
	private Double SVIoutlierThreshold;
	
	private boolean SVIFSenable=false;
	private String SVIFSsurvivalFile;
	private String SVIFSseqFile;
	private String SVIFSseqptnFolder;
	private String SVIFSseqCoxFolder;
	private String SVIFSseqFeatureFolder;
	private String SVIFSseqPreSemanticFolder;
	
	private boolean MFreeenable=false;
	private Integer MFreexStart;
	private ArrayList<String> MFreeyTitle =new ArrayList<String>();

	private Integer MFreecoxIter;
	
	private Integer MFreefeatureStart;
	private Integer MFreeStep;
	private Integer MFreefeatureStop;

	private MFStype MFreescreenType;
	private String MFreeaucFolder;
	private ArrayList<String> MFreefeatureScore=new ArrayList<String>();	
	private String MFreefeatureDescription;

	public String getInputFolder() {
		return inputFolder;
	}

	public void setInputFolder(String inputFolder) {
		this.inputFolder = inputFolder;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getVMSPinputFolder() {
		return VMSPinputFolder;
	}

	public void setVMSPinputFolder(String vMSPinputFolder) {
		VMSPinputFolder = vMSPinputFolder;
	}

	public String getVMSPoutputFolder() {
		return VMSPoutputFolder;
	}

	public void setVMSPoutputFolder(String vMSPoutputFolder) {
		VMSPoutputFolder = vMSPoutputFolder;
	}

	public ArrayList<Double> getVMSPthreshold() {
		return VMSPthreshold;
	}

	public void setVMSPthreshold(ArrayList<Double> vMSPthreshold) {
		VMSPthreshold = vMSPthreshold;
	}
	public void addVMSPthreshold(Double vMSPthreshold) {
		this.VMSPthreshold.add(vMSPthreshold);
	}

	public ArrayList<Integer> getVMSPMaxLen() {
		return VMSPMaxLen;
	}

	public void setVMSPMaxLen(ArrayList<Integer> vMSPMaxLen) {
		VMSPMaxLen = vMSPMaxLen;
	}
	public void addVMSPMaxLen(Integer MaxLen) {
		this.VMSPMaxLen.add(MaxLen);
	}
	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public Boolean getVMSPenable() {
		return VMSPenable;
	}

	public void setVMSPenable(Boolean vMSPenable) {
		VMSPenable = vMSPenable;
	}

	public boolean isMMRFSenable() {
		return MMRFSenable;
	}

	public void setMMRFSenable(boolean mMRFSenable) {
		MMRFSenable = mMRFSenable;
	}

	public String getMMRFSbasicItemsetsFolder() {
		return MMRFSbasicItemsetsFolder;
	}

	public void setMMRFSbasicItemsetsFolder(String mMRFSbasicItemsetsFolder) {
		MMRFSbasicItemsetsFolder = mMRFSbasicItemsetsFolder;
	}

	public String getMMRFSdiscrimItemsetsFolder() {
		return MMRFSdiscrimItemsetsFolder;
	}

	public void setMMRFSdiscrimItemsetsFolder(String mMRFSdiscrimItemsetsFolder) {
		MMRFSdiscrimItemsetsFolder = mMRFSdiscrimItemsetsFolder;
	}

	public String getMMRFSoutlierSourceFolder() {
		return MMRFSoutlierSourceFolder;
	}

	public void setMMRFSoutlierSourceFolder(String mMRFSoutlierSourceFolder) {
		MMRFSoutlierSourceFolder = mMRFSoutlierSourceFolder;
	}

	public ArrayList<Double> getMMRFSlabelDefineThreshold() {
		return MMRFSlabelDefineThreshold;
	}

	public void setMMRFSlabelDefineThreshold(
			ArrayList<Double> mMRFSlabelDefineThreshold) {
		MMRFSlabelDefineThreshold = mMRFSlabelDefineThreshold;
	}
	public void addMMRFSlabelDefineThreshold(Double mMRFSlabelDefineThreshold) {
		this.MMRFSlabelDefineThreshold.add(mMRFSlabelDefineThreshold);
	}

	public ArrayList<Double> getMMRFScoverage() {
		return MMRFScoverage;
	}

	public void setMMRFScoverage(ArrayList<Double> mMRFScoverage) {
		MMRFScoverage = mMRFScoverage;
	}
	public void addMMRFScoverage(Double mMRFScoverage) {
		this.MMRFScoverage.add(mMRFScoverage);
	}

	public String getMMRFSfeatureItemsetFolder() {
		return MMRFSfeatureItemsetFolder;
	}

	public void setMMRFSfeatureItemsetFolder(String mMRFSfeatureItemsetFolder) {
		MMRFSfeatureItemsetFolder = mMRFSfeatureItemsetFolder;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public void setVMSPenable(boolean vMSPenable) {
		VMSPenable = vMSPenable;
	}

	public SequenceDataSetConfig() {
		// TODO Auto-generated constructor stub
	}

	public String getMMRFSlabelBase() {
		return MMRFSlabelBase;
	}

	public void setMMRFSlabelBase(String mMRFSlabelBase) {
		MMRFSlabelBase = mMRFSlabelBase;
	}

	public boolean isSVIenable() {
		return SVIenable;
	}

	public void setSVIenable(boolean sVIenable) {
		SVIenable = sVIenable;
	}

	public String getSVIverticalSeqFile() {
		return SVIverticalSeqFile;
	}

	public void setSVIverticalSeqFile(String sVIverticalSeqFile) {
		SVIverticalSeqFile = sVIverticalSeqFile;
	}

	public String getSVIsurvivalFile() {
		return SVIsurvivalFile;
	}

	public void setSVIsurvivalFile(String sVIsurvivalFile) {
		SVIsurvivalFile = sVIsurvivalFile;
	}

	public String getSVIfilterFile() {
		return SVIfilterFile;
	}

	public void setSVIfilterFile(String sVIfilterFile) {
		SVIfilterFile = sVIfilterFile;
	}

	public String getSVIfilterCriteria() {
		return SVIfilterCriteria;
	}

	public void setSVIfilterCriteria(String sVIfilterCriteria) {
		SVIfilterCriteria = sVIfilterCriteria;
	}

	public String getSVIoutputFileName() {
		return SVIoutputFileName;
	}

	public void setSVIoutputFileName(String sVIoutputFileName) {
		SVIoutputFileName = sVIoutputFileName;
	}

	public boolean isSVIFSenable() {
		return SVIFSenable;
	}

	public void setSVIFSenable(boolean sVIFSenable) {
		SVIFSenable = sVIFSenable;
	}

	public String getSVIFSsurvivalFile() {
		return SVIFSsurvivalFile;
	}

	public void setSVIFSsurvivalFile(String sVIFSsurvivalFile) {
		SVIFSsurvivalFile = sVIFSsurvivalFile;
	}

	public String getSVIFSseqFile() {
		return SVIFSseqFile;
	}

	public void setSVIFSseqFile(String sVIFSseqFile) {
		SVIFSseqFile = sVIFSseqFile;
	}

	public String getSVIFSseqptnFolder() {
		return SVIFSseqptnFolder;
	}

	public void setSVIFSseqptnFolder(String sVIFSseqptnFolder) {
		SVIFSseqptnFolder = sVIFSseqptnFolder;
	}

	public String getSVIFSseqCoxFolder() {
		return SVIFSseqCoxFolder;
	}

	public void setSVIFSseqCoxFolder(String sVIFSseqCoxFolder) {
		SVIFSseqCoxFolder = sVIFSseqCoxFolder;
	}

	public String getSVIFSseqFeatureFolder() {
		return SVIFSseqFeatureFolder;
	}

	public void setSVIFSseqFeatureFolder(String sVIFSseqFeatureFolder) {
		SVIFSseqFeatureFolder = sVIFSseqFeatureFolder;
	}




	public String getSVIFSseqPreSemanticFolder() {
		return SVIFSseqPreSemanticFolder;
	}

	public void setSVIFSseqPreSemanticFolder(String sVIFSseqPreSemanticFolder) {
		SVIFSseqPreSemanticFolder = sVIFSseqPreSemanticFolder;
	}

	public Double getSVIoutlierThreshold() {
		return SVIoutlierThreshold;
	}

	public void setSVIoutlierThreshold(Double sVIoutlierThreshold) {
		SVIoutlierThreshold = sVIoutlierThreshold;
	}


	public boolean isMFreeenable() {
		return MFreeenable;
	}

	public void setMFreeenable(boolean mFreeenable) {
		MFreeenable = mFreeenable;
	}

	public Integer getMFreexStart() {
		return MFreexStart;
	}

	public void setMFreexStart(Integer mFreexStart) {
		MFreexStart = mFreexStart;
	}

	public ArrayList<String> getMFreeyTitle() {
		return MFreeyTitle;
	}

	public void setMFreeyTitle(ArrayList<String> mFreeyTitle) {
		MFreeyTitle = mFreeyTitle;
	}


	public Integer getMFreefeatureStart() {
		return MFreefeatureStart;
	}

	public void setMFreefeatureStart(Integer mFreefeatureStart) {
		MFreefeatureStart = mFreefeatureStart;
	}

	public Integer getMFreecoxIter() {
		return MFreecoxIter;
	}

	public void setMFreecoxIter(Integer mFreecoxIter) {
		MFreecoxIter = mFreecoxIter;
	}

	public Integer getMFreeStep() {
		return MFreeStep;
	}

	public void setMFreeStep(Integer mFreeStep) {
		MFreeStep = mFreeStep;
	}

	public MFStype getMFreescreenType() {
		return MFreescreenType;
	}

	public void setMFreescreenType(MFStype mFreescreenType) {
		MFreescreenType = mFreescreenType;
	}


	public String getMFreeaucFolder() {
		return MFreeaucFolder;
	}

	public void setMFreeaucFolder(String mFreeaucFolder) {
		MFreeaucFolder = mFreeaucFolder;
	}

	public ArrayList<String> getMFreefeatureScore() {
		return MFreefeatureScore;
	}

	public void setMFreefeatureScore(ArrayList<String> mFreefeatureScore) {
		MFreefeatureScore = mFreefeatureScore;
	}

	public void addMFreefeatureScore(String mFreefeatureScore) {
		this.MFreefeatureScore.add(mFreefeatureScore);
	}

	public String getMFreefeatureDescription() {
		return MFreefeatureDescription;
	}

	public void setMFreefeatureDescription(String mFreefeatureDescription) {
		MFreefeatureDescription = mFreefeatureDescription;
	}
	public Integer getMFreefeatureStop() {
		return MFreefeatureStop;
	}

	public void setMFreefeatureStop(Integer mFreefeatureStop) {
		MFreefeatureStop = mFreefeatureStop;
	}


}

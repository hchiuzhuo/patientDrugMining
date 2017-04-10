package org.imeds.data;

public class sampleConfig {
	private String  sample_id;
	private String  sample_label= "None";	
	private Boolean sample_random = false;
	private String  sample_range_str;
	private Integer sample_range_start = 0; //default
	private Integer sample_range_end = 500; //default
	private Boolean sample_append = false;
	
	public String getSample_id() {
		return sample_id;
	}

	public void setSample_id(String sample_id) {
		this.sample_id = sample_id;
	}

	
	public String getSample_label() {
		return sample_label;
	}

	public void setSample_label(String sample_label) {
		this.sample_label = sample_label;
	}

	public Boolean getSample_random() {
		return sample_random;
	}

	public void setSample_random(Boolean sample_random) {
		this.sample_random = sample_random;
	}

	public String getSample_range_str() {
		return sample_range_str;
	}

	public void setSample_range_str(String sample_range_str) {
		this.sample_range_str = sample_range_str;
	}

	public Integer getSample_range_start() {
		return sample_range_start;
	}

	public void setSample_range_start(Integer sample_range_start) {
		this.sample_range_start = sample_range_start;
	}

	public Integer getSample_range_end() {
		return sample_range_end;
	}

	public void setSample_range_end(Integer sample_range_end) {
		this.sample_range_end = sample_range_end;
	}

	public Boolean getSample_append() {
		return sample_append;
	}

	public void setSample_append(Boolean sample_append) {
		this.sample_append = sample_append;
	}

	public sampleConfig() {
		// TODO Auto-generated constructor stub
	}

	public sampleConfig(String sample_random, String sample_range_str, String sample_append) {
		sampleConfig( sample_random, sample_range_str, sample_append);
	}
	public sampleConfig(String sample_id, String sample_label, String sample_random, String sample_range_str, String sample_append) {
		this.sample_id = sample_id;
		this.sample_label=sample_label;
		sampleConfig( sample_random, sample_range_str, sample_append);
		/*
		if(sample_random.trim()!=null && sample_random.trim().equalsIgnoreCase("true"))this.sample_random = true;
		else this.sample_random = false;

		if(sample_range_str.trim()!=null){
			this.sample_range_str = sample_range_str;
			String[] rge=this.sample_range_str.split(",");
			this.sample_range_start=Integer.parseInt(rge[0]);
			this.sample_range_end = Integer.parseInt(rge[1]);
		}

		if(sample_append.trim()!=null && sample_append.trim().equalsIgnoreCase("true"))this.sample_append = true;
		else this.sample_append = false;*/
	}
	
	private void sampleConfig(String sample_random, String sample_range_str, String sample_append) {
		if(sample_random.trim()!=null && sample_random.trim().equalsIgnoreCase("true"))this.sample_random = true;
		else this.sample_random = false;

		if(sample_range_str.trim()!=null){
			this.sample_range_str = sample_range_str;
			String[] rge=this.sample_range_str.split(",");
			this.sample_range_start=Integer.parseInt(rge[0]);
			this.sample_range_end = Integer.parseInt(rge[1]);
		}

		if(sample_append.trim()!=null && sample_append.trim().equalsIgnoreCase("true"))this.sample_append = true;
		else this.sample_append = false;
		
	}
	@Override
	public String toString() {
		return "sampleConfig [sample_id=" + sample_id + ", sample_label="
				+ sample_label + ", sample_random=" + sample_random
				+ ", sample_range_str=" + sample_range_str
				+ ", sample_range_start=" + sample_range_start
				+ ", sample_range_end=" + sample_range_end + ", sample_append="
				+ sample_append + "]";
	}

}

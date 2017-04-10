package org.imeds.data.common;

public class IcdPair extends CodePair<Double> {
	private Double DStart;
	private Double DEnd;
	
	private String SStart;
	private String SEnd;
	public IcdPair() {
		// TODO Auto-generated constructor stub
	}

	public String getSStart() {
		return SStart;
	}

	public void setSStart(String sStart) {
		SStart = sStart;
	}

	public String getSEnd() {
		return SEnd;
	}

	public void setSEnd(String sEnd) {
		SEnd = sEnd;
	}

	@Override
	public Double getStart() {
		// TODO Auto-generated method stub
		return DStart;
	}

	@Override
	public void setStart(Double start) {
		// TODO Auto-generated method stub
		this.DStart = start;
	}

	@Override
	public Double getEnd() {
		// TODO Auto-generated method stub
		return this.DEnd;
	}

	@Override
	public void setEnd(Double end) {
		// TODO Auto-generated method stub
		this.DEnd = end;
		
	}

	@Override
	public String toString() {
		return "IcdPair [DStart=" + DStart + ", DEnd=" + DEnd + "]";
	}

}

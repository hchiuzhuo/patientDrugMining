package org.imeds.data.common;


public abstract class CodePair<T> {

	private Object Start;
	private Object End;
	public abstract T getStart(); 
	
	public abstract void setStart(T start);

	public abstract T getEnd();

	public abstract void setEnd(T end);

	
}

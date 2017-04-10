package org.imeds.data;



public abstract class Worker {

	public Worker() {
		// TODO Auto-generated constructor stub
	}

	public abstract void prepare();
	
	public abstract void ready();
	
	public abstract void go();
	
	public abstract void done();
}


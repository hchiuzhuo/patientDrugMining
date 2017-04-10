package org.imeds.data.common;

import java.util.Date;

public class seqItemPair {
	private Date timestamp;
	private Integer itemId;
	
	public seqItemPair(Date timestamp, Integer itemId) {
		super();
		this.timestamp = timestamp;
		this.itemId = itemId;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public Integer getItemId() {
		return itemId;
	}
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}
	
}

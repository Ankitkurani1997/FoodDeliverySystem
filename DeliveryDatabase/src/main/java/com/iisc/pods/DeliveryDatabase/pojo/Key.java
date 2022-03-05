package com.iisc.pods.DeliveryDatabase.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Key implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Column(name = "restid")
	int restId;
	
	@Column(name = "itemid")
	int itemId;
	
	public Key() {
		// TODO Auto-generated constructor stub
	}
	
	public Key(int restId, int itemId) {
		super();
		this.restId = restId;
		this.itemId = itemId;
	}

	public int getRestId() {
		return restId;
	}

	public void setRestId(int restId) {
		this.restId = restId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	@Override
	public String toString() {
		return "Key [restId=" + restId + ", itemId=" + itemId + "]";
	}
	
	
}

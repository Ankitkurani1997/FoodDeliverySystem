package com.iisc.pods.pojo;

import java.util.List;

public class Restaurant {

	int restId;
	
	List<Item> inventories;

	public int getRestId() {
		return restId;
	}

	public void setRestId(int restId) {
		this.restId = restId;
	}

	public List<Item> getInventories() {
		return inventories;
	}

	public void setInventories(List<Item> inventories) {
		this.inventories = inventories;
	}

	@Override
	public String toString() {
		return "Restaurant [restId=" + restId + ", inventories=" + inventories + "]";
	}
	
	
}

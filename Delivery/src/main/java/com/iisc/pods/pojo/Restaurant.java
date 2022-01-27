package com.iisc.pods.pojo;

import java.util.List;

public class Restaurant {

	int restId;
	
	List<Item> items;

	public int getRestId() {
		return restId;
	}

	public void setRestId(int restId) {
		this.restId = restId;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "Restaurant [restId=" + restId + ", items=" + items + "]";
	}

	
	
	
}

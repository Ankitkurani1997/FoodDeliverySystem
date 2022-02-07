package com.iisc.pods.pojo;

import java.util.List;

/**
 * @author ankit 
 * A Java class that corresponds to a restaurant. Each restaurant has the following information :
 * 1. restId : An unique ID assigned to every restaurant
 * 2. items : A list of items that is part of the inventory of that particular restaurant
*/
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

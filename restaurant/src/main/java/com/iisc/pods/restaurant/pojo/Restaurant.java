package com.iisc.pods.restaurant.pojo;
import java.util.HashMap;


public class Restaurant {
	
	int restId;
	
	HashMap<Integer, Integer> items;

	public int getRestId() {
		return restId;
	}

	public void setRestId(int restId) {
		this.restId = restId;
	}

	public HashMap<Integer, Integer> getItems() {
		return items;
	}

	public void setItems(HashMap<Integer, Integer> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "Restaurant [restId=" + restId + ", items=" + items + "]";
	}

}

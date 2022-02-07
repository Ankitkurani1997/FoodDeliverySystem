package com.iisc.pods.pojo;


/** 
 * @author ankit
 * A Java class that corresponds to List of Items in a restaurant. Each Item has following information :
 * 1. itemId : An unique ID assigned to every item in restaurant
 * 2. price : Price for the particular item Id mentioned above
*/
public class Item {
	int itemId;
	int price;
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	@Override
	public String toString() {
		return "Item [itemId=" + itemId + ", price=" + price + "]";
	}
	
}

package com.iisc.pods.pojo;

public class Order {
	int custId;
	int restId;
	int itemId;
	int qty;
	public int getCustId() {
		return custId;
	}
	public void setCustId(int custId) {
		this.custId = custId;
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
	public int getQty() {
		return qty;
	}
	public void setQty(int qty) {
		this.qty = qty;
	}
	@Override
	public String toString() {
		return "Order [custId=" + custId + ", restId=" + restId + ", itemId=" + itemId + ", qty=" + qty + "]";
	}
	
	
}

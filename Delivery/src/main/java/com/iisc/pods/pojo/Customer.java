package com.iisc.pods.pojo;

public class Customer {
	int custId;
	int amount;

	public int getCustId() {
		return custId;
	}

	public void setCustId(int custId) {
		this.custId = custId;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "Customer [custId=" + custId + ", amount=" + amount + "]";
	}
	
}

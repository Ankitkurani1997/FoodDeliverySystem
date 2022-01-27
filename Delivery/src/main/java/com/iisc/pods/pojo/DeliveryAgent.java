package com.iisc.pods.pojo;

public class DeliveryAgent {
	
	int agentId;
	String status;
	
	public DeliveryAgent() {
		status = "available";
	}

	public int getAgentId() {
		return agentId;
	}

	public void setAgentId(int agentId) {
		this.agentId = agentId;
	}
	
	
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "DeliveryAgent [agentId=" + agentId + ", status=" + status + "]";
	}
	
	
}

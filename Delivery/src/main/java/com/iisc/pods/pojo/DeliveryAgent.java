package com.iisc.pods.pojo;


/**
 * @author ankit
 * A Java class that corresponds to Delivery Agents. Each Agent has following information :
 * 1. agentId : An unique ID assigned to every agent
 * 2. status : Status of the agent
 */
public class DeliveryAgent {
	
	int agentId;
	String status;
	
	public DeliveryAgent() {
		status = "signed-out";
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

package com.iisc.pods.DeliveryDatabase.pojo;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * 
 * @author sawrush
 * This class corresponds to the "Agent" table in the H2 database. It has the following fields :
 * 		1. agentId : Primary key.
 * 		2. status : Status of the agent either "unavailable", "available", "signed-out"
 * 		3. order : reference from Orders table.
 */


@Entity
public class Agent {
	
	@Id
	int agentId;
	
	String status;
	
	@OneToMany(mappedBy = "agent")
	Set<Orders> order;
	
	
	public Agent() {
		// TODO Auto-generated constructor stub
	}

	public Agent(int agentId) {
		super();
		this.agentId = agentId;
		this.status = "signed-out";
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
		return "Agent [agentId=" + agentId + ", status=" + status + "]";
	}
	
	

}
